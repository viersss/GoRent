// =================================================================================
// File 1: src/Mainnn.java
// =================================================================================
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Mainnn {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Gagal menginisialisasi LaF.");
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}