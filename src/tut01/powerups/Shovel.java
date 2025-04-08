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
        // Draw a detailed shovel icon
        int centerX = x + size/2;

        // Draw the handle
        g.fillRect(centerX - 2, y + 6, 4, size - 18);

        // Draw the blade
        int[] xPointsBlade = {
                centerX - 10, centerX + 10, centerX + 8, centerX - 8
        };
        int[] yPointsBlade = {
                y + size - 12, y + size - 12, y + size - 6, y + size - 6
        };
        g.fillPolygon(xPointsBlade, yPointsBlade, 4);

        // Draw metallic highlight
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(centerX - 8, y + size - 9, centerX + 8, y + size - 9);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic to strengthen walls
    }
}