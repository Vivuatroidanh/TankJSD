package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import tut01.tanks.PlayerTank;

/**
 * Star power-up: Increases tank's offensive power
 */
public class Star extends PowerUp {
    public Star(int x, int y) {
        super(x, y);
        this.color = Color.YELLOW;
    }

    @Override
    protected void drawIcon(Graphics g) {
        // Draw a simple star icon
        int centerX = x + size/2;
        int centerY = y + size/2;
        int outerRadius = size/2 - 5;
        int innerRadius = outerRadius / 2;

        int numPoints = 5;
        int[] xPoints = new int[numPoints * 2];
        int[] yPoints = new int[numPoints * 2];

        for (int i = 0; i < numPoints * 2; i++) {
            double angle = Math.PI / 2 + i * Math.PI / numPoints;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + (int)(radius * Math.cos(angle));
            yPoints[i] = centerY - (int)(radius * Math.sin(angle));
        }

        g.fillPolygon(xPoints, yPoints, numPoints * 2);
    }

    @Override
    public void apply(PlayerTank tank) {
        tank.upgrade();
    }
}
