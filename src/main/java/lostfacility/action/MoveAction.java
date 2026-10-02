package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.event.MoveEvent;
import lostfacility.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles directional movement, room transitions, door unlocks, and collision resolution.
 */
public record MoveAction(Direction direction) implements GameAction {

    public MoveAction {
        Objects.requireNonNull(direction, "Direction must not be null");
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        Player player = state.getPlayer();
        Room room = state.getCurrentRoom();

        if (player == null || room == null) {
            return ActionResult.failure("Cannot move: invalid game state.");
        }

        player.setFacingDirection(direction);
        Position fromPos = player.getPosition();
        Position toPos = fromPos.add(direction);

        Tile targetTile = room.isInBounds(toPos) ? room.getTile(toPos) : null;

        // Check if movement goes through a room exit boundary or steps into boundary wall at an exit
        boolean isBoundaryExit = (targetTile != null && targetTile.getType() == TileType.WALL && room.getExit(direction) != null);
        if (!room.isInBounds(toPos) || isBoundaryExit) {
            String targetRoomId = room.getExit(direction);
            if (targetRoomId != null) {
                Room targetRoom = state.getWorld().getRoom(targetRoomId);
                if (targetRoom != null) {
                    state.setCurrentRoom(targetRoom);
                    Position entryPos = targetRoom.getDefaultSpawnPosition();
                    player.setPosition(entryPos);

                    MoveEvent moveEvent = new MoveEvent(player.getId(), fromPos, entryPos, direction, targetRoomId, true);
                    events.publish(moveEvent);
                    MessageEvent msg = new MessageEvent("Entered " + targetRoom.getName() + ".\n" + targetRoom.getDescription(), MessageEvent.Channel.NARRATIVE);
                    events.publish(msg);

                    return ActionResult.success("Entered " + targetRoom.getName(), List.of(moveEvent, msg));
                }
            }
            return ActionResult.failure("You cannot go that way.");
        }

        // Check door interaction
        if (targetTile.isDoor()) {
            if (!targetTile.isOpen()) {
                if (targetTile.isLocked()) {
                    String reqKey = targetTile.getRequiredKeyId();
                    Optional<Item> keyItem = player.getInventory().findByNameOrId(reqKey);
                    if (keyItem.isPresent()) {
                        targetTile.unlock(keyItem.get().getId());
                        MessageEvent unlockMsg = new MessageEvent("You unlocked the door using the " + keyItem.get().getName() + ".", MessageEvent.Channel.SYSTEM);
                        events.publish(unlockMsg);
                    } else {
                        return ActionResult.failure("The door is locked. You need a key.");
                    }
                } else {
                    targetTile.setOpen(true);
                }
            }
        }

        // Check wall or void collision
        if (!targetTile.isWalkable()) {
            return ActionResult.failure("The path is blocked.");
        }

        // Check living enemy collision
        Optional<Enemy> enemyAtTile = state.findEnemyAt(toPos);
        if (enemyAtTile.isPresent()) {
            return ActionResult.failure("The " + enemyAtTile.get().getName() + " is blocking the way!");
        }

        // Move successfully
        player.setPosition(toPos);
        MoveEvent moveEvent = new MoveEvent(player.getId(), fromPos, toPos, direction, room.getId(), false);
        events.publish(moveEvent);

        List<lostfacility.event.GameEvent> emittedEvents = new ArrayList<>();
        emittedEvents.add(moveEvent);

        if (targetTile.hasItems()) {
            for (Item item : targetTile.getItemsOnGround()) {
                MessageEvent itemNotice = new MessageEvent("You see " + item.getName() + " on the ground.", MessageEvent.Channel.NARRATIVE);
                events.publish(itemNotice);
                emittedEvents.add(itemNotice);
            }
        }

        return ActionResult.success("Moved " + direction.getDisplayName(), emittedEvents);
    }
}
