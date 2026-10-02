package lostfacility.model;

import java.io.Serializable;
import java.util.*;

/**
 * Represents an individual room containing a 2D tile grid, exits, items, and entities.
 */
public class Room implements Serializable {

    private final String id;
    private String name;
    private String description;
    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private final Map<Direction, String> exits;
    private Position defaultSpawnPosition;

    public Room(String id, String name, String description, int width, int height) {
        this.id = Objects.requireNonNull(id, "Room id must not be null");
        this.name = name != null ? name : id;
        this.description = description != null ? description : "";
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        this.exits = new EnumMap<>(Direction.class);
        this.defaultSpawnPosition = new Position(1, 1);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(new Position(x, y), TileType.FLOOR);
            }
        }
    }

    /**
     * Constructs a Room from ASCII grid lines.
     * # = Wall, . = Floor, D = Door, P = Spawn, X = Exit
     */
    public static Room fromAscii(String id, String name, String description, List<String> lines) {
        Objects.requireNonNull(lines, "ASCII lines must not be null");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("ASCII lines must not be empty");
        }
        int height = lines.size();
        int width = lines.stream().mapToInt(String::length).max().orElse(0);

        Room room = new Room(id, name, description, width, height);

        for (int y = 0; y < height; y++) {
            String row = lines.get(y);
            for (int x = 0; x < width; x++) {
                char c = (x < row.length()) ? row.charAt(x) : ' ';
                Position pos = new Position(x, y);
                TileType type = TileType.fromGlyph(c);

                Tile tile = new Tile(pos, type);
                if (c == 'D') {
                    tile.setDoor(true);
                    tile.setOpen(false);
                } else if (c == 'P') {
                    room.setDefaultSpawnPosition(pos);
                    tile.setType(TileType.FLOOR);
                }
                room.setTile(x, y, tile);
            }
        }
        return room;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position getDefaultSpawnPosition() {
        return defaultSpawnPosition;
    }

    public void setDefaultSpawnPosition(Position defaultSpawnPosition) {
        this.defaultSpawnPosition = defaultSpawnPosition;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isInBounds(Position pos) {
        return pos != null && isInBounds(pos.x(), pos.y());
    }

    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) {
            return null;
        }
        return tiles[y][x];
    }

    public Tile getTile(Position pos) {
        if (pos == null) return null;
        return getTile(pos.x(), pos.y());
    }

    public void setTile(int x, int y, Tile tile) {
        if (isInBounds(x, y)) {
            tiles[y][x] = tile;
        }
    }

    public boolean isWalkable(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null && tile.isWalkable();
    }

    public boolean isWalkable(Position pos) {
        if (pos == null) return false;
        return isWalkable(pos.x(), pos.y());
    }

    public void placeItem(Item item, int x, int y) {
        Tile tile = getTile(x, y);
        if (tile != null) {
            tile.addItem(item);
        }
    }

    public void placeItem(Item item, Position pos) {
        if (pos != null) {
            placeItem(item, pos.x(), pos.y());
        }
    }

    public void addExit(Direction direction, String targetRoomId) {
        if (direction != null && targetRoomId != null) {
            exits.put(direction, targetRoomId);
        }
    }

    public String getExit(Direction direction) {
        return exits.get(direction);
    }

    public Map<Direction, String> getExits() {
        return Collections.unmodifiableMap(exits);
    }

    /**
     * Renders an ASCII snapshot of the room with entity positions.
     */
    public String renderAscii(Position playerPos, Map<Position, Character> entityMarkers) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position current = new Position(x, y);
                if (playerPos != null && playerPos.equals(current)) {
                    sb.append('@');
                } else if (entityMarkers != null && entityMarkers.containsKey(current)) {
                    sb.append(entityMarkers.get(current));
                } else {
                    Tile tile = tiles[y][x];
                    sb.append(tile != null ? tile.getDisplayGlyph() : ' ');
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
