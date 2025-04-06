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

    public PlayerTank(int x, int y, int playerNumber) {
        super(
                x,
                y,
                2,                  // Default movement speed
                3,                  // Default bullet speed
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

    @Override
    public Bullet fire() {
        Bullet bullet = super.fire();
        bullet.setPowerLevel(powerLevel);
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



































