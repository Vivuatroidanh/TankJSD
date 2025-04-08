package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import tut01.tanks.PlayerTank;

/**
 * Grenade power-up: Destroys all enemies on screen
 */
public class Grenade extends PowerUp {
    public Grenade(int x, int y) {
        super(x, y);
        this.color = Color.RED;
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a detailed grenade icon
        int centerX = x + size/2;
        int centerY = y + size/2;

        // Draw main body (oval)
        g.fillOval(x + 8, y + 16, size - 16, size - 20);

        // Draw cap on top
        g.fillRect(centerX - 4, y + 6, 8, 10);

        // Draw fuse
        g.setColor(Color.ORANGE);
        g.fillRect(centerX - 1, y + 3, 2, 4);

        // Draw explosion highlight
        g.setColor(Color.YELLOW);
        g.fillOval(centerX - 5, y + 20, 10, 10);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic to destroy all enemies
    }
}