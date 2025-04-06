package gui.game;

import tut01.tanks.*;
import tut01.powerups.*;
import tut01.environments.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * Game panel class that handles the main game display and logic
 */
public class GamePanel extends JPanel {
    private BattleCityGame game;
    private int width = 520; // 13 cells * 40 pixels
    private int height = 520; // 13 cells * 40 pixels
    private int cellSize = 40;

    // Game elements
    private PlayerTank player1;
    private PlayerTank player2;
    private List<EnemyTank> enemyTanks;
    private List<Bullet> bullets;
    private List<Environment> environments;
    private List<PowerUp> powerUps;
    private boolean baseDestroyed = false;
    private boolean twoPlayerMode = false;
    private Point baseLocation;

    // Enemy spawn management
    private int maxEnemiesOnScreen = 4;
    private int enemiesSpawned = 0;
    private int totalEnemies = 20;
    private int enemiesDefeated = 0;
    private long lastEnemySpawnTime = 0;
    private long enemySpawnDelay = 3000; // 3 seconds
    private List<Point> enemySpawnPoints;

    // Power-up timer
    private PowerUp activePowerUp = null;
    private javax.swing.Timer powerUpTimer = null;
    private int powerUpDuration = 15000; // 15 seconds

    public GamePanel(BattleCityGame game) {
        this.game = game;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);

        // Initialize lists
        enemyTanks = new ArrayList<>();
        bullets = new ArrayList<>();
        environments = new ArrayList<>();
        powerUps = new ArrayList<>();
        enemySpawnPoints = new ArrayList<>();

