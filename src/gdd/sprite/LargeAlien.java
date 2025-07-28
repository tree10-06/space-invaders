package gdd.sprite;

import javax.swing.ImageIcon;
import java.awt.Image;
import static gdd.Global.*;
import java.util.ArrayList;
import java.util.List;

public class LargeAlien extends Enemy {
    private int dx = 1;
    private Image[] idleFrames;
    private int health = 20;
    private int fireCooldown = 0;
    private final int FIRE_INTERVAL = 60;
    private int frameIndex = 0;
    private boolean forward = true;
    private int animationCounter = 0;
    private final int ANIMATION_DELAY = 10;

    private boolean isStage2 = false;
    private long stage2StartTime = -1;
    private long createdAt;
    private int teleportCooldown = 0;
    private final int TELEPORT_INTERVAL = 180; // every 3 seconds

    public LargeAlien(int x, int y) {
        super(x, y);
        loadIdleFrames();
        createdAt = System.currentTimeMillis();
    }

    private void loadIdleFrames() {
        idleFrames = new Image[6];
        for (int i = 0; i < 6; i++) {
            var ii = new ImageIcon("src/images/boss_idle_" + i + ".png");
            idleFrames[i] = ii.getImage().getScaledInstance(102, 102, Image.SCALE_SMOOTH);
        }
        setImage(idleFrames[0]);
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health <= 0) {
            setDying(true);
            setVisible(false);
        }
    }

    public List<BossBullet> tryFire() {
        List<BossBullet> bullets = new ArrayList<>();
        if (fireCooldown > 0) {
            fireCooldown--;
            return bullets;
        }

        fireCooldown = isStage2 ? 45 : FIRE_INTERVAL;

        int centerX = x + 52;
        int centerY = y + 52;

        int numBullets = isStage2 ? 24 : 12;
        for (int i = 0; i < numBullets; i++) {
            double angle = 2 * Math.PI * i / numBullets;
            bullets.add(new BossBullet(centerX, centerY, angle, 6));
        }
        return bullets;
    }

    @Override
    public void act() {
        long elapsed = System.currentTimeMillis() - createdAt;
        if (!isStage2 && elapsed >= 5 * 60 * 1000) { // 5 minutes
            isStage2 = true;
            stage2StartTime = System.currentTimeMillis();
            System.out.println("Stage 2 activated!");
        }

        if (!isStage2) {
            x += dx;
            y += 1;
        } else {
            teleportCooldown--;
            if (teleportCooldown <= 0) {
                teleport();
                teleportCooldown = TELEPORT_INTERVAL;
            }
            // Do NOT increment y in stage 2
        }

        if (x <= BORDER_LEFT) {
            x = BORDER_LEFT;
            dx = 2;
        } else if (x >= BOARD_WIDTH - 104 - BORDER_RIGHT) {
            x = BOARD_WIDTH - 104 - BORDER_RIGHT;
            dx = -2;
        }

        if (y > GROUND - 104) {
            y = GROUND - 104;
        }

        animate();
    }

    private void teleport() {
        x = BORDER_LEFT + (int)(Math.random() * (BOARD_WIDTH - 104 - BORDER_RIGHT - BORDER_LEFT));
        y = 50 + (int)(Math.random() * 200);
    }

    private void animate() {
        animationCounter++;
        if (animationCounter >= ANIMATION_DELAY) {
            animationCounter = 0;
            if (forward) {
                frameIndex++;
                if (frameIndex == idleFrames.length - 1)
                    forward = false;
            } else {
                frameIndex--;
                if (frameIndex == 0)
                    forward = true;
            }
            setImage(idleFrames[frameIndex]);
        }
    }
}
