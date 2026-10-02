package lostfacility.gui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import lostfacility.model.Position;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High-performance 60 FPS animation loop managing 150ms tile movement interpolation,
 * 5-frame combat hit/slash effects, screen shake, and floating combat text.
 */
public class AnimationController extends AnimationTimer {

    private long lastTimeNano = 0;
    private double totalTimeSeconds = 0.0;
    private final Runnable frameCallback;

    // Movement interpolation per entity
    private final Map<String, MoveInterpolation> interpolations = new ConcurrentHashMap<>();

    // Visual combat animations
    private final List<CombatEffect> activeEffects = new CopyOnWriteArrayList<>();

    // Floating damage numbers and status text
    private final List<FloatingText> floatingTexts = new CopyOnWriteArrayList<>();

    // Screen shake
    private double shakeRemainingSeconds = 0.0;
    private double shakeIntensity = 0.0;
    private double shakeOffsetX = 0.0;
    private double shakeOffsetY = 0.0;
    private final Random random = new Random();

    public record MoveInterpolation(
            double fromX, double fromY,
            double toX, double toY,
            double durationSeconds,
            double[] elapsedRef
    ) {
        public double getProgress() {
            return Math.min(1.0, elapsedRef[0] / durationSeconds);
        }

        public boolean isComplete() {
            return elapsedRef[0] >= durationSeconds;
        }

        public double getInterpolatedX() {
            double t = getProgress();
            // Cosine smooth-step interpolation
            double smooth = 0.5 - 0.5 * Math.cos(t * Math.PI);
            return fromX + (toX - fromX) * smooth;
        }

        public double getInterpolatedY() {
            double t = getProgress();
            double smooth = 0.5 - 0.5 * Math.cos(t * Math.PI);
            return fromY + (toY - fromY) * smooth;
        }
    }

    public static class CombatEffect {
        private final double tileX;
        private final double tileY;
        private final String type;
        private final int totalFrames = 5;
        private int currentFrame = 0;
        private final double frameDuration = 0.030; // 30ms per frame = 150ms total
        private double timer = 0.0;

        public CombatEffect(double tileX, double tileY, String type) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.type = type != null ? type : "slash";
        }

        public void update(double delta) {
            timer += delta;
            if (timer >= frameDuration) {
                timer -= frameDuration;
                currentFrame++;
            }
        }

        public double getTileX() {
            return tileX;
        }

        public double getTileY() {
            return tileY;
        }

        public boolean isComplete() {
            return currentFrame >= totalFrames;
        }