        // Set base location (center bottom of map)
        baseLocation = new Point(width / 2 - cellSize / 2, height - cellSize);
        setPreferredSize(new Dimension(width, height));
        setBorder(BorderFactory.createLineBorder(Color.RED, 2)); // Add a visible border
    }

    public void setTwoPlayerMode(boolean twoPlayerMode) {
        this.twoPlayerMode = twoPlayerMode;
    }

    // Load map from input stream
    public void loadMapFromStream(InputStream is) throws IOException {
        // Clear existing data
        environments.clear();
        enemySpawnPoints.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        int row = 0;

        while ((line = reader.readLine()) != null) {
            System.out.println("Reading map line: " + line);
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                int x = col * cellSize;
                int y = row * cellSize;

                switch (c) {
                    case 'B': // Brick wall
                        environments.add(new BrickWall(x, y));
                        break;
                    case 'S': // Steel wall
                        environments.add(new SteelWall(x, y));
                        break;
                    case 'W': // Water
                        environments.add(new Water(x, y));
                        break;
                    case 'T': // Trees
                        environments.add(new Trees(x, y));
                        break;
                    case 'I': // Ice
                        environments.add(new Ice(x, y));
                        break;
                    case 'E': // Enemy spawn point
                        enemySpawnPoints.add(new Point(x, y));
                        break;
                    case '1': // Player 1 spawn point
                        player1 = new PlayerTank(x, y, 1);
                        break;
                    case '2': // Player 2 spawn point (if in two player mode)
                        if (twoPlayerMode) {
                            player2 = new PlayerTank(x, y, 2);
                        }
                        break;
                    case 'X': // Base location
                        baseLocation = new Point(x, y);
                        break;
                }
            }
            row++;
        }

        // Protect base with brick walls
        createBaseProtection();

        // Reset game state
        resetGameState();
    }

    // Create default map if no file available
    public void createDefaultMap() {
        // Clear existing data
        environments.clear();
        enemySpawnPoints.clear();

        // Add some brick walls
        for (int i = 0; i < 5; i++) {
            environments.add(new BrickWall(i * cellSize, 2 * cellSize));
            environments.add(new BrickWall((width - cellSize) - i * cellSize, 2 * cellSize));
            environments.add(new BrickWall(i * cellSize, height - 3 * cellSize));
            environments.add(new BrickWall((width - cellSize) - i * cellSize, height - 3 * cellSize));
        }

        // Add some steel walls
        environments.add(new SteelWall(width / 2 - cellSize, height / 2));
        environments.add(new SteelWall(width / 2, height / 2));
        environments.add(new SteelWall(width / 2 + cellSize, height / 2));

        // Add some water
        for (int i = 0; i < 3; i++) {
            environments.add(new Water(2 * cellSize, 5 * cellSize + i * cellSize));
            environments.add(new Water(width - 3 * cellSize, 5 * cellSize + i * cellSize));
        }

        // Add some trees
        for (int i = 0; i < 3; i++) {
            environments.add(new Trees(width / 2 - cellSize - i * cellSize, 4 * cellSize));
            environments.add(new Trees(width / 2 + cellSize + i * cellSize, 4 * cellSize));
        }

        // Add some ice
        environments.add(new Ice(width / 2 - 2 * cellSize, height / 2 + 2 * cellSize));
        environments.add(new Ice(width / 2 + 2 * cellSize, height / 2 + 2 * cellSize));

        // Set enemy spawn points (all four corners)
        enemySpawnPoints.add(new Point(0, 0));
        enemySpawnPoints.add(new Point(width - cellSize, 0));
        enemySpawnPoints.add(new Point(0, height - 5 * cellSize));
        enemySpawnPoints.add(new Point(width - cellSize, height - 5 * cellSize));

        // Set player spawn points
        player1 = new PlayerTank(width / 2 - 2 * cellSize, height - 2 * cellSize, 1);
        if (twoPlayerMode) {
            player2 = new PlayerTank(width / 2 + 2 * cellSize, height - 2 * cellSize, 2);
        } else {
            player2 = null;
        }

        // Base location
        baseLocation = new Point(width / 2 - cellSize / 2, height - cellSize);

        // Protect base with brick walls
        createBaseProtection();

        // Reset game state
        resetGameState();
    }

    // Create brick walls around base
    private void createBaseProtection() {
        // Create brick walls around the base
        int baseX = (int) baseLocation.getX();
        int baseY = (int) baseLocation.getY();

        // Top wall
        environments.add(new BrickWall(baseX - cellSize, baseY - cellSize));
        environments.add(new BrickWall(baseX, baseY - cellSize));
        environments.add(new BrickWall(baseX + cellSize, baseY - cellSize));

        // Side walls
        environments.add(new BrickWall(baseX - cellSize, baseY));
        environments.add(new BrickWall(baseX + cellSize, baseY));
    }

    // Reset game state for new game or level
    private void resetGameState() {
        // Clear bullets and power-ups
        bullets.clear();
        powerUps.clear();

        // Reset enemy data
        enemyTanks.clear();
        enemiesSpawned = 0;
        enemiesDefeated = 0;

        // Reset flags
        baseDestroyed = false;

        // Reset player positions
        if (player1 != null) {
            player1.setX(width / 2 - 2 * cellSize);
            player1.setY(height - 2 * cellSize);
            player1.setDirection(Tank.Direction.UP);
            player1.setMoving(false);
            player1.setHealth(1);
        }

        if (player2 != null) {
            player2.setX(width / 2 + 2 * cellSize);
            player2.setY(height - 2 * cellSize);
            player2.setDirection(Tank.Direction.UP);
            player2.setMoving(false);
            player2.setHealth(1);
        }
    }

    // Update game state
    public void updateGame() {
        // Spawn enemies if needed
        spawnEnemyIfNeeded();

        // Update player tanks
        if (player1 != null) {
            updateTank(player1);
        }

        if (player2 != null) {
            updateTank(player2);
        }

        // Update enemy tanks
        List<EnemyTank> enemyTanksToRemove = new ArrayList<>();
        for (EnemyTank enemyTank : enemyTanks) {
            // Update AI
            enemyTank.updateAI();

            // Update movement
            updateTank(enemyTank);

            // Check if tank is destroyed
            if (enemyTank.getHealth() <= 0) {
                enemyTanksToRemove.add(enemyTank);
                enemiesDefeated++;
                game.addScore(enemyTank.getPoints());

                // Randomly spawn power-up (20% chance)
                if (enemyTank.isFlashing() || Math.random() < 0.2) {
                    spawnPowerUp(enemyTank.getX(), enemyTank.getY());
                }
            }
        }
        enemyTanks.removeAll(enemyTanksToRemove);

        // Update bullets
        updateBullets();

        // Check for collisions
        checkCollisions();

        // Repaint the panel
        repaint();
    }

    // Update tank movement
    private void updateTank(Tank tank) {
        if (!tank.isMoving()) return;

        // Store old position
        int oldX = tank.getX();
        int oldY = tank.getY();

        // Move tank
        tank.move();

        // Check boundary collision
        if (tank.getX() < 0) {
            tank.setX(0);
        } else if (tank.getX() + tank.getSize() > width) {
            tank.setX(width - tank.getSize());
        }

        if (tank.getY() < 0) {
            tank.setY(0);
        } else if (tank.getY() + tank.getSize() > height) {
            tank.setY(height - tank.getSize());
        }

        // Check environment collision
        Rectangle tankBounds = tank.getBounds();
        for (Environment env : environments) {
            if (!env.isPassable() && env.getBounds().intersects(tankBounds)) {
                // Collision detected, revert to old position
                tank.setX(oldX);
                tank.setY(oldY);
                break;
            }
        }

        // Check tank-tank collision
        for (EnemyTank enemyTank : enemyTanks) {
            if (tank != enemyTank && tankBounds.intersects(enemyTank.getBounds())) {
                // Collision detected, revert to old position
                tank.setX(oldX);
                tank.setY(oldY);
                break;
            }
        }

        // Check player-player collision
        if (player1 != null && player2 != null && player1 != tank && player2 != tank) {
            if (tankBounds.intersects(player1.getBounds()) || tankBounds.intersects(player2.getBounds())) {
                // Collision detected, revert to old position
                tank.setX(oldX);
                tank.setY(oldY);
            }
        }
    }

    // Update bullets
    private void updateBullets() {
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            // Move bullet
            bullet.move();

            // Check if bullet is out of bounds
            if (bullet.getX() < 0 || bullet.getX() > width ||
                    bullet.getY() < 0 || bullet.getY() > height) {
                bulletsToRemove.add(bullet);
            }
        }

        bullets.removeAll(bulletsToRemove);
    }

    // Check for collisions
    private void checkCollisions() {
        // Bullet-environment collisions
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Environment> environmentsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();

            // Check collision with environments
            for (Environment env : environments) {
                if (bulletBounds.intersects(env.getBounds())) {
                    // Water doesn't stop bullets
                    if (env instanceof Water) {
                        continue;
                    }

                    bulletsToRemove.add(bullet);

                    // Check if environment is destroyed
                    if (env.hitByBullet(bullet.getDamage())) {
                        environmentsToRemove.add(env);
                    }

                    break;
                }
            }

            // Check collision with tanks
            if (!bulletsToRemove.contains(bullet)) {
                // Check enemy tanks
                for (EnemyTank enemyTank : enemyTanks) {
                    if (bulletBounds.intersects(enemyTank.getBounds())) {
                        bulletsToRemove.add(bullet);
                        enemyTank.takeDamage(bullet.getDamage());
                        break;
                    }
                }

                // Check player tanks
                if (!bulletsToRemove.contains(bullet)) {
                    if (player1 != null && bulletBounds.intersects(player1.getBounds())) {
                        bulletsToRemove.add(bullet);
                        if (player1.takeDamage(bullet.getDamage())) {
                            // Player 1 tank is destroyed
                            if (player1.loseLife()) {
                                // Game over - player 1 out of lives
                                player1 = null;
                            } else {
                                // Respawn player 1
                                player1.setX(width / 2 - 2 * cellSize);
                                player1.setY(height - 2 * cellSize);
                                player1.setHealth(1);
                                game.updatePlayerLives(player1.getLives());
                            }
                        }
                    } else if (player2 != null && bulletBounds.intersects(player2.getBounds())) {
                        bulletsToRemove.add(bullet);
                        if (player2.takeDamage(bullet.getDamage())) {
                            // Player 2 tank is destroyed
                            if (player2.loseLife()) {
                                // Player 2 out of lives
                                player2 = null;
                            } else {
                                // Respawn player 2
                                player2.setX(width / 2 + 2 * cellSize);
                                player2.setY(height - 2 * cellSize);
                                player2.setHealth(1);
                            }
                        }
                    }
                }
            }

            // Check for base collision
            if (!bulletsToRemove.contains(bullet)) {
                Rectangle baseBounds = new Rectangle(
                        (int) baseLocation.getX(),
                        (int) baseLocation.getY(),
                        cellSize,
                        cellSize
                );

                if (bulletBounds.intersects(baseBounds)) {
                    bulletsToRemove.add(bullet);
                    baseDestroyed = true;
                }
            }
        }

        // Apply removals
        bullets.removeAll(bulletsToRemove);
        environments.removeAll(environmentsToRemove);

        // Power-up collection
        List<PowerUp> powerUpsToRemove = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            // Check player 1
            if (player1 != null && powerUp.getBounds().intersects(player1.getBounds())) {
                powerUp.apply(player1);
                powerUpsToRemove.add(powerUp);
                game.addScore(powerUp.getPoints());

                // Special handling for certain power-ups
                handleSpecialPowerUp(powerUp);
            }
            // Check player 2
            else if (player2 != null && powerUp.getBounds().intersects(player2.getBounds())) {
                powerUp.apply(player2);
                powerUpsToRemove.add(powerUp);
                game.addScore(powerUp.getPoints());

                // Special handling for certain power-ups
                handleSpecialPowerUp(powerUp);
            }
        }
        powerUps.removeAll(powerUpsToRemove);
    }

    // Handle special power-ups that affect the game globally
    private void handleSpecialPowerUp(PowerUp powerUp) {
        if (powerUp instanceof Grenade) {
            // Destroy all enemies
            for (EnemyTank enemyTank : enemyTanks) {
                game.addScore(enemyTank.getPoints());
            }
            enemyTanks.clear();
            enemiesDefeated += enemyTanks.size();
        } else if (powerUp instanceof TimerPowerUp) {
            // Freeze all enemies for a duration
            for (EnemyTank enemyTank : enemyTanks) {
                enemyTank.setMoving(false);
            }

            // Set up timer to unfreeze
            if (powerUpTimer != null) {
                powerUpTimer.stop();
            }

            powerUpTimer = new Timer(powerUpDuration, e -> {
                // Unfreeze enemies
                for (EnemyTank enemyTank : enemyTanks) {
                    enemyTank.setMoving(true);
                }
                powerUpTimer.stop();
            });
            powerUpTimer.setRepeats(false);
            powerUpTimer.start();
        } else if (powerUp instanceof Helmet) {
            // Make players invulnerable temporarily
            // This would require more state tracking that we'll skip for now
        } else if (powerUp instanceof Shovel) {
            // Convert base brick walls to steel temporarily
            // This would require more environment manipulation that we'll skip for now
        }
    }

    // Spawn enemy if needed
    private void spawnEnemyIfNeeded() {
        long currentTime = System.currentTimeMillis();

        if (enemiesSpawned < totalEnemies &&
                enemyTanks.size() < maxEnemiesOnScreen &&
                currentTime - lastEnemySpawnTime > enemySpawnDelay) {

            // Select a random spawn point
            if (!enemySpawnPoints.isEmpty()) {
                int index = (int) (Math.random() * enemySpawnPoints.size());
                Point spawnPoint = enemySpawnPoints.get(index);

                // Create a random enemy type
                EnemyTank enemy = null;
                double rand = Math.random();

                if (rand < 0.4) {
                    enemy = new BasicTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                } else if (rand < 0.6) {
                    enemy = new FastTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                } else if (rand < 0.8) {
                    enemy = new PowerTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                } else {
                    enemy = new ArmorTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                }

                // Set some tanks to flash (for power-up generation)
                if (enemiesSpawned == 3 || enemiesSpawned == 10 || enemiesSpawned == 17) {
                    enemy.setFlashing(true);
                }

                enemyTanks.add(enemy);
                enemiesSpawned++;
                lastEnemySpawnTime = currentTime;
            }
        }
    }

    // Spawn a power-up at the given location
    private void spawnPowerUp(int x, int y) {
        PowerUp powerUp = null;
        double rand = Math.random();

        if (rand < 0.17) {
            powerUp = new Grenade(x, y);
        } else if (rand < 0.34) {
            powerUp = new Helmet(x, y);
        } else if (rand < 0.5) {
            powerUp = new Shovel(x, y);
        } else if (rand < 0.67) {
            powerUp = new Star(x, y);
        } else if (rand < 0.84) {
            powerUp = new TankPowerUp(x, y);
        } else {
            powerUp = new TimerPowerUp(x, y);
        }

        powerUps.add(powerUp);
    }

    // Method for player to fire
    public void playerFire(PlayerTank player) {
        Bullet bullet = player.fire();
        bullets.add(bullet);
    }

    // Check if level is complete
    public boolean isLevelComplete() {
        return enemiesDefeated >= totalEnemies && enemyTanks.isEmpty();
    }

    // Check if game is over
    public boolean isGameOver() {
        return (player1 == null && player2 == null) || baseDestroyed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        System.out.println("Painting game panel - size: " + getWidth() + "x" + getHeight());

        // Draw environments
        for (Environment env : environments) {
            env.draw(g);
        }

        // Draw base
        g.setColor(Color.GRAY);
        g.fillRect((int) baseLocation.getX(), (int) baseLocation.getY(), cellSize, cellSize);
        g.setColor(Color.WHITE);
        g.drawRect((int) baseLocation.getX(), (int) baseLocation.getY(), cellSize, cellSize);

        // Draw power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g);
        }

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        // Draw enemy tanks
        for (EnemyTank enemy : enemyTanks) {
            enemy.draw(g);
        }

        // Draw player tanks
        if (player1 != null) {
            player1.draw(g);
        }

        if (player2 != null) {
            player2.draw(g);
        }
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("GAME ACTIVE", width/2 - 80, height/2);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
    }

    // Getters for game elements
    public PlayerTank getPlayer1() {
        return player1;
    }

    public PlayerTank getPlayer2() {
        return player2;
    }

    public void testRender() {
        // Clear everything
        environments.clear();
        enemyTanks.clear();
        bullets.clear();

        // Add one visible element
        player1 = new PlayerTank(width/2, height/2, 1);
        baseLocation = new Point(width/2, height-100);

        // Add a test brick wall
        environments.add(new BrickWall(100, 100));

        repaint();
        System.out.println("Test render requested");
    }
}