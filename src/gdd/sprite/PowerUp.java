// gdd.sprite.PowerUp.java
package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Image;

public class PowerUp extends Sprite {

    public static final String TYPE_SPEED = "speed";
    public static final String TYPE_MULTI = "multi";
    public static final String TYPE_BOMB = "bomb";

    private String type;

    public PowerUp(String type, int x, int y) {
        this.type = type;
        setX(x);
        setY(y);

        String imgPath = switch (type) {
            case TYPE_SPEED -> "src/images/speedup.png";
            case TYPE_MULTI -> "src/images/multishot.png";
            case TYPE_BOMB -> "src/images/yellow-bomb.png";
            default -> "src/images/power_generic.png";
        };

        ImageIcon ii = new ImageIcon(imgPath);
        Image img = ii.getImage().getScaledInstance(
                ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                Image.SCALE_SMOOTH);
        setImage(img);
    }

    public String getType() {
        return type;
    }

    public void act() {
        y += 2;
    }
}
