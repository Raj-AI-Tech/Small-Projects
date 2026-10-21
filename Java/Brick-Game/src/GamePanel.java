import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Main game panel handling rendering, 60 FPS game loop, physics sub-stepping,
 * collision resolution, level progression, and UI state overlays.
 */
public class GamePanel extends JPanel implements ActionListener {
    public static final int PANEL_WIDTH = 900;
    public static final int PANEL_HEIGHT = 600;
    public static final int HUD_HEIGHT = 45;

    private final StarField starField;
    private final Paddle paddle;
    private final Ball ball;
    private final LevelManager levelManager;
    private final SoundManager soundManager;
    private final InputHandler inputHandler;
    private final Timer gameLoopTimer;

    private List<Brick> bricks = new ArrayList<>();
    private GameState gameState = GameState.START;
    
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int transitionFrames = 0;
    private long pulseTick = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        setBackground(Color.BLACK);

        this.starField = new StarField(PANEL_WIDTH, PANEL_HEIGHT, 180);
        this.paddle = new Paddle(PANEL_WIDTH, PANEL_HEIGHT);
        this.ball = new Ball(PANEL_WIDTH, PANEL_HEIGHT, HUD_HEIGHT);
        this.levelManager = new LevelManager();
        this.soundManager = new SoundManager();
        this.inputHandler = new InputHandler(this);

        addKeyListener(inputHandler.getKeyListener());
        addMouseListener(inputHandler.getMouseListener());
        addMouseMotionListener(inputHandler.getMouseMotionListener());

