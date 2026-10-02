package lostfacility.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Represents animated in-game text (damage numbers, healing, status alerts)
 * that floats upwards and fades out.
 */
public class FloatingText {

    private final String text;
    private double x;
    private double y;
    private final Color color;
    private final double maxLifetimeSeconds;
    private double ageSeconds;
    private final double velocityY;

    public FloatingText(String text, double x, double y, Color color) {
        this(text, x, y, color, 1.0, -35.0);
    }

    public FloatingText(String text, double x, double y, Color color, double lifetimeSeconds, double velocityY) {
        this.text = text != null ? text : "";
        this.x = x;
        this.y = y;
        this.color = color != null ? color : Color.WHITE;
        this.maxLifetimeSeconds = Math.max(0.1, lifetimeSeconds);
        this.ageSeconds = 0.0;
        this.velocityY = velocityY;
    }

    public void update(double deltaSeconds) {
        ageSeconds += deltaSeconds;
        y += velocityY * deltaSeconds;
    }

    public boolean isExpired() {
        return ageSeconds >= maxLifetimeSeconds;
    }

    public void render(GraphicsContext gc) {
        if (isExpired()) return;

        double progress = ageSeconds / maxLifetimeSeconds;
        double alpha = Math.max(0.0, 1.0 - progress);

        gc.save();
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));

        // Dark outline for contrast
        gc.setFill(new Color(0.0, 0.0, 0.0, alpha * 0.9));
        gc.fillText(text, x - 1, y - 1);
        gc.fillText(text, x + 1, y - 1);
        gc.fillText(text, x - 1, y + 1);
        gc.fillText(text, x + 1, y + 1);

        // Core text
        Color textColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        gc.setFill(textColor);
        gc.fillText(text, x, y);

        gc.restore();
    }
}
