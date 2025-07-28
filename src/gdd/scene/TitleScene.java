package gdd.scene;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import gdd.AudioPlayer;

public class TitleScene extends JPanel {

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private Timer timer;
    private Image image;
    private JFrame parentFrame;
    private int frame = 0;
    private AudioPlayer audioPlayer;

    public TitleScene(JFrame frame) {
        this.parentFrame = frame;
        initBoard();

    }

    private void initBoard() {

        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.black);

        timer = new Timer(DELAY, new GameCycle());
        timer.start();

        gameInit();
    }

    public void stop() {
        timer.stop();
        try {
            if (audioPlayer != null)
                audioPlayer.stop();
        } catch (Exception e) {
            System.err.println("Error stopping audio: " + e.getMessage());
        }
    }

    private void gameInit() {
        // Load graphics
        ImageIcon ii = new ImageIcon("src/images/title.png");
        image = ii.getImage();

        try {
            audioPlayer = new AudioPlayer("src\\audio\\title.wav");
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading audio: " + e.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (image != null) {
            g.drawImage(image, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        }
        g.drawImage(image, 0, -40, BOARD_WIDTH, BOARD_HEIGHT, null);

        if (frame % 60 < 30) {
            g.setColor(Color.white);
            g.setFont(getFont().deriveFont(Font.BOLD, 30));
            String text = "Press SPACE to Start";
            int stringWidth = g.getFontMetrics().stringWidth(text);
            int x = (d.width - stringWidth) / 2;
            g.drawString(text, x, 650);
        }
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Helvetica", Font.PLAIN, 14));

        String[] credits = {
                "Project by:",
                "Mengtry Heang",
                "Humam Khurram",
                "Puran Paodensakul"
        };

        int y = 600; // Vertical position
        for (String name : credits) {
            int textWidth = g.getFontMetrics().stringWidth(name);
            int x = (d.width - textWidth) / 2 - 250; // Shift 100px to the left
            g.drawString(name, x, y);
            y += 20;
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void update() {
        // future updates
    }

    private void doGameCycle() {
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                System.out.println("Detected Spacebar");

                stop();

                parentFrame.getContentPane().removeAll();
                Scene1 scene = new Scene1(parentFrame);
                parentFrame.getContentPane().add(scene);
                parentFrame.revalidate();
                parentFrame.repaint();
                scene.requestFocusInWindow();

            }
        }

    }
}
