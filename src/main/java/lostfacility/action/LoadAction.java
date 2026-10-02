package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.model.World;
import lostfacility.persistence.SaveManager;

import java.util.Optional;

/**
 * Restores game state from a named save slot.
 */
public record LoadAction(String slotName, SaveManager saveManager, World worldTemplate) implements GameAction {

    public LoadAction(SaveManager saveManager, World worldTemplate) {
        this("quicksave", saveManager, worldTemplate);
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        if (saveManager == null || worldTemplate == null) {
            return ActionResult.failure("Load system not available.");
        }
        String slot = (slotName != null && !slotName.isBlank()) ? slotName : "quicksave";
        Optional<GameState> loadedState = saveManager.load(slot, worldTemplate);

        if (loadedState.isPresent()) {
            GameState loaded = loadedState.get();
            // Copy loaded state into current state
            state.setCurrentRoom(loaded.getCurrentRoom());
            state.getPlayer().setHp(loaded.getPlayer().getHp());
            state.getPlayer().setMaxHp(loaded.getPlayer().getMaxHp());
            state.getPlayer().setPosition(loaded.getPlayer().getPosition());
            state.getPlayer().setFacingDirection(loaded.getPlayer().getFacingDirection());

            // Clear & reload inventory
            while (state.getPlayer().getInventory().size() > 0) {
                state.getPlayer().getInventory().removeItem(state.getPlayer().getInventory().getItems().get(0));
            }
            for (var item : loaded.getPlayer().getInventory().getItems()) {
                state.getPlayer().getInventory().addItem(item);
            }

            MessageEvent msg = new MessageEvent("Game loaded from slot: " + slot, MessageEvent.Channel.SYSTEM);
            events.publish(msg);
            return ActionResult.success("Game loaded.", msg);
        } else {
            return ActionResult.failure("Save slot '" + slot + "' not found or could not be read.");
        }
    }
}
