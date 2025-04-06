package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import tut01.tanks.PlayerTank;

/**
 * Timer power-up: Freezes enemies temporarily
 * Renamed from Timer to TimerPowerUp to avoid ambiguity with javax.swing.Timer
 */
public class TimerPowerUp extends PowerUp {
    public TimerPowerUp(int x, int y) {
        super(x, y);
        this.color = Color.WHITE;
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a simple clock icon
        g.drawOval(x + 5, y + 5, size - 10, size - 10);
        g.drawLine(x + size/2, y + size/2, x + size/2, y + 10);
        g.drawLine(x + size/2, y + size/2, x + size - 10, y + size/2);
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic to freeze enemies
    }
}