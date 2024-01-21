import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            new MainWindow();
        } catch (UnsupportedLookAndFeelException e) {

            AlertApp.Erroralert(e.getMessage());
        }
    }
}
