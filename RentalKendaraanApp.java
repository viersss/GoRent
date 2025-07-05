// File: src/RentalKendaraanApp.java
import javax.swing.*;
import java.awt.*;

public class RentalKendaraanApp extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final User activeUser;

    public RentalKendaraanApp(User user) {
        this.activeUser = user;
        setTitle("GoRent - Admin Control Panel | Logged in as: " + user.getUsername());
        setSize(1366, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Membuat sidebar dan semua panel konten
        SidebarPanel sidebar = new SidebarPanel(activeUser, this::showPanel, this::logout);

        mainPanel.add(new DashboardPanel(activeUser), "DASHBOARD");
        mainPanel.add(new KendaraanPanel(activeUser), "KENDARAAN");
        mainPanel.add(new PelangganPanel(activeUser), "PELANGGAN");
        mainPanel.add(new TransaksiPanel(), "TRANSAKSI");
        mainPanel.add(new ProfilePanel(activeUser, this::logout), "PROFIL");

        // Menambahkan komponen utama ke frame
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        showPanel("DASHBOARD"); // Tampilkan dashboard saat pertama kali login
    }

    private void showPanel(String panelName) {
        for (Component comp : mainPanel.getComponents()) {
            if (comp.isVisible()) {
                if (comp instanceof DashboardPanel) {
                    ((DashboardPanel) comp).updateStats();
                } else if (comp instanceof KendaraanPanel) {
                    ((KendaraanPanel) comp).refreshTable();
                } else if (comp instanceof PelangganPanel) {
                    ((PelangganPanel) comp).refreshTable();
                } else if (comp instanceof TransaksiPanel) {
                    ((TransaksiPanel) comp).refreshAll();
                } else if (comp instanceof ProfilePanel) {
                    // Panggil method refreshContent yang sudah ada
                    ((ProfilePanel) comp).refreshContent();
                }
            }
        }
        cardLayout.show(mainPanel, panelName);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}