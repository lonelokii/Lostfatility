package lostfacility.model;

import java.io.Serializable;

/**
 * Immutable grid coordinate representing a position in a 2D tile map.
 */
public record Position(int x, int y) implements Serializable {

    public Position add(Direction direction) {
        if (direction == null) return this;
        return new Position(this.x + direction.getDx(), this.y + direction.getDy());
    }

    public Position add(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }

    public int manhattanDistance(Position other) {
        if (other == null) return Integer.MAX_VALUE;
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public boolean isAdjacentTo(Position other) {
        if (other == null) return false;
        int dx = Math.abs(this.x - other.x);
        int dy = Math.abs(this.y - other.y);
        return (dx + dy == 1);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
