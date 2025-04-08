package tut01.tanks;

import tut01.environments.Environment;
import tut01.environments.BrickWall;
import tut01.environments.SteelWall;
import tut01.environments.Water;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;

/**
 * Base class for all enemy tanks with improved AI
 */
public abstract class EnemyTank extends Tank {
    protected boolean isFlashing;
    protected int aiState = 0; // 0 = patrol, 1 = chase player, 2 = attack base
    protected long lastStateChange = 0;
    protected long lastDirectionChange = 0;
    protected long lastFireTime = 0;
    protected int stuckCounter = 0;
    protected int previousX = 0;
    protected int previousY = 0;
    protected Point targetPosition = null;
    protected List<Point> pathWaypoints = new ArrayList<>();
    protected int currentWaypointIndex = 0;
    protected boolean pathFollowing = false;

    // Added fields for invulnerability
    protected boolean isInvulnerable = false;
    protected long invulnerableUntil = 0;
    protected static final long INVULNERABLE_DURATION = 3000; // 3 seconds of invulnerability

    // Debugging support
    protected static boolean debug = false;
    protected String lastAction = "Created";

    // Constants for AI behavior
    private static final long STATE_CHANGE_DELAY = 5000; // 5 seconds between state changes
    private static final long DIRECTION_CHANGE_DELAY = 2000; // 2 seconds between random direction changes
    private static final long FIRE_COOLDOWN = 1000; // 1 second between firing attempts
    private static final int STUCK_THRESHOLD = 10; // After 10 updates of no movement, consider stuck
    private static final double FIRE_CHANCE_BASE = 0.05; // Base chance to fire when in patrol state
    private static final double FIRE_CHANCE_CHASE = 0.15; // Higher chance when chasing player
    private static final double FIRE_CHANCE_BASE_ATTACK = 0.25; // Highest chance when attacking base
    private static final int PATH_FINDING_DISTANCE = 200; // Distance threshold for pathfinding

    public EnemyTank(int x, int y, int speed, int bulletSpeed, int health, int points) {
        super(x, y, speed, bulletSpeed, health, points);
        this.isFlashing = false;
        this.previousX = x;
        this.previousY = y;

        if (debug) {
            System.out.println("Created enemy tank at (" + x + "," + y + ") with health " + health);
        }
    }

    public void setFlashing(boolean flashing) {
        this.isFlashing = flashing;
    }

    public boolean isFlashing() {
        return isFlashing;
    }

    // Methods for invulnerability
    public boolean isInvulnerable() {
        return this.isInvulnerable;
    }

    public void checkInvulnerabilityExpired() {
        // Check if invulnerability has expired
        if (isInvulnerable && System.currentTimeMillis() > invulnerableUntil) {
            isInvulnerable = false;
            if (debug) {
                System.out.println("Tank invulnerability expired");
            }
        }
    }

    public void setInvulnerable(boolean invulnerable) {
        this.isInvulnerable = invulnerable;
        if (invulnerable) {
            this.invulnerableUntil = System.currentTimeMillis() + INVULNERABLE_DURATION;
            if (debug) {
                System.out.println("Tank is now invulnerable until " + invulnerableUntil);
            }
        }
    }

    // Override takeDamage to implement invulnerability
    @Override
    public boolean takeDamage(int damage) {
        // Don't take damage while invulnerable
        if (isInvulnerable()) {
            if (debug) {
                System.out.println("Tank is invulnerable, ignoring damage");
            }
            return false;
        }

        // Apply damage and track it
        int oldHealth = this.health;
        boolean destroyed = super.takeDamage(damage);

        if (debug) {
            System.out.println("Tank took " + damage + " damage. Health: " + oldHealth + " -> " + this.health);
            if (destroyed) {
                System.out.println("Tank was destroyed by damage!");
            }
        }

        return destroyed;
    }

    // Advanced AI update method - takes player locations and base location as parameters
    public void updateAI(PlayerTank player1, PlayerTank player2, Point baseLocation, List<Environment> environments) {
        long currentTime = System.currentTimeMillis();

        // Check invulnerability first
        checkInvulnerabilityExpired();

        // Detect if tank is stuck
        if (Math.abs(x - previousX) < 2 && Math.abs(y - previousY) < 2) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
        }

        // Remember current position for next update
        previousX = x;
        previousY = y;

        // If stuck for too long, change direction or try to avoid obstacle
        if (stuckCounter > STUCK_THRESHOLD) {
            handleStuckSituation(environments);
            stuckCounter = 0;
        }

