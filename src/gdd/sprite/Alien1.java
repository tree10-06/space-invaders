package gdd.sprite;

import javax.swing.ImageIcon;
import java.awt.Image;
import static gdd.Global.*;

public class Alien1 extends Enemy {

    private int dx = 1;

    private Image[] idleFrames;
    private int frameIndex = 0;
    private boolean forward = true;
    private int animationCounter = 0;
    private final int ANIMATION_DELAY = 10; // Adjust speed of animation

    public Alien1(int x, int y) {
        super(x, y);
        loadIdleFrames();
        setImage(idleFrames[0]); // Start with first frame
    }

    private void loadIdleFrames() {
        idleFrames = new Image[3];
        for (int i = 0; i < 3; i++) {
            var ii = new ImageIcon("src/images/alien1_idle_" + i + ".png");
            idleFrames[i] = ii.getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
        }
        setImage(idleFrames[0]);

    }

    @Override
    public void act() {
        // Move
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

        // Animate
        animate();
    }

    private void animate() {
        animationCounter++;
        if (animationCounter >= ANIMATION_DELAY) {
            animationCounter = 0;

            // Ping-pong frame direction
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
