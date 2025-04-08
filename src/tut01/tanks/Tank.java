package tut01.tanks;

import gui.game.BattleCityGame;

import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.text.DecimalFormat;

/**
 * Base Tank class that represents common properties and behaviors of all tanks
 */
public abstract class Tank {
    // Common properties for all tanks
    protected int x, y;            // Position
    protected int speed;           // Movement speed
    protected int bulletSpeed;     // Bullet speed
    protected int health;          // Health points
    protected int points;          // Points value when destroyed
    protected Direction direction; // Current direction
    protected Color color;         // Tank color
    protected boolean isMoving;    // Movement status
    protected int size = 40;       // Default tank size (pixels)

    // Added for ice sliding effect
    protected boolean sliding = false;
    protected double slideFactor = 0.0; // 0.0 - 1.0, where 1.0 is full slide effect

    // Enum for direction
    public enum Direction {
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        LEFT(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() { return dx; }
        public int getDy() { return dy; }
    }

    // Constructor
    public Tank(int x, int y, int speed, int bulletSpeed, int health, int points) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.bulletSpeed = bulletSpeed;
        this.health = health;
        this.points = points;
        this.direction = Direction.UP;
        this.isMoving = false;
    }

    // Basic movement
    public void move() {
        if (isMoving) {
            int newX = x + (direction.getDx() * speed);
            int newY = y + (direction.getDy() * speed);

            // Set new position
            x = newX;
            y = newY;
        }
    }

    // Fire bullet
    public Bullet fire() {
        // Calculate bullet's starting position at the front of the tank
        int bulletX = x + (size / 2);
        int bulletY = y + (size / 2);

        // Adjust position based on direction
        switch (direction) {
            case UP:
                bulletY -= size / 2;
                break;
            case RIGHT:
                bulletX += size / 2;
                break;
            case DOWN:
                bulletY += size / 2;
                break;
            case LEFT:
                bulletX -= size / 2;
                break;
        }

        return new Bullet(bulletX, bulletY, bulletSpeed, direction);
    }

    // Take damage
    public boolean takeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    // Draw tank
    public void draw(Graphics g) {
        Color originalColor = g.getColor();

        // Draw different tank based on type
        if (this instanceof PlayerTank) {
            PlayerTank playerTank = (PlayerTank) this;

            // Base color based on player number
            if (playerTank.getPlayerNumber() == 1) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.GREEN);
            }

            // Draw tank body
            g.fillRect(x + 4, y + 4, size - 8, size - 8);

            // Draw tracks
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, 4, size);
            g.fillRect(x + size - 4, y, 4, size);

            // Draw turret based on direction
            g.setColor(Color.BLACK);
            int turretWidth = 6;
            int turretLength = 14;

            switch (direction) {
                case UP:
                    g.fillRect(x + (size/2) - (turretWidth/2), y - turretLength + 4, turretWidth, turretLength);
                    break;
                case RIGHT:
                    g.fillRect(x + size - 4, y + (size/2) - (turretWidth/2), turretLength, turretWidth);
                    break;
                case DOWN:
                    g.fillRect(x + (size/2) - (turretWidth/2), y + size - 4, turretWidth, turretLength);
                    break;
                case LEFT:
                    g.fillRect(x - turretLength + 4, y + (size/2) - (turretWidth/2), turretLength, turretWidth);
                    break;
            }

            // Draw sliding indicator if applicable
            if (sliding) {
                g.setColor(new Color(100, 200, 255, (int)(100 * slideFactor)));
                g.drawRect(x - 1, y - 1, size + 2, size + 2);
            }
        } else {
            // Enemy tank drawing
            g.setColor(color);
            g.fillRect(x + 2, y + 2, size - 4, size - 4);

            // Draw tracks
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, 4, size);
            g.fillRect(x + size - 4, y, 4, size);

            // Draw turret
            g.setColor(Color.BLACK);
            int turretWidth = 6;
            int turretLength = 12;

            switch (direction) {
                case UP:
                    g.fillRect(x + (size/2) - (turretWidth/2), y - turretLength + 6, turretWidth, turretLength);
                    break;
                case RIGHT:
                    g.fillRect(x + size - 6, y + (size/2) - (turretWidth/2), turretLength, turretWidth);
                    break;
                case DOWN:
                    g.fillRect(x + (size/2) - (turretWidth/2), y + size - 6, turretWidth, turretLength);
                    break;
                case LEFT:
                    g.fillRect(x - turretLength + 6, y + (size/2) - (turretWidth/2), turretLength, turretWidth);
                    break;
            }

            // Draw sliding indicator if applicable
            if (sliding) {
                g.setColor(new Color(100, 200, 255, (int)(100 * slideFactor)));
                g.drawRect(x - 1, y - 1, size + 2, size + 2);
            }
        }

        g.setColor(originalColor);
    }

    // Get tank bounds for collision detection
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    // Methods for sliding effect
    public boolean isSliding() {
        return sliding;
    }

    public void setSliding(boolean sliding) {
        this.sliding = sliding;
    }

    public double getSlideFactor() {
        return slideFactor;
    }

    public void setSlideFactor(double factor) {
        this.slideFactor = factor;
    }

    public void resetSliding() {
        this.sliding = false;
        this.slideFactor = 0.0;
    }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public int getBulletSpeed() { return bulletSpeed; }
    public void setBulletSpeed(int bulletSpeed) { this.bulletSpeed = bulletSpeed; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getPoints() { return points; }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public boolean isMoving() { return isMoving; }
    public void setMoving(boolean moving) { isMoving = moving; }

    public int getSize() { return size; }
}