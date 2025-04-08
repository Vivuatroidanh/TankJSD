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
        this.passable = true; // Ensure this is true so tanks can move through
        this.color = new Color(0, 100, 0); // Dark green
    }

    @Override
    protected void drawBlock(Graphics g) {
        // Draw a more authentic trees pattern
        g.setColor(new Color(0, 100, 0)); // Dark green
        g.fillRect(x, y, size, size);

        // Add texture to trees
        g.setColor(new Color(34, 139, 34)); // Forest green
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((i + j) % 2 == 0) {
                    g.fillRect(x + i*size/4, y + j*size/4, size/4, size/4);
                }
            }
        }
    }
}