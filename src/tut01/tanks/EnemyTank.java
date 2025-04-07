package tut01.tanks;

import tut01.environments.Environment;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

/**
 * Base class for all enemy tanks with improved AI
 */
public abstract class EnemyTank extends Tank {
    protected boolean isFlashing;
    protected int aiState = 0; // 0 = patrol, 1 = chase player, 2 = attack base
    protected long lastStateChange = 0;
    protected long lastDirectionChange = 0;
    protected int lastAIDecision = 0;
    protected int stuckCounter = 0;
    protected int previousX = 0;
    protected int previousY = 0;

    // Constants for AI behavior
    private static final long STATE_CHANGE_DELAY = 5000; // 5 seconds between state changes
    private static final long DIRECTION_CHANGE_DELAY = 2000; // 2 seconds between random direction changes
    private static final int STUCK_THRESHOLD = 5; // After 5 updates of no movement, consider stuck
    private static final double FIRE_CHANCE_BASE = 0.03; // Base chance to fire when in patrol state
    private static final double FIRE_CHANCE_CHASE = 0.10; // Higher chance when chasing player

    public EnemyTank(int x, int y, int speed, int bulletSpeed, int health, int points) {
        super(x, y, speed, bulletSpeed, health, points);
        this.isFlashing = false;
        this.previousX = x;
        this.previousY = y;
    }

    public void setFlashing(boolean flashing) {
        this.isFlashing = flashing;
    }

    public boolean isFlashing() {
        return isFlashing;
    }

    // Advanced AI update method - takes player locations and base location as parameters
    public void updateAI(PlayerTank player1, PlayerTank player2, Point baseLocation, List<Environment> environments) {
        long currentTime = System.currentTimeMillis();

        // Detect if tank is stuck
        if (Math.abs(x - previousX) < 2 && Math.abs(y - previousY) < 2) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
        }

        // Remember current position for next update
        previousX = x;
        previousY = y;

        // If stuck for too long, change direction
        if (stuckCounter > STUCK_THRESHOLD) {
            changeToRandomDirection();
            stuckCounter = 0;
        }

        // Periodically change AI state
        if (currentTime - lastStateChange > STATE_CHANGE_DELAY) {
            // Randomly switch between patrol and chase states, with small chance to target base
            double stateRandom = Math.random();
            if (stateRandom < 0.6) {
                aiState = 0; // Patrol
            } else if (stateRandom < 0.9) {
                aiState = 1; // Chase player
            } else {
                aiState = 2; // Target base
            }
            lastStateChange = currentTime;
        }

        // Execute behavior based on current state
        switch (aiState) {
            case 0: // Patrol mode - move randomly, occasionally fire
                if (currentTime - lastDirectionChange > DIRECTION_CHANGE_DELAY) {
                    changeToRandomDirection();
                    lastDirectionChange = currentTime;
                }

                // Random chance to fire in patrol mode
                if (Math.random() < FIRE_CHANCE_BASE) {
                    fire();
                }
                break;

            case 1: // Chase player mode - target nearest player
                PlayerTank target = player1;

                // Find which player is closer (if both exist)
                if (player1 != null && player2 != null) {
                    double dist1 = calculateDistance(player1);
                    double dist2 = calculateDistance(player2);
                    target = (dist1 <= dist2) ? player1 : player2;
                } else if (player1 == null && player2 != null) {
                    target = player2;
                } else if (player1 == null) {
                    // No players? Fall back to patrol mode
                    aiState = 0;
                    break;
                }

                // Move toward target
                moveToward(target.getX(), target.getY());

                // Higher chance to fire when chasing player
                if (Math.random() < FIRE_CHANCE_CHASE) {
                    // Check if player is aligned (horizontally or vertically)
                    if (isAligned(target)) {
                        fire(); // Fire if player is in line of sight
                    }
                }
                break;

            case 2: // Target base mode - move toward base
                if (baseLocation != null) {
                    moveToward((int)baseLocation.getX(), (int)baseLocation.getY());

                    // High chance to fire when targeting base
                    if (Math.random() < FIRE_CHANCE_CHASE * 1.5) {
                        fire();
                    }
                } else {
                    // No base? Fall back to patrol mode
                    aiState = 0;
                }
                break;
        }

        // Always set to moving to avoid getting stuck
        setMoving(true);
    }

    // Check if target is aligned horizontally or vertically with this tank
    private boolean isAligned(Tank target) {
        // Check if in the same column (x position)
        boolean alignedX = Math.abs(target.getX() - this.x) < 10;

        // Check if in the same row (y position)
        boolean alignedY = Math.abs(target.getY() - this.y) < 10;

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

    // Calculate distance to target
    private double calculateDistance(Tank target) {
        return Math.sqrt(Math.pow(target.getX() - this.x, 2) + Math.pow(target.getY() - this.y, 2));
    }

    // Move toward a target position
    private void moveToward(int targetX, int targetY) {
        // Determine if we should move horizontally or vertically
        boolean moveHorizontally = Math.random() < 0.5;

        // If we're already aligned on one axis, move on the other
        if (Math.abs(targetX - this.x) < 10) {
            moveHorizontally = false; // Already aligned horizontally, so move vertically
        } else if (Math.abs(targetY - this.y) < 10) {
            moveHorizontally = true; // Already aligned vertically, so move horizontally
        }

        if (moveHorizontally) {
            // Move horizontally toward target
            if (targetX < this.x) {
                setDirection(Direction.LEFT);
            } else {
                setDirection(Direction.RIGHT);
            }
        } else {
            // Move vertically toward target
            if (targetY < this.y) {
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
    }

    // Check if two directions are opposites
    private boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
                (dir1 == Direction.DOWN && dir2 == Direction.UP) ||
                (dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
                (dir1 == Direction.RIGHT && dir2 == Direction.LEFT);
    }
}