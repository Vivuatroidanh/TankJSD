package tut01.tanks;

import java.awt.Color;

/**
 * Power Tank - Normal movement, fast bullets, low health
 */
public class PowerTank extends EnemyTank {
    public PowerTank(int x, int y) {
        super(
                x,
                y,
                3, // Normal movement
                4, // Fast bullets
                1, // Low health
                300 // Points when destroyed
        );
        this.color = new Color(255, 165, 0); // Orange
    }
}