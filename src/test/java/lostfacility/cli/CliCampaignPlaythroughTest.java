package lostfacility.cli;

import lostfacility.action.ActionResult;
import lostfacility.model.Item;
import lostfacility.model.Position;
import lostfacility.system.Quest;
import lostfacility.system.QuestState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CliCampaignPlaythroughTest {

    @Test
    @DisplayName("Full End-to-End Walkthrough of The Lost Facility campaign via CLI commands")
    void testFullCampaignPlaythrough() {
        CliApp app = CliApp.createDefault();
        assertNotNull(app);

        // 1. Initial State in Maintenance Room
        assertEquals("maintenance_room", app.getState().getCurrentRoom().getId());
        assertEquals(new Position(3, 1), app.getState().getPlayer().getPosition());

        // 2. Move to (1, 2) to pick up Rusty Key: start (3, 1) -> west(a), west(a), south(s) -> (1, 2)
        app.executeCommand("a");
        app.executeCommand("a");
        app.executeCommand("s");
        assertEquals(new Position(1, 2), app.getState().getPlayer().getPosition());

        ActionResult takeKeyResult = app.executeCommand("take rusty key");
        assertTrue(takeKeyResult.isSuccess(), "Taking rusty key should succeed");
        assertTrue(app.getState().getPlayer().getInventory().hasItem("rusty_key"), "Player should have rusty key");

        // 3. Move to (5, 2) to pick up and equip Iron Sword
        app.executeCommand("d");
        app.executeCommand("d");
        app.executeCommand("d");
        app.executeCommand("d");
        assertEquals(new Position(5, 2), app.getState().getPlayer().getPosition());

        ActionResult takeSwordResult = app.executeCommand("take iron sword");
        assertTrue(takeSwordResult.isSuccess(), "Taking sword should succeed");

        ActionResult equipResult = app.executeCommand("equip iron sword");
        assertTrue(equipResult.isSuccess(), "Equipping sword should succeed");
        assertEquals(25, app.getState().getPlayer().getEffectiveAttack(), "Attack should now be 25");

        // 4. Move to door at (4, 3) and exit South into Central Corridor
        app.executeCommand("a");
        app.executeCommand("s"); // on door (4, 3)
        ActionResult enterCorridor = app.executeCommand("s"); // exit south to corridor
        assertTrue(enterCorridor.isSuccess());
        assertEquals("corridor", app.getState().getCurrentRoom().getId(), "Player should be in corridor");

        // 5. Encounter and defeat Security Robot Unit-A in corridor
        // Player moves east towards robot at (6, 2)
        app.executeCommand("d");
        app.executeCommand("d");
        app.executeCommand("d");

        // Attack robot until defeated
        var robot = app.getState().findEnemyByNameOrId("robot_corridor");
        assertTrue(robot.isPresent(), "Robot must be present in corridor");

        int maxRounds = 10;
        while (robot.get().isAlive() && maxRounds-- > 0) {
            app.executeCommand("attack robot");
        }
        assertFalse(robot.get().isAlive(), "Robot should be defeated");

        // 6. Move East through corridor exit into Security Operations Room
        app.executeCommand("d");
        app.executeCommand("d");
        app.executeCommand("d");
        ActionResult enterSecurity = app.executeCommand("d");
        assertTrue(enterSecurity.isSuccess());
        assertEquals("security_room", app.getState().getCurrentRoom().getId(), "Player should be in security room");

        // 7. Talk to Dr. Aris (Scientist NPC is at (2, 1))
        // Position player at (1, 1) adjacent to Dr. Aris
        app.getState().getPlayer().setPosition(new Position(1, 1));
        ActionResult talkResult = app.executeCommand("talk");
        assertTrue(talkResult.isSuccess(), "Talk action should succeed adjacent to NPC");
        assertTrue(app.getDialogueManager().isInDialogue(), "Dialogue should be active");

        // Dialogue option 3: "Do you have the security card..."
        ActionResult chooseCardQuery = app.executeCommand("3");
        assertTrue(chooseCardQuery.isSuccess());
        assertTrue(app.getDialogueManager().isInDialogue());
        assertEquals("give_card", app.getDialogueManager().getActiveNode().getId());

        // Dialogue option 1 on give_card: "Thank you, Doctor..." -> grants card
        ActionResult chooseAcceptCard = app.executeCommand("1");
        assertTrue(chooseAcceptCard.isSuccess());
        assertFalse(app.getDialogueManager().isInDialogue(), "Dialogue should finish");
        assertTrue(app.getState().getPlayer().getInventory().hasItem("access_card"), "Player should now hold access_card");

        // 8. Move South to blast door at (3, 3) and exit South into Main Exit
        app.getState().getPlayer().setPosition(new Position(3, 2));
        ActionResult unlockBlastDoor = app.executeCommand("s");
        assertTrue(unlockBlastDoor.isSuccess(), "Blast door should unlock with access card");

        ActionResult exitToSurface = app.executeCommand("s"); // exit south to main_exit
        assertTrue(exitToSurface.isSuccess());
        assertEquals("main_exit", app.getState().getCurrentRoom().getId(), "Player should reach the surface airway!");

        // 9. Verify Quest & Victory
        Quest activeQuest = app.getQuestManager().getActiveQuest();
        assertNotNull(activeQuest);
        assertEquals(QuestState.COMPLETED, activeQuest.getState(), "Escape the Facility quest should be completed!");
    }
}
