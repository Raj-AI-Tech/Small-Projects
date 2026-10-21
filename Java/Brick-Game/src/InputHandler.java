import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Handles keyboard and mouse inputs for smooth paddle movement and gameplay control.
 */
public class InputHandler {
    private final GamePanel gamePanel;
    private boolean leftPressed;
    private boolean rightPressed;

    public InputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public void resetKeys() {
        leftPressed = false;
        rightPressed = false;
    }

    public KeyAdapter getKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        leftPressed = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        rightPressed = true;
                        break;
                    case KeyEvent.VK_P:
                        gamePanel.togglePause();
                        break;
                    case KeyEvent.VK_SPACE:
                        gamePanel.handleSpaceAction();
                        break;
                    case KeyEvent.VK_ENTER:
                        gamePanel.handleEnterAction();
                        break;
                    case KeyEvent.VK_M:
                        gamePanel.getSoundManager().toggleMute();
                        gamePanel.repaint();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        gamePanel.handleEscapeAction();
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        leftPressed = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        rightPressed = false;
                        break;
                }
            }
        };
    }

    public MouseAdapter getMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gamePanel.handleMouseClick();
            }
        };
    }

    public MouseMotionAdapter getMouseMotionListener() {
        return new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                gamePanel.handleMouseMove(e.getX());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                gamePanel.handleMouseMove(e.getX());
            }
        };
    }
}
