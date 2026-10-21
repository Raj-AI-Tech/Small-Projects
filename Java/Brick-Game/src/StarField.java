import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages procedural space star field with subtle twinkling.
 */
public class StarField {
    private final List<Star> stars = new ArrayList<>();
    private final int width;
    private final int height;

    public StarField(int width, int height, int starCount) {
        this.width = width;
        this.height = height;
        initStars(starCount);
    }

    private void initStars(int count) {
        Random rand = new Random(1337); // Consistent pleasant distribution
        for (int i = 0; i < count; i++) {
            double x = rand.nextDouble() * width;
            double y = rand.nextDouble() * height;
            stars.add(new Star(x, y, rand));
        }
    }

    public void update() {
        for (Star star : stars) {
            star.update();
        }
    }

    public void draw(Graphics2D g2d) {
        // Deep arcade space background
        g2d.setColor(new Color(6, 8, 14));
        g2d.fillRect(0, 0, width, height);

        for (Star star : stars) {
            star.draw(g2d);
        }
    }
}
