/*
 * Enemy Tank implementations
 */
package tut01.tanks;

import java.awt.Color;

/**
 * Base class for all enemy tanks
 */
public abstract class EnemyTank extends Tank {
    protected boolean isFlashing;

    public EnemyTank(int x, int y, int speed, int bulletSpeed, int health, int points) {
        super(x, y, speed, bulletSpeed, health, points);
        this.isFlashing = false;
    }

    public void setFlashing(boolean flashing) {
        this.isFlashing = flashing;
    }

    public boolean isFlashing() {
        return isFlashing;
    }

    // AI movement (basic random movement)
    public void updateAI() {
        // Randomly change direction occasionally
        if (Math.random() < 0.02) {
            int dirValue = (int)(Math.random() * 4);
            Tank.Direction[] directions = Tank.Direction.values();
            setDirection(directions[dirValue]);
        }

        // Randomly decide to move or stop
        if (Math.random() < 0.01) {
            setMoving(!isMoving());
        }

        // Randomly fire
        if (Math.random() < 0.01) {
            fire();
        }
    }
}