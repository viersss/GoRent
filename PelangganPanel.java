// =================================================================================
// File 18: src/PelangganPanel.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

class PelangganPanel extends JPanel {
    private final RentalService rentalService = RentalService.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField namaInput, noTeleponInput;
    public PelangganPanel(User user) {
        setLayout(new BorderLayout(10, 10)); setBorder(new EmptyBorder(15, 15, 15, 15));
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "No. Telepon"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14)); table.setRowHeight(28);
        JScrollPane tableSP = new JScrollPane(table);
        if (user.getRole() == Role.ADMIN) {
            JScrollPane formSP = new JScrollPane(createFormPanel());
            formSP.setBorder(BorderFactory.createTitledBorder("Form Tambah Pelanggan"));
            formSP.setMinimumSize(new Dimension(380, 0));
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableSP, formSP);
            splitPane.setResizeWeight(0.7); splitPane.setOneTouchExpandable(true);
            add(splitPane, BorderLayout.CENTER);
            add(createBottomPanel(), BorderLayout.SOUTH);
        } else {
            add(tableSP, BorderLayout.CENTER);
        }
        add(createHeader(), BorderLayout.NORTH); refreshTable();
    }
    private void tambahData() {
        String nama = namaInput.getText().trim(); String noTelp = noTeleponInput.getText().trim();
        if(nama.isEmpty() || noTelp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan No. Telepon wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        if(rentalService.tambahPelanggan(nama, noTelp)) {
            namaInput.setText(""); noTeleponInput.setText("");
            JOptionPane.showMessageDialog(this, "Pelanggan berhasil ditambahkan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } else JOptionPane.showMessageDialog(this, "Gagal menambahkan pelanggan.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void hapusData() {
        int row = table.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Pilih pelanggan untuk dihapus!", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        String id = table.getModel().getValueAt(table.convertRowIndexToModel(row), 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && rentalService.hapusPelanggan(id)) refreshTable();
        else if (confirm == JOptionPane.YES_OPTION) JOptionPane.showMessageDialog(this, "Gagal menghapus! Pelanggan mungkin memiliki sewa aktif atau merupakan seorang User.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void refreshTable() {
        tableModel.setRowCount(0);
        rentalService.getAllPelanggan().forEach(p -> tableModel.addRow(new Object[]{p.getId(), p.getNama(), p.getNoTelepon()}));
    }
    private JPanel createHeader() { JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); JLabel l = new JLabel("Manajemen Data Pelanggan"); l.setFont(new Font("Segoe UI", Font.BOLD, 28)); p.add(l); return p; }
    private JPanel createFormPanel() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(10, 10, 10, 10));
        namaInput = new JTextField(); noTeleponInput = new JTextField();
        JButton b = new JButton("[+] Tambah Pelanggan"); b.setAlignmentX(Component.CENTER_ALIGNMENT); b.addActionListener(e -> tambahData());
        p.add(createFormRow("Nama:", namaInput)); p.add(createFormRow("No. Telepon:", noTeleponInput));
        p.add(Box.createRigidArea(new Dimension(0, 15))); p.add(b); p.add(Box.createVerticalGlue()); return p;
    }
    private JPanel createBottomPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0)); p.setBorder(new EmptyBorder(10, 0, 0, 0));
        JTextField sf = new JTextField(20); sf.putClientProperty("JTextField.placeholderText", "🔍 Cari...");
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel); table.setRowSorter(sorter);
        sf.getDocument().addDocumentListener((DocumentListener) new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); } public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            private void filter() { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + sf.getText())); }
        });
        JButton db = new JButton("Hapus Terpilih"); db.addActionListener(e -> hapusData());
        p.add(sf, BorderLayout.WEST); p.add(db, BorderLayout.EAST); return p;
    }
    private JPanel createFormRow(String l, JComponent c) { JPanel r = new JPanel(new BorderLayout(5, 5)); r.setBorder(new EmptyBorder(0, 0, 10, 0)); r.add(new JLabel(l), BorderLayout.NORTH); r.add(c, BorderLayout.CENTER); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); return r; }
}