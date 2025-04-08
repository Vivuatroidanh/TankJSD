package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import tut01.tanks.PlayerTank;

/**
 * Tank power-up: Gives an extra life
 * Renamed from Tank to TankPowerUp to avoid ambiguity with tut01.tanks.Tank
 */
public class TankPowerUp extends PowerUp {
    public TankPowerUp(int x, int y) {
        super(x, y);
        this.color = Color.GREEN;
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a detailed tank icon
        int tankWidth = size - 16;
        int tankHeight = size - 20;

        // Tank body
        g.fillRect(x + 8, y + 18, tankWidth, tankHeight);

        // Tank turret
        g.fillRect(x + size/2 - 3, y + 10, 6, 10);

        // Tank cannon
        g.fillRect(x + size/2 - 2, y + 6, 4, 4);

        // Tank tracks
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x + 6, y + 18, 4, tankHeight);
        g.fillRect(x + size - 10, y + 18, 4, tankHeight);

        // Highlight
        g.setColor(Color.WHITE);
        g.drawLine(x + 10, y + 22, x + size/2 - 4, y + 22);
    }

    @Override
    public void apply(PlayerTank tank) {
        tank.addLife();
    }
}