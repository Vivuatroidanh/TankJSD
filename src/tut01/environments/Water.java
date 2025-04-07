package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Water - Bullets can pass through but tanks cannot
 */
public class Water extends Environment {
    public Water(int x, int y) {
        super(x, y);
        this.destructible = false;
        this.passable = false; // Tanks cannot pass
        this.color = new Color(0, 0, 139); // Dark blue
    }

    @Override
    protected void drawBlock(Graphics g) {
        // Draw a more authentic water pattern
        g.setColor(new Color(30, 144, 255)); // Dodger blue
        g.fillRect(x, y, size, size);

        // Add wave pattern
        g.setColor(new Color(65, 105, 225)); // Royal blue
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((i + j) % 2 == 0) {
                    g.fillRect(x + i*size/4, y + j*size/4, size/4, size/4);
                }
            }
        }
    }

    public boolean canBulletPass() {
        return true; // Bullets can pass through water
    }
}