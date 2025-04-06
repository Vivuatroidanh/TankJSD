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
        this.color = Color.DARK_GRAY;
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a simple grenade icon
        g.fillOval(x + 5, y + 5, size - 10, size - 10);
        g.fillRect(x + size/2 - 2, y, 4, 5);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic to destroy all enemies
    }
}