package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class BossBullet extends Sprite {
    private double angle;
    private double speed;
    private double posX, posY;

    public BossBullet(int x, int y, double angle, double speed) {
        this.angle = angle;
        this.speed = speed;
        this.posX = x;
        this.posY = y;
        setX(x);
        setY(y);
        setImage(createBulletImage());
    }

    private Image createBulletImage() {
        int size = 12;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.RED);
        g.fillOval(0, 0, size, size);
        g.dispose();
        return img;
    }

    public void act() {
        posX += Math.cos(angle) * speed;
        posY += Math.sin(angle) * speed;
        setX((int) posX);
        setY((int) posY);
    }
} 