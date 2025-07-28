// gdd.sprite.Bomb.java
package gdd.sprite;

import javax.swing.ImageIcon;
import static gdd.Global.*;
import java.awt.Image;

public class Bomb extends Sprite {
    private boolean destroyed;

    public Bomb(int x, int y) {
        setDestroyed(false);
        this.x = x;
        this.y = y;
        var bombImg = "src/images/bomb.png";
        var ii = new ImageIcon(bombImg);
        setImage(ii.getImage().getScaledInstance(4, 10, Image.SCALE_SMOOTH));
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void update() {
        y += 1;
        if (y >= GROUND - BOMB_HEIGHT) {
            setDestroyed(true);
        }
    }
}