        // Periodically change AI state
        if (currentTime - lastStateChange > STATE_CHANGE_DELAY) {
            // Adapt state selection based on tank type
            double stateRandom = Math.random();

            // Power tanks and fast tanks are more aggressive toward players
            if (this instanceof PowerTank || this instanceof FastTank) {
                if (stateRandom < 0.3) {
                    aiState = 0; // Patrol
                } else if (stateRandom < 0.8) {
                    aiState = 1; // Chase player
                } else {
                    aiState = 2; // Target base
                }
            }
            // Armor tanks prioritize base destruction
            else if (this instanceof ArmorTank) {
                if (stateRandom < 0.3) {
                    aiState = 0; // Patrol
                } else if (stateRandom < 0.5) {
                    aiState = 1; // Chase player
                } else {
                    aiState = 2; // Target base
                }
            }
            // Basic tanks are more evenly distributed
            else {
                if (stateRandom < 0.5) {
                    aiState = 0; // Patrol
                } else if (stateRandom < 0.8) {
                    aiState = 1; // Chase player
                } else {
                    aiState = 2; // Target base
                }
            }

            lastStateChange = currentTime;
            pathFollowing = false; // Reset path following when changing states

            if (debug) {
                String[] stateNames = {"Patrol", "Chase", "AttackBase"};
                System.out.println("Tank changed state to: " + stateNames[aiState]);
            }
        }

        // Execute behavior based on current state
        switch (aiState) {
            case 0: // Patrol mode - move randomly, occasionally fire
                if (currentTime - lastDirectionChange > DIRECTION_CHANGE_DELAY) {
                    changeToRandomDirection();
                    lastDirectionChange = currentTime;
                    lastAction = "Random direction change";
                }

                // Random chance to fire in patrol mode
                if (currentTime - lastFireTime > FIRE_COOLDOWN && Math.random() < FIRE_CHANCE_BASE) {
                    fire();
                    lastFireTime = currentTime;
                }
                break;

            case 1: // Chase player mode - target nearest player
                PlayerTank target = choosePlayerTarget(player1, player2);
                if (target != null) {
                    // Determine if we should use pathfinding
                    double distanceToTarget = calculateDistance(target);

                    if (distanceToTarget <= PATH_FINDING_DISTANCE) {
                        // If close enough, use direct targeting
                        moveTowardTarget(target.getX(), target.getY(), environments);
                        lastAction = "Moving toward player";

                        // Check if player is aligned (horizontally or vertically)
                        if (isAligned(target)) {
                            // Higher chance to fire when aligned with player
                            if (currentTime - lastFireTime > FIRE_COOLDOWN && Math.random() < FIRE_CHANCE_CHASE) {
                                fire();
                                lastFireTime = currentTime;
                            }
                        }
                    } else {
                        // For distant targets, use intermittent path recalculation
                        if (!pathFollowing || pathWaypoints.isEmpty()) {
                            // Calculate path to target
                            calculatePathTo(target.getX(), target.getY(), environments);
                            pathFollowing = true;
                            lastAction = "Calculated path to player";
                        }
                        followPath();
                    }
                } else {
                    // No players? Fall back to patrol mode
                    aiState = 0;
                    lastAction = "No players, falling back to patrol";
                }
                break;

            case 2: // Target base mode - move toward base
                if (baseLocation != null) {
                    double distanceToBase = Math.sqrt(
                            Math.pow(baseLocation.getX() - this.x, 2) +
                                    Math.pow(baseLocation.getY() - this.y, 2));

                    if (distanceToBase <= PATH_FINDING_DISTANCE) {
                        // Direct approach to base when close
                        moveTowardTarget((int)baseLocation.getX(), (int)baseLocation.getY(), environments);
                        lastAction = "Moving toward base";
                    } else {
                        // Path finding for longer distances
                        if (!pathFollowing || pathWaypoints.isEmpty()) {
                            calculatePathTo((int)baseLocation.getX(), (int)baseLocation.getY(), environments);
                            pathFollowing = true;
                            lastAction = "Calculated path to base";
                        }
                        followPath();
                    }

                    // Check if aligned with base for firing
                    if (isAlignedWithBase(baseLocation)) {
                        // High chance to fire when targeting base
                        if (currentTime - lastFireTime > FIRE_COOLDOWN && Math.random() < FIRE_CHANCE_BASE_ATTACK) {
                            fire();
                            lastFireTime = currentTime;
                        }
                    }
                } else {
                    // No base? Fall back to patrol mode
                    aiState = 0;
                    lastAction = "No base, falling back to patrol";
                }
                break;
        }

