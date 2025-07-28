package gdd.sprite;

import java.awt.Image;

import java.awt.Rectangle;

public class Sprite {

    private boolean visible;
    private Image image;
    private boolean dying;
    private int visibleFrames = 10;

    int x;
    int y;
    public int dx;

    public Sprite() {
        visible = true;
    }

    public void die() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void visibleCountDown() {
        if (visibleFrames > 0) {
            visibleFrames--;
        } else {
            visible = false;
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public boolean isDying() {
        return this.dying;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, getImageWidth(), getImageHeight());
    }

    public int getImageWidth() {
        return image != null ? image.getWidth(null) : 0;
    }

    public int getImageHeight() {
        return image != null ? image.getHeight(null) : 0;
    }

}
