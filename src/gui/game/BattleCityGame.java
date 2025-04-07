package gui.game;

import tut01.tanks.*;
import tut01.powerups.*;
import tut01.environments.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.text.DecimalFormat;

/**
 * Main game class that controls the Battle City game
 */
public class BattleCityGame extends JFrame {
    private GamePanel gamePanel;
    private InfoPanel infoPanel;
    private boolean gameRunning = false;
    private boolean gamePaused = false;
    private javax.swing.Timer gameTimer;
    private int gameTime = 0; // Time in seconds
    private int score = 0;
    private int level = 1;
    private int playerLives = 3;

    public BattleCityGame() {
        setTitle("Battle City (Tank 1990)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create game components
        gamePanel = new GamePanel(this);
        infoPanel = new InfoPanel(this);

        // Set up layout
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);

        // Adjust the preferred size of info panel to match taller game panel
        infoPanel.setPreferredSize(new Dimension(200, 640));

        // Create menu
        setupMenu();

        // Add keyboard controls
        setupKeyboardControls();
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New Game");
        JMenuItem pauseItem = new JMenuItem("Pause");
        JMenuItem exitItem = new JMenuItem("Exit");

        newGameItem.addActionListener(e -> startNewGame());
        pauseItem.addActionListener(e -> togglePause());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(pauseItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // Options menu
        JMenu optionsMenu = new JMenu("Options");
        JMenuItem soundItem = new JMenuItem("Sound Settings");
        JMenuItem difficultyItem = new JMenuItem("Difficulty");
        JMenuItem debugItem = new JMenuItem("Toggle Debug Mode");

        soundItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Sound settings not implemented yet.", "Sound Settings", JOptionPane.INFORMATION_MESSAGE));
        difficultyItem.addActionListener(e -> showDifficultyDialog());
        debugItem.addActionListener(e -> gamePanel.toggleDebug());

        optionsMenu.add(soundItem);
        optionsMenu.add(difficultyItem);
        optionsMenu.add(debugItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem instructionsItem = new JMenuItem("Instructions");
        JMenuItem aboutItem = new JMenuItem("About");

        instructionsItem.addActionListener(e -> showInstructions());
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(instructionsItem);
        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(gameMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void setupKeyboardControls() {
        // Set up keyboard controls
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyPress(e.getKeyCode(), false);
            }
        };

        // Add the key listener to the game panel
        gamePanel.addKeyListener(keyAdapter);
        gamePanel.setFocusable(true);

        // Pack and center on screen
        pack();
        setLocationRelativeTo(null);

        // Optimize game timer - reduce update frequency from 100ms to 16ms (approx 60fps)
        // This makes movement smoother while maintaining performance
        gameTimer = new javax.swing.Timer(16, e -> updateGame());
    }

    private void handleKeyPress(int keyCode, boolean pressed) {
        if (!gameRunning) return;
        if (gamePaused && keyCode != KeyEvent.VK_P && keyCode != KeyEvent.VK_ESCAPE) return;

        // Player 1 controls
        PlayerTank player1 = gamePanel.getPlayer1();
        if (player1 != null) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    player1.setDirection(Tank.Direction.UP);
                    player1.setMoving(pressed);
                    break;
                case KeyEvent.VK_RIGHT:
                    player1.setDirection(Tank.Direction.RIGHT);
                    player1.setMoving(pressed);
                    break;
                case KeyEvent.VK_DOWN:
                    player1.setDirection(Tank.Direction.DOWN);
                    player1.setMoving(pressed);
                    break;
                case KeyEvent.VK_LEFT:
                    player1.setDirection(Tank.Direction.LEFT);
                    player1.setMoving(pressed);
                    break;
                case KeyEvent.VK_SPACE:
                    if (pressed) {
                        // We'll set a flag but the actual firing will be controlled by cooldown
                        player1.setWantsToFire(true);
                    } else {
                        player1.setWantsToFire(false);
                    }
                    break;
            }
        }

        // Player 2 controls remain similar but with setWantsToFire
        PlayerTank player2 = gamePanel.getPlayer2();
        if (player2 != null) {
            switch (keyCode) {
                case KeyEvent.VK_W:
                    player2.setDirection(Tank.Direction.UP);
                    player2.setMoving(pressed);
                    break;
                case KeyEvent.VK_D:
                    player2.setDirection(Tank.Direction.RIGHT);
                    player2.setMoving(pressed);
                    break;
                case KeyEvent.VK_S:
                    player2.setDirection(Tank.Direction.DOWN);
                    player2.setMoving(pressed);
                    break;
                case KeyEvent.VK_A:
                    player2.setDirection(Tank.Direction.LEFT);
                    player2.setMoving(pressed);
                    break;
                case KeyEvent.VK_Q:
                    if (pressed) {
                        player2.setWantsToFire(true);
                    } else {
                        player2.setWantsToFire(false);
                    }
                    break;
            }
        }

        // Game controls
        if (pressed) {
            switch (keyCode) {
                case KeyEvent.VK_P:
                    togglePause();
                    break;
                case KeyEvent.VK_ESCAPE:
                    showPauseMenu();
                    break;
                // Debug key for immediate testing
                case KeyEvent.VK_F1:
                    gamePanel.toggleDebug();
                    break;
            }
        }
    }

