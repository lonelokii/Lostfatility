package lostfacility.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lostfacility.model.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages game sprites and tiles. Attempts to load external PNG image assets
 * from the classpath (/sprites/), falling back to rich procedural vector Canvas
 * rendering routines designed with a dark retro cyberpunk aesthetic.
 */
public class SpriteManager {

    private final Map<String, Image> imageCache = new HashMap<>();
    private boolean externalAssetsChecked = false;

    public SpriteManager() {
    }

    /**
     * Attempts to retrieve or load an image from resources.
     */
    public Image getImage(String name) {
        if (name == null || name.isBlank()) return null;
        if (imageCache.containsKey(name)) {
            return imageCache.get(name);
        }

        try {
            String path = "/sprites/" + name + ".png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                Image img = new Image(is);
                imageCache.put(name, img);
                return img;
            }
        } catch (Exception ignored) {
        }
        imageCache.put(name, null);
        return null;
    }

    // ==========================================
    // TILE RENDERING
    // ==========================================

    public void drawTile(GraphicsContext gc, Tile tile, double x, double y, double size, double time) {
        if (tile == null) return;

        TileType type = tile.getType();
        switch (type) {
            case WALL -> drawWallTile(gc, x, y, size);
            case FLOOR -> drawFloorTile(gc, x, y, size);
            case DOOR -> drawDoorTile(gc, tile, x, y, size, time);
            case EXIT -> drawExitTile(gc, x, y, size, time);
            case VOID -> {
                gc.setFill(Color.web("#050811"));
                gc.fillRect(x, y, size, size);
            }
        }
    }

    private void drawFloorTile(GraphicsContext gc, double x, double y, double size) {
        // Dark metallic facility panel floor
        gc.setFill(Color.web("#0e1422"));
        gc.fillRect(x, y, size, size);

        // Subtle tile seam border
        gc.setStroke(Color.web("#172033"));
        gc.setLineWidth(1.0);
        gc.strokeRect(x + 0.5, y + 0.5, size - 1, size - 1);

        // 4 Corner bolts/rivets
        gc.setFill(Color.web("#24314c"));
        double r = 1.5;
        gc.fillOval(x + 3, y + 3, r * 2, r * 2);
        gc.fillOval(x + size - 3 - r * 2, y + 3, r * 2, r * 2);
        gc.fillOval(x + 3, y + size - 3 - r * 2, r * 2, r * 2);
        gc.fillOval(x + size - 3 - r * 2, y + size - 3 - r * 2, r * 2, r * 2);
    }

    private void drawWallTile(GraphicsContext gc, double x, double y, double size) {
        // Base dark titanium wall
        gc.setFill(Color.web("#0a0e17"));
        gc.fillRect(x, y, size, size);

        // Inset beveled border
        gc.setFill(Color.web("#161f30"));
        gc.fillRect(x + 2, y + 2, size - 4, size - 4);

        // Inner plate
        gc.setFill(Color.web("#1a2438"));
        gc.fillRect(x + 5, y + 5, size - 10, size - 10);

        // Tech conduits / circuit accent
        gc.setStroke(Color.web("#00f0ff", 0.4));
        gc.setLineWidth(1.5);
        gc.strokeLine(x + 8, y + size / 2, x + size - 8, y + size / 2);

        // Border outline
        gc.setStroke(Color.web("#223354"));
        gc.setLineWidth(1.0);
        gc.strokeRect(x + 0.5, y + 0.5, size - 1, size - 1);
    }

    private void drawDoorTile(GraphicsContext gc, Tile tile, double x, double y, double size, double time) {
        // Floor underneath
        drawFloorTile(gc, x, y, size);

        boolean open = tile.isOpen();
        boolean locked = tile.isLocked();

        if (open) {
            // Open door sliding into wall jambs
            gc.setFill(Color.web("#1e293b"));
            gc.fillRect(x, y, 6, size);
            gc.fillRect(x + size - 6, y, 6, size);

            // Green sensor indicator
            gc.setFill(Color.web("#10b981"));
            gc.fillOval(x + size / 2 - 3, y + 2, 6, 6);
        } else {
            // Heavy blast door panels
            gc.setFill(Color.web("#162032"));
            gc.fillRect(x + 2, y + 2, size - 4, size - 4);

            // Door frame
            gc.setStroke(Color.web("#334155"));
            gc.setLineWidth(2.0);
            gc.strokeRect(x + 2, y + 2, size - 4, size - 4);

            // Center split seam
            gc.setStroke(Color.web("#0f172a"));
            gc.strokeLine(x + size / 2, y + 2, x + size / 2, y + size - 2);

            if (locked) {
                // Warning stripes or red lock glow
                double pulse = 0.5 + 0.5 * Math.sin(time * 6.0);
                gc.setFill(Color.web("#ef4444", 0.8 + 0.2 * pulse));
                gc.fillOval(x + size / 2 - 4, y + size / 2 - 4, 8, 8);

                // Red hazard bar
                gc.setStroke(Color.web("#ef4444", 0.6));
                gc.setLineWidth(2.0);
                gc.strokeLine(x + 6, y + size / 2, x + size - 6, y + size / 2);
            } else {
                // Amber unlocked / ready light
                gc.setFill(Color.web("#f59e0b"));
                gc.fillOval(x + size / 2 - 3, y + size / 2 - 3, 6, 6);
            }
        }
    }

    private void drawExitTile(GraphicsContext gc, double x, double y, double size, double time) {
        // Teleport / escape platform
        drawFloorTile(gc, x, y, size);

        double pulse = 0.6 + 0.4 * Math.sin(time * 4.0);

        // Pulsing cyan emitter ring
        gc.setStroke(Color.web("#00f0ff", pulse));
        gc.setLineWidth(2.5);
        gc.strokeOval(x + 5, y + 5, size - 10, size - 10);

        // Core glow
        RadialGradient glow = new RadialGradient(0, 0, x + size / 2, y + size / 2, size / 3, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00f0ff", 0.7 * pulse)),
                new Stop(1, Color.web("#00f0ff", 0.0)));
        gc.setFill(glow);
        gc.fillOval(x + 8, y + 8, size - 16, size - 16);

        // Exit chevron / arrow pointing up
        gc.setFill(Color.web("#ffffff", pulse));
        double cx = x + size / 2;
        double cy = y + size / 2;
        gc.fillPolygon(
                new double[]{cx, cx - 7, cx + 7},
                new double[]{cy - 8, cy + 4, cy + 4},
                3
        );
    }

    // ==========================================
    // ITEM RENDERING
    // ==========================================

    public void drawItem(GraphicsContext gc, Item item, double x, double y, double size, double time) {
        if (item == null) return;

        double cx = x + size / 2;
        double cy = y + size / 2;
        double pulse = 0.6 + 0.4 * Math.sin(time * 5.0);

        // Ground item halo
        Color haloColor = switch (item.getType()) {
            case KEY -> Color.web("#38bdf8", 0.4 * pulse);
            case WEAPON -> Color.web("#f43f5e", 0.4 * pulse);
            case ARMOR -> Color.web("#3b82f6", 0.4 * pulse);
            case CONSUMABLE -> Color.web("#10b981", 0.4 * pulse);
            default -> Color.web("#eab308", 0.4 * pulse);
        };
        gc.setFill(haloColor);
        gc.fillOval(cx - 14, cy - 14, 28, 28);

        switch (item.getType()) {
            case KEY -> {
                // Keycard shape
                gc.setFill(Color.web("#0284c7"));
                gc.fillRoundRect(cx - 9, cy - 6, 18, 12, 3, 3);
                // Magnetic stripe
                gc.setFill(Color.web("#0f172a"));
                gc.fillRect(cx - 9, cy - 3, 18, 3);
                // Chip
                gc.setFill(Color.web("#fbbf24"));
                gc.fillRect(cx + 2, cy + 1, 4, 3);
            }
            case WEAPON -> {
                // Laser blade / plasma weapon
                gc.setStroke(Color.web("#f43f5e", pulse));
                gc.setLineWidth(3.0);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(cx - 8, cy + 8, cx + 8, cy - 8);
                // Hilt
                gc.setStroke(Color.web("#64748b"));
                gc.setLineWidth(4.0);
                gc.strokeLine(cx - 8, cy + 8, cx - 4, cy + 4);
            }
            case CONSUMABLE -> {
                // Medkit box
                gc.setFill(Color.web("#f8fafc"));
                gc.fillRoundRect(cx - 8, cy - 8, 16, 16, 4, 4);
                // Red or green cross
                gc.setFill(Color.web("#ef4444"));
                gc.fillRect(cx - 2, cy - 6, 4, 12);
                gc.fillRect(cx - 6, cy - 2, 12, 4);
            }
            case ARMOR -> {
                // Energy shield badge
                gc.setFill(Color.web("#1d4ed8"));
                gc.fillPolygon(
                        new double[]{cx, cx + 9, cx + 6, cx, cx - 6, cx - 9},
                        new double[]{cy - 9, cy - 5, cy + 6, cy + 9, cy + 6, cy - 5},
                        6
                );
                gc.setStroke(Color.web("#60a5fa"));
                gc.setLineWidth(1.5);
                gc.strokePolygon(
                        new double[]{cx, cx + 9, cx + 6, cx, cx - 6, cx - 9},
                        new double[]{cy - 9, cy - 5, cy + 6, cy + 9, cy + 6, cy - 5},
                        6
                );
            }
            default -> {
                // Glowing power cell / data artifact
                gc.setFill(Color.web("#eab308"));
                gc.fillRoundRect(cx - 7, cy - 9, 14, 18, 4, 4);
                gc.setFill(Color.web("#00f0ff", pulse));
                gc.fillRect(cx - 4, cy - 4, 8, 8);
            }
        }
    }

    // ==========================================
    // ENTITY RENDERING
    // ==========================================

    public void drawPlayer(GraphicsContext gc, Player player, Direction facing, double x, double y, double size, double time) {
        double cx = x + size / 2;
        double cy = y + size / 2;

        // Shadow under character
        gc.setFill(Color.web("#000000", 0.4));
        gc.fillOval(cx - 12, y + size - 10, 24, 8);

        // Slight breathing idle bob
        double bob = Math.sin(time * 4.0) * 1.0;
        double py = cy + bob;

        // Torso / armor suit (sleek dark slate & teal)
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(cx - 10, py - 6, 20, 18, 6, 6);

        // Shoulder plates
        gc.setFill(Color.web("#0284c7"));
        gc.fillRoundRect(cx - 12, py - 5, 5, 8, 2, 2);
        gc.fillRoundRect(cx + 7, py - 5, 5, 8, 2, 2);

        // Head / helmet
        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(cx - 8, py - 18, 16, 15);

        // Glowing cyan Visor according to facing direction
        gc.setFill(Color.web("#00f0ff"));
        Direction dir = facing != null ? facing : Direction.SOUTH;
        switch (dir) {
            case NORTH -> {
                // Back of helmet, subtle sensor dot
                gc.fillOval(cx - 2, py - 16, 4, 3);
            }
            case SOUTH -> {
                // Forward horizontal visor
                gc.fillRoundRect(cx - 6, py - 13, 12, 4, 2, 2);
            }
            case WEST -> {
                // Left-facing visor
                gc.fillRoundRect(cx - 8, py - 13, 7, 4, 2, 2);
            }
            case EAST -> {
                // Right-facing visor
                gc.fillRoundRect(cx + 1, py - 13, 7, 4, 2, 2);
            }
        }

        // Energy core chest light
        gc.setFill(Color.web("#00f0ff", 0.8));
        gc.fillOval(cx - 2, py, 4, 4);
    }

    public void drawEnemy(GraphicsContext gc, Enemy enemy, double x, double y, double size, double time) {
        double cx = x + size / 2;
        double cy = y + size / 2;

        if (!enemy.isAlive()) {
            // Defeated scrap heap
            gc.setFill(Color.web("#475569", 0.7));
            gc.fillOval(cx - 12, y + size - 12, 24, 10);
            gc.setFill(Color.web("#1e293b"));
            gc.fillRect(cx - 8, y + size - 14, 16, 6);
            return;
        }

        // Shadow under enemy
        gc.setFill(Color.web("#000000", 0.45));
        gc.fillOval(cx - 14, y + size - 10, 28, 8);

        String type = enemy.getEnemyType() != null ? enemy.getEnemyType().toLowerCase() : "";
        String name = enemy.getName() != null ? enemy.getName().toLowerCase() : "";

        if (type.contains("drone") || name.contains("drone")) {
            // Hovering Security Drone
            double hover = Math.sin(time * 5.0) * 3.0;
            double dy = cy + hover;

            // Jet propulsion thruster glow
            gc.setFill(Color.web("#f97316", 0.7 + 0.3 * Math.sin(time * 12)));
            gc.fillPolygon(
                    new double[]{cx - 5, cx, cx + 5},
                    new double[]{dy + 8, dy + 16, dy + 8},
                    3
            );

            // Drone chassis (metallic disc)
            gc.setFill(Color.web("#334155"));
            gc.fillOval(cx - 14, dy - 10, 28, 18);
            gc.setStroke(Color.web("#64748b"));
            gc.setLineWidth(1.5);
            gc.strokeOval(cx - 14, dy - 10, 28, 18);

            // Cyclops red scanning eye
            gc.setFill(Color.web("#ef4444"));
            gc.fillOval(cx - 5, dy - 5, 10, 8);
            gc.setFill(Color.web("#fee2e2"));
            gc.fillOval(cx - 2, dy - 3, 4, 3);
        } else if (type.contains("turret") || name.contains("turret")) {
            // Heavy defense turret
            // Fixed octagonal base
            gc.setFill(Color.web("#1e293b"));
            gc.fillPolygon(
                    new double[]{cx - 12, cx - 6, cx + 6, cx + 12, cx + 12, cx + 6, cx - 6, cx - 12},
                    new double[]{cy + 2, cy - 4, cy - 4, cy + 2, cy + 10, cy + 14, cy + 14, cy + 10},
                    8
            );
            // Swivel dome
            gc.setFill(Color.web("#475569"));
            gc.fillOval(cx - 8, cy - 10, 16, 14);
            // Twin red barrels
            gc.setFill(Color.web("#dc2626"));
            gc.fillRect(cx - 5, cy + 2, 3, 9);
            gc.fillRect(cx + 2, cy + 2, 3, 9);
            // Laser sight
            gc.setFill(Color.web("#ef4444"));
            gc.fillOval(cx - 2, cy - 6, 4, 4);
        } else {
            // Mechanized Combat Robot
            double bob = Math.sin(time * 3.5) * 1.5;
            double ry = cy + bob;

            // Armored chassis
            gc.setFill(Color.web("#1e293b"));
            gc.fillRoundRect(cx - 12, ry - 8, 24, 20, 4, 4);

            // Hazard shoulder plates
            gc.setFill(Color.web("#eab308"));
            gc.fillRect(cx - 14, ry - 7, 5, 8);
            gc.fillRect(cx + 9, ry - 7, 5, 8);

            // Head unit
            gc.setFill(Color.web("#0f172a"));
            gc.fillRoundRect(cx - 9, ry - 18, 18, 12, 3, 3);

            // Menacing red visor slit
            gc.setFill(Color.web("#ef4444"));
            gc.fillRect(cx - 7, ry - 14, 14, 4);

            // Grill / vents
            gc.setFill(Color.web("#334155"));
            gc.fillRect(cx - 6, ry, 12, 2);
            gc.fillRect(cx - 6, ry + 4, 12, 2);
        }
    }

    public void drawNpc(GraphicsContext gc, Npc npc, double x, double y, double size, double time) {
        double cx = x + size / 2;
        double cy = y + size / 2;

        // Shadow under NPC
        gc.setFill(Color.web("#000000", 0.4));
        gc.fillOval(cx - 12, y + size - 10, 24, 8);

        // Idle bob
        double bob = Math.sin(time * 3.0) * 1.0;
        double ny = cy + bob;

        // Scientist lab coat (clean light slate/white)
        gc.setFill(Color.web("#f1f5f9"));
        gc.fillRoundRect(cx - 9, ny - 6, 18, 18, 5, 5);

        // Cyan tech collar/badge
        gc.setFill(Color.web("#0284c7"));
        gc.fillRect(cx - 4, ny - 4, 8, 4);

        // Head
        gc.setFill(Color.web("#fed7aa")); // Warm face tone
        gc.fillOval(cx - 7, ny - 18, 14, 14);

        // Hair / comm headset
        gc.setFill(Color.web("#334155"));
        gc.fillArc(cx - 8, ny - 20, 16, 12, 0, 180, ArcType.ROUND);
        // Headset cyan earpiece
        gc.setFill(Color.web("#00f0ff"));
        gc.fillOval(cx + 6, ny - 14, 3, 5);

        // Pulsing dialogue bubble indicator [💬] above head
        double bubblePulse = 0.7 + 0.3 * Math.sin(time * 4.0);
        gc.setFill(Color.web("#00f0ff", bubblePulse));
        gc.fillRoundRect(cx - 8, ny - 30, 16, 10, 4, 4);
        gc.fillPolygon(
                new double[]{cx - 3, cx + 1, cx - 1},
                new double[]{ny - 20, ny - 20, ny - 17},
                3
        );
        // Bubble dots
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(cx - 5, ny - 27, 2, 2);
        gc.fillOval(cx - 1, ny - 27, 2, 2);
        gc.fillOval(cx + 3, ny - 27, 2, 2);
    }

    // ==========================================
    // HEALTH BAR RENDERING
    // ==========================================

    public void drawHealthBar(GraphicsContext gc, double x, double y, double width, double height, int currentHp, int maxHp) {
        if (maxHp <= 0) return;

        double ratio = Math.max(0.0, Math.min(1.0, (double) currentHp / maxHp));

        // Background bar
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(x, y, width, height, 2, 2);
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(0.8);
        gc.strokeRoundRect(x, y, width, height, 2, 2);

        // Fill color based on HP ratio
        Color fillColor = ratio > 0.5 ? Color.web("#10b981") : (ratio > 0.25 ? Color.web("#f59e0b") : Color.web("#ef4444"));
        gc.setFill(fillColor);
        if (ratio > 0.0) {
            gc.fillRoundRect(x + 1, y + 1, (width - 2) * ratio, height - 2, 1, 1);
        }
    }
}
