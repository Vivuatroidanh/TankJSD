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
        // Draw a simple tank icon
        g.fillRect(x + 5, y + 10, size - 10, size - 15);
        g.fillRect(x + size/2 - 2, y + 5, 4, 5);
    }

    @Override
    public void apply(PlayerTank tank) {
        tank.addLife();
    }
}