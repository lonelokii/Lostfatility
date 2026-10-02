package lostfacility.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lostfacility.engine.GameState;
import lostfacility.event.*;
import lostfacility.model.*;

import java.util.List;

/**
 * 2D JavaFX Canvas rendering the current room tiles, player, enemies, NPCs,
 * items, doors, combat animations, floating combat text, and screen shake.
 */
public class GameCanvas extends Canvas {

    private GameState gameState;
    private final SpriteManager spriteManager;
    private final AnimationController animationController;
    private double tileSize = 48.0;

    public GameCanvas(double width, double height) {
        super(width, height);
        this.spriteManager = new SpriteManager();
        this.animationController = new AnimationController(this::render);

        // Responsive resizing support
        widthProperty().addListener(evt -> render());
        heightProperty().addListener(evt -> render());

        // Start 60 FPS animation timer
        this.animationController.start();
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        render();
    }

    public GameState getGameState() {
        return gameState;
    }

    public SpriteManager getSpriteManager() {
        return spriteManager;
    }

    public AnimationController getAnimationController() {
        return animationController;
    }

    public double getTileSize() {
        return tileSize;
    }

    public void setTileSize(double tileSize) {
        this.tileSize = Math.max(16.0, tileSize);
        render();
    }

    /**
     * Wires canvas animation triggers to the game event stream.
     */
    public void attachToEventManager(EventManager eventManager) {
        if (eventManager == null) return;

        eventManager.subscribe(event -> {
            if (event instanceof MoveEvent me) {
                if (me.roomChanged()) {
                    animationController.resetAll();
                } else if (me.fromPosition() != null && me.toPosition() != null) {
                    animationController.startInterpolation(me.entityId(), me.fromPosition(), me.toPosition(), 0.150);
                }
            } else if (event instanceof CombatEvent ce) {
                handleCombatEvent(ce);
            } else if (event instanceof ItemEvent ie) {
                handleItemEvent(ie);
            }
        });
    }

    private void handleCombatEvent(CombatEvent ce) {
        if (gameState == null || gameState.getCurrentRoom() == null) return;

        Position targetPos = null;
        if ("player".equalsIgnoreCase(ce.targetId())) {
            targetPos = gameState.getPlayer().getPosition();
        } else {
            for (Enemy enemy : gameState.getEnemiesInCurrentRoom()) {
                if (enemy.getId().equalsIgnoreCase(ce.targetId())) {
                    targetPos = enemy.getPosition();
                    break;
                }
            }
        }

        if (targetPos != null) {
            double[] offset = getRoomOffsets();
            double screenX = offset[0] + targetPos.x() * tileSize + tileSize / 2;
            double screenY = offset[1] + targetPos.y() * tileSize;

            // Combat slash effect
            animationController.addCombatEffect(targetPos.x(), targetPos.y(), "slash");

            // Damage floating text
            Color color = "player".equalsIgnoreCase(ce.targetId()) ? Color.web("#ef4444") : Color.web("#fbbf24");
            String label = "-" + ce.damage() + (ce.isDefeated() ? " [KILL]" : "");
            animationController.addFloatingText(new FloatingText(label, screenX - 10, screenY, color));

            // Screen shake on hit
            animationController.triggerScreenShake(4.0, 0.12);
        }
    }

    private void handleItemEvent(ItemEvent ie) {
        if (gameState == null || gameState.getPlayer() == null) return;

        Position pos = gameState.getPlayer().getPosition();
        if (pos != null) {
            double[] offset = getRoomOffsets();
            double screenX = offset[0] + pos.x() * tileSize + tileSize / 2;
            double screenY = offset[1] + pos.y() * tileSize - 10;

            if (ie.actionType() == ItemEvent.ActionType.TAKE) {
                animationController.addFloatingText(new FloatingText("+" + ie.itemName(), screenX - 15, screenY, Color.web("#38bdf8")));
            } else if (ie.actionType() == ItemEvent.ActionType.USE) {
                animationController.addFloatingText(new FloatingText(ie.itemName(), screenX - 10, screenY, Color.web("#10b981")));
            }
        }
    }

