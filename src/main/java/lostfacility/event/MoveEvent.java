package lostfacility.event;

import lostfacility.model.Direction;
import lostfacility.model.Position;

import java.time.Instant;

/**
 * Emitted when an entity moves between tiles or transitions between rooms.
 */
public record MoveEvent(
        String entityId,
        Position fromPosition,
        Position toPosition,
        Direction direction,
        String roomId,
        boolean roomChanged,
        Instant timestamp
) implements GameEvent {

    public MoveEvent(String entityId, Position fromPosition, Position toPosition, Direction direction, String roomId, boolean roomChanged) {
        this(entityId, fromPosition, toPosition, direction, roomId, roomChanged, Instant.now());
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getDescription() {
        if (roomChanged) {
            return entityId + " transitioned to room " + roomId + " at " + toPosition;
        }
        return entityId + " moved " + (direction != null ? direction.getDisplayName() : "") + " to " + toPosition;
    }
}