        // Just set the direction and moving flag, let the game engine handle actual movement
        // This prevents diagonal movement and ensures tanks move in cardinal directions only
        setMoving(true);
    }

    // Choose the best player to target
    private PlayerTank choosePlayerTarget(PlayerTank player1, PlayerTank player2) {
        // If only one player exists, return that one
        if (player1 == null && player2 == null) return null;
        if (player1 == null) return player2;
        if (player2 == null) return player1;

        // Otherwise compare distances
        double dist1 = calculateDistance(player1);
        double dist2 = calculateDistance(player2);

        // Target the closer player
        return (dist1 <= dist2) ? player1 : player2;
    }

    // Handle stuck situations
    private void handleStuckSituation(List<Environment> environments) {
        // First, try to find a clear direction to move
        for (Direction dir : Direction.values()) {
            // Skip current direction and opposite
            if (dir == getDirection() || isOppositeDirection(getDirection(), dir)) {
                continue;
            }

            // Check if direction is clear
            if (isDirectionClear(dir, environments)) {
                setDirection(dir);
                lastAction = "Unstuck: Found clear direction";
                return;
            }
        }

        // If no clear direction, just pick a random one
        changeToRandomDirection();
        lastAction = "Unstuck: Random direction";
    }

    // Check if a direction is clear of obstacles
    private boolean isDirectionClear(Direction dir, List<Environment> environments) {
        // Calculate new position in that direction
        int newX = x + dir.getDx() * speed * 2;
        int newY = y + dir.getDy() * speed * 2;

        // Create a test rectangle
        Rectangle testBounds = new Rectangle(newX, newY, size, size);

        // Check for collisions with impassable objects
        for (Environment env : environments) {
            if (!env.isPassable() && env.getBounds().intersects(testBounds)) {
                return false;
            }
        }

        return true;
    }

    // Check if target is aligned horizontally or vertically with this tank
    private boolean isAligned(Tank target) {
        // Check if in the same column (x position)
        boolean alignedX = Math.abs(target.getX() + target.getSize()/2 - (this.x + this.size/2)) < size/2;

        // Check if in the same row (y position)
        boolean alignedY = Math.abs(target.getY() + target.getSize()/2 - (this.y + this.size/2)) < size/2;

        if (alignedX) {
            // Set direction to face the target vertically
            if (target.getY() < this.y) {
                setDirection(Direction.UP);
            } else {
                setDirection(Direction.DOWN);
            }
            return true;
        } else if (alignedY) {
            // Set direction to face the target horizontally
            if (target.getX() < this.x) {
                setDirection(Direction.LEFT);
            } else {
                setDirection(Direction.RIGHT);
            }
            return true;
        }

        return false;
    }

    // Check if aligned with base for shooting
    private boolean isAlignedWithBase(Point baseLocation) {
        int baseX = (int)baseLocation.getX() + size/2;
        int baseY = (int)baseLocation.getY() + size/2;
        int tankCenterX = this.x + this.size/2;
        int tankCenterY = this.y + this.size/2;

        // Check if in the same column (x position)
        boolean alignedX = Math.abs(baseX - tankCenterX) < size/2;

        // Check if in the same row (y position)
        boolean alignedY = Math.abs(baseY - tankCenterY) < size/2;

        if (alignedX) {
            // Set direction to face the base vertically
            setDirection(baseY < tankCenterY ? Direction.UP : Direction.DOWN);
            return true;
        } else if (alignedY) {
            // Set direction to face the base horizontally
            setDirection(baseX < tankCenterX ? Direction.LEFT : Direction.RIGHT);
            return true;
        }

        return false;
    }

    // Calculate distance to target
    private double calculateDistance(Tank target) {
        return Math.sqrt(Math.pow(target.getX() - this.x, 2) + Math.pow(target.getY() - this.y, 2));
    }

    // Basic path finding to target
    private void calculatePathTo(int targetX, int targetY, List<Environment> environments) {
        // Simple waypoint generation - we'll create a few intermediate waypoints
        pathWaypoints.clear();

        // Start with direct path
        pathWaypoints.add(new Point(targetX, targetY));

        // Find obstacles in the direct path and create alternative waypoints
        Rectangle directPath = createPathRectangle(this.x + size/2, this.y + size/2, targetX + size/2, targetY + size/2);

        boolean hasObstacle = false;
        for (Environment env : environments) {
            if (!env.isPassable() && directPath.intersects(env.getBounds())) {
                hasObstacle = true;
                break;
            }
        }

        if (hasObstacle) {
            // Add intermediate waypoints to navigate around obstacles
            int midX = (this.x + targetX) / 2;
            int midY = (this.y + targetY) / 2;

            // Try different potential waypoints
            Point[] potentialWaypoints = {
                    new Point(midX + 100, midY),
                    new Point(midX - 100, midY),
                    new Point(midX, midY + 100),
                    new Point(midX, midY - 100)
            };

            for (Point waypoint : potentialWaypoints) {
                // Check if path to this waypoint is clear
                Rectangle pathToWaypoint = createPathRectangle(
                        this.x + size/2, this.y + size/2,
                        waypoint.x, waypoint.y
                );

                boolean waypointClear = true;
                for (Environment env : environments) {
                    if (!env.isPassable() && pathToWaypoint.intersects(env.getBounds())) {
                        waypointClear = false;
                        break;
                    }
                }

                if (waypointClear) {
                    // Insert this waypoint before the target
                    pathWaypoints.add(0, waypoint);
                    break;
                }
            }
        }

        currentWaypointIndex = 0;
    }

    // Create a rectangle representing a path between two points
    private Rectangle createPathRectangle(int x1, int y1, int x2, int y2) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int width = Math.abs(x2 - x1);
        int height = Math.abs(y2 - y1);

        // Ensure minimum dimensions
        width = Math.max(width, 10);
        height = Math.max(height, 10);

        return new Rectangle(minX, minY, width, height);
    }

    // Follow the calculated path
    private void followPath() {
        if (pathWaypoints.isEmpty() || currentWaypointIndex >= pathWaypoints.size()) {
            pathFollowing = false;
            lastAction = "Path following ended";
            return;
        }

        Point currentTarget = pathWaypoints.get(currentWaypointIndex);
        moveTowardTarget(currentTarget.x, currentTarget.y, null);
        lastAction = "Following path";

        // Check if we've reached the current waypoint
        double distToWaypoint = Math.sqrt(
                Math.pow(currentTarget.x - (this.x + size/2), 2) +
                        Math.pow(currentTarget.y - (this.y + size/2), 2)
        );

        if (distToWaypoint < size) {
            currentWaypointIndex++;
            if (debug) {
                System.out.println("Tank reached waypoint, moving to next one");
            }
        }
    }

    // Move toward a target position with improved obstacle avoidance
    private void moveTowardTarget(int targetX, int targetY, List<Environment> environments) {
        int tankCenterX = this.x + size/2;
        int tankCenterY = this.y + size/2;

        // Calculate distances to target on each axis
        int distX = targetX - tankCenterX;
        int distY = targetY - tankCenterY;

        // Determine if we should move horizontally or vertically
        boolean moveHorizontally;

        // Handle exact alignment cases first for more precise movement
        if (Math.abs(distX) < size/4) {
            // Very close horizontally, move vertically
            moveHorizontally = false;
        } else if (Math.abs(distY) < size/4) {
            // Very close vertically, move horizontally
            moveHorizontally = true;
        } else {
            // Otherwise, decide based on which distance is greater
            // This creates a more direct path while still using only cardinal directions
            moveHorizontally = Math.abs(distX) > Math.abs(distY);

            // If there are environments to check, see if there's an obstacle in the way
            if (environments != null) {
                Direction horizontalDir = distX < 0 ? Direction.LEFT : Direction.RIGHT;
                Direction verticalDir = distY < 0 ? Direction.UP : Direction.DOWN;

                boolean horizontalClear = isDirectionClear(horizontalDir, environments);
                boolean verticalClear = isDirectionClear(verticalDir, environments);

                // If one direction is clear and the other isn't, choose the clear one
                if (horizontalClear && !verticalClear) {
                    moveHorizontally = true;
                } else if (!horizontalClear && verticalClear) {
                    moveHorizontally = false;
                }
            }
        }

        // Set direction based on decision - using ONLY cardinal directions
        if (moveHorizontally) {
            // Move horizontally toward target
            if (distX < 0) {
                setDirection(Direction.LEFT);
            } else {
                setDirection(Direction.RIGHT);
            }
        } else {
            // Move vertically toward target
            if (distY < 0) {
                setDirection(Direction.UP);
            } else {
                setDirection(Direction.DOWN);
            }
        }
    }

    // Change to a random direction, but avoid reversing direction which can cause getting stuck
    private void changeToRandomDirection() {
        Direction currentDir = getDirection();
        Direction newDir;

        do {
            int dirValue = (int)(Math.random() * 4);
            Direction[] directions = Direction.values();
            newDir = directions[dirValue];

            // Don't pick the exact opposite direction (to avoid getting stuck)
        } while (isOppositeDirection(currentDir, newDir));

        setDirection(newDir);

        if (debug) {
            System.out.println("Tank changed direction to: " + newDir);
        }
    }

    // Check if two directions are opposites
    private boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
                (dir1 == Direction.DOWN && dir2 == Direction.UP) ||
                (dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
                (dir1 == Direction.RIGHT && dir2 == Direction.LEFT);
    }

    // Enable debug mode
    public static void setDebug(boolean debugMode) {
        debug = debugMode;
    }

    // Get last action for debugging
    public String getLastAction() {
        return lastAction;
    }
}