package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import tut01.tanks.PlayerTank;

/**
 * Helmet power-up: Gives temporary invulnerability
 */
public class Helmet extends PowerUp {
    public Helmet(int x, int y) {
        super(x, y);
        this.color = Color.LIGHT_GRAY;
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a simple helmet icon
        int[] xPoints = {x + 5, x + size/2, x + size - 5};
        int[] yPoints = {y + size - 5, y + 5, y + size - 5};
        g.fillPolygon(xPoints, yPoints, 3);
        g.fillRect(x + 5, y + size - 10, size - 10, 5);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic for temporary invulnerability
    }
}