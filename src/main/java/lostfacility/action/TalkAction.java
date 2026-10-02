package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.model.Npc;
import lostfacility.model.Player;
import lostfacility.system.DialogueManager;

/**
 * Initiates conversation with an adjacent NPC.
 */
public record TalkAction(String npcQuery, DialogueManager dialogueManager) implements GameAction {

    public TalkAction(DialogueManager dialogueManager) {
        this(null, dialogueManager);
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        if (dialogueManager == null) {
            return ActionResult.failure("Dialogue system is not initialized.");
        }

        Player player = state.getPlayer();
        if (player == null) {
            return ActionResult.failure("Player not available.");
        }

        // Find NPC in current room
        Npc targetNpc = null;
        for (var entity : state.getCurrentRoom().getTile(player.getPosition()).getItemsOnGround()) {
            // Check adjacent tiles
        }

        // Search adjacent tiles for NPC
        for (lostfacility.model.Direction dir : lostfacility.model.Direction.values()) {
            lostfacility.model.Position checkPos = player.getPosition().add(dir);
            for (Npc npc : state.getNpcsInCurrentRoom()) {
                if (npc.getPosition().equals(checkPos)) {
                    targetNpc = npc;
                    break;
                }
            }
            if (targetNpc != null) break;
        }

        // If explicit query was provided, try by name
        if (targetNpc == null && npcQuery != null && !npcQuery.isBlank()) {
            targetNpc = state.findNpcByNameOrId(npcQuery).orElse(null);
        }

        if (targetNpc == null) {
            return ActionResult.failure("There is nobody nearby to talk to.");
        }

        boolean started = dialogueManager.startDialogue(targetNpc.getDialogueTreeId(), "start", events);
        if (started) {
            return ActionResult.success("Talking to " + targetNpc.getName() + ".");
        }

        return ActionResult.failure("The " + targetNpc.getName() + " has nothing to say.");
    }
}
