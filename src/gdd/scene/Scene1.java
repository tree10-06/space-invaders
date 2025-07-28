package gdd.scene;

import static gdd.Global.*;
import gdd.AudioPlayer;
import gdd.Game;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.PowerUp;
import gdd.sprite.Shot;
import gdd.sprite.Alien1;
import gdd.sprite.Bomb;
import gdd.sprite.LargeAlien;
import gdd.sprite.BossBullet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private Player player;
    private int direction = -1;
    private int deaths = 0;
    private int score = 0;
    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;
    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;
    private boolean inGame = true;
    private String message = "Game Over";
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
    private Timer timer;
    private AudioPlayer audioPlayer;
    private HashMap<Integer, List<SpawnDetails>> spawnMap = new HashMap<>();
    private int frame = 0;
    private final int[][] MAP;
    private List<PowerUp> powerUps = new ArrayList<>();
    private boolean multiShotEnabled = false;
    private int multiShotFrames = 0;
    public int speedFrames = 0;
    private List<Bomb> bombs = new ArrayList<>();
    private LargeAlien boss = null;
    private boolean bossSpawned = false;
    private List<BossBullet> bossBullets = new ArrayList<>();
    private int lastEnemyWaveFrame = 0;
    private int elapsedFrames = 0; // Add this field to track time
    private boolean switchedToScene2 = false; // Prevent multiple switches
    private javax.swing.JFrame parentFrame; // To allow scene switching

    public Scene1(javax.swing.JFrame parentFrame) {
        this.parentFrame = parentFrame;
        MAP = loadMap("src/map/map_expanded.csv");
        loadSpawnMap("src/map/spawn_new.csv");
        initBoard();
        lastEnemyWaveFrame = getLastEnemyWaveFrame();
    }

    // Keep the old constructor for compatibility
    public Scene1() {
        this(null);
    }

    private int getLastEnemyWaveFrame() {
        int maxFrame = 0;
        for (var entry : spawnMap.entrySet()) {
            int frameNum = entry.getKey();
            for (SpawnDetails sd : entry.getValue()) {
                if (!sd.type.trim().equalsIgnoreCase("largealien")) {
                    maxFrame = Math.max(maxFrame, frameNum);
                }
            }
        }
        return maxFrame;
    }

    private void loadSpawnMap(String csvFile) {
        spawnMap.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("frame")) {
                    continue; // skip header or blank lines
                }
                String[] tokens = line.split(",");
                int key = Integer.parseInt(tokens[0].trim());
                String type = tokens[1].trim().toLowerCase();
                int x = Integer.parseInt(tokens[2].trim());
                int yOff = Integer.parseInt(tokens[3].trim());
                spawnMap.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new SpawnDetails(type, x, yOff));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);
        timer = new Timer(DELAY, new GameCycle());
        timer.start();
        gameInit();
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        player = new Player();
        score = 0;
        deaths = 0;
        try {
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading audio: " + e.getMessage());
        }
    }

    /**
     * Reads the CSV, pads rows to full width, and returns a 2D int array
     */
    private int[][] loadMap(String csvFile) {
        List<int[]> rows = new ArrayList<>();
        int targetCols = BOARD_WIDTH / BLOCKWIDTH;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                int[] row = Arrays.stream(line.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                // pad with zeros if row is shorter than needed
                if (row.length < targetCols) {
                    row = Arrays.copyOf(row, targetCols);
                }
                rows.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows.toArray(new int[rows.size()][]);
    }

    private void drawMap(Graphics g) {
        int scrollOffset = frame % BLOCKHEIGHT;
        int baseRow = frame / BLOCKHEIGHT;
        int rowsNeeded = (BOARD_HEIGHT / BLOCKHEIGHT) + 5;

        for (int screenRow = 0; screenRow < rowsNeeded; screenRow++) {
            int mapRow = (baseRow + screenRow) % MAP.length;
            int y = BOARD_HEIGHT - ((screenRow * BLOCKHEIGHT) - scrollOffset);
            if (y > BOARD_HEIGHT || y < -BLOCKHEIGHT)
                continue;
            for (int col = 0; col < MAP[mapRow].length; col++) {
                if (MAP[mapRow][col] == 1) {
                    int x = col * BLOCKWIDTH;
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }
    }

    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);
        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }

    private void drawAliens(Graphics g) {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
            }
            if (enemy.isDying()) {
                enemy.die();
            }
        }
    }

    private void drawPlayer(Graphics g) {
        if (player.isVisible()) {
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }
        if (player.isDying()) {
            player.die();
            inGame = false;
        }
    }

    private void drawShot(Graphics g) {
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawBombing(Graphics g) {
        for (Bomb b : bombs) {
            if (b.isVisible()) {
                g.drawImage(b.getImage(), b.getX(), b.getY(), this);
            }
        }
    }

    private void drawExplosions(Graphics g) {
        List<Explosion> toRemove = new ArrayList<>();
        for (Explosion explosion : explosions) {
            if (explosion.isVisible()) {
                g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
                explosion.visibleCountDown();
                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }
        explosions.removeAll(toRemove);
    }

    private void drawPowerUps(Graphics g) {
        for (PowerUp p : powerUps) {
            if (p.isVisible()) {
                g.drawImage(p.getImage(), p.getX(), p.getY(), this);
            }
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
        g.setColor(Color.white);
        g.drawString("Space Invaders", 10, 10);
        g.setColor(Color.green);
        g.setColor(Color.white);
        g.setFont(new Font("Helvetica", Font.PLAIN, 18));
        g.drawString("Score: " + score, 10, 50);
        g.drawString("Time: " + (elapsedFrames / 60) + "s", 200, 50); // Show timer in seconds
        int statusX = 350;
        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        if (multiShotEnabled) {
            g.setColor(Color.cyan);
            g.drawString("MULTI SHOT", statusX, 50);
            statusX += 100;
        }
        if (speedFrames > 0) {
            g.setColor(Color.orange);
            g.drawString("SPEED BOOST", statusX, 50);
        }
        if (boss != null && boss.isVisible()) {
            g.setColor(Color.RED);
            g.fillRect(200, 20, boss.getHealth() * 10, 20); // Health bar
            g.setColor(Color.WHITE);
            g.drawRect(200, 20, 200, 20);
            g.drawString("BOSS HEALTH", 200, 15);
        }
        if (inGame) {
            drawMap(g);
            drawExplosions(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g);
            drawPowerUps(g);
            // Draw boss bullets
            for (BossBullet bullet : bossBullets) {
                g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), this);
            }
        } else {
            if (timer.isRunning())
                timer.stop();
            gameOver(g);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        Font small = new Font("Helvetica", Font.BOLD, 14);
        var fm = this.getFontMetrics(small);
        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fm.stringWidth(message)) / 2,
                BOARD_WIDTH / 2);
    }

    private void update() {
        List<SpawnDetails> spawns = spawnMap.get(frame);
        if (spawns != null && !bossSpawned) {
            for (SpawnDetails sd : spawns) {
                String type = sd.type.trim().toLowerCase();
                switch (type) {
                    case "alien1" -> enemies.add(new Alien1(sd.x, sd.y));
                    case "largealien" -> {
                    } // Boss will be spawned after all enemies are dead
                    case "speed" -> powerUps.add(new PowerUp(PowerUp.TYPE_SPEED, sd.x, sd.y));
                    case "multi" -> powerUps.add(new PowerUp(PowerUp.TYPE_MULTI, sd.x, sd.y));
                    default -> System.out.println("Unknown spawn type: " + type); // Debug
                }
            }
        }

        // Only spawn boss after all regular enemies are dead AND all waves have spawned
        if (!bossSpawned && frame > lastEnemyWaveFrame
                && enemies.stream().noneMatch(e -> !(e instanceof LargeAlien) && e.isVisible())) {
            // Find boss spawn details from spawnMap
            for (List<SpawnDetails> sds : spawnMap.values()) {
                for (SpawnDetails sd : sds) {
                    if (sd.type.trim().equalsIgnoreCase("largealien")) {
                        boss = new LargeAlien(sd.x, sd.y);
                        enemies.add(boss);
                        bossSpawned = true;
                        break;
                    }
                }
                if (bossSpawned)
                    break;
            }
        }

        // The win condition is now only when the boss is defeated (handled in the boss
        // hit logic)

        player.updateSpeed(speedFrames > 0);
        player.act();
        player.updateShootCooldown();

        if (speedFrames > 0)
            speedFrames--;

        if (multiShotEnabled && --multiShotFrames <= 0) {
            multiShotEnabled = false;
        }

        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp p : powerUps) {
            p.act();
            if (player.getBounds().intersects(p.getBounds())) {
                switch (p.getType()) {
                    case PowerUp.TYPE_MULTI -> {
                        multiShotEnabled = true;
                        multiShotFrames = 600; // ~10 seconds
                    }
                    case PowerUp.TYPE_SPEED -> speedFrames = 600;
                }
                toRemove.add(p);
            }
        }
        powerUps.removeAll(toRemove);

        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                int shotX = shot.getX(), shotY = shot.getY();
                for (Enemy enemy : enemies) {
                    int enemyW = (enemy instanceof LargeAlien) ? enemy.getImageWidth() : ALIEN_WIDTH;
                    int enemyH = (enemy instanceof LargeAlien) ? enemy.getImageHeight() : ALIEN_HEIGHT;
                    if (enemy.isVisible() &&
                            shotX >= enemy.getX() && shotX <= enemy.getX() + enemyW &&
                            shotY >= enemy.getY() && shotY <= enemy.getY() + enemyH) {
                        if (enemy instanceof LargeAlien largeAlien) {
                            largeAlien.takeDamage(1);
                            if (!largeAlien.isVisible()) {
                                explosions.add(new Explosion(largeAlien.getX(), largeAlien.getY()));
                            }
                        } else {
                            enemy.setImage(new ImageIcon(IMG_EXPLOSION).getImage());
                            enemy.setDying(true);
                            explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                            deaths++;
                            score += 10;
                        }
                        shot.die();
                        shotsToRemove.add(shot);
                        try {
                            new AudioPlayer("src/audio/explosion.wav", true).play();
                        } catch (Exception ex) {
                            System.err.println("Error playing explosion sound: " + ex.getMessage());
                        }
                    }
                }
                if (shot.getY() - 20 < 0) {
                    shot.die();
                    shotsToRemove.add(shot);
                } else {
                    shot.setY(shot.getY() - 20);
                }
            }
        }
        shots.removeAll(shotsToRemove);

        for (Enemy enemy : enemies) {
            int x = enemy.getX();
            if (x >= BOARD_WIDTH - BORDER_RIGHT && direction != -1) {
                direction = -1;
                enemies.forEach(e -> e.setY(e.getY() + GO_DOWN));
            }
            if (x <= BORDER_LEFT && direction != 1) {
                direction = 1;
                enemies.forEach(e -> e.setY(e.getY() + GO_DOWN));
            }
        }

        enemies.stream().filter(Enemy::isVisible).forEach(enemy -> {
            if (enemy.getY() > GROUND - ALIEN_HEIGHT) {
                inGame = false;
                message = "Invasion!";
                if (audioPlayer != null)
                    try {
                        audioPlayer.stop();
                    } catch (Exception ignored) {
                    }
            }
            enemy.act();
        });

        // Randomly drop bombs from visible enemies
        for (Enemy enemy : enemies) {
            if (randomizer.nextInt(240) == CHANCE && enemy.isVisible()) {
                bombs.add(new Bomb(enemy.getX(), enemy.getY()));
            }
        }

        // Update all bombs
        List<Bomb> bombsToRemove = new ArrayList<>();
        for (Bomb bomb : bombs) {
            bomb.setY(bomb.getY() + 6);

            // Check collision with player
            if (player.isVisible() &&
                    bomb.getX() >= player.getX() && bomb.getX() <= player.getX() + PLAYER_WIDTH &&
                    bomb.getY() >= player.getY() && bomb.getY() <= player.getY() + PLAYER_HEIGHT) {
                player.setImage(new ImageIcon(IMG_EXPLOSION).getImage());
                player.setDying(true);
                bombsToRemove.add(bomb);
            }

            // Remove bomb if it hits the ground
            if (bomb.getY() >= GROUND - BOMB_HEIGHT) {
                bombsToRemove.add(bomb);
            }
        }

        // Clean up
        bombs.removeAll(bombsToRemove);

        // Boss bullet logic
        if (boss != null && boss.isVisible()) {
            bossBullets.addAll(boss.tryFire());
        }
        List<BossBullet> bossBulletsToRemove = new ArrayList<>();
        for (BossBullet bullet : bossBullets) {
            bullet.act();
            // Remove if off screen
            if (bullet.getY() > BOARD_HEIGHT || bullet.getY() < 0 || bullet.getX() < 0 || bullet.getX() > BOARD_WIDTH) {
                bossBulletsToRemove.add(bullet);
            } else if (player.isVisible() && bullet.getBounds().intersects(player.getBounds())) {
                player.setImage(new ImageIcon(IMG_EXPLOSION).getImage());
                player.setDying(true);
                bossBulletsToRemove.add(bullet);
            }
        }
        bossBullets.removeAll(bossBulletsToRemove);

        // Remove dead/invisible enemies from the list
        enemies.removeIf(e -> !e.isVisible() && !(e instanceof LargeAlien));
    }

    private void doGameCycle() {
        update();
        frame++;
        elapsedFrames++; // Increment timer every frame

        // SWITCH TO SCENE2 BASED ON TIME PASSED
        if (!switchedToScene2 && elapsedFrames / 60 >= 10 && parentFrame instanceof Game) {
            switchedToScene2 = true;
            timer.stop();
            if (audioPlayer != null) {
                try {
                    audioPlayer.stop();
                } catch (Exception ignored) {
                }
            }

            // Use Game's setScene2() method for cleaner transition
            ((Game) parentFrame).setScene2();
            return;
        }

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
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            if (e.getKeyCode() == KeyEvent.VK_SPACE && inGame && shots.size() < 7 && player.canShoot()) {
                if (multiShotEnabled) {
                    // Fire up to 3 shots from slightly offset positions if space permits
                    int spaceLeft = 7 - shots.size();

                    if (spaceLeft >= 2)
                        shots.add(new Shot(player.getX() - 10, player.getY())); // Left
                    if (spaceLeft >= 3)
                        shots.add(new Shot(player.getX() + 10, player.getY())); // Right
                } else {
                    shots.add(new Shot(player.getX(), player.getY()));
                }
                player.resetShootCooldown();
                try {
                    new AudioPlayer("src/audio/fire.wav", true).play();
                } catch (Exception ex) {
                }
            }
        }
    }
}