        public void render(GraphicsContext gc, double screenX, double screenY, double tileSize) {
            if (isComplete()) return;

            double cx = screenX + tileSize / 2;
            double cy = screenY + tileSize / 2;
            double progress = (double) currentFrame / totalFrames;

            gc.save();
            if ("slash".equalsIgnoreCase(type)) {
                // 5-frame energized arc slash
                double angleStart = -45 + progress * 60;
                double arcExtent = 60 + progress * 20;
                double radius = (tileSize * 0.45) * (0.8 + 0.3 * progress);

                gc.setStroke(Color.web("#f43f5e", 1.0 - progress * 0.4));
                gc.setLineWidth(3.5 - progress * 1.5);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeArc(cx - radius, cy - radius, radius * 2, radius * 2, angleStart, arcExtent, javafx.scene.shape.ArcType.OPEN);

                // Secondary cyan energy blade line
                gc.setStroke(Color.web("#00f0ff", 1.0 - progress * 0.5));
                gc.setLineWidth(1.5);
                gc.strokeArc(cx - radius * 0.85, cy - radius * 0.85, radius * 1.7, radius * 1.7, angleStart + 5, arcExtent * 0.8, javafx.scene.shape.ArcType.OPEN);
            } else {
                // Impact sparks / explosion burst
                int sparkCount = 6 + currentFrame * 2;
                double sparkRadius = (tileSize * 0.2) + progress * (tileSize * 0.4);

                for (int i = 0; i < sparkCount; i++) {
                    double angle = (2 * Math.PI / sparkCount) * i + (progress * 0.5);
                    double sx = cx + Math.cos(angle) * sparkRadius;
                    double sy = cy + Math.sin(angle) * sparkRadius;

                    gc.setFill(Color.web("#fbbf24", 1.0 - progress));
                    gc.fillOval(sx - 2, sy - 2, 4, 4);

                    gc.setStroke(Color.web("#ef4444", 0.8 - progress * 0.6));
                    gc.setLineWidth(1.5);
                    gc.strokeLine(cx, cy, sx, sy);
                }
            }
            gc.restore();
        }
    }

    public AnimationController(Runnable frameCallback) {
        this.frameCallback = frameCallback;
    }

    @Override
    public void handle(long nowNano) {
        if (lastTimeNano == 0) {
            lastTimeNano = nowNano;
            return;
        }

        double deltaSeconds = (nowNano - lastTimeNano) / 1_000_000_000.0;
        // Cap max delta to prevent animation jumping during lag spikes or debugger pause
        if (deltaSeconds > 0.1) deltaSeconds = 0.1;
        lastTimeNano = nowNano;
        totalTimeSeconds += deltaSeconds;

        // 1. Update movement interpolations
        for (Iterator<Map.Entry<String, MoveInterpolation>> it = interpolations.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MoveInterpolation> entry = it.next();
            MoveInterpolation interp = entry.getValue();
            interp.elapsedRef()[0] += deltaSeconds;
            if (interp.isComplete()) {
                it.remove();
            }
        }

        // 2. Update combat effects
        for (Iterator<CombatEffect> it = activeEffects.iterator(); it.hasNext(); ) {
            CombatEffect effect = it.next();
            effect.update(deltaSeconds);
            if (effect.isComplete()) {
                activeEffects.remove(effect);
            }
        }

        // 3. Update floating texts
        for (Iterator<FloatingText> it = floatingTexts.iterator(); it.hasNext(); ) {
            FloatingText text = it.next();
            text.update(deltaSeconds);
            if (text.isExpired()) {
                floatingTexts.remove(text);
            }
        }

        // 4. Update screen shake
        if (shakeRemainingSeconds > 0) {
            shakeRemainingSeconds -= deltaSeconds;
            if (shakeRemainingSeconds <= 0) {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
                shakeIntensity = 0;
            } else {
                shakeOffsetX = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
                shakeOffsetY = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            }
        }

        // 5. Trigger redraw
        if (frameCallback != null) {
            frameCallback.run();
        }
    }

    public void startInterpolation(String entityId, Position from, Position to, double durationSeconds) {
        if (entityId == null || from == null || to == null) return;
        interpolations.put(entityId, new MoveInterpolation(
                from.x(), from.y(),
                to.x(), to.y(),
                Math.max(0.05, durationSeconds),
                new double[]{0.0}
        ));
    }

    public double[] getEntityRenderPosition(String entityId, Position currentTilePos) {
        if (entityId != null) {
            MoveInterpolation interp = interpolations.get(entityId);
            if (interp != null && !interp.isComplete()) {
                return new double[]{interp.getInterpolatedX(), interp.getInterpolatedY()};
            }
        }
        if (currentTilePos != null) {
            return new double[]{currentTilePos.x(), currentTilePos.y()};
        }
        return new double[]{0.0, 0.0};
    }

    public void addCombatEffect(double tileX, double tileY, String type) {
        activeEffects.add(new CombatEffect(tileX, tileY, type));
    }

    public void addFloatingText(FloatingText text) {
        if (text != null) {
            floatingTexts.add(text);
        }
    }

    public void triggerScreenShake(double intensityPixels, double durationSeconds) {
        this.shakeIntensity = intensityPixels;
        this.shakeRemainingSeconds = durationSeconds;
    }

    public void resetAll() {
        interpolations.clear();
        activeEffects.clear();
        floatingTexts.clear();
        shakeRemainingSeconds = 0;
        shakeOffsetX = 0;
        shakeOffsetY = 0;
    }

    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public double getShakeOffsetX() {
        return shakeOffsetX;
    }

    public double getShakeOffsetY() {
        return shakeOffsetY;
    }

    public List<CombatEffect> getActiveEffects() {
        return activeEffects;
    }

    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }
}
