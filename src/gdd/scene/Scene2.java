package gdd.scene;

import static gdd.Global.*;
import gdd.AudioPlayer;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.PowerUp;
import gdd.sprite.Shot;
import gdd.sprite.Alien1;
import gdd.sprite.Bomb;
import gdd.sprite.LargeAlien;
import gdd.sprite.BossBullet;
import gdd.sprite.ShieldedEnemy;

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

public class Scene2 extends JPanel {
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
    private List<LargeAlien> bosses = new ArrayList<>();
    private List<BossBullet> bossBullets = new ArrayList<>();
    private int lastEnemyWaveFrame = 0;
    private int elapsedFrames = 0; // Add this field to track time
    private javax.swing.JFrame parentFrame; // To allow scene switching
    private final Color SPACE_BG = new Color(5, 10, 30); // deep dark blue space color
    private final Color STAR_WHITE = new Color(255, 255, 255);
    private final Color STAR_BLUE = new Color(200, 200, 255);
    private final Color STAR_ORANGE = new Color(255, 240, 200);
    private final Color STAR_CYAN = new Color(180, 255, 255);
    private final Color[] STAR_COLORS = { STAR_WHITE, STAR_BLUE, STAR_ORANGE, STAR_CYAN };

    public Scene2(javax.swing.JFrame parentFrame) {
        this.parentFrame = parentFrame;
        MAP = loadMap("src/map/map_expanded.csv"); // Use a different map for Scene2 if available
        loadSpawnMap("src/map/spawn.csv"); // Use a different spawn file for Scene2 if available
        initBoard();
        lastEnemyWaveFrame = getLastEnemyWaveFrame();
    }
    // Keep the old constructor for compatibility
    public Scene2() {
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
        setBackground(SPACE_BG); // CHANGED
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
            audioPlayer = new AudioPlayer("src/audio/scene1.wav"); // Use a different audio for Scene2 if available
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading audio: " + e.getMessage());
        }
    }

    private int[][] loadMap(String csvFile) {
        List<int[]> rows = new ArrayList<>();
        int targetCols = BOARD_WIDTH / BLOCKWIDTH;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                int[] row = Arrays.stream(line.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
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
        Color starColor = STAR_COLORS[randomizer.nextInt(STAR_COLORS.length)];
        g.setColor(starColor);
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
        g.setColor(SPACE_BG); // CHANGED
        g.fillRect(0, 0, d.width, d.height);

        // HUD Text
        g.setColor(Color.CYAN); // CHANGED
        g.setFont(new Font("Helvetica", Font.BOLD, 16)); // CHANGED
        g.drawString("Galaxy Siege - Sector 2", 10, 20); // CHANGED Title
        g.setFont(new Font("Helvetica", Font.PLAIN, 18));
        g.drawString("Score: " + score, 10, 50);
        g.drawString("Time: " + (elapsedFrames / 60) + "s", 200, 50);
        g.setColor(Color.PINK);
        g.drawString("Health: " + player.getHealth(), 350, 50);

        int statusX = 350;
        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        if (multiShotEnabled) {
            g.setColor(Color.CYAN);
            g.drawString("MULTI SHOT", statusX, 50);
            statusX += 100;
        }
        if (speedFrames > 0) {
            g.setColor(Color.ORANGE);
            g.drawString("SPEED BOOST", statusX, 50);
        }

        // Boss Health Bar
        if (bosses.stream().anyMatch(boss -> boss.isVisible())) {
            g.setColor(Color.RED);
            for (LargeAlien boss : bosses) {
                if (boss.isVisible()) {
                    g.fillRect(200, 20, boss.getHealth() * 10, 20);
                    g.setColor(Color.WHITE);
                    g.drawRect(200, 20, 200, 20);
                    g.drawString("BOSS HEALTH", 200, 15);
                }
            }
        }

        if (inGame) {
            drawMap(g);
            drawExplosions(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g);
            drawPowerUps(g);
            for (BossBullet bullet : bossBullets) {
                g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), this);
            }
        } else {
            if (timer.isRunning()) timer.stop();
            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {
        g.setColor(SPACE_BG); // CHANGED
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(20, 40, 70)); // CHANGED overlay box
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        g.setColor(Color.CYAN); // CHANGED text
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        Font small = new Font("Helvetica", Font.BOLD, 14);
        var fm = this.getFontMetrics(small);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fm.stringWidth(message)) / 2, BOARD_WIDTH / 2);
    }

    private void update() {
        List<SpawnDetails> spawns = spawnMap.get(frame);
        if (spawns != null) {
            for (SpawnDetails sd : spawns) {
                String type = sd.type.trim().toLowerCase();
                switch (type) {
                    case "alien1": enemies.add(new Alien1(sd.x, sd.y)); break;
                    case "shielded": enemies.add(new ShieldedEnemy(sd.x, sd.y)); break;
                    case "largealien":
                        LargeAlien newBoss = new LargeAlien(sd.x, sd.y);
                        enemies.add(newBoss);
                        bosses.add(newBoss);
                        break;
                    case "speed": powerUps.add(new PowerUp(PowerUp.TYPE_SPEED, sd.x, sd.y)); break;
                    case "multi": powerUps.add(new PowerUp(PowerUp.TYPE_MULTI, sd.x, sd.y)); break;
                    case "bomb": powerUps.add(new PowerUp(PowerUp.TYPE_BOMB, sd.x, sd.y)); break;
                    default: System.out.println("Unknown spawn type: " + type); // Debug
                }
            }
        }
        player.updateSpeed(speedFrames > 0);
        player.act();
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
                        multiShotFrames = 600;
                    }
                    case PowerUp.TYPE_SPEED -> speedFrames = 600;
                    case PowerUp.TYPE_BOMB -> {
                        // Clear all enemies except the boss
                        List<Enemy> toExplode = new ArrayList<>();
                        for (Enemy e : enemies) {
                            if (!(e instanceof LargeAlien) && e.isVisible()) {
                                toExplode.add(e);
                            }
                        }
                        for (Enemy e : toExplode) {
                            e.setImage(new ImageIcon(IMG_EXPLOSION).getImage());
                            e.setDying(true);
                            explosions.add(new Explosion(e.getX(), e.getY()));
                            score += 10;
                            try {
                                new AudioPlayer("src/audio/explosion.wav", true).play();
                            } catch (Exception ex) {
                                System.err.println("Error playing explosion sound: " + ex.getMessage());
                            }
                        }
                    }
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
                        if (enemy instanceof ShieldedEnemy shielded) {
                            shielded.hit();
                            if (!shielded.isVisible()) {
                                explosions.add(new Explosion(shielded.getX(), shielded.getY()));
                                score += 30; // Bonus for shielded
                            }
                        } else if (enemy instanceof LargeAlien largeAlien) {
                            largeAlien.takeDamage(1);
                            if (!largeAlien.isVisible()) {
                                explosions.add(new Explosion(largeAlien.getX(), largeAlien.getY()));
                                // Only end game if this was the last boss
                                if (bosses.stream().noneMatch(b -> b.isVisible())) {
                                    message = "Bosses Defeated! You win!";
                                    inGame = false;
                                    timer.stop();
                                }
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
            if (enemy.getY() >= GROUND - ALIEN_HEIGHT) {
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
        for (Enemy enemy : enemies) {
            if (randomizer.nextInt(240) == CHANCE && enemy.isVisible()) {
                bombs.add(new Bomb(enemy.getX(), enemy.getY()));
            }
        }
        List<Bomb> bombsToRemove = new ArrayList<>();
        for (Bomb bomb : bombs) {
            bomb.setY(bomb.getY() + 6);
            if (player.isVisible() &&
                    bomb.getX() >= player.getX() && bomb.getX() <= player.getX() + PLAYER_WIDTH &&
                    bomb.getY() >= player.getY() && bomb.getY() <= player.getY() + PLAYER_HEIGHT) {
                player.takeHit();
                bombsToRemove.add(bomb);
                if (player.isDead()) {
                    player.setImage(new ImageIcon(IMG_EXPLOSION).getImage());
                    player.setDying(true);
                }
            }
            if (bomb.getY() >= GROUND - BOMB_HEIGHT) {
                bombsToRemove.add(bomb);
            }
        }
        bombs.removeAll(bombsToRemove);
        // Boss logic: act and fire bullets for all alive bosses
        for (LargeAlien boss : bosses) {
            if (boss.isVisible() && !boss.isDying()) {
                boss.act();
                bossBullets.addAll(boss.tryFire());
            }
        }
        List<BossBullet> bossBulletsToRemove = new ArrayList<>();
        for (BossBullet bullet : bossBullets) {
            bullet.act();
            if (bullet.getY() > BOARD_HEIGHT || bullet.getY() < 0 || bullet.getX() < 0 || bullet.getX() > BOARD_WIDTH) {
                bossBulletsToRemove.add(bullet);
            } else if (player.isVisible() && bullet.getBounds().intersects(player.getBounds())) {
                player.takeHit();
                bossBulletsToRemove.add(bullet);
                if (player.isDead()) {
                    player.setImage(new ImageIcon(IMG_EXPLOSION).getImage());
                    player.setDying(true);
                }
            }
        }
        bossBullets.removeAll(bossBulletsToRemove);
        enemies.removeIf(e -> !e.isVisible() && !(e instanceof LargeAlien));
        // Shielded enemies fire bombs
        for (Enemy enemy : enemies) {
            if (enemy instanceof ShieldedEnemy shielded && shielded.isVisible()) {
                bombs.addAll(shielded.tryFire());
            }
        }
        player.updateShootCooldown();
    }

    private void doGameCycle() {
        update();
        frame++;
        elapsedFrames++; // Increment timer every frame
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
                    int spaceLeft = 7 - shots.size();
                    if (spaceLeft >= 2)
                        shots.add(new Shot(player.getX() - 10, player.getY()));
                    if (spaceLeft >= 3)
                        shots.add(new Shot(player.getX() + 10, player.getY()));
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