package lostfacility.engine;

import lostfacility.action.*;
import lostfacility.event.EventManager;
import lostfacility.model.*;
import lostfacility.persistence.JsonLoader;
import lostfacility.persistence.SaveManager;
import lostfacility.system.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FullCampaignIntegrationTest {

    @Test
    @DisplayName("Complete end-to-end integration test of engine actions, persistence, and campaign victory")
    void testEndToEndCampaignFlow() throws IOException {
        // 1. Bootstrap campaign via JsonLoader
        JsonLoader loader = new JsonLoader();
        JsonLoader.GameBundle bundle = loader.loadCampaign("games/lost_facility");
        assertNotNull(bundle);

        GameState state = bundle.gameState();
        EventManager events = new EventManager();
        DialogueManager dialogueManager = bundle.dialogueManager();
        QuestManager questManager = bundle.questManager();
        questManager.attachToEventManager(events);

        CommandParser parser = new CommandParser();
        GameEngine engine = new GameEngine(state, events, parser);
        SaveManager saveManager = new SaveManager(java.nio.file.Path.of("saves/test_integration"));

        // Initial sanity checks
        assertEquals("maintenance_room", state.getCurrentRoom().getId());
        Player player = state.getPlayer();
        assertEquals(new Position(3, 1), player.getPosition());
        assertEquals(100, player.getHp());
        assertEquals(15, player.getEffectiveAttack());

        // 2. Collect and equip equipment
        // Walk to (5, 2) where iron_sword is
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.SOUTH));
        assertEquals(new Position(5, 2), player.getPosition());

        ActionResult takeSword = engine.execute(new TakeAction("iron_sword"));
        assertTrue(takeSword.isSuccess(), "Player should take iron_sword");

        ActionResult equipSword = engine.execute(new EquipAction("iron_sword"));
        assertTrue(equipSword.isSuccess(), "Player should equip iron_sword");
        assertEquals(25, player.getEffectiveAttack(), "Attack bonus should apply");

        // Walk to (1, 2) to pick up rusty_key
        engine.execute(new MoveAction(Direction.WEST));
        engine.execute(new MoveAction(Direction.WEST));
        engine.execute(new MoveAction(Direction.WEST));
        engine.execute(new MoveAction(Direction.WEST));
        assertEquals(new Position(1, 2), player.getPosition());

        ActionResult takeKey = engine.execute(new TakeAction("rusty_key"));
        assertTrue(takeKey.isSuccess(), "Player should take rusty_key");
        assertTrue(player.getInventory().hasItem("rusty_key"));

        // 3. Move south through door (4, 3) into corridor
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.SOUTH)); // at (4, 3)
        ActionResult stepToCorridor = engine.execute(new MoveAction(Direction.SOUTH));
        assertTrue(stepToCorridor.isSuccess());
        assertEquals("corridor", state.getCurrentRoom().getId());

        // 4. Test Persistence Mid-Campaign (Save & Reload)
        ActionResult saveResult = engine.execute(new SaveAction("checkpoint", saveManager));
        assertTrue(saveResult.isSuccess(), "Checkpoint save should succeed");

        // Verify save file exists
        File saveFile = new File("saves/test_integration/save_checkpoint.json");
        assertTrue(saveFile.exists(), "Save file should exist on disk");

        // Reload state to verify fidelity
        ActionResult loadResult = engine.execute(new LoadAction("checkpoint", saveManager, bundle.world()));
        assertTrue(loadResult.isSuccess(), "Checkpoint load should succeed");
        assertEquals("corridor", state.getCurrentRoom().getId());
        assertEquals(25, state.getPlayer().getEffectiveAttack());

        // 5. Combat: Defeat security robot in corridor
        // Walk east towards robot
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));

        var robotOpt = state.findEnemyByNameOrId("robot_corridor");
        assertTrue(robotOpt.isPresent());
        Enemy robot = robotOpt.get();

        int rounds = 0;
        while (robot.isAlive() && rounds++ < 10) {
            engine.execute(new AttackAction("robot"));
        }
        assertFalse(robot.isAlive(), "Security robot must be defeated");
        assertTrue(player.getExperience() > 0, "Player should gain experience from defeated enemy");

        // 6. Enter Security Operations Room
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));
        engine.execute(new MoveAction(Direction.EAST));
        ActionResult enterSecurity = engine.execute(new MoveAction(Direction.EAST));
        assertTrue(enterSecurity.isSuccess());
        assertEquals("security_room", state.getCurrentRoom().getId());

        // 7. Branching Dialogue with Dr. Aris
        player.setPosition(new Position(1, 1)); // Adjacent to Dr. Aris at (2, 1)
        ActionResult talkResult = engine.execute(new TalkAction(dialogueManager));
        assertTrue(talkResult.isSuccess());
        assertTrue(dialogueManager.isInDialogue());

        // Choice 3: "Do you have the security card..."
        boolean cardQuery = dialogueManager.chooseOption(2, state, events);
        assertTrue(cardQuery);
        assertEquals("give_card", dialogueManager.getActiveNode().getId());

        // Choice 1 on give_card: Accept card
        boolean acceptCard = dialogueManager.chooseOption(0, state, events);
        assertTrue(acceptCard);
        assertFalse(dialogueManager.isInDialogue(), "Dialogue concluded");
        assertTrue(player.getInventory().hasItem("access_card"), "Player now has access_card");

        // 8. Unlock blast door at (3, 3) leading to main exit
        player.setPosition(new Position(3, 2));
        ActionResult unlockDoor = engine.execute(new MoveAction(Direction.SOUTH));
        assertTrue(unlockDoor.isSuccess(), "Blast door should unlock");

        ActionResult escapeSurface = engine.execute(new MoveAction(Direction.SOUTH));
        assertTrue(escapeSurface.isSuccess());
        assertEquals("main_exit", state.getCurrentRoom().getId(), "Player reached surface airway");

        // 9. Assert Quest Completion
        Quest quest = questManager.getActiveQuest();
        assertNotNull(quest);
        assertEquals(QuestState.COMPLETED, quest.getState(), "Main campaign quest must be COMPLETED");

        // Clean up test saves directory
        if (saveFile.exists()) saveFile.delete();
        File metaFile = new File("saves/test_integration/save_checkpoint.meta.json");
        if (metaFile.exists()) metaFile.delete();
        new File("saves/test_integration").delete();
    }
}
