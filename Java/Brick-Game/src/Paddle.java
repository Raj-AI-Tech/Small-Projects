import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

/**
 * Represents the player paddle.
 * Styled with a golden rounded capsule body and metallic silver inset bar
 * matching the visual reference images.
 */
public class Paddle {
    private double x;
    private double y;
    private final int width;
    private final int height;
    private final double speed;
    private final int screenWidth;

    public Paddle(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.width = 96;
        this.height = 16;
        this.speed = 7.5;
        this.y = screenHeight - 65; // Positioned near bottom
        reset();
    }

    /**
     * Resets paddle to horizontal center of playfield.
     */
    public void reset() {
        this.x = (screenWidth - width) / 2.0;
    }

    /**
     * Moves the paddle left by its speed, clamped at the left boundary.
     */
    public void moveLeft() {
        x -= speed;
        if (x < 0) {
            x = 0;
        }
    }

    /**
     * Moves the paddle right by its speed, clamped at the right boundary.
     */
    public void moveRight() {
        x += speed;
        if (x + width > screenWidth) {
            x = screenWidth - width;
        }
    }

    /**
     * Smoothly positions paddle toward a target X (e.g., from mouse tracking).
     */
    public void setPositionClamped(double targetX) {
        this.x = Math.max(0, Math.min(screenWidth - width, targetX));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    /**
     * Draws the golden capsule paddle with metallic silver center.
     */
    public void draw(Graphics2D g2d) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // 1. Outer golden body (pill / capsule shape)
        RoundRectangle2D outerBody = new RoundRectangle2D.Double(px, py, width, height, 16, 16);
        
        GradientPaint goldGrad = new GradientPaint(
            px, py, new Color(255, 235, 70),
            px, py + height, new Color(190, 125, 0)
        );
        g2d.setPaint(goldGrad);
        g2d.fill(outerBody);

        // Subtle dark golden border
        g2d.setColor(new Color(110, 75, 0));
        g2d.draw(outerBody);

        // Upper glossy highlight rim
        g2d.setColor(new Color(255, 255, 200, 160));
        g2d.drawRoundRect(px + 2, py + 1, width - 5, height - 3, 14, 14);

        // 2. Inner metallic silver inset bar (as seen in all reference images)
        int insetWidth = 60;
        int insetHeight = 8;
        int insetX = px + (width - insetWidth) / 2;
        int insetY = py + (height - insetHeight) / 2;

        RoundRectangle2D innerPill = new RoundRectangle2D.Double(insetX, insetY, insetWidth, insetHeight, 8, 8);
        GradientPaint silverGrad = new GradientPaint(
            insetX, insetY, new Color(245, 248, 255),
            insetX, insetY + insetHeight, new Color(135, 145, 160)
        );
        g2d.setPaint(silverGrad);
        g2d.fill(innerPill);

        // Dark metallic rim
        g2d.setColor(new Color(75, 80, 90));
        g2d.draw(innerPill);

        // Top specular gleam on silver inset
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.drawLine(insetX + 4, insetY + 1, insetX + insetWidth - 5, insetY + 1);
    }
}