        // ~60 FPS Swing Timer (16ms per frame)
        this.gameLoopTimer = new Timer(16, this);
        this.gameLoopTimer.start();
    }

    /**
     * Loads the specified level, resets paddle and ball.
     */
    public void startLevel(int targetLevel) {
        this.level = targetLevel;
        this.bricks = levelManager.loadLevel(targetLevel);
        this.paddle.reset();
        this.ball.resetOnPaddle(paddle);
        this.gameState = GameState.BALL_READY;
    }

    /**
     * Resets score, lives, and starts from Level 1.
     */
    public void restartGame() {
        this.score = 0;
        this.lives = 3;
        startLevel(1);
    }

    /**
     * Launches the ball from the paddle.
     */
    public void launchBall() {
        if (gameState == GameState.BALL_READY) {
            double levelSpeed = levelManager.getBallSpeedForLevel(level);
            // Slight angle influence if paddle was moving
            double initialAngle = 0;
            if (inputHandler.isLeftPressed()) {
                initialAngle = -Math.toRadians(25);
            } else if (inputHandler.isRightPressed()) {
                initialAngle = Math.toRadians(25);
            }
            ball.launch(levelSpeed, initialAngle);
            gameState = GameState.PLAYING;
            soundManager.playPaddleHit();
        }
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }

    public void handleSpaceAction() {
        switch (gameState) {
            case START:
                restartGame();
                break;
            case BALL_READY:
                launchBall();
                break;
            case PLAYING:
                togglePause();
                break;
            case PAUSED:
                togglePause();
                break;
            case LEVEL_COMPLETE:
                // Fast-forward transition
                transitionFrames = 0;
                break;
            case GAME_OVER:
                restartGame();
                break;
            case WIN:
                // Enter endless mode
                startLevel(11);
                break;
        }
    }

    public void handleEnterAction() {
        switch (gameState) {
            case START:
                restartGame();
                break;
            case BALL_READY:
                launchBall();
                break;
            case PAUSED:
                togglePause();
                break;
            case GAME_OVER:
                restartGame();
                break;
            case WIN:
                startLevel(11);
                break;
            default:
                break;
        }
    }

    public void handleEscapeAction() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        } else if (gameState == GameState.WIN || gameState == GameState.GAME_OVER) {
            gameState = GameState.START;
        }
    }

    public void handleMouseClick() {
        switch (gameState) {
            case START:
                restartGame();
                break;
            case BALL_READY:
                launchBall();
                break;
            case PAUSED:
                togglePause();
                break;
            case GAME_OVER:
                restartGame();
                break;
            case WIN:
                startLevel(11);
                break;
            default:
                break;
        }
    }

    public void handleMouseMove(int mouseX) {
        if (gameState == GameState.PLAYING || gameState == GameState.BALL_READY) {
            paddle.setPositionClamped(mouseX - paddle.getWidth() / 2.0);
            if (gameState == GameState.BALL_READY) {
                ball.resetOnPaddle(paddle);
            }
        }
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        pulseTick++;
        updateGame();
        repaint();
    }

    /**
     * Main update tick for game logic.
     */
    private void updateGame() {
        starField.update();

        // 1. Paddle movement via keyboard
        if (gameState == GameState.PLAYING || gameState == GameState.BALL_READY) {
            if (inputHandler.isLeftPressed()) {
                paddle.moveLeft();
            }
            if (inputHandler.isRightPressed()) {
                paddle.moveRight();
            }
            if (gameState == GameState.BALL_READY) {
                ball.resetOnPaddle(paddle);
            }
        }

        // 2. Active gameplay physics (2 sub-steps to prevent any tunneling)
        if (gameState == GameState.PLAYING) {
            final int subSteps = 2;
            for (int step = 0; step < subSteps; step++) {
                // Apply half step movement
                ball.setX(ball.getX() + ball.getVx() / subSteps);
                ball.setY(ball.getY() + ball.getVy() / subSteps);

                // Wall collisions
                if (ball.checkWallCollisions()) {
                    soundManager.playWallHit();
                }

                // Paddle collision
                if (ball.checkPaddleCollision(paddle)) {
                    soundManager.playPaddleHit();
                }

                // Brick collisions
                if (checkBrickCollisions()) {
                    // Check if all bricks are cleared
                    if (isLevelCleared()) {
                        gameState = GameState.LEVEL_COMPLETE;
                        transitionFrames = 90; // ~1.5 seconds banner
                        soundManager.playLevelComplete();
                        break;
                    }
                }

                // Ball falls below bottom edge
                if (ball.isBelowBottom()) {
                    lives--;
                    soundManager.playLifeLost();
                    if (lives > 0) {
                        gameState = GameState.BALL_READY;
                        paddle.reset();
                        ball.resetOnPaddle(paddle);
                    } else {
                        gameState = GameState.GAME_OVER;
                        soundManager.playGameOver();
                    }
                    break;
                }
            }
        }

        // 3. Level complete transition countdown
        if (gameState == GameState.LEVEL_COMPLETE) {
            transitionFrames--;
            if (transitionFrames <= 0) {
                if (level == LevelManager.MAX_CAMPAIGN_LEVELS) {
                    gameState = GameState.WIN;
                } else {
                    startLevel(level + 1);
                }
            }
        }
    }

    /**
     * Resolves collision between ball and active bricks.
     * Uses AABB overlap depth to reflect velocity and clamp ball position.
     * @return true if a brick collision occurred.
     */
    private boolean checkBrickCollisions() {
        Brick hitBrick = null;
        double minDistanceSq = Double.MAX_VALUE;

        // Find nearest intersecting brick
        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && ball.getBounds().intersects(brick.getBounds())) {
                double bCenterX = brick.getX() + brick.getWidth() / 2.0;
                double bCenterY = brick.getY() + brick.getHeight() / 2.0;
                double distSq = Math.pow(ball.getCenterX() - bCenterX, 2) + Math.pow(ball.getCenterY() - bCenterY, 2);
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    hitBrick = brick;
                }
            }
        }

        if (hitBrick != null) {
            // Determine penetration depth on all 4 faces
            double ballLeft = ball.getX();
            double ballRight = ball.getX() + ball.getDiameter();
            double ballTop = ball.getY();
            double ballBottom = ball.getY() + ball.getDiameter();

            double brickLeft = hitBrick.getX();
            double brickRight = hitBrick.getX() + hitBrick.getWidth();
            double brickTop = hitBrick.getY();
            double brickBottom = hitBrick.getY() + hitBrick.getHeight();

            double overlapLeft = ballRight - brickLeft;
            double overlapRight = brickRight - ballLeft;
            double overlapTop = ballBottom - brickTop;
            double overlapBottom = brickBottom - ballTop;

            double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (minOverlap == overlapLeft) {
                ball.setVx(-Math.abs(ball.getVx()));
                ball.setX(brickLeft - ball.getDiameter());
            } else if (minOverlap == overlapRight) {
                ball.setVx(Math.abs(ball.getVx()));
                ball.setX(brickRight);
            } else if (minOverlap == overlapTop) {
                ball.setVy(-Math.abs(ball.getVy()));
                ball.setY(brickTop - ball.getDiameter());
            } else {
                ball.setVy(Math.abs(ball.getVy()));
                ball.setY(brickBottom);
            }

            ball.enforceMinVerticalVelocity();

            boolean destroyed = hitBrick.takeHit();
            if (destroyed) {
                score += hitBrick.getPoints();
            } else {
                // Partial damage on reinforced brick
                score += 10;
            }
            soundManager.playBrickHit();
            return true;
        }

        return false;
    }

    private boolean isLevelCleared() {
        for (Brick b : bricks) {
            if (!b.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing for smooth circles and text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Star field background
        starField.draw(g2d);

        // 2. Bricks
        for (Brick brick : bricks) {
            brick.draw(g2d);
        }

        // 3. Paddle & Ball (during playable states)
        if (gameState != GameState.START) {
            paddle.draw(g2d);
            ball.draw(g2d);
        }

        // 4. Top HUD
        drawHUD(g2d);

        // 5. State Overlays
        drawStateOverlays(g2d);
    }

    /**
     * Renders clean arcade HUD:
     * "Lives   X" on top-left, "Score   Y", "Pause?" in top-center, "Level   Z" on top-right.
     */
    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2d.setColor(Color.WHITE);

        // Top-left: Lives & Score
        String livesText = "Lives   " + lives;
        String scoreText = "Score   " + score;
        g2d.drawString(livesText, 30, 30);
        g2d.drawString(scoreText, 170, 30);

        // Top-center: "Pause?" indicator (matching reference 2)
        String pauseText = (gameState == GameState.PAUSED) ? "[ PAUSED ]" : "Pause?";
        FontMetrics fm = g2d.getFontMetrics();
        int pauseX = (PANEL_WIDTH - fm.stringWidth(pauseText)) / 2;
        g2d.drawString(pauseText, pauseX, 30);

        // Top-right: "Level   X"
        String levelText = "Level   " + level;
        int levelX = PANEL_WIDTH - fm.stringWidth(levelText) - 35;
        g2d.drawString(levelText, levelX, 30);

        // Subtle divider separating HUD from gameplay area
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.drawLine(20, HUD_HEIGHT, PANEL_WIDTH - 20, HUD_HEIGHT);

        // Unobtrusive mute status in lower right corner
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2d.setColor(new Color(255, 255, 255, 90));
        String soundInfo = soundManager.isMuted() ? "[M] Audio: Muted" : "[M] Audio: ON";
        g2d.drawString(soundInfo, PANEL_WIDTH - 110, PANEL_HEIGHT - 12);
    }

    /**
     * Renders overlays for Start, Ready, Paused, Level Complete, Game Over, and Win states.
     */
    private void drawStateOverlays(Graphics2D g2d) {
        FontMetrics fm;

        switch (gameState) {
            case START:
                // Dark translucent backdrop
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

                // Title
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 46));
                g2d.setColor(new Color(255, 215, 0));
                String title = "BRICK BREAKER";
                fm = g2d.getFontMetrics();
                g2d.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 210);

                // Subtitle
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
                g2d.setColor(new Color(180, 220, 255));
                String subtitle = "CLASSIC ARCADE EDITION";
                fm = g2d.getFontMetrics();
                g2d.drawString(subtitle, (PANEL_WIDTH - fm.stringWidth(subtitle)) / 2, 245);

                // Blinking prompt
                float alpha = (float) (0.5 + 0.5 * Math.sin(pulseTick * 0.1));
                g2d.setColor(new Color(1.0f, 1.0f, 1.0f, alpha));
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                String startPrompt = "Press ENTER to Start";
                fm = g2d.getFontMetrics();
                g2d.drawString(startPrompt, (PANEL_WIDTH - fm.stringWidth(startPrompt)) / 2, 330);

                // Controls description
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                g2d.setColor(new Color(210, 215, 230));
                String[] controls = {
                    "Controls:",
                    "← / → or A / D : Move Paddle",
                    "SPACE / ENTER : Launch Ball",
                    "P : Pause / Resume",
                    "M : Toggle Sound",
                    "(Mouse tracking also supported)"
                };
                int startY = 380;
                for (String line : controls) {
                    fm = g2d.getFontMetrics();
                    g2d.drawString(line, (PANEL_WIDTH - fm.stringWidth(line)) / 2, startY);
                    startY += 24;
                }
                break;

            case BALL_READY:
                // Subtle prompt matching reference image 3: "(click/touch to start)"
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                float readyAlpha = (float) (0.6 + 0.4 * Math.sin(pulseTick * 0.12));
                g2d.setColor(new Color(1.0f, 1.0f, 1.0f, readyAlpha));
                String launchPrompt = "(Press SPACE, ENTER or Click to Launch)";
                fm = g2d.getFontMetrics();
                g2d.drawString(launchPrompt, (PANEL_WIDTH - fm.stringWidth(launchPrompt)) / 2, (int) paddle.getY() + 45);
                break;

            case PAUSED:
                g2d.setColor(new Color(0, 0, 0, 160));
                g2d.fillRect(0, HUD_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT - HUD_HEIGHT);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
                g2d.setColor(Color.WHITE);
                String pausedTitle = "PAUSED";
                fm = g2d.getFontMetrics();
                g2d.drawString(pausedTitle, (PANEL_WIDTH - fm.stringWidth(pausedTitle)) / 2, 300);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
                g2d.setColor(new Color(200, 215, 230));
                String resumePrompt = "Press P or SPACE to Resume";
                fm = g2d.getFontMetrics();
                g2d.drawString(resumePrompt, (PANEL_WIDTH - fm.stringWidth(resumePrompt)) / 2, 340);
                break;

            case LEVEL_COMPLETE:
                g2d.setColor(new Color(0, 0, 0, 140));
                g2d.fillRect(0, HUD_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT - HUD_HEIGHT);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
                g2d.setColor(new Color(100, 255, 120));
                String winLevel = "LEVEL " + level + " COMPLETE!";
                fm = g2d.getFontMetrics();
                g2d.drawString(winLevel, (PANEL_WIDTH - fm.stringWidth(winLevel)) / 2, 290);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
                g2d.setColor(Color.WHITE);
                String nextPrompt = "Loading Level " + (level + 1) + "...";
                fm = g2d.getFontMetrics();
                g2d.drawString(nextPrompt, (PANEL_WIDTH - fm.stringWidth(nextPrompt)) / 2, 335);
                break;

            case GAME_OVER:
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, HUD_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT - HUD_HEIGHT);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 46));
                g2d.setColor(new Color(255, 60, 60));
                String gameOverText = "GAME OVER";
                fm = g2d.getFontMetrics();
                g2d.drawString(gameOverText, (PANEL_WIDTH - fm.stringWidth(gameOverText)) / 2, 260);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                g2d.setColor(Color.WHITE);
                String finalScoreText = "Final Score: " + score;
                fm = g2d.getFontMetrics();
                g2d.drawString(finalScoreText, (PANEL_WIDTH - fm.stringWidth(finalScoreText)) / 2, 310);

                String levelReached = "Reached Level: " + level;
                fm = g2d.getFontMetrics();
                g2d.drawString(levelReached, (PANEL_WIDTH - fm.stringWidth(levelReached)) / 2, 345);

                float restartAlpha = (float) (0.5 + 0.5 * Math.sin(pulseTick * 0.12));
                g2d.setColor(new Color(1.0f, 1.0f, 1.0f, restartAlpha));
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                String restartPrompt = "Press ENTER to Restart";
                fm = g2d.getFontMetrics();
                g2d.drawString(restartPrompt, (PANEL_WIDTH - fm.stringWidth(restartPrompt)) / 2, 410);
                break;

            case WIN:
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, HUD_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT - HUD_HEIGHT);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 46));
                g2d.setColor(new Color(255, 215, 0));
                String winText = "YOU WIN!";
                fm = g2d.getFontMetrics();
                g2d.drawString(winText, (PANEL_WIDTH - fm.stringWidth(winText)) / 2, 240);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                g2d.setColor(new Color(140, 230, 255));
                String allCleared = "You completed all 10 arcade levels!";
                fm = g2d.getFontMetrics();
                g2d.drawString(allCleared, (PANEL_WIDTH - fm.stringWidth(allCleared)) / 2, 290);

                g2d.setColor(Color.WHITE);
                String winScore = "Final Score: " + score;
                fm = g2d.getFontMetrics();
                g2d.drawString(winScore, (PANEL_WIDTH - fm.stringWidth(winScore)) / 2, 330);

                float winAlpha = (float) (0.5 + 0.5 * Math.sin(pulseTick * 0.12));
                g2d.setColor(new Color(1.0f, 1.0f, 1.0f, winAlpha));
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                String endlessPrompt = "Press ENTER to Continue to Endless Mode";
                fm = g2d.getFontMetrics();
                g2d.drawString(endlessPrompt, (PANEL_WIDTH - fm.stringWidth(endlessPrompt)) / 2, 400);

                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                g2d.setColor(new Color(200, 200, 200));
                String restartOpt = "or press ESC to restart campaign from Level 1";
                fm = g2d.getFontMetrics();
                g2d.drawString(restartOpt, (PANEL_WIDTH - fm.stringWidth(restartOpt)) / 2, 435);
                break;
        }
    }
}
