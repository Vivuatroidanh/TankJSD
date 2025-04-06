import gui.game.BattleCityGame;
import gui.game.GamePanel;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BattleCityGame game = new BattleCityGame();
            game.showMainMenu();
            game.setVisible(true);
        });
    }
}