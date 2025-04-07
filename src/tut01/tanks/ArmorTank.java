package tut01.tanks;

import java.awt.Color;

/**
 * Armor Tank - Normal movement, normal bullets, high health
 */
public class ArmorTank extends EnemyTank {
    public ArmorTank(int x, int y) {
        super(
                x,
                y,
                2, // Normal movement
                3, // Normal bullets
                4, // High health
                400 // Points when destroyed
        );
        this.color = new Color(0, 128, 0); // Dark green
    }
}