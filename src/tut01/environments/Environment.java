/*
 * Environment implementations
 */
package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Base class for all environment elements
 */
public abstract class Environment {
    protected int x, y;
    protected final int size = 40; // Standard cell size
    protected boolean destructible;
    protected boolean passable;
    protected Color color;

    public Environment(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        Color originalColor = g.getColor();
        g.setColor(color);

        // Draw environment block
        drawBlock(g);

        g.setColor(originalColor);
    }

    // Method to draw specific block appearance
    protected abstract void drawBlock(Graphics g);

    // Method to handle bullet hits
    public boolean hitByBullet(int damage) {
        return false; // Default implementation does nothing
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }
    public boolean isDestructible() { return destructible; }
    public boolean isPassable() { return passable; }
}