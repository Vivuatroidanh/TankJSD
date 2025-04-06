package gui.game;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Info panel class to display game information
 */
public class InfoPanel extends JPanel {
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