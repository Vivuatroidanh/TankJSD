package tut01.tanks;

import java.awt.Color;

/**
 * Fast Tank - Fast movement, normal bullets, low health
 */
public class FastTank extends EnemyTank {
    public FastTank(int x, int y) {
        super(
                x,
                y,
                3, // Fast movement
                2, // Normal bullets
                1, // Low health
                200 // Points when destroyed
        );
        this.color = new Color(173, 216, 230); // Light blue
    }
}