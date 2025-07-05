// =================================================================================
// File 15: src/DashboardPanel.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

class DashboardPanel extends JPanel {
    // Service untuk mengakses data dari database
    private final RentalService rentalService = RentalService.getInstance();
    // Array untuk menyimpan label angka statistik (6 kartu)
    private JLabel[] valueLabels = new JLabel[6];
    // Model tabel untuk menampilkan transaksi terbaru
    private DefaultTableModel recentTableModel;

    public DashboardPanel(User user) {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 30, 20, 30));
        setBackground(Color.WHITE);

        // Membuat header dengan judul dan pesan selamat datang
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel headerLabel = new JLabel("Dashboard");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        JLabel welcomeLabel = new JLabel("Selamat datang kembali, " + user.getUsername() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(welcomeLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Membuat grid 2x3 untuk menampilkan 6 kartu statistik
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        statsGrid.setOpaque(false);
        String[] titles = {"Total Kendaraan", "Tersedia", "Disewa", "Total Pelanggan", "Transaksi Aktif", "Transaksi Selesai"};
        String[] icons = {"🚗", "✅", "❌", "👥", "🔄", "🏁"};
        Color[] bgColors = {new Color(225, 245, 254), new Color(232, 245, 233), new Color(255, 235, 238), new Color(232, 234, 246), new Color(255, 243, 224), new Color(241, 248, 233)};

        // Loop untuk membuat setiap kartu statistik
        for (int i = 0; i < valueLabels.length; i++) {
            valueLabels[i] = createValueLabel();
            statsGrid.add(createStatCard(titles[i], valueLabels[i], icons[i], bgColors[i]));
        }
        add(statsGrid, BorderLayout.CENTER);
        add(createRecentActivityPanel(), BorderLayout.SOUTH);
        updateStats(); // Memuat data statistik dari database
    }

    // Membuat label untuk menampilkan angka statistik
    private JLabel createValueLabel() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 48));
        return l;
    }

    // Membuat kartu statistik dengan judul, nilai, ikon, dan warna background
    private JPanel createStatCard(String title, JLabel vLabel, String icon, Color bg) {
        JPanel c = new JPanel(new BorderLayout(10, 10));
        c.setBackground(bg);
        // Border kiri dengan warna lebih gelap untuk aksen
        Border line = BorderFactory.createMatteBorder(0, 5, 0, 0, bg.darker());
        c.setBorder(BorderFactory.createCompoundBorder(line, new EmptyBorder(15, 15, 15, 15)));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 16)); t.setForeground(Color.DARK_GRAY);
        JLabel i = new JLabel(icon); i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        c.add(t, BorderLayout.NORTH); c.add(vLabel, BorderLayout.CENTER); c.add(i, BorderLayout.EAST);
        return c;
    }

    // Membuat panel untuk menampilkan tabel aktivitas terbaru
    private JPanel createRecentActivityPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false); p.setBorder(BorderFactory.createTitledBorder("Aktivitas Terbaru"));
        // Tabel read-only untuk transaksi terbaru
        recentTableModel = new DefaultTableModel(new String[]{"ID", "Kendaraan", "Pelanggan", "Status"}, 0) {
            public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable t = new JTable(recentTableModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(0, 180));
        return p;
    }

    // Method untuk memperbarui data statistik dari database
    public void updateStats() {
        Map<String, Long> stats = rentalService.getDashboardStats();
        String[] keys = {"totalKendaraan", "kendaraanTersedia", "kendaraanDisewa", "totalPelanggan", "transaksiAktif", "transaksiSelesai"};
        // Update setiap label dengan data dari database
        for (int i = 0; i < keys.length; i++) {
            valueLabels[i].setText(stats.getOrDefault(keys[i], 0L).toString());
        }
        // Refresh tabel dengan 5 transaksi terbaru
        recentTableModel.setRowCount(0);
        List<Transaksi> recent = rentalService.getRecentTransactions(5);
        for (Transaksi t : recent) {
            recentTableModel.addRow(new Object[]{t.getId(), t.getMerkKendaraan(), t.getNamaPelanggan(), t.getStatus()});
        }
    }
}