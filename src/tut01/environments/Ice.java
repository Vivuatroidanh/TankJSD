package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Ice - Tanks slide on it
 */
public class Ice extends Environment {
    public Ice(int x, int y) {
        super(x, y);
        this.destructible = false;
        this.passable = true;
        this.color = new Color(200, 200, 255); // Light blue/white
    }

    @Override
    protected void drawBlock(Graphics g) {
        g.fillRect(x, y, size, size);

        // Add ice crystal details
        g.setColor(Color.WHITE);
        g.drawLine(x + 10, y + 10, x + size - 10, y + size - 10);
        g.drawLine(x + size - 10, y + 10, x + 10, y + size - 10);
        g.drawLine(x + size/2, y + 5, x + size/2, y + size - 5);
        g.drawLine(x + 5, y + size/2, x + size - 5, y + size/2);
    }
}