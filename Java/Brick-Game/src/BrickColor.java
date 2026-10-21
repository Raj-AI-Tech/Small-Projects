import java.awt.Color;

/**
 * Defines the brick types, colors, point values, and hit points matching the arcade reference style.
 */
public enum BrickColor {
    RED(
        new Color(220, 35, 35),
        new Color(255, 100, 95),
        new Color(130, 10, 10),
        10,
        1
    ),
    ORANGE(
        new Color(245, 125, 20),
        new Color(255, 175, 75),
        new Color(160, 65, 0),
        15,
        1
    ),
    YELLOW(
        new Color(245, 210, 20),
        new Color(255, 240, 95),
        new Color(165, 135, 0),
        20,
        1
    ),
    GREEN(
        new Color(40, 190, 45),
        new Color(105, 235, 110),
        new Color(15, 110, 20),
        25,
        1
    ),
    CYAN(
        new Color(20, 195, 235),
        new Color(110, 235, 255),
        new Color(5, 110, 140),
        30,
        1
    ),
    BLUE(
        new Color(30, 100, 235),
        new Color(105, 160, 255),
        new Color(10, 50, 150),
        35,
        1
    ),
    MAGENTA(
        new Color(220, 35, 195),
        new Color(255, 115, 235),
        new Color(135, 10, 115),
        40,
        1
    ),
    SILVER(
        new Color(185, 192, 205),
        new Color(240, 244, 252),
        new Color(105, 112, 125),
        50,
        2 // Reinforced metallic brick: takes 2 hits, shows damage crack on hit 1
    ),
    GOLD(
        new Color(240, 190, 40),
        new Color(255, 240, 130),
        new Color(160, 115, 10),
        100,
        2
    );

    private final Color baseColor;
    private final Color highlightColor;
    private final Color shadowColor;
    private final int points;
    private final int defaultHits;

    BrickColor(Color baseColor, Color highlightColor, Color shadowColor, int points, int defaultHits) {
        this.baseColor = baseColor;
        this.highlightColor = highlightColor;
        this.shadowColor = shadowColor;
        this.points = points;
        this.defaultHits = defaultHits;
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public int getPoints() {
        return points;
    }

    public int getDefaultHits() {
        return defaultHits;
    }
}
