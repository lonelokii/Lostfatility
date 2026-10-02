package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.persistence.SaveManager;

/**
 * Persists the current game state to a named save slot.
 */
public record SaveAction(String slotName, SaveManager saveManager) implements GameAction {

    public SaveAction(SaveManager saveManager) {
        this("quicksave", saveManager);
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        if (saveManager == null) {
            return ActionResult.failure("Save system not available.");
        }
        String slot = (slotName != null && !slotName.isBlank()) ? slotName : "quicksave";
        boolean saved = saveManager.save(slot, state);

        if (saved) {
            MessageEvent msg = new MessageEvent("Game successfully saved to slot: " + slot, MessageEvent.Channel.SYSTEM);
            events.publish(msg);
            return ActionResult.success("Game saved.", msg);
        } else {
            return ActionResult.failure("Failed to save game.");
        }
    }
}
