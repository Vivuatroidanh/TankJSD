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

        // Draw a more detailed bullet with a tail
        // Main bullet
        g.fillOval(x - size/2, y - size/2, size, size);

        // Draw a tail based on direction (motion blur effect)
        int tailLength = 3 + powerLevel * 2; // Longer tail for more powerful bullets

        int tailX = x - direction.getDx() * tailLength;
        int tailY = y - direction.getDy() * tailLength;

        // Draw tail as a line
        g.drawLine(x, y, tailX, tailY);

        // For higher power levels, add a glow effect
        if (powerLevel > 0) {
            g.setColor(new Color(255, 255, 200, 100)); // Transparent yellow glow
            g.fillOval(x - size/2 - 2, y - size/2 - 2, size + 4, size + 4);
        }

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
    public Tank.Direction getDirection() { return direction; }
    public int getPowerLevel() { return powerLevel; }
    public void setPowerLevel(int powerLevel) { this.powerLevel = powerLevel; }

    public int getDamage() {
        return powerLevel + 1; // Base damage plus power level
    }
}