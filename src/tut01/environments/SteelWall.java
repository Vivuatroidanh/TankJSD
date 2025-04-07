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
        // Draw steel wall with a more authentic look
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, size, size);

        // Draw a grid pattern with highlights
        g.setColor(Color.WHITE);
        g.fillRect(x + 2, y + 2, size/2 - 4, size/2 - 4);
        g.fillRect(x + size/2 + 2, y + 2, size/2 - 4, size/2 - 4);
        g.fillRect(x + 2, y + size/2 + 2, size/2 - 4, size/2 - 4);
        g.fillRect(x + size/2 + 2, y + size/2 + 2, size/2 - 4, size/2 - 4);

        // Draw shadow effect
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, size - 1, size - 1);
        g.drawLine(x + size/2, y, x + size/2, y + size);
        g.drawLine(x, y + size/2, x + size, y + size/2);
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