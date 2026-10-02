package lostfacility.event;

import java.time.Instant;

/**
 * Emitted when an entity manipulates an item.
 */
public record ItemEvent(
        ActionType actionType,
        String itemId,
        String itemName,
        String entityId,
        String detail,
        Instant timestamp
) implements GameEvent {

    public enum ActionType {
        TAKE,
        DROP,
        USE,
        EQUIP,
        UNEQUIP
    }

    public ItemEvent(ActionType actionType, String itemId, String itemName, String entityId, String detail) {
        this(actionType, itemId, itemName, entityId, detail, Instant.now());
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getDescription() {
        return switch (actionType) {
            case TAKE -> entityId + " picked up " + itemName + ".";
            case DROP -> entityId + " dropped " + itemName + ".";
            case USE -> entityId + " used " + itemName + " (" + detail + ").";
            case EQUIP -> entityId + " equipped " + itemName + ".";
            case UNEQUIP -> entityId + " unequipped " + itemName + ".";
        };
    }
}
