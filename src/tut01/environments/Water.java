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
        g.fillRect(x, y, size, size);

        // Add wave details
        g.setColor(new Color(0, 191, 255)); // Light blue
        for (int i = 0; i < 3; i++) {
            g.drawArc(x + (i * 15), y + 10, 10, 8, 0, 180);
            g.drawArc(x + 5 + (i * 15), y + 20, 10, 8, 0, 180);
            g.drawArc(x + (i * 15), y + 30, 10, 8, 0, 180);
        }
    }

    public boolean canBulletPass() {
        return true; // Bullets can pass through water
    }
}