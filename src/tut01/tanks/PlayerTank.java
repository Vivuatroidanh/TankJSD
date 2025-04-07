/*
 * Player Tank implementation
 */
package tut01.tanks;

import java.awt.*;

/**
 * PlayerTank represents the tanks controlled by players
 */
public class PlayerTank extends Tank {
    private final int playerNumber;
    private int lives = 3;
    private int powerLevel = 0; // Default power level
    // PlayerTank.java - Add cooldown logic
    private long lastFireTime = 0;
    private static final long FIRE_COOLDOWN = 500; // 0.5 seconds in milliseconds
    private boolean wantsToFire = false;


    public PlayerTank(int x, int y, int playerNumber) {
        super(
                x,
                y,
                4,                  // Default movement speed
                5,                  // Default bullet speed
                1,                  // Initial health
                0                   // Player tanks don't give points when destroyed
        );
        this.playerNumber = playerNumber;

        // Set color based on player number
        if (playerNumber == 1) {
            this.color = Color.YELLOW;
        } else {
            this.color = Color.GREEN;
        }
    }

    public void setWantsToFire(boolean wantsToFire) {
        this.wantsToFire = wantsToFire;
    }

    public boolean wantsToFire() {
        return this.wantsToFire;
    }

    @Override
    public Bullet fire() {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since last bullet was fired
        if (currentTime - lastFireTime < FIRE_COOLDOWN) {
            return null; // Still in cooldown, can't fire
        }

        // Update last fire time
        lastFireTime = currentTime;

        // Create and return the bullet
        Bullet bullet = super.fire();
        if (bullet != null) {
            bullet.setPowerLevel(powerLevel);
        }
        return bullet;
    }

    public void upgrade() {
        if (powerLevel < 3) {
            powerLevel++;
        }
    }

    public void addLife() {
        lives++;
    }

    public boolean loseLife() {
        lives--;
        if (lives <= 0) {
            return true; // Game over
        }
        // Reset power level when losing a life
        powerLevel = 0;
        return false;
    }

    public int getLives() {
        return lives;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public int getPowerLevel() {
        return powerLevel;
    }
}