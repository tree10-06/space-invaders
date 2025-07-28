package gdd;

import static gdd.Global.*;
import javax.swing.JFrame;
import gdd.scene.TitleScene;
import gdd.scene.Scene1;
import gdd.scene.Scene2;

public class Game extends JFrame {

    public Game() {
        initUI();
    }

    private void initUI() {
        setTitle("Space Invaders");
        setSize(BOARD_WIDTH, BOARD_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Start with the title screen
        setTitleScene();
    }

    public void setTitleScene() {
        getContentPane().removeAll();
        getContentPane().add(new TitleScene(this));
        revalidate();
        repaint();
    }

    public void setScene1() {
        getContentPane().removeAll();
        getContentPane().add(new Scene1(this));
        revalidate();
        repaint();
    }

    public void setScene2() {
        getContentPane().removeAll();
        Scene2 scene2 = new Scene2(this);
        getContentPane().add(scene2);
        revalidate();
        repaint();
        scene2.requestFocusInWindow(); // ✅ Ensure keyboard input works
    }

}
