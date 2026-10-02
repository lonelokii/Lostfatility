package lostfacility.model;

/**
 * Fundamental tile classifications in the 2D grid map.
 */
public enum TileType {
    WALL('#', "Wall", false),
    FLOOR('.', "Floor", true),
    DOOR('D', "Door", false),
    EXIT('X', "Exit", true),
    VOID(' ', "Void", false);

    private final char glyph;
    private final String description;
    private final boolean inherentlyWalkable;

    TileType(char glyph, String description, boolean inherentlyWalkable) {
        this.glyph = glyph;
        this.description = description;
        this.inherentlyWalkable = inherentlyWalkable;
    }

    public char getGlyph() {
        return glyph;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInherentlyWalkable() {
        return inherentlyWalkable;
    }

    public static TileType fromGlyph(char c) {
        return switch (c) {
            case '#' -> WALL;
            case '.' -> FLOOR;
            case 'D' -> DOOR;
            case 'X' -> EXIT;
            default -> FLOOR;
        };
    }
}
