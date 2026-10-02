package lostfacility.system;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.ItemEvent;
import lostfacility.event.MoveEvent;
import lostfacility.model.*;
import lostfacility.persistence.JsonLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class QuestAndDialogueTest {

    private JsonLoader.GameBundle bundle;
    private GameState state;
    private DialogueManager dialogueManager;
    private QuestManager questManager;
    private EventManager eventManager;

    @BeforeEach
    void setUp() throws IOException {
        JsonLoader loader = new JsonLoader();
        bundle = loader.loadCampaign("games/lost_facility");
        state = bundle.gameState();
        dialogueManager = bundle.dialogueManager();
        questManager = bundle.questManager();
        eventManager = new EventManager();
        questManager.attachToEventManager(eventManager);
    }

    @Test
    @DisplayName("Dialogue system supports branching choices and item grants")
    void testBranchingDialogueAndItemGrant() {
        assertFalse(dialogueManager.isInDialogue());

        boolean started = dialogueManager.startDialogue("scientist_dialogue", "start", eventManager);
        assertTrue(started, "Dialogue should start");
        assertTrue(dialogueManager.isInDialogue());
        assertEquals("Dr. Aris", dialogueManager.getActiveNode().getSpeaker());

        // Choose option 3: "Do you have the security card..." -> goes to "give_card"
        boolean choseOption3 = dialogueManager.chooseOption(2, state, eventManager);
        assertTrue(choseOption3);
        assertTrue(dialogueManager.isInDialogue());
        assertEquals("give_card", dialogueManager.getActiveNode().getId());
        assertTrue(state.isFlag("talked_to_scientist"), "Flag talked_to_scientist should be granted");

        // Player does not yet have access card
        assertFalse(state.getPlayer().getInventory().hasItem("access_card"));

        // Choose final option on give_card node -> grants card and ends dialogue
        boolean choseFinal = dialogueManager.chooseOption(0, state, eventManager);
        assertTrue(choseFinal);
        assertFalse(dialogueManager.isInDialogue(), "Dialogue should conclude");
        assertTrue(state.isFlag("has_access_card"), "Flag has_access_card should be granted");
        assertTrue(state.getPlayer().getInventory().hasItem("access_card"), "Player should now have access_card");
    }

    @Test
    @DisplayName("QuestManager updates objectives in response to GameEvents")
    void testQuestProgressionViaEvents() {
        Quest quest = questManager.getActiveQuest();
        assertNotNull(quest);
        assertEquals(QuestState.ACTIVE, quest.getState());

        QuestObjective keyObj = quest.getObjectives().get(0);
        assertFalse(keyObj.isCompleted(), "Key objective starts uncompleted");

        // Emit ItemEvent for collecting rusty_key
        eventManager.publish(new ItemEvent(ItemEvent.ActionType.TAKE, "rusty_key", "Rusty Key", "Investigator", "Picked up"));

        assertTrue(keyObj.isCompleted(), "Collecting rusty key should complete objective");
        assertEquals(QuestState.OBJECTIVE_UPDATED, quest.getState());

        // Emit MoveEvent entering corridor
        QuestObjective corridorObj = quest.getObjectives().get(1);
        assertFalse(corridorObj.isCompleted());

        eventManager.publish(new MoveEvent("Investigator", new Position(2, 2), new Position(1, 1), Direction.SOUTH, "corridor", true));

        assertTrue(corridorObj.isCompleted(), "Entering corridor should complete second objective");
    }
}
