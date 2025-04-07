package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Brick Wall - Can be destroyed by bullets
 */
public class BrickWall extends Environment {
    private int health = 4; // Takes 4 hits to destroy

    public BrickWall(int x, int y) {
        super(x, y);
        this.destructible = true;
        this.passable = false;
        this.color = new Color(165, 42, 42); // Brown
    }

    @Override
    protected void drawBlock(Graphics g) {
        // Draw a more authentic brick wall pattern
        g.setColor(new Color(165, 42, 42)); // Brick red
        g.fillRect(x, y, size, size);

        // Draw brick pattern
        g.setColor(Color.BLACK);
        // Horizontal lines
        g.fillRect(x, y + size/4, size, 2);
        g.fillRect(x, y + size/2, size, 2);
        g.fillRect(x, y + 3*size/4, size, 2);

        // Vertical lines - offset for odd/even rows
        for (int i = 0; i < 2; i++) {
            g.fillRect(x + size/4, y + i*size/2, 2, size/4);
            g.fillRect(x + size/2, y + i*size/2, 2, size/4);
            g.fillRect(x + 3*size/4, y + i*size/2, 2, size/4);

            g.fillRect(x, y + size/4 + i*size/2, 2, size/4);
            g.fillRect(x + size/4 - 2, y + size/4 + i*size/2, 2, size/4);
            g.fillRect(x + size/2 - 2, y + size/4 + i*size/2, 2, size/4);
            g.fillRect(x + 3*size/4 - 2, y + size/4 + i*size/2, 2, size/4);
            g.fillRect(x + size - 2, y + size/4 + i*size/2, 2, size/4);
        }
    }

    @Override
    public boolean hitByBullet(int damage) {
        if (destructible) {
            health -= damage;
            return health <= 0; // Return true if destroyed
        }
        return false;
    }
}