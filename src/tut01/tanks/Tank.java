/*
 * Tank class hierarchy implementation
 */
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

        /**
         * Info panel class to display game information
         */
        class InfoPanel extends JPanel {
            private BattleCityGame game;
            private JLabel titleLabel;
            private JLabel scoreLabel;
            private JLabel levelLabel;
            private JLabel livesLabel;
            private JLabel timeLabel;
            private JTextArea instructionsArea;
            private JLabel pauseLabel;

            public InfoPanel(BattleCityGame game) {
                this.game = game;

                // Set panel properties
                setPreferredSize(new Dimension(200, 520));
                setBackground(Color.DARK_GRAY);
                setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                // Create components
                titleLabel = new JLabel("BATTLE CITY");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setForeground(Color.WHITE);
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                scoreLabel = new JLabel("Score: 0");
                scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                scoreLabel.setForeground(Color.WHITE);
                scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                levelLabel = new JLabel("Level: 1");
                levelLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                levelLabel.setForeground(Color.WHITE);
                levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                livesLabel = new JLabel("Lives: 3");
                livesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                livesLabel.setForeground(Color.WHITE);
                livesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                timeLabel = new JLabel("Time: 00:00");
                timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                timeLabel.setForeground(Color.WHITE);
                timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Instructions area
                instructionsArea = new JTextArea();
                instructionsArea.setEditable(false);
                instructionsArea.setLineWrap(true);
                instructionsArea.setWrapStyleWord(true);
                instructionsArea.setBackground(Color.DARK_GRAY);
                instructionsArea.setForeground(Color.LIGHT_GRAY);
                instructionsArea.setText(
                        "Controls:\n\n" +
                                "Player 1:\n" +
                                "Arrow Keys - Move\n" +
                                "Space - Fire\n\n" +
                                "Player 2:\n" +
                                "W,A,S,D - Move\n" +
                                "Q - Fire\n\n" +
                                "P - Pause Game\n" +
                                "ESC - Menu"
                );

                // Pause message
                pauseLabel = new JLabel("PAUSED");
                pauseLabel.setFont(new Font("Arial", Font.BOLD, 20));
                pauseLabel.setForeground(Color.RED);
                pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pauseLabel.setVisible(false);

                // Add components to panel
                add(titleLabel);
                add(Box.createRigidArea(new Dimension(0, 20)));
                add(scoreLabel);
                add(Box.createRigidArea(new Dimension(0, 5)));
                add(levelLabel);
                add(Box.createRigidArea(new Dimension(0, 5)));
                add(livesLabel);
                add(Box.createRigidArea(new Dimension(0, 5)));
                add(timeLabel);
                add(Box.createRigidArea(new Dimension(0, 20)));
                add(instructionsArea);
                add(Box.createVerticalGlue());
                add(pauseLabel);
            }

            // Update score display
            public void updateScore(int score) {
                scoreLabel.setText("Score: " + score);
            }

            // Update level display
            public void updateLevel(int level) {
                levelLabel.setText("Level: " + level);
            }

            // Update lives display
            public void updateLives(int lives) {
                livesLabel.setText("Lives: " + lives);
            }

            // Update time display
            public void updateTime(int seconds) {
                int minutes = seconds / 600; // 10 * 60 seconds (since timer fires every 100ms)
                int secs = (seconds / 10) % 60;
                DecimalFormat df = new DecimalFormat("00");
                timeLabel.setText("Time: " + df.format(minutes) + ":" + df.format(secs));
            }

            // Update all game info
            public void updateGameInfo() {
                // This would be called when game state changes significantly
            }

            // Show pause message
            public void showPauseMessage(String message) {
                pauseLabel.setText(message);
                pauseLabel.setVisible(true);
            }

            // Hide pause message
            public void hidePauseMessage() {
                pauseLabel.setVisible(false);
            }
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
        } else {
            // Enemy tank drawing (existing code with slight improvements)
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
        }

        g.setColor(originalColor);
    }

    // Get tank bounds for collision detection
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
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

