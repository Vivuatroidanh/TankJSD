package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Steel Wall - Very strong, only destroyed by max power bullets
 */
public class SteelWall extends Environment {
    private int health = 2; // Takes 2 max power hits to destroy

    public SteelWall(int x, int y) {
        super(x, y);
        this.destructible = true;
        this.passable = false;
        this.color = Color.GRAY;
    }

    @Override
    protected void drawBlock(Graphics g) {
        g.fillRect(x, y, size, size);

        // Add some detail to make it look like steel
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x + 5, y + 5, size/2 - 5, size/2 - 5);
        g.fillRect(x + size/2 + 5, y + 5, size/2 - 10, size/2 - 5);
        g.fillRect(x + 5, y + size/2 + 5, size/2 - 5, size/2 - 10);
        g.fillRect(x + size/2 + 5, y + size/2 + 5, size/2 - 10, size/2 - 10);
    }

    @Override
    public boolean hitByBullet(int damage) {
        // Only max power bullets (level 3) can damage steel walls
        if (destructible && damage > 3) {
            health -= 1;
            return health <= 0; // Return true if destroyed
        }
        return false;
    }
}