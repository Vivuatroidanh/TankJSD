package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Brick Wall - Matches original Tank 1990 behavior
 */
public class BrickWall extends Environment {
    // 2x2 grid of brick sections
    private boolean[][] sections;
    private int numSections = 4;

    public BrickWall(int x, int y) {
        super(x, y);
        this.destructible = true;
        this.passable = false;
        this.color = new Color(165, 42, 42); // Brick red

        // Initialize all sections as intact
        sections = new boolean[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                sections[i][j] = true; // Section is intact
            }
        }
    }

    @Override
    protected void drawBlock(Graphics g) {
        // Draw brick sections individually based on their state
        int halfSize = size / 2;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (sections[row][col]) {
                    // Only draw if the section exists
                    int sectionX = x + col * halfSize;
                    int sectionY = y + row * halfSize;

                    // Draw the brick section
                    g.setColor(new Color(205, 102, 29)); // Darker brick red like original game
                    g.fillRect(sectionX, sectionY, halfSize, halfSize);

                    // Draw brick pattern lines (simplified from original game)
                    g.setColor(Color.BLACK);
                    g.drawLine(sectionX, sectionY + halfSize/2, sectionX + halfSize - 1, sectionY + halfSize/2);
                    g.drawLine(sectionX + halfSize/2, sectionY, sectionX + halfSize/2, sectionY + halfSize - 1);
                }
            }
        }
    }

    @Override
    public boolean hitByBullet(int damage) {
        if (!destructible) return false;

        // Default implementation - destroy a random section
        int row = (int)(Math.random() * 2);
        int col = (int)(Math.random() * 2);

        // If the chosen section is already destroyed, find another
        if (!sections[row][col]) {
            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 2; c++) {
                    if (sections[r][c]) {
                        row = r;
                        col = c;
                        break;
                    }
                }
            }
        }

        // Destroy the section
        if (sections[row][col]) {
            sections[row][col] = false;
            numSections--;
        }

        // Return true if all sections are destroyed
        return numSections <= 0;
    }

    // More accurate method taking bullet position
    public boolean hitByBulletWithPosition(int damage, int bulletX, int bulletY) {
        if (!destructible) return false;

        // Get bullet position relative to this wall
        int relativeX = bulletX - x;
        int relativeY = bulletY - y;

        // Calculate which sections to destroy based on bullet position
        boolean destroyed = false;

        // Get the primary section hit
        int col = relativeX < size/2 ? 0 : 1;
        int row = relativeY < size/2 ? 0 : 1;

        // Check if bullet hit near a boundary between sections (original game behavior)
        boolean nearHorizontalBorder = Math.abs(relativeX - size/2) < 3;
        boolean nearVerticalBorder = Math.abs(relativeY - size/2) < 3;

        // First destroy the main section that was hit
        if (sections[row][col]) {
            sections[row][col] = false;
            numSections--;
            destroyed = true;
        }

        // If near horizontal border between sections, destroy adjacent section
        if (nearHorizontalBorder) {
            int otherCol = 1 - col; // Toggle 0->1 or 1->0
            if (sections[row][otherCol]) {
                sections[row][otherCol] = false;
                numSections--;
                destroyed = true;
            }
        }

        // If near vertical border between sections, destroy adjacent section
        if (nearVerticalBorder) {
            int otherRow = 1 - row; // Toggle 0->1 or 1->0
            if (sections[otherRow][col]) {
                sections[otherRow][col] = false;
                numSections--;
                destroyed = true;
            }
        }

        // Return true if at least one section was destroyed
        return destroyed;
    }

    // Utility method to check if the wall is completely destroyed
    public boolean isCompletelyDestroyed() {
        return numSections <= 0;
    }

    // Getter for sections - useful for testing
    public boolean[][] getSections() {
        return sections;
    }
}