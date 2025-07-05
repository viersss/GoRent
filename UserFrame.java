// =================================================================================
// File 13: src/UserFrame.java (User Frame - Versi Asli)
// =================================================================================
import javax.swing.*;
import java.awt.*;

class UserFrame extends JFrame {
    private final User activeUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);

    public UserFrame(User user) {
        this.activeUser = user;
        setTitle("GoRent - Selamat Datang, " + user.getUsername());
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        SidebarPanel sidebar = new SidebarPanel(user, this::showPanel, this::logout);

        mainContentPanel.add(new SewaPanel(activeUser, this::refreshSewaPanel), "SEWA");
        mainContentPanel.add(new RiwayatPanel(user), "RIWAYAT");
        mainContentPanel.add(new ProfilePanel(user, this::logout), "PROFIL");

        add(sidebar, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
        showPanel("SEWA");
    }

    // Logika refresh panel yang asli, di mana instance baru dibuat untuk memastikan data terupdate
    private void showPanel(String panelName) {
        if ("RIWAYAT".equals(panelName)) {
            // Membuat instance baru untuk memastikan data riwayat paling update
            mainContentPanel.add(new RiwayatPanel(activeUser), "RIWAYAT_REFRESH");
            cardLayout.show(mainContentPanel, "RIWAYAT_REFRESH");
        } else if ("PROFIL".equals(panelName)) {
            // Membuat instance baru untuk memastikan data profil paling update
            mainContentPanel.add(new ProfilePanel(activeUser, this::logout), "PROFIL_REFRESH");
            cardLayout.show(mainContentPanel, "PROFIL_REFRESH");
        } else {
            cardLayout.show(mainContentPanel, panelName);
        }
    }

    private void refreshSewaPanel() {
        // Membuat ulang panel sewa untuk me-refresh daftar kendaraan yang tersedia
        mainContentPanel.add(new SewaPanel(activeUser, this::refreshSewaPanel), "SEWA_REFRESH");
        cardLayout.show(mainContentPanel, "SEWA_REFRESH");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}