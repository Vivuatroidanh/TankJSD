package gui.game;

import tut01.tanks.*;
import tut01.powerups.*;
import tut01.environments.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.util.Map;
import java.util.Iterator;

/**
 * Game panel class that handles the main game display and logic
 */
public class GamePanel extends JPanel {
    private BattleCityGame game;
    private int width = 640; // 16 cells * 40 pixels
    private int height = 640; // 16 cells * 40 pixels
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

    // Enemy tank tracking - for debugging disappearing tanks
    private Map<EnemyTank, Long> enemySpawnTimes = new HashMap<>();
    private List<String> enemyDestroyLog = new ArrayList<>();

    // Power-up timer
    private PowerUp activePowerUp = null;
    private javax.swing.Timer powerUpTimer = null;
    private int powerUpDuration = 15000; // 15 seconds

    // Debug flag
    private boolean debug = false;

    // Game effects
    private List<Effect> effects = new ArrayList<>();

    // FPS calculation
    private long lastTime = System.nanoTime();
    private double fps = 0;

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
        effects = new ArrayList<>();

        // Set base location (center bottom of map)
        baseLocation = new Point(width / 2 - cellSize / 2, height - cellSize);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public void setTwoPlayerMode(boolean twoPlayerMode) {
        this.twoPlayerMode = twoPlayerMode;
    }

