package lostfacility.cli;

import lostfacility.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders stylized ASCII map frames for the terminal console.
 */
public class AsciiMapRenderer {

    // ANSI Color Escape Sequences
    public static final String RESET = "\u001B[0m";
    public static final String CYAN = "\u001B[36m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";

    private final boolean useColors;

    public AsciiMapRenderer() {
        this(true);
    }

    public AsciiMapRenderer(boolean useColors) {
        this.useColors = useColors;
    }

    public String render(Room room, Player player, List<Enemy> enemies, List<Npc> npcs) {
        if (room == null) return "[No Map Available]";

        Map<Position, Character> markerMap = new HashMap<>();
        Map<Position, String> colorMap = new HashMap<>();

        // 1. Mark NPCs
        if (npcs != null) {
            for (Npc npc : npcs) {
                if (npc.isAlive()) {
                    markerMap.put(npc.getPosition(), 'N');
                    colorMap.put(npc.getPosition(), GREEN);
                }
            }
        }

        // 2. Mark Enemies
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive()) {
                    markerMap.put(enemy.getPosition(), 'R');
                    colorMap.put(enemy.getPosition(), RED);
                }
            }
        }

        // 3. Mark Player (highest priority on top of ground items)
        Position playerPos = (player != null) ? player.getPosition() : null;

        StringBuilder sb = new StringBuilder();
        sb.append("+").append("-".repeat(room.getWidth() * 2 + 1)).append("+\n");

        for (int y = 0; y < room.getHeight(); y++) {
            sb.append("| ");
            for (int x = 0; x < room.getWidth(); x++) {
                Position pos = new Position(x, y);

                if (playerPos != null && playerPos.equals(pos)) {
                    sb.append(colorize("@", CYAN)).append(" ");
                } else if (markerMap.containsKey(pos)) {
                    char marker = markerMap.get(pos);
                    String color = colorMap.getOrDefault(pos, WHITE);
                    sb.append(colorize(String.valueOf(marker), color)).append(" ");
                } else {
                    Tile tile = room.getTile(x, y);
                    char glyph = (tile != null) ? tile.getDisplayGlyph() : ' ';
                    String color = switch (glyph) {
                        case '#' -> GRAY;
                        case '*' -> YELLOW;
                        case '+', '/' -> BLUE;
                        case 'X' -> GREEN;
                        default -> WHITE;
                    };
                    sb.append(colorize(String.valueOf(glyph), color)).append(" ");
                }
            }
            sb.append("|\n");
        }

        sb.append("+").append("-".repeat(room.getWidth() * 2 + 1)).append("+");
        return sb.toString();
    }

    private String colorize(String text, String color) {
        if (!useColors || color == null) {
            return text;
        }
        return color + text + RESET;
    }
}
