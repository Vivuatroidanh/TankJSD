package tut01.tanks;

import java.awt.Color;

/**
 * Basic Tank - Slow movement, slow bullets, low health
 */
public class BasicTank extends EnemyTank {
    public BasicTank(int x, int y) {
        super(
                x,
                y,
                1, // Slow movement
                1, // Slow bullets
                1, // Low health
                100 // Points when destroyed
        );
        this.color = Color.LIGHT_GRAY;
    }
}