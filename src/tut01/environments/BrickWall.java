package tut01.environments;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Brick Wall - Can be destroyed by bullets
 */
public class BrickWall extends Environment {
    private int health = 4; // Takes 4 hits to destroy

    public BrickWall(int x, int y) {
        super(x, y);
        this.destructible = true;
        this.passable = false;
        this.color = new Color(165, 42, 42); // Brown
    }

    @Override
    protected void drawBlock(Graphics g) {
        int brickWidth = size / 4;
        int brickHeight = size / 4;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((i + j) % 2 == 0) {
                    g.fillRect(x + i * brickWidth, y + j * brickHeight, brickWidth, brickHeight);
                }
            }
        }
    }

    @Override
    public boolean hitByBullet(int damage) {
        if (destructible) {
            health -= damage;
            return health <= 0; // Return true if destroyed
        }
        return false;
    }
}