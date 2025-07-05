// =================================================================================
// File: src/KendaraanPanel.java (dengan Preview Gambar di Admin)
// =================================================================================
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class KendaraanPanel extends JPanel {
    private final RentalService rentalService = RentalService.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField merkInput, tipeInput, tahunInput, transmisiInput, bahanBakarInput, hargaSewaInput, imageUrlInput;
    private JLabel previewLabel;

    public KendaraanPanel(User user) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        tableModel = new DefaultTableModel(new String[]{"ID", "Merk", "Tipe", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);

        JScrollPane tableSP = new JScrollPane(table);

        if (user.getRole() == Role.ADMIN) {
            JScrollPane formSP = new JScrollPane(createFormPanel());
            formSP.setBorder(BorderFactory.createTitledBorder("Form Tambah Kendaraan"));
            formSP.setMinimumSize(new Dimension(400, 0));

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableSP, formSP);
            splitPane.setResizeWeight(0.7);
            splitPane.setOneTouchExpandable(true);
            add(splitPane, BorderLayout.CENTER);
            add(createBottomPanel(), BorderLayout.SOUTH);
        } else {
            add(tableSP, BorderLayout.CENTER);
        }

        add(createHeader(), BorderLayout.NORTH);
        refreshTable();
    }

    private void tambahData() {
        String merk = merkInput.getText().trim();
        String tipe = tipeInput.getText().trim();
        String tahunStr = tahunInput.getText().trim();
        String transmisi = transmisiInput.getText().trim();
        String bahanBakar = bahanBakarInput.getText().trim();
        String hargaStr = hargaSewaInput.getText().trim();
        String imageUrl = imageUrlInput.getText().trim();

        if (merk.isEmpty() || tipe.isEmpty() || tahunStr.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Merk, Tipe, Tahun, dan Harga wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int tahun, harga;
        try {
            tahun = Integer.parseInt(tahunStr);
            harga = Integer.parseInt(hargaStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tahun dan Harga Sewa harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (imageUrl.isEmpty()) imageUrl = "https://placehold.co/400x300/e0e0e0/555?text=" + merk;

        if (rentalService.tambahKendaraan(merk, tipe, tahun, transmisi, bahanBakar, harga, imageUrl)) {
            merkInput.setText(""); tipeInput.setText(""); tahunInput.setText("");
            transmisiInput.setText(""); bahanBakarInput.setText(""); hargaSewaInput.setText("");
            imageUrlInput.setText(""); previewLabel.setIcon(null);
            JOptionPane.showMessageDialog(this, "Kendaraan berhasil ditambahkan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan kendaraan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusData() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kendaraan untuk dihapus!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = table.getModel().getValueAt(table.convertRowIndexToModel(row), 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && rentalService.hapusKendaraan(id)) refreshTable();
        else if (confirm == JOptionPane.YES_OPTION) JOptionPane.showMessageDialog(this, "Gagal menghapus! Kendaraan mungkin sedang disewa.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        rentalService.getAllKendaraan().forEach(k -> tableModel.addRow(new Object[]{k.getId(), k.getMerk(), k.getTipe(), k.getStatus()}));
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel l = new JLabel("Manajemen Data Kendaraan");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(l);
        return p;
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(new Color(250, 250, 250));

        merkInput = new JTextField(); tipeInput = new JTextField(); tahunInput = new JTextField();
        transmisiInput = new JTextField(); bahanBakarInput = new JTextField(); hargaSewaInput = new JTextField();
        imageUrlInput = new JTextField(); imageUrlInput.setEditable(false);

        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(260, 180));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setOpaque(true); previewLabel.setBackground(Color.WHITE);

        JLabel previewTitle = new JLabel("Preview Gambar");
        previewTitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        previewTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        previewTitle.setBorder(new EmptyBorder(5, 0, 5, 0));

        // panel khusus untuk gambar
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        imagePanel.add(previewLabel);
        imagePanel.add(previewTitle);

        JButton pilihGambarBtn = new JButton("\uD83D\uDCC1 Pilih Gambar...");
        pilihGambarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        pilihGambarBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int r = fc.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                imageUrlInput.setText(f.getAbsolutePath());
                try {
                    BufferedImage img = ImageIO.read(f);
                    Image scaled = img.getScaledInstance(260, 180, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaled));
                } catch (IOException ex) {
                    previewLabel.setText("Gagal load gambar");
                    previewLabel.setIcon(null);
                }
            }
        });

        JButton tambahBtn = new JButton("[+] Tambah Kendaraan");
        tambahBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        tambahBtn.addActionListener(e -> tambahData());

        p.add(createFormRow("Merk:", merkInput));
        p.add(createFormRow("Tipe:", tipeInput));
        p.add(createFormRow("Tahun:", tahunInput));
        p.add(createFormRow("Transmisi (Manual/Matic):", transmisiInput));
        p.add(createFormRow("Bahan Bakar:", bahanBakarInput));
        p.add(createFormRow("Harga Sewa/hari (Rp):", hargaSewaInput));
        p.add(createFormRow("Path Gambar:", imageUrlInput));
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(pilihGambarBtn);
        p.add(imagePanel); // ini ditambahkan!
        p.add(Box.createRigidArea(new Dimension(0, 15)));
        p.add(tambahBtn);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel createBottomPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        JTextField sf = new JTextField(20);
        sf.putClientProperty("JTextField.placeholderText", "\uD83D\uDD0D Cari...");
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        sf.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + sf.getText()));
            }
        });

        JButton db = new JButton("Hapus Terpilih");
        db.addActionListener(e -> hapusData());

        p.add(sf, BorderLayout.WEST);
        p.add(db, BorderLayout.EAST);
        return p;
    }

    private JPanel createFormRow(String l, JComponent c) {
        JPanel r = new JPanel(new BorderLayout(5, 5));
        r.setBorder(new EmptyBorder(0, 0, 10, 0));
        r.add(new JLabel(l), BorderLayout.NORTH);
        r.add(c, BorderLayout.CENTER);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        return r;
    }
}