// =================================================================================
// File 16: src/IdentitasPanel.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class IdentitasPanel extends JPanel {
    public IdentitasPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(40, 60, 40, 60));
        setBackground(Color.WHITE);

        // Panel placeholder untuk foto profil
        JPanel photoPanel = new JPanel(new GridBagLayout());
        photoPanel.setPreferredSize(new Dimension(220, 220));
        photoPanel.setBackground(new Color(230, 230, 230));
        JLabel photoLabel = new JLabel("Foto Anda");
        photoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        photoPanel.add(photoLabel);

        // Panel untuk menampilkan informasi pengembang
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Identitas Pengembang");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(20));

        // Menambahkan baris-baris data identitas
        textPanel.add(createIdentityRow("Nama:", "Viery Nugroho"));
        textPanel.add(createIdentityRow("NIM:", "A11.2021.13904"));
        textPanel.add(createIdentityRow("Kelas:", "A11.4708"));
        textPanel.add(createIdentityRow("Email:", "111202113904@mhs.dinus.ac.id"));
        textPanel.add(createIdentityRow("GitHub:", "github.com/nugrohovier"));
        textPanel.add(Box.createVerticalGlue());

        add(photoPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    // Helper method untuk membuat baris label dan value yang aligned
    private JPanel createIdentityRow(String l, String v) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        r.setOpaque(false); r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel label = new JLabel(l); label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setPreferredSize(new Dimension(120, 30)); // Lebar tetap untuk alignment
        JLabel value = new JLabel(v); value.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        r.add(label); r.add(value); return r;
    }
}