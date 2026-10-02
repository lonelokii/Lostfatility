package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.ItemEvent;
import lostfacility.model.*;

import java.util.List;

/**
 * Picks up an item from the current tile and adds it to the player inventory.
 */
public record TakeAction(String itemQuery) implements GameAction {

    public TakeAction() {
        this(null);
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        Player player = state.getPlayer();
        Room room = state.getCurrentRoom();

        if (player == null || room == null) {
            return ActionResult.failure("Cannot take item: invalid state.");
        }

        Tile currentTile = room.getTile(player.getPosition());
        if (currentTile == null || !currentTile.hasItems()) {
            return ActionResult.failure("There is nothing here to take.");
        }

        if (player.getInventory().isFull()) {
            return ActionResult.failure("Your inventory is full.");
        }

        Item itemToTake = null;
        if (itemQuery != null && !itemQuery.isBlank()) {
            itemToTake = currentTile.removeItemByName(itemQuery);
        } else {
            List<Item> items = currentTile.getItemsOnGround();
            if (!items.isEmpty()) {
                itemToTake = currentTile.removeItemByName(items.get(0).getName());
            }
        }

        if (itemToTake == null) {
            return ActionResult.failure("No matching item found on the ground.");
        }

        player.getInventory().addItem(itemToTake);
        ItemEvent takeEvent = new ItemEvent(ItemEvent.ActionType.TAKE, itemToTake.getId(), itemToTake.getName(), player.getName(), "Picked up");
        events.publish(takeEvent);

        return ActionResult.success("Took " + itemToTake.getName() + ".", takeEvent);
    }
}
