import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Represents a destructible brick in the game playfield.
 * Features 3D beveled rendering, specular gloss, and damage overlays.
 */
public class Brick {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final BrickColor colorType;
    private int hitPoints;
    private final int maxHitPoints;
    private boolean destroyed;

    public Brick(int x, int y, int width, int height, BrickColor colorType) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colorType = colorType;
        this.maxHitPoints = colorType.getDefaultHits();
        this.hitPoints = this.maxHitPoints;
        this.destroyed = false;
    }

    public Brick(int x, int y, int width, int height, BrickColor colorType, int customHits) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colorType = colorType;
        this.maxHitPoints = customHits;
        this.hitPoints = customHits;
        this.destroyed = false;
    }

    /**
     * Called when the ball hits the brick.
     * Decrements hit points.
     * @return true if the brick was completely destroyed by this hit, false if damaged.
     */
    public boolean takeHit() {
        if (destroyed) return false;
        hitPoints--;
        if (hitPoints <= 0) {
            destroyed = true;
            return true;
        }
        return false;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BrickColor getColorType() {
        return colorType;
    }

    public int getPoints() {
        return colorType.getPoints();
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Draws the authentic 3D beveled arcade brick.
     */
    public void draw(Graphics2D g2d) {
        if (destroyed) return;

        // 1. Dark outer border (1px)
        g2d.setColor(new Color(15, 18, 24));
        g2d.drawRect(x, y, width - 1, height - 1);

        // 2. Main inner body
        g2d.setColor(colorType.getBaseColor());
        g2d.fillRect(x + 1, y + 1, width - 2, height - 2);

        // 3. Top and Left highlights (Raised 3D effect)
        g2d.setColor(colorType.getHighlightColor());
        g2d.fillRect(x + 1, y + 1, width - 2, 2); // Top bevel
        g2d.fillRect(x + 1, y + 1, 2, height - 2); // Left bevel

        // 4. Bottom and Right shadows (Depth bevel)
        g2d.setColor(colorType.getShadowColor());
        g2d.fillRect(x + 1, y + height - 3, width - 2, 2); // Bottom bevel
        g2d.fillRect(x + width - 3, y + 1, 2, height - 2); // Right bevel

        // 5. Specular gloss highlight across top inner row
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillRect(x + 3, y + 3, width - 6, 2);

        // 6. If brick is damaged (e.g. silver brick with 1 HP remaining), draw retro damage emblem
        if (maxHitPoints > 1 && hitPoints < maxHitPoints) {
            int cx = x + width / 2;
            int cy = y + height / 2;
            
            // Red cracked emblem inspired by the reference images
            g2d.setColor(new Color(220, 30, 30));
            g2d.drawLine(cx - 6, cy, cx + 6, cy);
            g2d.drawLine(cx, cy - 4, cx, cy + 4);
            g2d.drawLine(cx - 4, cy - 3, cx + 4, cy + 3);
            g2d.drawLine(cx - 4, cy + 3, cx + 4, cy - 3);

            // Subtle dark crack line
            g2d.setColor(new Color(70, 10, 10, 180));
            g2d.drawLine(cx - 2, cy - 4, cx + 1, cy + 3);
        }
    }
}
