package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Steel Wall - Very strong, needs max power bullets to damage
 */
public class SteelWall extends Environment {
    // 2x2 grid of steel sections (true = intact, false = destroyed)
    private boolean[][] sections;
    private int sectionsRemaining = 4;

    public SteelWall(int x, int y) {
        super(x, y);
        this.destructible = true;
        this.passable = false;
        this.color = new Color(192, 192, 192); // Steel gray

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

                    // Draw the steel section
                    g.setColor(color);
                    g.fillRect(sectionX, sectionY, halfSize, halfSize);

                    // Draw inner highlights
                    g.setColor(Color.WHITE);
                    g.fillRect(sectionX + 2, sectionY + 2, halfSize/2 - 2, halfSize/2 - 2);

                    // Draw edge shading
                    g.setColor(Color.DARK_GRAY);
                    g.drawLine(sectionX + halfSize - 1, sectionY, sectionX + halfSize - 1, sectionY + halfSize - 1); // Right
                    g.drawLine(sectionX, sectionY + halfSize - 1, sectionX + halfSize - 1, sectionY + halfSize - 1); // Bottom
                }
            }
        }
    }

    @Override
    public boolean hitByBullet(int damage) {
        return hitByBulletWithPosition(damage, x + size/2, y + size/2);
    }

    // Only max level bullets (power level >= 3) can damage steel walls
    public boolean hitByBulletWithPosition(int damage, int bulletX, int bulletY) {
        if (!destructible || damage < 3) return false; // Only max power bullets can damage steel

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
}