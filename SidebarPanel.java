// =================================================================================
// File 14: src/SidebarPanel.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

class SidebarPanel extends JPanel {
    private final Color sidebarBackground = new Color(35, 40, 50);
    private final Color hoverColor = new Color(55, 60, 70);
    private final Color fontColor = new Color(220, 220, 220);
    private final Font navFont = new Font("Segoe UI", Font.BOLD, 14);

    public SidebarPanel(User user, Consumer<String> navigationConsumer, Runnable logoutAction) {
        setLayout(new BorderLayout());
        setBackground(sidebarBackground);
        setPreferredSize(new Dimension(240, 0));
        add(createLogoPanel(), BorderLayout.NORTH);
        add(createNavPanel(user, navigationConsumer), BorderLayout.CENTER);
        add(createUserInfoPanel(user, logoutAction), BorderLayout.SOUTH);
    }

    private JPanel createLogoPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        p.setBackground(sidebarBackground);
        JLabel l = new JLabel("GoRent");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(Color.WHITE);
        p.add(l);
        return p;
    }

    private JPanel createNavPanel(User user, Consumer<String> navigationConsumer) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(sidebarBackground);
        p.add(Box.createVerticalStrut(20));
        if (user.getRole() == Role.ADMIN) {
            p.add(createNavButton("📊", "Dashboard", "DASHBOARD", navigationConsumer));
            p.add(createNavButton("🚗", "Data Kendaraan", "KENDARAAN", navigationConsumer));
            p.add(createNavButton("👥", "Data Pelanggan", "PELANGGAN", navigationConsumer));
            p.add(createNavButton("📋", "Data Transaksi", "TRANSAKSI", navigationConsumer));
        } else {
            p.add(createNavButton("✅", "Sewa Kendaraan", "SEWA", navigationConsumer));
            p.add(createNavButton("📖", "Riwayat Saya", "RIWAYAT", navigationConsumer));
        }
        p.add(createNavButton("👤", "Profil Akun", "PROFIL", navigationConsumer));
        return p;
    }

    private JPanel createNavButton(String icon, String text, String command, Consumer<String> consumer) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btn.setBackground(sidebarBackground);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel i = new JLabel(icon); i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18)); i.setForeground(fontColor);
        JLabel l = new JLabel(text); l.setFont(navFont); l.setForeground(fontColor); l.setBorder(new EmptyBorder(15, 0, 15, 0));
        btn.add(i); btn.add(l);
        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { consumer.accept(command); }
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { btn.setBackground(sidebarBackground); }
        });
        return btn;
    }

    private JPanel createUserInfoPanel(User user, Runnable logoutAction) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(hoverColor);
        p.setBorder(new EmptyBorder(10, 20, 10, 10));
        JLabel l = new JLabel("<html><b>" + user.getUsername() + "</b><br><font size='-2'>" + user.getRole() + "</font></html>");
        l.setForeground(Color.WHITE);
        JButton b = new JButton("Logout");
        b.setFocusPainted(false);
        b.addActionListener(e -> logoutAction.run());
        p.add(l, BorderLayout.CENTER);
        p.add(b, BorderLayout.EAST);
        return p;
    }
}