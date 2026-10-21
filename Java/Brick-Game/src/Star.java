import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Represents a single twinkling star in the space background.
 */
public class Star {
    private final double x;
    private final double y;
    private final double size;
    private final float baseBrightness;
    private final float twinkleSpeed;
    private float twinklePhase;
    private final Color starColor;

    public Star(double x, double y, Random random) {
        this.x = x;
        this.y = y;
        
        // Random star diameter: mostly 1.0 - 1.8 px, occasional larger 2.2 px
        double r = random.nextDouble();
        if (r < 0.70) {
            this.size = 1.0 + random.nextDouble() * 0.5;
        } else if (r < 0.92) {
            this.size = 1.6 + random.nextDouble() * 0.5;
        } else {
            this.size = 2.2 + random.nextDouble() * 0.4;
        }

        this.baseBrightness = 0.35f + random.nextFloat() * 0.55f;
        this.twinkleSpeed = 0.02f + random.nextFloat() * 0.04f;
        this.twinklePhase = random.nextFloat() * (float) (Math.PI * 2);

        // Palette inspired by classic arcade deep space (white, ice blue, pale amber)
        float hueType = random.nextFloat();
        if (hueType < 0.70f) {
            this.starColor = new Color(245, 248, 255); // Crisp star white
        } else if (hueType < 0.85f) {
            this.starColor = new Color(190, 220, 255); // Pale cyan / ice blue
        } else {
            this.starColor = new Color(255, 245, 205); // Pale warm gold
        }
    }

    public void update() {
        twinklePhase += twinkleSpeed;
        if (twinklePhase > Math.PI * 2) {
            twinklePhase -= Math.PI * 2;
        }
    }

    public void draw(Graphics2D g2d) {
        // Compute fluctuating alpha based on sine twinkle
        float sineVal = (float) Math.sin(twinklePhase);
        float alpha = baseBrightness + sineVal * 0.25f;
        if (alpha < 0.15f) alpha = 0.15f;
        if (alpha > 1.0f) alpha = 1.0f;

        g2d.setColor(new Color(
            starColor.getRed() / 255f,
            starColor.getGreen() / 255f,
            starColor.getBlue() / 255f,
            alpha
        ));

        if (size <= 1.2) {
            g2d.fillRect((int) x, (int) y, 1, 1);
        } else {
            g2d.fillOval((int) x, (int) y, (int) Math.round(size), (int) Math.round(size));
        }
    }
}
