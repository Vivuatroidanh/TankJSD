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
        // Draw a detailed helmet icon
        int centerX = x + size/2;

        // Draw helmet dome
        int[] xPointsTop = {
                x + 10, centerX, x + size - 10
        };
        int[] yPointsTop = {
                y + size - 15, y + 10, y + size - 15
        };
        g.fillPolygon(xPointsTop, yPointsTop, 3);

        // Draw helmet base/rim
        g.fillRect(x + 7, y + size - 15, size - 14, 8);

        // Draw helmet shine/highlight
        g.setColor(Color.WHITE);
        g.drawLine(centerX - 8, y + 15, centerX - 3, y + 12);
        g.drawLine(centerX - 3, y + 12, centerX + 2, y + 15);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic for temporary invulnerability
    }
}