    private double[] getRoomOffsets() {
        if (gameState == null || gameState.getCurrentRoom() == null) {
            return new double[]{0.0, 0.0};
        }
        Room room = gameState.getCurrentRoom();
        double roomW = room.getWidth() * tileSize;
        double roomH = room.getHeight() * tileSize;
        double ox = Math.max(10.0, (getWidth() - roomW) / 2.0);
        double oy = Math.max(10.0, (getHeight() - roomH) / 2.0);
        return new double[]{ox, oy};
    }

    /**
     * Main 60 FPS Canvas draw pass.
     */
    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // 1. Clear background
        gc.setFill(Color.web("#060a12"));
        gc.fillRect(0, 0, w, h);

        if (gameState == null || gameState.getCurrentRoom() == null) {
            gc.setFill(Color.web("#94a3b8"));
            gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 16));
            gc.fillText("Awaiting Facility Grid Feed...", w / 2 - 120, h / 2);
            return;
        }

        Room room = gameState.getCurrentRoom();
        double time = animationController.getTotalTimeSeconds();
        double[] offsets = getRoomOffsets();
        double ox = offsets[0];
        double oy = offsets[1];

        gc.save();

        // Apply screen shake
        double shakeX = animationController.getShakeOffsetX();
        double shakeY = animationController.getShakeOffsetY();
        gc.translate(shakeX, shakeY);

        // 2. Render room tiles and items
        for (int y = 0; y < room.getHeight(); y++) {
            for (int x = 0; x < room.getWidth(); x++) {
                Tile tile = room.getTile(x, y);
                if (tile == null) continue;

                double tx = ox + x * tileSize;
                double ty = oy + y * tileSize;

                spriteManager.drawTile(gc, tile, tx, ty, tileSize, time);

                // Render ground items
                if (tile.hasItems()) {
                    List<Item> items = tile.getItemsOnGround();
                    spriteManager.drawItem(gc, items.get(0), tx, ty, tileSize, time);

                    if (items.size() > 1) {
                        // Badge indicating stack count
                        gc.setFill(Color.web("#f59e0b"));
                        gc.fillOval(tx + tileSize - 14, ty + 2, 12, 12);
                        gc.setFill(Color.BLACK);
                        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
                        gc.fillText(String.valueOf(items.size()), tx + tileSize - 11, ty + 11);
                    }
                }
            }
        }

        // 3. Render NPCs
        for (Npc npc : gameState.getNpcsInCurrentRoom()) {
            Position pos = npc.getPosition();
            double nx = ox + pos.x() * tileSize;
            double ny = oy + pos.y() * tileSize;
            spriteManager.drawNpc(gc, npc, nx, ny, tileSize, time);
        }

        // 4. Render Enemies
        for (Enemy enemy : gameState.getEnemiesInCurrentRoom()) {
            double[] renderPos = animationController.getEntityRenderPosition(enemy.getId(), enemy.getPosition());
            double ex = ox + renderPos[0] * tileSize;
            double ey = oy + renderPos[1] * tileSize;

            spriteManager.drawEnemy(gc, enemy, ex, ey, tileSize, time);

            if (enemy.isAlive() && enemy.getHp() < enemy.getMaxHp()) {
                spriteManager.drawHealthBar(gc, ex + 6, ey - 6, tileSize - 12, 4, enemy.getHp(), enemy.getMaxHp());
            }
        }

        // 5. Render Player
        Player player = gameState.getPlayer();
        if (player != null) {
            double[] renderPos = animationController.getEntityRenderPosition("player", player.getPosition());
            double px = ox + renderPos[0] * tileSize;
            double py = oy + renderPos[1] * tileSize;

            spriteManager.drawPlayer(gc, player, player.getFacingDirection(), px, py, tileSize, time);

            if (player.getHp() < player.getMaxHp()) {
                spriteManager.drawHealthBar(gc, px + 6, py - 6, tileSize - 12, 4, player.getHp(), player.getMaxHp());
            }
        }

        // 6. Render Combat Effects
        for (AnimationController.CombatEffect effect : animationController.getActiveEffects()) {
            // Calculate screen coordinates for effect
            double fx = ox + effect.getTileX() * tileSize;
            double fy = oy + effect.getTileY() * tileSize;
            effect.render(gc, fx, fy, tileSize);
        }

        // 7. Render Floating Text
        for (FloatingText ft : animationController.getFloatingTexts()) {
            ft.render(gc);
        }

        gc.restore();
    }
}
