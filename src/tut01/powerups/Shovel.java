package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import tut01.tanks.PlayerTank;

/**
 * Shovel power-up: Strengthens base walls
 */
public class Shovel extends PowerUp {
    public Shovel(int x, int y) {
        super(x, y);
        this.color = new Color(139, 69, 19); // Brown
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a simple shovel icon
        g.fillRect(x + size/2 - 2, y + 5, 4, size - 10);
        g.fillRect(x + size/2 - 5, y + size - 10, 10, 5);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic to strengthen walls
    }
}
