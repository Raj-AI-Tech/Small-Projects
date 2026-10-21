import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * Represents the ball in the game.
 * Features 2D vector movement, angle reflection, speed clamping,
 * and 3D spherical arcade rendering.
 */
public class Ball {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private final int diameter;
    private double baseSpeed;
    private final int screenWidth;
    private final int screenHeight;
    private final int topBoundary;

    public Ball(int screenWidth, int screenHeight, int topBoundary) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.topBoundary = topBoundary;
        this.diameter = 13; // Radius ~6.5px
        this.baseSpeed = 4.2;
        resetOnPaddle(new Paddle(screenWidth, screenHeight));
    }

    /**
     * Places the ball directly on top of the paddle in ready-to-launch state.
     */
    public void resetOnPaddle(Paddle paddle) {
        this.x = paddle.getCenterX() - diameter / 2.0;
        this.y = paddle.getY() - diameter - 1;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Launches the ball upward from the paddle with a slight random or directional angle.
     */
    public void launch(double speed, double angleOffset) {
        this.baseSpeed = speed;
        // Clamp launch angle between -45 and 45 degrees
        double maxLaunchAngle = Math.toRadians(40);
        double angle = Math.max(-maxLaunchAngle, Math.min(maxLaunchAngle, angleOffset));
        
        // If almost vertical, add slight tilt for gameplay variety
        if (Math.abs(angle) < Math.toRadians(5)) {
            angle = (Math.random() < 0.5 ? -1 : 1) * Math.toRadians(15);
        }

        this.vx = baseSpeed * Math.sin(angle);
        this.vy = -baseSpeed * Math.cos(angle);
        enforceMinVerticalVelocity();
    }

    /**
     * Updates ball position.
     */
    public void update() {
        x += vx;
        y += vy;
    }

    /**
     * Checks collision with left, right, and top walls.
     * @return true if wall collision occurred.
     */
    public boolean checkWallCollisions() {
        boolean collided = false;

        // Left wall
        if (x <= 0) {
            x = 0;
            vx = Math.abs(vx);
            collided = true;
        }
        // Right wall
        else if (x + diameter >= screenWidth) {
            x = screenWidth - diameter;
            vx = -Math.abs(vx);
            collided = true;
        }

        // Top wall (below HUD area)
        if (y <= topBoundary) {
            y = topBoundary;
            vy = Math.abs(vy);
            collided = true;
        }

        if (collided) {
            enforceMinVerticalVelocity();
        }

        return collided;
    }

    /**
     * Checks if ball has fallen past the bottom of the screen.
     */
    public boolean isBelowBottom() {
        return y > screenHeight;
    }

    /**
     * Handles paddle bounce physics based on impact location across the paddle width.
     * @return true if ball bounced off the paddle.
     */
    public boolean checkPaddleCollision(Paddle paddle) {
        Rectangle paddleBounds = paddle.getBounds();
        Rectangle ballBounds = getBounds();

        if (ballBounds.intersects(paddleBounds) && vy > 0) {
            // Place ball right on top of paddle to prevent sticking
            y = paddle.getY() - diameter;

            // Normalized hit offset: -1.0 (far left) to +1.0 (far right)
            double ballCenter = x + diameter / 2.0;
            double paddleCenter = paddle.getCenterX();
            double halfPaddleW = paddle.getWidth() / 2.0;
            double offset = (ballCenter - paddleCenter) / halfPaddleW;
            offset = Math.max(-0.95, Math.min(0.95, offset));

            // Deflection angle up to 60 degrees
            double maxBounceAngle = Math.toRadians(60);
            double bounceAngle = offset * maxBounceAngle;

            double currentSpeed = Math.max(baseSpeed, Math.hypot(vx, vy));
            vx = currentSpeed * Math.sin(bounceAngle);
            vy = -Math.abs(currentSpeed * Math.cos(bounceAngle));

            enforceMinVerticalVelocity();
            return true;
        }
        return false;
    }

    /**
     * Prevents the ball from getting stuck in an almost purely horizontal trajectory.
     */
    public void enforceMinVerticalVelocity() {
        double minVy = 2.2;
        if (Math.abs(vy) < minVy) {
            vy = (vy < 0 ? -minVy : minVy);
            // Re-normalize speed so ball doesn't accelerate unnaturally
            double currentSpeed = Math.hypot(vx, vy);
            if (currentSpeed > 0) {
                double scale = baseSpeed / currentSpeed;
                vx *= scale;
                vy *= scale;
            }
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public int getDiameter() {
        return diameter;
    }

    public double getCenterX() {
        return x + diameter / 2.0;
    }

    public double getCenterY() {
        return y + diameter / 2.0;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), diameter, diameter);
    }

    /**
     * Draws the spherical white ball with specular 3D shading.
     */
    public void draw(Graphics2D g2d) {
        int bx = (int) Math.round(x);
        int by = (int) Math.round(y);

        Point2D center = new Point2D.Float(bx + 4.0f, by + 4.0f);
        float radius = (float) (diameter / 2.0);
        float[] dist = {0.0f, 0.65f, 1.0f};
        Color[] colors = {
            new Color(255, 255, 255),
            new Color(225, 230, 240),
            new Color(160, 170, 185)
        };

        RadialGradientPaint ballPaint = new RadialGradientPaint(
            center, radius, dist, colors,
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        );

        g2d.setPaint(ballPaint);
        g2d.fillOval(bx, by, diameter, diameter);

        // Subtle dark rim for crisp definition
        g2d.setColor(new Color(60, 65, 75, 120));
        g2d.drawOval(bx, by, diameter - 1, diameter - 1);
    }
}
