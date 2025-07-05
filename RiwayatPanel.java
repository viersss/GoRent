// =================================================================================
// File 21: src/RiwayatPanel.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class RiwayatPanel extends JPanel {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    public RiwayatPanel(User user) {
        setLayout(new BorderLayout(10, 10)); setBorder(new EmptyBorder(25, 40, 40, 40));
        setBackground(new Color(245, 247, 250));
        JLabel header = new JLabel("Riwayat Penyewaan Anda");
        header.setFont(new Font("Segoe UI", Font.BOLD, 32));
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(header, BorderLayout.NORTH);
        String[] cols = {"ID Transaksi", "Kendaraan", "Tanggal Pinjam", "Status", "Denda (Rp)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0){
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        List<Transaksi> txs = RentalService.getInstance().getTransaksiByUser(user);
        if (txs.isEmpty()) {
            JLabel empty = new JLabel("Anda belum pernah melakukan transaksi.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            add(empty, BorderLayout.CENTER);
        } else {
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            for(Transaksi t : txs) {
                model.addRow(new Object[]{t.getId(), t.getMerkKendaraan(), t.getTglPinjam().format(dateFormatter), t.getStatus(), currency.format(t.getDenda())});
            }
            add(new JScrollPane(table), BorderLayout.CENTER);
        }
    }
}