package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.ItemEvent;
import lostfacility.model.*;

import java.util.Objects;
import java.util.Optional;

/**
 * Equips a weapon or armor from the player inventory.
 */
public record EquipAction(String itemQuery) implements GameAction {

    public EquipAction {
        Objects.requireNonNull(itemQuery, "Item query must not be null");
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        Player player = state.getPlayer();
        if (player == null) {
            return ActionResult.failure("No player available.");
        }

        Optional<Item> itemOpt = player.getInventory().findByNameOrId(itemQuery);
        if (itemOpt.isEmpty()) {
            return ActionResult.failure("Item not found in inventory: " + itemQuery);
        }

        Item item = itemOpt.get();
        if (!item.getType().isEquippable()) {
            return ActionResult.failure(item.getName() + " cannot be equipped.");
        }

        boolean success = player.getInventory().equip(item);
        if (success) {
            ItemEvent equipEvent = new ItemEvent(
                    ItemEvent.ActionType.EQUIP,
                    item.getId(),
                    item.getName(),
                    player.getName(),
                    "Equipped"
            );
            events.publish(equipEvent);
            return ActionResult.success("Equipped " + item.getName() + ".", equipEvent);
        }

        return ActionResult.failure("Failed to equip " + item.getName() + ".");
    }
}
