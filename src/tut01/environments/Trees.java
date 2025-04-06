package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Trees - Tanks and bullets can pass through, but tanks are hidden
 */
public class Trees extends Environment {
    public Trees(int x, int y) {
        super(x, y);
        this.destructible = false;
        this.passable = true;
        this.color = new Color(0, 100, 0); // Dark green
    }

    @Override
    protected void drawBlock(Graphics g) {
        // Draw multiple tree circles to create a forest look
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                g.fillOval(x + i * (size/3), y + j * (size/3), size/2, size/2);
            }
        }
    }
}