    private void showDifficultyDialog() {
        String[] options = {"Easy", "Normal", "Hard"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select difficulty level:",
                "Difficulty Settings",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);  // Default to Normal

        if (choice >= 0) {
            // Implement difficulty settings based on choice
            JOptionPane.showMessageDialog(this,
                    "Difficulty set to " + options[choice] + ".\nChanges will take effect in the next game.",
                    "Difficulty Changed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to start a new game
    public void startNewGame() {
        level = 1;
        score = 0;
        gameTime = 0;
        playerLives = 3;

        // Restore original layout
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);

        // Refresh the UI
        revalidate();
        repaint();

        // Load the first level
        loadLevel(level);
        gamePanel.repaint();
        System.out.println("Requested repaint after loading");

        // Start the game
        gameRunning = true;
        gamePaused = false;
        gameTimer.start();
        System.out.println("Game timer started");

        // Focus on game panel
        gamePanel.requestFocus();

        // Update info panel
        infoPanel.updateGameInfo();
    }

    // Method to toggle pause state
    public void togglePause() {
        gamePaused = !gamePaused;

        if (gamePaused) {
            gameTimer.stop();
            infoPanel.showPauseMessage("GAME PAUSED - Press P to resume");
        } else {
            gameTimer.start();
            infoPanel.hidePauseMessage();
            gamePanel.requestFocus();
        }
    }

    // Method to load a level
    private void loadLevel(int level) {
        try {
            String mapFile = "/resources/maps/level" + level + ".map";
            System.out.println("Trying to load map: " + mapFile);
            InputStream is = getClass().getResourceAsStream(mapFile);

            if (is == null) {
                System.out.println("Map file not found at: " + mapFile);

                // Try alternate path
                mapFile = "src/resources/maps/level" + level + ".map";
                System.out.println("Trying alternate path: " + mapFile);

                try {
                    is = new FileInputStream(mapFile);
                } catch (FileNotFoundException e) {
                    System.out.println("Map file not found at alternate path. Creating default map.");
                    gamePanel.createDefaultMap();
                    return;
                }
            }

            // Load map file
            System.out.println("Map file found, loading...");
            gamePanel.loadMapFromStream(is);
            is.close();

            // Update info panel
            infoPanel.updateLevel(level);
        } catch (IOException e) {
            System.err.println("Error loading level: " + e.getMessage());
            e.printStackTrace();
            gamePanel.createDefaultMap();
        }
    }

    // Method to update the game (called by timer)
    private void updateGame() {
        if (!gameRunning || gamePaused) return;

        // Update game time
        gameTime++;
        if (gameTime % 60 == 0) { // Update every second (60 frames at 16ms)
            infoPanel.updateTime(gameTime / 60);
        }

        // Update game state
        gamePanel.updateGame();

        // Check for level completion
        if (gamePanel.isLevelComplete()) {
            levelComplete();
        }

        // Check for game over
        if (gamePanel.isGameOver()) {
            gameOver();
        }
    }

    // Method to handle level completion
    private void levelComplete() {
        gameTimer.stop();

        // Show level complete message
        JOptionPane.showMessageDialog(this,
                "Level " + level + " Complete!\nScore: " + score,
                "Level Complete",
                JOptionPane.INFORMATION_MESSAGE);

        // Load next level
        level++;
        loadLevel(level);

        // Resume game
        gameTimer.start();
    }

    // Method to handle game over
    private void gameOver() {
        gameRunning = false;
        gameTimer.stop();

        // Show game over message
        JOptionPane.showMessageDialog(this,
                "GAME OVER\nFinal Score: " + score,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);

        // Show main menu
        showMainMenu();
    }

    // Method to add points to score
    public void addScore(int points) {
        score += points;
        infoPanel.updateScore(score);
    }

    // Method to get current player lives
    public int getPlayerLives() {
        return playerLives;
    }

    // Method to update player lives
    public void updatePlayerLives(int lives) {
        playerLives = lives;
        infoPanel.updateLives(playerLives);
    }

    // Method to show pause menu
    private void showPauseMenu() {
        if (!gameRunning) return;

        // Pause the game
        boolean wasPaused = gamePaused;
        if (!wasPaused) {
            togglePause();
        }

        // Show menu
        String[] options = {"Resume Game", "Restart Level", "Exit to Main Menu"};
        int choice = JOptionPane.showOptionDialog(this,
                "Game Paused",
                "Pause Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0: // Resume
                if (wasPaused) break;
                togglePause();
                break;
            case 1: // Restart Level
                loadLevel(level);
                if (wasPaused) {
                    togglePause();
                }
                break;
            case 2: // Exit to Main Menu
                // Stop the game and show main menu
                gameRunning = false;
                gameTimer.stop();
                showMainMenu();
                break;
        }
    }

    // Method to show instructions
    private void showInstructions() {
        String instructions =
                "Battle City (Tank 1990) Instructions\n\n" +
                        "Player 1 Controls:\n" +
                        "Arrow Keys - Move tank\n" +
                        "Space - Fire\n\n" +
                        "Player 2 Controls:\n" +
                        "W, A, S, D - Move tank\n" +
                        "Q - Fire\n\n" +
                        "Game Controls:\n" +
                        "P - Pause/Resume\n" +
                        "ESC - Pause Menu\n" +
                        "F1 - Toggle Debug Mode\n\n" +
                        "Game Objective:\n" +
                        "Destroy enemy tanks and protect your base. Collect power-ups to improve your tank!\n\n" +
                        "Environment:\n" +
                        "- Brick walls can be destroyed by bullets\n" +
                        "- Steel walls require high-power bullets to destroy\n" +
                        "- Water blocks tank movement but allows bullets to pass\n" +
                        "- Trees hide tanks but allow movement and bullets\n" +
                        "- Ice makes tanks slide faster\n\n" +
                        "Power-ups:\n" +
                        "- Star: Increases bullet power\n" +
                        "- Tank: Extra life\n" +
                        "- Grenade: Destroys all enemies\n" +
                        "- Timer: Freezes enemies\n" +
                        "- Helmet: Temporary invincibility\n" +
                        "- Shovel: Reinforces base walls";

        JTextArea textArea = new JTextArea(instructions);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to show about dialog
    private void showAbout() {
        String about =
                "Battle City (Tank 1990)\n" +
                        "Version 1.0\n\n" +
                        "A Java implementation of the classic NES game\n" +
                        "Originally developed by Namco in 1985\n\n" +
                        "Features:\n" +
                        "- Single and two-player modes\n" +
                        "- Multiple enemy tank types\n" +
                        "- Power-ups and special effects\n" +
                        "- Advanced AI behavior\n" +
                        "- Destructible environments";

        JOptionPane.showMessageDialog(this,
                about,
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to show main menu
    public void showMainMenu() {
        // Create menu panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuPanel.setBackground(Color.BLACK);

        // Title label
        JLabel titleLabel = new JLabel("BATTLE CITY");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sub title
        JLabel subtitleLabel = new JLabel("TANK 1990");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 24));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons with proper styling
        JButton singlePlayerButton = createMenuButton("1 Player");
        JButton twoPlayerButton = createMenuButton("2 Players");
        JButton instructionsButton = createMenuButton("Instructions");
        JButton exitButton = createMenuButton("Exit");

        // Add action listeners
        singlePlayerButton.addActionListener(e -> {
            gamePanel.setTwoPlayerMode(false);
            startNewGame();
        });

        twoPlayerButton.addActionListener(e -> {
            gamePanel.setTwoPlayerMode(true);
            startNewGame();
        });

        instructionsButton.addActionListener(e -> showInstructions());

        exitButton.addActionListener(e -> System.exit(0));

        // Add components to panel
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(titleLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(subtitleLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        menuPanel.add(singlePlayerButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(twoPlayerButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(instructionsButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(exitButton);
        menuPanel.add(Box.createVerticalGlue());

        // Remove existing components
        getContentPane().removeAll();

        // Add menu panel
        getContentPane().add(menuPanel);

        // Refresh display
        revalidate();
        repaint();
    }


    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setMaximumSize(new Dimension(200, 50));
        button.setPreferredSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(220, 220, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }
}