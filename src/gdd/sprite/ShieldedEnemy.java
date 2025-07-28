package gdd.sprite;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import static gdd.Global.*;

public class ShieldedEnemy extends Enemy {
    private int health;
    private Image[] shieldFrames;
    private int fireCooldown = 0;
    private static final int FIRE_INTERVAL = 90; // frames
    private int dx = 1;

    public ShieldedEnemy(int x, int y) {
        super(x, y);
        this.health = 3;
        loadShieldFrames();
        setImage(shieldFrames[0]);
    }

    private void loadShieldFrames() {
        shieldFrames = new Image[3];
        shieldFrames[0] = new ImageIcon("src/images/shielded_enemy.png").getImage();
        shieldFrames[1] = new ImageIcon("src/images/shielded_enemy_damaged1.png").getImage();
        shieldFrames[2] = new ImageIcon("src/images/shielded_enemy_damaged2.png").getImage();
    }

    public void hit() {
        health--;
        if (health > 0) {
            setImage(shieldFrames[3 - health]);
        } else {
            setVisible(false);
        }
    }

    public int getHealth() {
        return health;
    }

    // Call this each frame; returns a list of Bombs to add to the game
    public List<Bomb> tryFire() {
        List<Bomb> bombs = new ArrayList<>();
        if (fireCooldown > 0) {
            fireCooldown--;
            return bombs;
        }
        fireCooldown = FIRE_INTERVAL;
        bombs.add(new Bomb(getX() + 10, getY() + 20)); // Adjust offset as needed
        return bombs;
    }

    @Override
    public void act() {
        // Move like Alien1
        x += dx;
        y += 1;

        if (x <= BORDER_LEFT) {
            x = BORDER_LEFT;
            dx = 2;
        } else if (x >= BOARD_WIDTH - ALIEN_WIDTH - BORDER_RIGHT) {
            x = BOARD_WIDTH - ALIEN_WIDTH - BORDER_RIGHT;
            dx = -2;
        }

        if (y > GROUND - ALIEN_HEIGHT) {
            y = GROUND - ALIEN_HEIGHT;
        }
    }
} 