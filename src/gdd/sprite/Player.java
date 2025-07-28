package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 270;
    private static final int START_Y = 540;
    private int width;

    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private Image[] idleFrames;
    private Image leftImage;
    private Image rightImage;

    private int idleFrameIndex = 0;
    private boolean idleForward = true;
    private int idleFrameDelay = 5; // frames to wait before switching
    private int idleFrameCounter = 0;

    private int health = 10;
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_FRAMES = 15; // 0.25s at 60fps

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        // Load idle animation frames
        idleFrames = new Image[4];
        for (int i = 0; i < 4; i++) {
            idleFrames[i] = new ImageIcon("src/images/player_idle_" + i + ".png")
                    .getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
        }

        // Load directional images
        leftImage = new ImageIcon("src/images/player_left.png")
                .getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
        rightImage = new ImageIcon("src/images/player_right.png")
                .getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);

        setImage(idleFrames[0]);
        setX(START_X);
        setY(START_Y);
    }

    public void act() {
        x += dx;

        if (x <= 2)
            x = 2;
        if (x >= BOARD_WIDTH - 52)  // Using actual player sprite width (52)
            x = BOARD_WIDTH - 52;

        animate();
    }

    private void animate() {
        if (dx < 0) {
            setImage(leftImage);
        } else if (dx > 0) {
            setImage(rightImage);
        } else {
            // Ping-pong idle animation
            idleFrameCounter++;
            if (idleFrameCounter >= idleFrameDelay) {
                idleFrameCounter = 0;

                setImage(idleFrames[idleFrameIndex]);

                if (idleForward) {
                    idleFrameIndex++;
                    if (idleFrameIndex >= idleFrames.length - 1) {
                        idleForward = false;
                    }
                } else {
                    idleFrameIndex--;
                    if (idleFrameIndex <= 0) {
                        idleForward = true;
                    }
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT)
            leftPressed = true;
        if (key == KeyEvent.VK_RIGHT)
            rightPressed = true;

        resolveMovement();
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT)
            leftPressed = false;
        if (key == KeyEvent.VK_RIGHT)
            rightPressed = false;

        resolveMovement();
    }

    private void resolveMovement() {
        if (leftPressed && rightPressed) {
            dx = 0;
        } else if (leftPressed) {
            dx = -4;
        } else if (rightPressed) {
            dx = 4;
        } else {
            dx = 0;
        }
    }

    public void updateSpeed(boolean boosted) {
        if (dx > 0) {
            dx = boosted ? 10 : 4;
        } else if (dx < 0) {
            dx = boosted ? -10 : -4;
        }
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void takeHit() {
        if (health > 0) health--;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean canShoot() {
        return shootCooldown == 0;
    }

    public void resetShootCooldown() {
        shootCooldown = SHOOT_COOLDOWN_FRAMES;
    }

    public void updateShootCooldown() {
        if (shootCooldown > 0) shootCooldown--;
    }
}
