package lostfacility.model;

/**
 * 4-way cardinal directions for grid movement and entity facing.
 */
public enum Direction {
    NORTH(0, -1, "North"),
    SOUTH(0, 1, "South"),
    EAST(1, 0, "East"),
    WEST(-1, 0, "West");

    private final int dx;
    private final int dy;
    private final String displayName;

    Direction(int dx, int dy, String displayName) {
        this.dx = dx;
        this.dy = dy;
        this.displayName = displayName;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    public static Direction fromString(String text) {
        if (text == null) return null;
        String s = text.trim().toLowerCase();
        return switch (s) {
            case "n", "north", "up", "w" -> NORTH;
            case "s", "south", "down" -> SOUTH;
            case "e", "east", "right", "d" -> EAST;
            case "west", "left", "a" -> WEST;
            default -> null;
        };
    }
}