    // Load map from input stream
    public void loadMapFromStream(InputStream is) throws IOException {
        // Clear existing data
        environments.clear();
        enemySpawnPoints.clear();
        enemyTanks.clear();
        enemySpawnTimes.clear();
        enemyDestroyLog.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        int row = 0;

        while ((line = reader.readLine()) != null && row < height / cellSize) {
            System.out.println("Reading map line: " + line);
            for (int col = 0; col < line.length() && col < width / cellSize; col++) {
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

        // If there aren't enough enemy spawn points, add some default ones
        if (enemySpawnPoints.size() < 4) {
            // Add default spawn points at corners
            enemySpawnPoints.add(new Point(0, 0));
            enemySpawnPoints.add(new Point(width - cellSize, 0));
            enemySpawnPoints.add(new Point(0, height / 2 - cellSize));
            enemySpawnPoints.add(new Point(width - cellSize, height / 2 - cellSize));
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
        enemyTanks.clear();
        enemySpawnTimes.clear();
        enemyDestroyLog.clear();

        // Add some brick walls - adjusted for larger map
        for (int i = 0; i < 10; i++) {
            environments.add(new BrickWall(i * cellSize, 2 * cellSize));
            environments.add(new BrickWall((width - cellSize) - i * cellSize, 2 * cellSize));
            environments.add(new BrickWall(i * cellSize, height - 3 * cellSize));
            environments.add(new BrickWall((width - cellSize) - i * cellSize, height - 3 * cellSize));
        }

        // Add some steel walls
        environments.add(new SteelWall(width / 2 - cellSize, height / 2));
        environments.add(new SteelWall(width / 2, height / 2));
        environments.add(new SteelWall(width / 2 + cellSize, height / 2));

        // Add additional steel walls
        environments.add(new SteelWall(width / 4, height / 4));
        environments.add(new SteelWall(3 * width / 4, height / 4));
        environments.add(new SteelWall(width / 4, 3 * height / 4));
        environments.add(new SteelWall(3 * width / 4, 3 * height / 4));

        // Add steel wall foundation at bottom
        for (int i = 0; i < width/cellSize; i++) {
            if (i % 3 == 0) {
                environments.add(new SteelWall(i * cellSize, height - cellSize));
            }
        }

        // Add some water
        for (int i = 0; i < 4; i++) {
            environments.add(new Water(2 * cellSize, 5 * cellSize + i * cellSize));
            environments.add(new Water(width - 3 * cellSize, 5 * cellSize + i * cellSize));
        }

        // Add more water to create a river
        for (int i = 0; i < 3; i++) {
            environments.add(new Water(3 * cellSize + i * cellSize, 9 * cellSize));
            environments.add(new Water(width - 4 * cellSize - i * cellSize, 9 * cellSize));
        }

        // Add some trees for cover
        for (int i = 0; i < 4; i++) {
            environments.add(new Trees(width / 2 - cellSize - i * cellSize, 4 * cellSize));
            environments.add(new Trees(width / 2 + cellSize + i * cellSize, 4 * cellSize));
            environments.add(new Trees(3 * cellSize, 12 * cellSize + i * cellSize));
            environments.add(new Trees(width - 4 * cellSize, 12 * cellSize + i * cellSize));
        }

        // Add some ice for sliding
        environments.add(new Ice(width / 2 - 2 * cellSize, height / 2 + 2 * cellSize));
        environments.add(new Ice(width / 2 + 2 * cellSize, height / 2 + 2 * cellSize));
        environments.add(new Ice(width / 2 - 2 * cellSize, height / 2 - 2 * cellSize));
        environments.add(new Ice(width / 2 + 2 * cellSize, height / 2 - 2 * cellSize));

        // Add ice path
        for (int i = 0; i < 3; i++) {
            environments.add(new Ice(width / 4 + i * cellSize, 10 * cellSize));
            environments.add(new Ice(3 * width / 4 - i * cellSize, 10 * cellSize));
        }

        // Set enemy spawn points (all four corners and additional points)
        enemySpawnPoints.add(new Point(0, 0));
        enemySpawnPoints.add(new Point(width - cellSize, 0));
        enemySpawnPoints.add(new Point(0, height - 5 * cellSize));
        enemySpawnPoints.add(new Point(width - cellSize, height - 5 * cellSize));
        // Add center top spawn points
        enemySpawnPoints.add(new Point(width / 2 - cellSize, 0));
        enemySpawnPoints.add(new Point(width / 2 + cellSize, 0));

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
        effects.clear();

        // Reset enemy data
        enemyTanks.clear();
        enemySpawnTimes.clear();
        enemyDestroyLog.clear();
        enemiesSpawned = 0;
        enemiesDefeated = 0;

        // Reset flags
        baseDestroyed = false;

        // Enable debug logging in enemy tanks if needed
        EnemyTank.setDebug(debug);

        // Reset player positions
        if (player1 != null) {
            player1.setX(width / 2 - 2 * cellSize);
            player1.setY(height - 2 * cellSize);
            player1.setDirection(Tank.Direction.UP);
            player1.setMoving(false);
            player1.setHealth(1);
            player1.setWantsToFire(false); // Reset fire flag
            player1.resetSliding(); // Reset sliding status
        }

        if (player2 != null) {
            player2.setX(width / 2 + 2 * cellSize);
            player2.setY(height - 2 * cellSize);
            player2.setDirection(Tank.Direction.UP);
            player2.setMoving(false);
            player2.setHealth(1);
            player2.setWantsToFire(false); // Reset fire flag
            player2.resetSliding(); // Reset sliding status
        }
    }

    // Update game state
    public void updateGame() {
        // Spawn enemies if needed
        spawnEnemyIfNeeded();

        // Update player tanks
        if (player1 != null) {
            updateTank(player1);
            // Check if player wants to fire and handle firing
            if (player1.wantsToFire()) {
                playerFire(player1);
            }
        }

        if (player2 != null) {
            updateTank(player2);
            // Check if player wants to fire and handle firing
            if (player2.wantsToFire()) {
                playerFire(player2);
            }
        }

        // Update enemy tanks with improved AI
        List<EnemyTank> enemyTanksToRemove = new ArrayList<>();
        Iterator<EnemyTank> tankIterator = enemyTanks.iterator();

        // Process each enemy tank - using iterator to avoid concurrent modification issues
        while (tankIterator.hasNext()) {
            EnemyTank enemyTank = tankIterator.next();

            try {
                // Check invulnerability status first
                enemyTank.checkInvulnerabilityExpired();

                // Update AI with player and base information
                enemyTank.updateAI(player1, player2, baseLocation, environments);

                // Update movement - only move in cardinal directions
                updateTank(enemyTank);

                // Handle enemy shooting
                if (shouldEnemyFire(enemyTank)) {
                    Bullet bullet = enemyTank.fire();
                    if (bullet != null) {
                        bullets.add(bullet);
                    }
                }

                // Check if tank is destroyed - only due to damage, not from other causes
                if (enemyTank.getHealth() <= 0) {
                    enemyTanksToRemove.add(enemyTank);
                    enemiesDefeated++;
                    game.addScore(enemyTank.getPoints());

                    // Track when and why the tank was removed
                    if (debug) {
                        Long spawnTime = enemySpawnTimes.get(enemyTank);
                        long lifespan = spawnTime != null ? System.currentTimeMillis() - spawnTime : -1;
                        String logEntry = "Tank destroyed after " + lifespan + "ms. Health: " +
                                enemyTank.getHealth() + ", Last action: " + enemyTank.getLastAction();
                        System.out.println(logEntry);
                        enemyDestroyLog.add(logEntry);
                    }

                    // Add explosion effect
                    addExplosionEffect(enemyTank.getX(), enemyTank.getY());

                    // Randomly spawn power-up (20% chance)
                    if (enemyTank.isFlashing() || Math.random() < 0.2) {
                        spawnPowerUp(enemyTank.getX(), enemyTank.getY());
                    }
                }
            } catch (Exception e) {
                // If any exception occurs with this tank, log it and remove the tank
                if (debug) {
                    System.err.println("Error updating enemy tank: " + e.getMessage());
                    e.printStackTrace();

                    // Add to removal list so we don't keep processing a broken tank
                    enemyTanksToRemove.add(enemyTank);
                }
            }
        }

        // Remove only the tanks that were actually destroyed
        if (!enemyTanksToRemove.isEmpty()) {
            for (EnemyTank tank : enemyTanksToRemove) {
                enemyTanks.remove(tank);
                enemySpawnTimes.remove(tank);
            }

            if (debug) {
                System.out.println("Removed " + enemyTanksToRemove.size() + " enemy tanks. Remaining: " +
                        enemyTanks.size() + ", Total defeated: " + enemiesDefeated);
            }
        }

        // Update bullets
        updateBullets();

        // Check for bullet-to-bullet collisions (bullets from opposite directions)
        checkBulletCollisions();

        // Update effects
        updateEffects();

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

        // Check if tank is on ice
        boolean onIce = isOnIce(tank);

        // Update sliding status
        if (onIce) {
            // Increase sliding on ice
            tank.setSlideFactor(Math.min(1.0, tank.getSlideFactor() + 0.1));
            tank.setSliding(true);
        } else if (tank.isSliding()) {
            // Gradually decrease sliding when off ice
            tank.setSlideFactor(Math.max(0.0, tank.getSlideFactor() - 0.05));
            if (tank.getSlideFactor() == 0.0) {
                tank.setSliding(false);
            }
        }

        // Adjust speed based on sliding factor
        int effectiveSpeed = tank.getSpeed();
        if (tank.isSliding()) {
            effectiveSpeed = (int)(tank.getSpeed() * (1.0 + 0.5 * tank.getSlideFactor()));
        }

        // Move tank with possibly adjusted speed - ONLY IN CARDINAL DIRECTIONS
        int dx = tank.getDirection().getDx() * effectiveSpeed;
        int dy = tank.getDirection().getDy() * effectiveSpeed;

        // Enforce cardinal direction movement - this is the critical fix for diagonal movement
        if (dx != 0 && dy != 0) {
            // In case both dx and dy are non-zero (shouldn't happen with fixed directions)
            // Choose the dominant direction
            if (Math.abs(dx) > Math.abs(dy)) {
                dy = 0; // Move horizontally only
            } else {
                dx = 0; // Move vertically only
            }

            if (debug && tank instanceof EnemyTank) {
                System.out.println("Fixed diagonal movement in enemy tank.");
            }
        }

        // Apply movement
        tank.setX(tank.getX() + dx);
        tank.setY(tank.getY() + dy);

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

        // Check environment collision - THIS IS THE CRITICAL FIX
        boolean collided = false;
        Rectangle tankBounds = tank.getBounds();
        for (Environment env : environments) {
            // Only check for collision with non-passable environments
            if (!env.isPassable() && env.getBounds().intersects(tankBounds)) {
                // For brick walls with sections, check more precise collision
                if (env instanceof BrickWall) {
                    BrickWall brickWall = (BrickWall)env;
                    // Check each corner of the tank against the brick wall sections
                    int[] tankCornerX = {tank.getX(), tank.getX() + tank.getSize() - 1,
                            tank.getX(), tank.getX() + tank.getSize() - 1};
                    int[] tankCornerY = {tank.getY(), tank.getY(),
                            tank.getY() + tank.getSize() - 1, tank.getY() + tank.getSize() - 1};

                    for (int i = 0; i < 4; i++) {
                        if (!brickWall.isPositionPassable(tankCornerX[i], tankCornerY[i])) {
                            collided = true;
                            break;
                        }
                    }
                } else if (env instanceof SteelWall) {
                    SteelWall steelWall = (SteelWall)env;
                    // Similar precise collision for steel walls
                    int[] tankCornerX = {tank.getX(), tank.getX() + tank.getSize() - 1,
                            tank.getX(), tank.getX() + tank.getSize() - 1};
                    int[] tankCornerY = {tank.getY(), tank.getY(),
                            tank.getY() + tank.getSize() - 1, tank.getY() + tank.getSize() - 1};

                    for (int i = 0; i < 4; i++) {
                        if (!steelWall.isPositionPassable(tankCornerX[i], tankCornerY[i])) {
                            collided = true;
                            break;
                        }
                    }
                } else {
                    // Standard collision for other environment types
                    collided = true;
                    break;
                }
            }
        }

        // If collision occurred with any non-passable environment, revert position
        if (collided) {
            tank.setX(oldX);
            tank.setY(oldY);
        }

        // Check tank-tank collision - keep separate to better track collision sources
        for (EnemyTank enemyTank : enemyTanks) {
            if (tank != enemyTank && tankBounds.intersects(enemyTank.getBounds())) {
                tank.setX(oldX);
                tank.setY(oldY);
                break;
            }
        }

        // Check player-player collision
        if (player1 != null && player2 != null && player1 != tank && player2 != tank) {
            if (tankBounds.intersects(player1.getBounds()) || tankBounds.intersects(player2.getBounds())) {
                tank.setX(oldX);
                tank.setY(oldY);
            }
        }

        // Additional check for player tanks with enemy tanks
        if (tank instanceof PlayerTank) {
            for (EnemyTank enemyTank : enemyTanks) {
                if (tankBounds.intersects(enemyTank.getBounds())) {
                    tank.setX(oldX);
                    tank.setY(oldY);
                    break;
                }
            }
        }
    }

    // Check if tank is on ice
    private boolean isOnIce(Tank tank) {
        Rectangle tankBounds = tank.getBounds();
        for (Environment env : environments) {
            if (env instanceof Ice && env.getBounds().intersects(tankBounds)) {
                return true;
            }
        }
        return false;
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

    // Check for bullet-to-bullet collisions
    private void checkBulletCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();

        // Compare each bullet with every other bullet
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet1 = bullets.get(i);

            // Skip if this bullet is already marked for removal
            if (bulletsToRemove.contains(bullet1)) continue;

            for (int j = i + 1; j < bullets.size(); j++) {
                Bullet bullet2 = bullets.get(j);

                // Skip if this bullet is already marked for removal
                if (bulletsToRemove.contains(bullet2)) continue;

                // Check if bullets are traveling in opposite directions
                boolean oppositeDirections =
                        (bullet1.getDirection() == Tank.Direction.UP && bullet2.getDirection() == Tank.Direction.DOWN) ||
                                (bullet1.getDirection() == Tank.Direction.DOWN && bullet2.getDirection() == Tank.Direction.UP) ||
                                (bullet1.getDirection() == Tank.Direction.LEFT && bullet2.getDirection() == Tank.Direction.RIGHT) ||
                                (bullet1.getDirection() == Tank.Direction.RIGHT && bullet2.getDirection() == Tank.Direction.LEFT);

                // Check if bullets are close enough to collide
                // Use a small collision radius for more accurate collision
                int collisionRadius = 10;
                boolean closeEnough =
                        Math.abs(bullet1.getX() - bullet2.getX()) < collisionRadius &&
                                Math.abs(bullet1.getY() - bullet2.getY()) < collisionRadius;

                if (oppositeDirections && closeEnough) {
                    // Mark both bullets for removal
                    bulletsToRemove.add(bullet1);
                    bulletsToRemove.add(bullet2);

                    // Add small explosion effect
                    addSmallExplosionEffect((bullet1.getX() + bullet2.getX()) / 2,
                            (bullet1.getY() + bullet2.getY()) / 2);

                    break; // Found a collision for bullet1, no need to check more
                }
            }
        }

        // Remove collided bullets
        bullets.removeAll(bulletsToRemove);
    }

    // Update visual effects
    private void updateEffects() {
        List<Effect> effectsToRemove = new ArrayList<>();
        for (Effect effect : effects) {
            effect.update();
            if (effect.isFinished()) {
                effectsToRemove.add(effect);
            }
        }
        effects.removeAll(effectsToRemove);
    }

    // Add explosion effect
    private void addExplosionEffect(int x, int y) {
        effects.add(new ExplosionEffect(x, y));
    }

    // Add small explosion effect for bullet collisions
    private void addSmallExplosionEffect(int x, int y) {
        effects.add(new SmallExplosionEffect(x, y));
    }

    // Check for collisions
    private void checkCollisions() {
        // Lists for removing objects after processing collisions
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Environment> environmentsToRemove = new ArrayList<>();

        // Process each bullet
        bulletLoop: for (Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();
            int bulletCenterX = bullet.getX();
            int bulletCenterY = bullet.getY();
            boolean bulletHitSolid = false;

            // Check if bullet hits environment objects
            for (Environment env : environments) {
                if (bulletBounds.intersects(env.getBounds())) {
                    // Trees and water don't affect bullets - SKIP them
                    if (env instanceof Trees || env instanceof Water) {
                        continue;  // Skip collision for trees and water
                    }

                    // Special handling for brick walls
                    if (env instanceof BrickWall) {
                        BrickWall brickWall = (BrickWall) env;
                        boolean destroyed = false;

                        // Use position-aware hit method for precise wall section destruction
                        destroyed = brickWall.hitByBulletWithPosition(
                                bullet.getDamage(), bulletCenterX, bulletCenterY);

                        // Check if the entire wall is destroyed
                        if (destroyed) {
                            environmentsToRemove.add(env);
                        }

                        // Important: We mark the bullet as hitting something solid
                        bulletHitSolid = true;

                        // For high power bullets (level 2+), allow penetration through brick
                        // For level 0-1, remove the bullet
                        if (bullet.getPowerLevel() < 2) {
                            bulletsToRemove.add(bullet);
                            continue bulletLoop;
                        }
                    }
                    // Steel walls and other environments
                    else if (env instanceof SteelWall) {
                        SteelWall steelWall = (SteelWall) env;
                        boolean destroyed = false;

                        // For steel walls, only max power bullets do damage
                        if (bullet.getPowerLevel() >= 3) {
                            destroyed = steelWall.hitByBulletWithPosition(
                                    bullet.getDamage(), bulletCenterX, bulletCenterY);

                            if (destroyed) {
                                environmentsToRemove.add(env);
                            }
                        }

                        // All bullets stop at steel walls regardless of power
                        bulletsToRemove.add(bullet);
                        continue bulletLoop;
                    } else {
                        // For other destructible environments
                        boolean destroyed = env.hitByBullet(bullet.getDamage());
                        if (destroyed) {
                            environmentsToRemove.add(env);
                        }

                        // All bullets stop at other environments
                        bulletsToRemove.add(bullet);
                        continue bulletLoop;
                    }
                }
            }

            // If bullet hit solid environment and is still being processed,
            // check for additional collisions (penetration case)

            // Check enemy tank collisions
            for (EnemyTank enemyTank : enemyTanks) {
                if (bulletBounds.intersects(enemyTank.getBounds())) {
                    bulletsToRemove.add(bullet);

                    // Check if tank is invulnerable (for newly spawned tanks)
                    if (!enemyTank.isInvulnerable()) {
                        boolean destroyed = enemyTank.takeDamage(bullet.getDamage());
                        if (destroyed && debug) {
                            System.out.println("Tank took " + bullet.getDamage() + " damage, health now: " + enemyTank.getHealth());
                        }
                    }
                    continue bulletLoop;
                }
            }

            // Check player tank collisions
            if (player1 != null && bulletBounds.intersects(player1.getBounds())) {
                bulletsToRemove.add(bullet);
                if (player1.takeDamage(bullet.getDamage())) {
                    // Add explosion effect
                    addExplosionEffect(player1.getX(), player1.getY());

                    // Player 1 tank destroyed
                    if (player1.loseLife()) {
                        player1 = null;
                    } else {
                        // Respawn
                        player1.setX(width / 2 - 2 * cellSize);
                        player1.setY(height - 2 * cellSize);
                        player1.setHealth(1);
                        player1.resetSliding(); // Reset sliding when respawning
                        game.updatePlayerLives(player1.getLives());
                    }
                }
                continue bulletLoop;
            }

            if (player2 != null && bulletBounds.intersects(player2.getBounds())) {
                bulletsToRemove.add(bullet);
                if (player2.takeDamage(bullet.getDamage())) {
                    // Add explosion effect
                    addExplosionEffect(player2.getX(), player2.getY());

                    // Player 2 tank destroyed
                    if (player2.loseLife()) {
                        player2 = null;
                    } else {
                        // Respawn
                        player2.setX(width / 2 + 2 * cellSize);
                        player2.setY(height - 2 * cellSize);
                        player2.setHealth(1);
                        player2.resetSliding(); // Reset sliding when respawning
                    }
                }
                continue bulletLoop;
            }

            // Check base collision
            Rectangle baseBounds = new Rectangle(
                    (int) baseLocation.getX(),
                    (int) baseLocation.getY(),
                    cellSize,
                    cellSize
            );

            if (bulletBounds.intersects(baseBounds)) {
                bulletsToRemove.add(bullet);
                baseDestroyed = true;

                // Add explosion effects for base
                addExplosionEffect((int)baseLocation.getX(), (int)baseLocation.getY());
                addExplosionEffect((int)baseLocation.getX() + cellSize/2, (int)baseLocation.getY() + cellSize/2);
            }
        }

        // Apply all removals
        bullets.removeAll(bulletsToRemove);
        environments.removeAll(environmentsToRemove);

        // Handle power-up collection
        List<PowerUp> powerUpsToRemove = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            // Check player 1
            if (player1 != null && powerUp.getBounds().intersects(player1.getBounds())) {
                powerUp.apply(player1);
                powerUpsToRemove.add(powerUp);
                game.addScore(powerUp.getPoints());
                handleSpecialPowerUp(powerUp);
            }
            // Check player 2
            else if (player2 != null && powerUp.getBounds().intersects(player2.getBounds())) {
                powerUp.apply(player2);
                powerUpsToRemove.add(powerUp);
                game.addScore(powerUp.getPoints());
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
                // Add explosion effect for each tank
                addExplosionEffect(enemyTank.getX(), enemyTank.getY());
            }
            int enemiesCount = enemyTanks.size();
            enemyTanks.clear();
            enemySpawnTimes.clear();
            enemiesDefeated += enemiesCount;

            if (debug) {
                System.out.println("Grenade power-up destroyed " + enemiesCount + " tanks");
            }
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
            // Convert base protection to steel walls temporarily
            List<Environment> baseWalls = new ArrayList<>();
            int baseX = (int) baseLocation.getX();
            int baseY = (int) baseLocation.getY();

            for (Environment env : environments) {
                if (env instanceof BrickWall) {
                    // Check if this is part of the base protection
                    int envX = env.getX();
                    int envY = env.getY();

                    if ((envX == baseX - cellSize && envY == baseY - cellSize) ||
                            (envX == baseX && envY == baseY - cellSize) ||
                            (envX == baseX + cellSize && envY == baseY - cellSize) ||
                            (envX == baseX - cellSize && envY == baseY) ||
                            (envX == baseX + cellSize && envY == baseY)) {

                        baseWalls.add(env);
                    }
                }
            }

            // Remove the brick walls
            environments.removeAll(baseWalls);

            // Add steel walls in their place
            environments.add(new SteelWall(baseX - cellSize, baseY - cellSize));
            environments.add(new SteelWall(baseX, baseY - cellSize));
            environments.add(new SteelWall(baseX + cellSize, baseY - cellSize));
            environments.add(new SteelWall(baseX - cellSize, baseY));
            environments.add(new SteelWall(baseX + cellSize, baseY));

            // Set up a timer to revert the walls after a duration
            Timer shovelTimer = new Timer(powerUpDuration, e -> {
                // Remove the steel walls
                List<Environment> steelWalls = new ArrayList<>();
                for (Environment env : environments) {
                    if (env instanceof SteelWall) {
                        int envX = env.getX();
                        int envY = env.getY();

                        if ((envX == baseX - cellSize && envY == baseY - cellSize) ||
                                (envX == baseX && envY == baseY - cellSize) ||
                                (envX == baseX + cellSize && envY == baseY - cellSize) ||
                                (envX == baseX - cellSize && envY == baseY) ||
                                (envX == baseX + cellSize && envY == baseY)) {

                            steelWalls.add(env);
                        }
                    }
                }
                environments.removeAll(steelWalls);

                // Add brick walls back
                environments.add(new BrickWall(baseX - cellSize, baseY - cellSize));
                environments.add(new BrickWall(baseX, baseY - cellSize));
                environments.add(new BrickWall(baseX + cellSize, baseY - cellSize));
                environments.add(new BrickWall(baseX - cellSize, baseY));
                environments.add(new BrickWall(baseX + cellSize, baseY));
            });
            shovelTimer.setRepeats(false);
            shovelTimer.start();
        }
    }

    // Spawn enemy if needed
    private void spawnEnemyIfNeeded() {
        long currentTime = System.currentTimeMillis();

        // Increase the number of enemies for larger map
        if (enemiesSpawned < totalEnemies &&
                enemyTanks.size() < maxEnemiesOnScreen &&
                currentTime - lastEnemySpawnTime > enemySpawnDelay) {

            // Select a random spawn point
            if (!enemySpawnPoints.isEmpty()) {
                int index = (int) (Math.random() * enemySpawnPoints.size());
                Point spawnPoint = enemySpawnPoints.get(index);

                // Check if spawn point is clear
                Rectangle spawnRect = new Rectangle(
                        (int)spawnPoint.getX(), (int)spawnPoint.getY(), cellSize, cellSize);

                boolean spawnClear = true;

                // Check for collisions with existing tanks
                for (EnemyTank tank : enemyTanks) {
                    if (tank.getBounds().intersects(spawnRect)) {
                        spawnClear = false;
                        break;
                    }
                }

                if (player1 != null && player1.getBounds().intersects(spawnRect)) {
                    spawnClear = false;
                }

                if (player2 != null && player2.getBounds().intersects(spawnRect)) {
                    spawnClear = false;
                }

                // Additional check for environment objects
                for (Environment env : environments) {
                    if (!env.isPassable() && env.getBounds().intersects(spawnRect)) {
                        spawnClear = false;
                        break;
                    }
                }

                // Check if any bullets are near this spawn point
                for (Bullet bullet : bullets) {
                    if (bullet.getBounds().intersects(spawnRect)) {
                        spawnClear = false;
                        break;
                    }
                }

                if (spawnClear) {
                    // Create a random enemy type
                    EnemyTank enemy = null;
                    double rand = Math.random();

                    // Adjust enemy type distribution based on level progression
                    double basicTankChance = 0.6 - (enemiesSpawned / (double)totalEnemies) * 0.4;
                    double fastTankChance = 0.2 + (enemiesSpawned / (double)totalEnemies) * 0.1;
                    double powerTankChance = 0.1 + (enemiesSpawned / (double)totalEnemies) * 0.1;
                    double armorTankChance = 0.1 + (enemiesSpawned / (double)totalEnemies) * 0.2;

                    if (rand < basicTankChance) {
                        enemy = new BasicTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                    } else if (rand < basicTankChance + fastTankChance) {
                        enemy = new FastTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                    } else if (rand < basicTankChance + fastTankChance + powerTankChance) {
                        enemy = new PowerTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                    } else {
                        enemy = new ArmorTank((int) spawnPoint.getX(), (int) spawnPoint.getY());
                    }

                    // Set some tanks to flash (for power-up generation)
                    if (enemiesSpawned == 3 || enemiesSpawned == 10 || enemiesSpawned == 17) {
                        enemy.setFlashing(true);
                    }

                    // Set initial invulnerability period - critical for preventing disappearing tanks
                    enemy.setInvulnerable(true);

                    // Track spawn time
                    enemySpawnTimes.put(enemy, currentTime);

                    if (debug) {
                        System.out.println("Spawned enemy tank: " + enemy.getClass().getSimpleName() +
                                " Health: " + enemy.getHealth() + ", Total spawned: " + enemiesSpawned);
                    }

                    enemyTanks.add(enemy);
                    enemiesSpawned++;
                    lastEnemySpawnTime = currentTime;
                }
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

        // Only add bullet if one was created (not in cooldown)
        if (bullet != null) {
            bullets.add(bullet);
        }
    }

    private Map<EnemyTank, Long> enemyFireCooldowns = new HashMap<>();

    // Determine when enemies should fire
    private boolean shouldEnemyFire(EnemyTank tank) {
        long currentTime = System.currentTimeMillis();

        // Don't fire if tank is invulnerable (newly spawned)
        if (tank.isInvulnerable()) {
            return false;
        }

        // Each tank type has a different cooldown period
        long cooldownPeriod;
        if (tank instanceof PowerTank) {
            cooldownPeriod = 2000; // 2 seconds for power tanks
        } else if (tank instanceof FastTank) {
            cooldownPeriod = 2500; // 2.5 seconds for fast tanks
        } else if (tank instanceof ArmorTank) {
            cooldownPeriod = 3000; // 3 seconds for armor tanks
        } else {
            cooldownPeriod = 3500; // 3.5 seconds for basic tanks
        }

        // Check if the tank is still in cooldown
        Long lastFireTime = enemyFireCooldowns.get(tank);
        if (lastFireTime != null && (currentTime - lastFireTime) < cooldownPeriod) {
            return false; // Still in cooldown
        }

        // Get AI state to determine firing probability
        int aiState = getEnemyAIState(tank);

        // Determine if the tank should fire based on alignment and state
        boolean shouldFire = false;

        if (aiState == 1 && isAlignedWithPlayer(tank)) {
            // In chase player state and aligned with player - high chance to fire
            shouldFire = Math.random() < 0.8;
        } else if (aiState == 2 && isAlignedWithBase(tank)) {
            // In attack base state and aligned with base - very high chance to fire
            shouldFire = Math.random() < 0.9;
        } else {
            // In patrol state or not aligned - low chance to fire
            shouldFire = Math.random() < 0.1;
        }

        // If deciding to fire, update the cooldown timestamp
        if (shouldFire) {
            enemyFireCooldowns.put(tank, currentTime);
        }

        return shouldFire;
    }


    // Helper method to get AI state using reflection (since it's private in EnemyTank)
    private int getEnemyAIState(EnemyTank tank) {
        try {
            java.lang.reflect.Field field = EnemyTank.class.getDeclaredField("aiState");
            field.setAccessible(true);
            return field.getInt(tank);
        } catch (Exception e) {
            return 0; // Default to patrol state if can't access
        }
    }

    // Helper method to check if enemy is aligned with a player
    private boolean isAlignedWithPlayer(EnemyTank tank) {
        // Check alignment with player 1
        if (player1 != null) {
            if (isAligned(tank, player1)) {
                return true;
            }
        }

        // Check alignment with player 2
        if (player2 != null) {
            if (isAligned(tank, player2)) {
                return true;
            }
        }

        return false;
    }

    // Helper method to check if two tanks are aligned for shooting
    private boolean isAligned(Tank shooter, Tank target) {
        // Check if in the same column (x position)
        boolean alignedX = Math.abs((shooter.getX() + shooter.getSize()/2) -
                (target.getX() + target.getSize()/2)) < shooter.getSize()/2;

        // Check if in the same row (y position)
        boolean alignedY = Math.abs((shooter.getY() + shooter.getSize()/2) -
                (target.getY() + target.getSize()/2)) < shooter.getSize()/2;

        // Check if there's a clear line of sight (no walls in between)
        if (alignedX) {
            // Vertically aligned - check if path is clear
            int startY = Math.min(shooter.getY(), target.getY());
            int endY = Math.max(shooter.getY() + shooter.getSize(), target.getY() + target.getSize());
            int x = shooter.getX() + shooter.getSize()/2;

            return isPathClear(x, x, startY, endY);
        }
        else if (alignedY) {
            // Horizontally aligned - check if path is clear
            int startX = Math.min(shooter.getX(), target.getX());
            int endX = Math.max(shooter.getX() + shooter.getSize(), target.getX() + target.getSize());
            int y = shooter.getY() + shooter.getSize()/2;

            return isPathClear(startX, endX, y, y);
        }

        return false;
    }

    // Helper method to check if aligned with base
    private boolean isAlignedWithBase(EnemyTank tank) {
        int baseX = (int)baseLocation.getX() + cellSize/2;
        int baseY = (int)baseLocation.getY() + cellSize/2;
        int tankCenterX = tank.getX() + tank.getSize()/2;
        int tankCenterY = tank.getY() + tank.getSize()/2;

        // Check if in the same column (x position)
        boolean alignedX = Math.abs(baseX - tankCenterX) < cellSize/2;

        // Check if in the same row (y position)
        boolean alignedY = Math.abs(baseY - tankCenterY) < cellSize/2;

        // Check for clear line of sight
        if (alignedX) {
            int startY = Math.min(tankCenterY, baseY);
            int endY = Math.max(tankCenterY, baseY);
            return isPathClear(tankCenterX, tankCenterX, startY, endY);
        }
        else if (alignedY) {
            int startX = Math.min(tankCenterX, baseX);
            int endX = Math.max(tankCenterX, baseX);
            return isPathClear(startX, endX, tankCenterY, tankCenterY);
        }

        return false;
    }

    // Helper method to check if path is clear (no walls in between)
    private boolean isPathClear(int startX, int endX, int startY, int endY) {
        // Check for any walls in the path
        for (Environment env : environments) {
            // Trees don't block line of sight
            if (!env.isPassable() && !(env instanceof Trees)) {
                Rectangle envBounds = env.getBounds();

                // Simple check if environment is in the path
                if (startX == endX) {
                    // Vertical path
                    if (envBounds.x <= startX && startX <= envBounds.x + envBounds.width &&
                            envBounds.y <= endY && startY <= envBounds.y + envBounds.height) {
                        return false;
                    }
                } else if (startY == endY) {
                    // Horizontal path
                    if (envBounds.y <= startY && startY <= envBounds.y + envBounds.height &&
                            envBounds.x <= endX && startX <= envBounds.x + envBounds.width) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Check if level is complete - FIXED to prevent premature completion
    public boolean isLevelComplete() {
        if (debug) {
            System.out.println("Level complete check: enemiesDefeated=" + enemiesDefeated +
                    ", totalEnemies=" + totalEnemies + ", enemyTanks.size=" + enemyTanks.size());
        }
        return enemiesDefeated >= totalEnemies && enemyTanks.isEmpty();
    }

    // Check if game is over
    public boolean isGameOver() {
        return (player1 == null && player2 == null) || baseDestroyed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background environments (except trees)
        for (Environment env : environments) {
            if (!(env instanceof Trees)) {
                env.draw(g);
            }
        }

        // Draw base (eagle)
        if (!baseDestroyed) {
            // Draw active eagle
            g.setColor(Color.DARK_GRAY);
            g.fillRect((int) baseLocation.getX() + 10, (int) baseLocation.getY() + 5, cellSize - 20, cellSize - 10);

            // Draw wings
            g.setColor(Color.GRAY);
            // Left wing
            int[] xPointsLeft = {
                    (int) baseLocation.getX() + 5,
                    (int) baseLocation.getX() + 15,
                    (int) baseLocation.getX() + 15
            };
            int[] yPointsLeft = {
                    (int) baseLocation.getY() + cellSize/2,
                    (int) baseLocation.getY() + 15,
                    (int) baseLocation.getY() + cellSize - 10
            };
            g.fillPolygon(xPointsLeft, yPointsLeft, 3);

            // Right wing
            int[] xPointsRight = {
                    (int) baseLocation.getX() + cellSize - 5,
                    (int) baseLocation.getX() + cellSize - 15,
                    (int) baseLocation.getX() + cellSize - 15
            };
            int[] yPointsRight = {
                    (int) baseLocation.getY() + cellSize/2,
                    (int) baseLocation.getY() + 15,
                    (int) baseLocation.getY() + cellSize - 10
            };
            g.fillPolygon(xPointsRight, yPointsRight, 3);
        } else {
            // Draw destroyed eagle
            g.setColor(Color.DARK_GRAY);
            g.fillRect((int) baseLocation.getX() + 5, (int) baseLocation.getY() + 5, cellSize - 10, cellSize - 10);

            // Draw X over destroyed eagle
            g.setColor(Color.RED);
            g.drawLine((int) baseLocation.getX() + 5, (int) baseLocation.getY() + 5,
                    (int) baseLocation.getX() + cellSize - 5, (int) baseLocation.getY() + cellSize - 5);
            g.drawLine((int) baseLocation.getX() + cellSize - 5, (int) baseLocation.getY() + 5,
                    (int) baseLocation.getX() + 5, (int) baseLocation.getY() + cellSize - 5);
        }

        // Draw base outline
        g.setColor(Color.GRAY);
        g.fillRect((int) baseLocation.getX(), (int) baseLocation.getY(), cellSize, cellSize);
        g.setColor(Color.WHITE);
        g.drawRect((int) baseLocation.getX(), (int) baseLocation.getY(), cellSize, cellSize);

        // Draw power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g);
        }

        // Draw effects behind tanks
        for (Effect effect : effects) {
            if (!effect.isInFront()) {
                effect.draw(g);
            }
        }

        // Draw all tanks (enemy tanks first, then player tanks to ensure players are on top)
        for (EnemyTank enemy : enemyTanks) {
            enemy.draw(g);

            // Draw invulnerability indicator if needed
            if (enemy.isInvulnerable()) {
                g.setColor(Color.WHITE);
                g.drawRect(enemy.getX() - 2, enemy.getY() - 2, enemy.getSize() + 4, enemy.getSize() + 4);
            }
        }

        if (player1 != null) {
            player1.draw(g);
        }

        if (player2 != null) {
            player2.draw(g);
        }

        // Draw trees AFTER tanks to ensure tanks appear to go "behind" trees
        for (Environment env : environments) {
            if (env instanceof Trees) {
                env.draw(g);
            }
        }

        // Draw bullets (always on top of everything except effects)
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        // Draw effects in front
        for (Effect effect : effects) {
            if (effect.isInFront()) {
                effect.draw(g);
            }
        }

        // Draw debug information if debug mode is enabled
        if (debug) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Enemies: " + enemyTanks.size() + "/" + totalEnemies, 10, 20);
            g.drawString("Spawned: " + enemiesSpawned, 10, 35);
            g.drawString("Defeated: " + enemiesDefeated, 10, 50);
            g.drawString("Bullets: " + bullets.size(), 10, 65);
            g.drawString("FPS: " + calculateFPS(), 10, 80);
            g.drawString("Effects: " + effects.size(), 10, 95);

            // Display recent destroyer log entries
            int yPos = 110;
            g.drawString("Recent tank destructions:", 10, yPos);
            yPos += 15;

            // Show the last 3 entries
            int startIdx = Math.max(0, enemyDestroyLog.size() - 3);
            for (int i = startIdx; i < enemyDestroyLog.size(); i++) {
                g.drawString(enemyDestroyLog.get(i), 10, yPos);
                yPos += 15;
            }

            // Draw AI state for debugging - limit to a few tanks to prevent crowding
            int maxTanksToShow = Math.min(3, enemyTanks.size());
            for (int i = 0; i < maxTanksToShow; i++) {
                EnemyTank enemy = enemyTanks.get(i);
                g.drawString("Tank " + i + ": " + enemy.getClass().getSimpleName() +
                                " Health: " + enemy.getHealth() +
                                " Action: " + enemy.getLastAction().substring(0, Math.min(10, enemy.getLastAction().length())),
                        10, yPos);
                yPos += 15;

                // Draw a line to show where the enemy is targeting
                g.setColor(Color.ORANGE);
                g.drawLine(
                        enemy.getX() + enemy.getSize()/2,
                        enemy.getY() + enemy.getSize()/2,
                        enemy.getX() + enemy.getSize()/2 + enemy.getDirection().getDx() * 80,
                        enemy.getY() + enemy.getSize()/2 + enemy.getDirection().getDy() * 80
                );
                g.setColor(Color.RED);
            }
        }
    }

    // Get AI state name for debugging
    private String getAIStateName(EnemyTank enemy) {
        try {
            java.lang.reflect.Field field = EnemyTank.class.getDeclaredField("aiState");
            field.setAccessible(true);
            int state = field.getInt(enemy);
            switch (state) {
                case 0: return "Patrol";
                case 1: return "Chase";
                case 2: return "Base";
                default: return "Unknown";
            }
        } catch (Exception e) {
            return "Error";
        }
    }

    // FPS calculation for debug mode
    private String calculateFPS() {
        long now = System.nanoTime();
        double deltaTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;
        fps = 0.9 * fps + 0.1 * (1.0 / deltaTime); // Smooth FPS
        return String.format("%.1f", fps);
    }

    // Toggle debug mode
    public void toggleDebug() {
        debug = !debug;
        // Also set debug in EnemyTank
        EnemyTank.setDebug(debug);

        System.out.println("Debug mode: " + debug);
        if (debug) {
            System.out.println("Enemy tanks: " + enemyTanks.size());
            System.out.println("Enemies spawned: " + enemiesSpawned);
            System.out.println("Enemies defeated: " + enemiesDefeated);
            System.out.println("Total enemies: " + totalEnemies);
        }
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

    // Visual effects for the game
    private abstract class Effect {
        protected int x, y;
        protected int lifetime;
        protected int age = 0;

        public Effect(int x, int y, int lifetime) {
            this.x = x;
            this.y = y;
            this.lifetime = lifetime;
        }

        public void update() {
            age++;
        }

        public boolean isFinished() {
            return age >= lifetime;
        }

        public abstract void draw(Graphics g);

        public boolean isInFront() {
            return false;
        }
    }

    // Explosion effect
    private class ExplosionEffect extends Effect {
        private static final int EXPLOSION_LIFETIME = 15;
        private Color[] colors = {Color.WHITE, Color.YELLOW, Color.ORANGE, Color.RED, Color.GRAY};

        public ExplosionEffect(int x, int y) {
            super(x, y, EXPLOSION_LIFETIME);
        }

        @Override
        public void draw(Graphics g) {
            Color originalColor = g.getColor();

            // Calculate explosion size based on age
            int size = (int)(cellSize * (1.0 - (double)age / lifetime));

            // Choose color based on age
            int colorIndex = Math.min((int)((double)age / lifetime * colors.length), colors.length - 1);
            g.setColor(colors[colorIndex]);

            // Draw explosion
            g.fillOval(x + cellSize/2 - size/2, y + cellSize/2 - size/2, size, size);

            g.setColor(originalColor);
        }

        @Override
        public boolean isInFront() {
            return true;
        }
    }

    // Small explosion effect for bullet collisions
    private class SmallExplosionEffect extends Effect {
        private static final int EXPLOSION_LIFETIME = 10;
        private Color[] colors = {Color.WHITE, Color.YELLOW, Color.ORANGE, Color.GRAY};

        public SmallExplosionEffect(int x, int y) {
            super(x, y, EXPLOSION_LIFETIME);
        }

        @Override
        public void draw(Graphics g) {
            Color originalColor = g.getColor();

            // Calculate explosion size based on age
            int size = (int)(cellSize/2 * (1.0 - (double)age / lifetime));

            // Choose color based on age
            int colorIndex = Math.min((int)((double)age / lifetime * colors.length), colors.length - 1);
            g.setColor(colors[colorIndex]);

            // Draw small explosion
            g.fillOval(x - size/2, y - size/2, size, size);

            g.setColor(originalColor);
        }

        @Override
        public boolean isInFront() {
            return true;
        }
    }
}