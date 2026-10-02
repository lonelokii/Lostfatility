package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.ItemEvent;
import lostfacility.model.*;

import java.util.Objects;
import java.util.Optional;

/**
 * Uses a consumable or usable item from inventory.
 */
public record UseAction(String itemQuery) implements GameAction {

    public UseAction {
        Objects.requireNonNull(itemQuery, "Item query must not be null");
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        Player player = state.getPlayer();
        if (player == null || !player.isAlive()) {
            return ActionResult.failure("You cannot use items right now.");
        }

        Optional<Item> itemOpt = player.getInventory().findByNameOrId(itemQuery);
        if (itemOpt.isEmpty()) {
            return ActionResult.failure("You don't have '" + itemQuery + "' in your inventory.");
        }

        Item item = itemOpt.get();

        if (item.getType() == ItemType.CONSUMABLE) {
            int healed = player.heal(item.getHealAmount());
            player.getInventory().removeItem(item);

            ItemEvent useEvent = new ItemEvent(
                    ItemEvent.ActionType.USE,
                    item.getId(),
                    item.getName(),
                    player.getName(),
                    "Restored " + healed + " HP"
            );
            events.publish(useEvent);
            return ActionResult.success("Used " + item.getName() + ", restored " + healed + " HP.", useEvent);
        } else if (item.getType() == ItemType.WEAPON || item.getType() == ItemType.ARMOR) {
            boolean equipped = player.getInventory().equip(item);
            if (equipped) {
                ItemEvent equipEvent = new ItemEvent(
                        ItemEvent.ActionType.EQUIP,
                        item.getId(),
                        item.getName(),
                        player.getName(),
                        "Equipped " + item.getType().getLabel()
                );
                events.publish(equipEvent);
                return ActionResult.success("Equipped " + item.getName() + ".", equipEvent);
            }
        }

        return ActionResult.failure("You cannot use the " + item.getName() + " this way.");
    }
}
