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
        // Draw a detailed timer/clock icon
        int centerX = x + size/2;
        int centerY = y + size/2;
        int radius = size/2 - 8;

        // Clock face
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Clock center
        g.fillOval(centerX - 2, centerY - 2, 4, 4);

        // Clock hands
        g.drawLine(centerX, centerY, centerX, centerY - radius + 4); // Hour hand
        g.drawLine(centerX, centerY, centerX + radius - 6, centerY); // Minute hand

        // Clock markings
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            int markLength = (i % 3 == 0) ? 5 : 3;
            int x1 = centerX + (int)((radius - markLength) * Math.sin(angle));
            int y1 = centerY - (int)((radius - markLength) * Math.cos(angle));
            int x2 = centerX + (int)(radius * Math.sin(angle));
            int y2 = centerY - (int)(radius * Math.cos(angle));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    @Override
    public void apply(PlayerTank tank) {
        // Effect is handled by the game logic to freeze enemies
    }
}