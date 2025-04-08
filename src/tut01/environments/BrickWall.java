package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Brick Wall - Matches original Tank 1990 behavior with 4 separate destroyable sections
 */
public class BrickWall extends Environment {
    // 2x2 grid of brick sections (true = intact, false = destroyed)
    private boolean[][] sections;
    private int sectionsRemaining = 4;

    public BrickWall(int x, int y) {
        super(x, y);
        this.destructible = true;
        this.passable = false;
        this.color = new Color(205, 102, 29); // Brick red color matched to original

        // Initialize all sections as intact
        sections = new boolean[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                sections[i][j] = true;
            }
        }
    }

    @Override
    protected void drawBlock(Graphics g) {
        int halfSize = size / 2;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (sections[row][col]) {
                    // Only draw if the section exists
                    int sectionX = x + col * halfSize;
                    int sectionY = y + row * halfSize;

                    // Draw the brick section
                    g.setColor(color);
                    g.fillRect(sectionX, sectionY, halfSize, halfSize);

                    // Draw brick pattern lines
                    g.setColor(new Color(160, 82, 45)); // Darker brown for brick lines
                    g.drawLine(sectionX, sectionY + halfSize/2, sectionX + halfSize - 1, sectionY + halfSize/2);
                    g.drawLine(sectionX + halfSize/2, sectionY, sectionX + halfSize/2, sectionY + halfSize - 1);

                    // Draw edge highlighting
                    g.setColor(new Color(230, 150, 100)); // Lighter brick for edge highlight
                    g.drawLine(sectionX, sectionY, sectionX + halfSize - 1, sectionY); // Top
                    g.drawLine(sectionX, sectionY, sectionX, sectionY + halfSize - 1); // Left
                }
            }
        }
    }

    @Override
    public boolean hitByBullet(int damage) {
        return hitByBulletWithPosition(damage, x + size/2, y + size/2);
    }

    // Precise hit detection for brick walls - returns true if completely destroyed
    public boolean hitByBulletWithPosition(int damage, int bulletX, int bulletY) {
        if (!destructible) return false;

        // Calculate which section was hit
        int relativeX = bulletX - x;
        int relativeY = bulletY - y;

        // Get the section coordinates
        int col = relativeX < size/2 ? 0 : 1;
        int row = relativeY < size/2 ? 0 : 1;

        // Check if the section is already destroyed
        if (!sections[row][col]) {
            return false; // Already destroyed, nothing happens
        }

        // Destroy this section
        sections[row][col] = false;
        sectionsRemaining--;

        // Return true if all sections are destroyed
        return sectionsRemaining <= 0;
    }

    // Check if completely destroyed
    public boolean isCompletelyDestroyed() {
        return sectionsRemaining <= 0;
    }

    // Check if a specific position is passable (for more precise collision detection)
    public boolean isPositionPassable(int posX, int posY) {
        // Get relative position
        int relativeX = posX - x;
        int relativeY = posY - y;

        // Check bounds
        if (relativeX < 0 || relativeX >= size || relativeY < 0 || relativeY >= size) {
            return true; // Outside bounds
        }

        // Get section
        int col = relativeX < size/2 ? 0 : 1;
        int row = relativeY < size/2 ? 0 : 1;

        // Return true if section is destroyed (passable)
        return !sections[row][col];
    }

    // Getter for sections array - useful for testing
    public boolean[][] getSections() {
        return sections;
    }
}