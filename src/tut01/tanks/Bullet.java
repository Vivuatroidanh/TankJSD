package tut01.tanks;

import java.awt.*;

/**
 * Bullet class for tank projectiles
 */
public class Bullet {
    private int x, y;
    private int speed;
    private Tank.Direction direction;
    private boolean active = true;
    private int powerLevel = 0;
    private final int size = 6;

    public Bullet(int x, int y, int speed, Tank.Direction direction) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
    }

    public void move() {
        x += direction.getDx() * speed;
        y += direction.getDy() * speed;
    }

    public void draw(Graphics g) {
        Color originalColor = g.getColor();

        // Set color based on power level
        switch (powerLevel) {
            case 0:
                g.setColor(Color.WHITE);
                break;
            case 1:
                g.setColor(Color.YELLOW);
                break;
            case 2:
                g.setColor(Color.ORANGE);
                break;
            case 3:
                g.setColor(Color.RED);
                break;
            default:
                g.setColor(Color.WHITE);
        }

        // Larger bullet size for better visibility at higher speeds
        g.fillOval(x - size/2, y - size/2, size, size);

        g.setColor(originalColor);
    }

    public Rectangle getBounds() {
        return new Rectangle(x - size/2, y - size/2, size, size);
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getPowerLevel() { return powerLevel; }
    public void setPowerLevel(int powerLevel) { this.powerLevel = powerLevel; }

    public int getDamage() {
        return powerLevel + 1; // Base damage plus power level
    }
}
