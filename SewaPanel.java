// =================================================================================
// File 20: src/SewaPanel.java (Versi Asli)
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

class SewaPanel extends JPanel {
    private final User activeUser;
    private final Runnable onRentalSuccess;

    public SewaPanel(User user, Runnable onRentalSuccess) {
        this.activeUser = user;
        this.onRentalSuccess = onRentalSuccess;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 245, 245));

        JLabel header = new JLabel("Katalog Kendaraan Tersedia");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(new EmptyBorder(0, 5, 15, 0));
        add(header, BorderLayout.NORTH);

        VerticalFlowPanel vehicleGrid = new VerticalFlowPanel();
        vehicleGrid.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(vehicleGrid);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        List<Kendaraan> available = RentalService.getInstance()
                .getAllKendaraan()
                .stream()
                .filter(k -> k.getStatus().equals("Tersedia"))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            vehicleGrid.setLayout(new BorderLayout());
            JLabel empty = new JLabel("Maaf, tidak ada kendaraan yang tersedia saat ini.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            vehicleGrid.add(empty, BorderLayout.CENTER);
        } else {
            for (Kendaraan k : available) vehicleGrid.add(createVehicleCard(k));
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createVehicleCard(Kendaraan k) {
        JPanel c = new JPanel(new BorderLayout(0, 0));
        c.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(260, 360));

        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);
        img.setPreferredSize(new Dimension(260, 180));
        img.setBackground(new Color(230, 230, 230));
        img.setOpaque(true);

        try {
            String imgPath = k.getImageUrl();
            ImageIcon icon;

            if (imgPath.startsWith("http")) {
                icon = new ImageIcon(new URL(imgPath));
            } else {
                icon = new ImageIcon(imgPath); // file lokal
            }

            Image scaled = icon.getImage().getScaledInstance(260, 180, Image.SCALE_SMOOTH);
            img.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            img.setText("Gambar tidak tersedia");
        }

        JPanel info = new JPanel(new BorderLayout(5, 2));
        info.setBorder(new EmptyBorder(10, 15, 10, 15));
        info.setOpaque(false);

        JLabel name = new JLabel("<html><b>" + k.getMerk() + "</b><br>" + k.getTipe() + "</html>");
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        JLabel price = new JLabel(currency.format(k.getHargaSewa()) + "/hari");
        price.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        price.setForeground(new Color(0, 150, 136));

        info.add(name, BorderLayout.CENTER);
        info.add(price, BorderLayout.SOUTH);

        JPanel spec = new JPanel(new GridLayout(1, 3, 5, 5));
        spec.setBorder(new EmptyBorder(0, 15, 10, 15));
        spec.setOpaque(false);
        spec.add(createSpecBox("Tahun", String.valueOf(k.getTahun())));
        spec.add(createSpecBox("Transmisi", k.getTransmisi()));
        spec.add(createSpecBox("Bahan Bakar", k.getBahanBakar()));

        JButton btn = new JButton("Sewa Sekarang");
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(25, 118, 210));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> showConfirmationDialog(k));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(spec, BorderLayout.CENTER);
        bottom.add(btn, BorderLayout.SOUTH);

        c.add(img, BorderLayout.NORTH);
        c.add(info, BorderLayout.CENTER);
        c.add(bottom, BorderLayout.SOUTH);

        return c;
    }

    private void showConfirmationDialog(Kendaraan k) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel t = new JLabel("Anda akan menyewa kendaraan ini?");
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(t, BorderLayout.NORTH);

        JPanel s = new JPanel(new GridLayout(0, 2, 5, 5));
        s.add(new JLabel("Merk:"));
        s.add(new JLabel(k.getMerk()));
        s.add(new JLabel("Tipe:"));
        s.add(new JLabel(k.getTipe()));
        s.add(new JLabel("Tahun:"));
        s.add(new JLabel(String.valueOf(k.getTahun())));
        s.add(new JLabel("Harga Sewa:"));
        s.add(new JLabel(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(k.getHargaSewa()) + "/hari"));
        p.add(s, BorderLayout.CENTER);

        int confirm = JOptionPane.showConfirmDialog(this, p, "Konfirmasi Penyewaan", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) performRental(k);
    }

    private void performRental(Kendaraan k) {
        Pelanggan p = RentalService.getInstance().findPelangganByUsername(activeUser.getUsername());
        if (p != null) {
            if (RentalService.getInstance().tambahTransaksi(k.getId(), p.getId(), LocalDate.now()) != null) {
                JOptionPane.showMessageDialog(this, "Penyewaan berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                if (onRentalSuccess != null) onRentalSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memproses transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Gagal! Data pelanggan tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createSpecBox(String t, String v) {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel tl = new JLabel(t);
        tl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel vl = new JLabel(v);
        vl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        vl.setAlignmentX(Component.CENTER_ALIGNMENT);

        b.add(Box.createVerticalStrut(5));
        b.add(tl);
        b.add(vl);
        b.add(Box.createVerticalStrut(5));

        return b;
    }
}