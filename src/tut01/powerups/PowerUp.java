/*
 * Power-up implementations
 */
package tut01.powerups;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Base class for all power-ups
 */
public abstract class PowerUp {
    protected int x, y;
    protected boolean active = true;
    protected Color color;
    protected final int size = 30;
    protected final int points = 500; // All power-ups give 500 points

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        Color originalColor = g.getColor();

        // Draw power-up background
        g.setColor(Color.BLACK);
        g.fillRect(x, y, size, size);

        // Draw power-up icon
        g.setColor(color);
        drawIcon(g);

        g.setColor(originalColor);
    }

    // Draw the specific icon for each power-up
    protected abstract void drawIcon(Graphics g);

    // Apply the power-up effect
    public abstract void apply(tut01.tanks.PlayerTank tank);

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getPoints() { return points; }
}