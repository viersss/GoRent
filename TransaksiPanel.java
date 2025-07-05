// =================================================================================
// File 19: src/TransaksiPanel.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.text.NumberFormat;
import java.util.Locale;

class TransaksiPanel extends JPanel {
    private final RentalService rentalService = RentalService.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> kendaraanComboBox, pelangganComboBox;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    public TransaksiPanel() {
        setLayout(new BorderLayout(10, 10)); setBorder(new EmptyBorder(15, 15, 15, 15));
        tableModel = new DefaultTableModel(new String[]{"ID", "Kendaraan", "Pelanggan", "Tgl Pinjam", "Status", "Denda (Rp)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14)); table.setRowHeight(28);
        kendaraanComboBox = new JComboBox<>(); pelangganComboBox = new JComboBox<>();
        add(createHeader(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);
        refreshAll();
    }
    private void tambahData() {
        if(kendaraanComboBox.getSelectedIndex() == -1) { JOptionPane.showMessageDialog(this, "Tidak ada kendaraan tersedia!", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (pelangganComboBox.getSelectedIndex() == -1) { JOptionPane.showMessageDialog(this, "Pilih pelanggan!", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        String idKendaraan = Objects.requireNonNull(kendaraanComboBox.getSelectedItem()).toString().split(" - ")[0];
        String idPelanggan = Objects.requireNonNull(pelangganComboBox.getSelectedItem()).toString().split(" - ")[0];
        if (rentalService.tambahTransaksi(idKendaraan, idPelanggan, LocalDate.now()) != null) {
            JOptionPane.showMessageDialog(this, "Transaksi berhasil ditambahkan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } else JOptionPane.showMessageDialog(this, "Gagal menambahkan transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void kembalikanData() {
        int row = table.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Pilih transaksi dari tabel untuk dikembalikan!", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        String id = table.getModel().getValueAt(table.convertRowIndexToModel(row), 0).toString();
        String status = table.getModel().getValueAt(table.convertRowIndexToModel(row), 4).toString();
        if (!"Disewa".equals(status)) { JOptionPane.showMessageDialog(this, "Transaksi ini sudah selesai.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
        String dendaStr = JOptionPane.showInputDialog(this, "Masukkan denda tambahan jika ada:", "0");
        try {
            double denda = Double.parseDouble(dendaStr);
            if (rentalService.kembalikanKendaraan(id, denda)) {
                JOptionPane.showMessageDialog(this, "Kendaraan berhasil dikembalikan.", "Info", JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            } else JOptionPane.showMessageDialog(this, "Gagal mengembalikan kendaraan.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Input denda tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ex) { /* User cancel */ }
    }
    public void refreshAll() {
        tableModel.setRowCount(0);
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        rentalService.getAllTransaksi().forEach(t -> tableModel.addRow(new Object[]{t.getId(), t.getMerkKendaraan(), t.getNamaPelanggan(), t.getTglPinjam().format(dateFormatter), t.getStatus(), currency.format(t.getDenda())}));
        Object selKend = kendaraanComboBox.getSelectedItem(); kendaraanComboBox.removeAllItems();
        rentalService.getAllKendaraan().stream().filter(k -> k.getStatus().equals("Tersedia")).forEach(k -> kendaraanComboBox.addItem(k.getId() + " - " + k.getMerk() + " " + k.getTipe()));
        kendaraanComboBox.setSelectedItem(selKend);
        Object selPel = pelangganComboBox.getSelectedItem(); pelangganComboBox.removeAllItems();
        rentalService.getAllPelanggan().forEach(p -> pelangganComboBox.addItem(p.getId() + " - " + p.getNama()));
        pelangganComboBox.setSelectedItem(selPel);
    }
    private JPanel createHeader() { JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); JLabel l = new JLabel("Manajemen Transaksi"); l.setFont(new Font("Segoe UI", Font.BOLD, 28)); p.add(l); return p; }
    private JPanel createFormPanel() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(BorderFactory.createTitledBorder("Form Transaksi")); p.setPreferredSize(new Dimension(350, 0));
        JButton sb = new JButton("✓ Sewa Kendaraan Terpilih"); sb.addActionListener(e -> tambahData());
        JButton kb = new JButton("↩ Kembalikan Kendaraan (dari Tabel)"); kb.addActionListener(e -> kembalikanData());
        p.add(createFormRow("Pilih Kendaraan Tersedia:", kendaraanComboBox));
        p.add(createFormRow("Pilih Pelanggan:", pelangganComboBox));
        p.add(Box.createVerticalStrut(20)); p.add(sb);
        p.add(Box.createVerticalStrut(10)); p.add(kb);
        p.add(Box.createVerticalGlue()); return p;
    }
    private JPanel createFormRow(String l, JComponent c) { JPanel r = new JPanel(new BorderLayout(5, 5)); r.setBorder(new EmptyBorder(5,5,5,5)); r.add(new JLabel(l), BorderLayout.NORTH); r.add(c, BorderLayout.CENTER); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); return r; }
}