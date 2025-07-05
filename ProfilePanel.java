// File: src/ProfilePanel.java
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel; // <-- IMPORT YANG HILANG SEBELUMNYA
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ProfilePanel extends JPanel {
    private final User activeUser;
    private final Runnable logoutAction;
    private final RentalService rentalService;
    private CardLayout cardLayout;
    private JPanel rightContentPanel;
    private JLabel avatarLabel;
    private JLabel usernameInfoLabel;
    private JLabel roleInfoLabel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    public ProfilePanel(User user, Runnable onLogout) {
        this.activeUser = user;
        this.logoutAction = onLogout;
        this.rentalService = RentalService.getInstance();

        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(25, 40, 40, 40));
        setBackground(new Color(245, 247, 250));

        // Header Utama
        JLabel headerLabel = new JLabel("Pengaturan Akun");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(headerLabel, BorderLayout.NORTH);

        JComponent rightPanel = createRightContentPanel();

        JComponent leftPanel = createLeftNavPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.1);
        splitPane.setBorder(BorderFactory.createLineBorder(new Color(220, 223, 228)));

        add(splitPane, BorderLayout.CENTER);
    }

    // Membuat panel navigasi di sebelah kiri
    private JComponent createLeftNavPanel() {
        String[] menuItems = {"Informasi Akun", "Edit Profil", "Keamanan", "Aktivitas Akun"};
        JList<String> navList = new JList<>(menuItems);
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setFont(new Font("Segoe UI", Font.BOLD, 16));
        navList.setFixedCellHeight(50);
        navList.setCellRenderer(new NavListCellRenderer());
        navList.setBackground(new Color(250, 251, 252));

        navList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = navList.getSelectedValue();
                if ("Informasi Akun".equals(selectedValue)) {
                    cardLayout.show(rightContentPanel, "INFO");
                } else if ("Edit Profil".equals(selectedValue)) {
                    cardLayout.show(rightContentPanel, "EDIT");
                } else if ("Keamanan".equals(selectedValue)) {
                    cardLayout.show(rightContentPanel, "SECURITY");
                } else if ("Aktivitas Akun".equals(selectedValue)) {
                    refreshActivityHistory(); // Refresh riwayat setiap kali diklik
                    cardLayout.show(rightContentPanel, "ACTIVITY");
                }
            }
        });

        navList.setSelectedIndex(0);
        return new JScrollPane(navList);
    }

    // Membuat panel konten di sebelah kanan dengan CardLayout
    private JPanel createRightContentPanel() {
        cardLayout = new CardLayout();
        rightContentPanel = new JPanel(cardLayout);
        rightContentPanel.add(createAccountInfoPanel(), "INFO");
        rightContentPanel.add(createEditProfilePanel(), "EDIT");
        rightContentPanel.add(createSecurityPanel(), "SECURITY");
        rightContentPanel.add(createActivityPanel(), "ACTIVITY");
        return rightContentPanel;
    }

    // Panel untuk menampilkan info akun
    private JPanel createAccountInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);

        avatarLabel = new JLabel();
        loadAvatar();
        panel.add(avatarLabel, gbc);

        usernameInfoLabel = new JLabel(activeUser.getUsername());
        usernameInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panel.add(usernameInfoLabel, gbc);

        roleInfoLabel = new JLabel("Login sebagai: " + activeUser.getRole());
        roleInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        roleInfoLabel.setForeground(Color.GRAY);
        panel.add(roleInfoLabel, gbc);

        return panel;
    }

    // Panel untuk menampilkan form edit profil
    private JPanel createEditProfilePanel() {
        JPanel panel = createTitledPanel("Edit Informasi Profil");
        Pelanggan p = rentalService.findPelangganByUsername(activeUser.getUsername());

        JTextField phoneField = new JTextField(p != null ? p.getNoTelepon() : "");
        JButton savePhoneButton = new JButton("Simpan No. Telepon");

        panel.add(createFormRow("No. Telepon:", phoneField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(savePhoneButton);

        savePhoneButton.addActionListener(e -> {
            if(rentalService.updateProfil(activeUser.getUsername(), phoneField.getText().trim())){
                JOptionPane.showMessageDialog(this, "No. Telepon berhasil diperbarui.");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui No. Telepon.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(Box.createVerticalStrut(20));

        JButton changeAvatarButton = new JButton("Ubah Foto Profil");
        panel.add(changeAvatarButton);
        changeAvatarButton.addActionListener(e -> chooseAndUploadAvatar());

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // Panel untuk menampilkan pengaturan keamanan
    private JPanel createSecurityPanel() {
        JPanel panel = createTitledPanel("Keamanan Akun");

        JButton changePasswordButton = createActionButton("Ubah Password", "🔑");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        panel.add(changePasswordButton);
        panel.add(Box.createVerticalStrut(10));

        JButton deleteButton = createActionButton("Hapus Akun Saya", "🗑️");
        deleteButton.setForeground(new Color(211, 47, 47));
        deleteButton.addActionListener(e -> showDeleteAccountDialog());
        panel.add(deleteButton);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // Panel untuk menampilkan riwayat aktivitas login
    private JPanel createActivityPanel() {
        JPanel panel = createTitledPanel("Aktivitas Login Terbaru");
        historyTableModel = new DefaultTableModel(new String[]{"Waktu Login", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        panel.add(new JScrollPane(historyTable));
        return panel;
    }

    // --- DIALOGS DAN LOGIKA FITUR ---

    private void chooseAndUploadAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Foto Profil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Path destDir = Paths.get("data/avatars");
                Files.createDirectories(destDir);
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                Path destPath = destDir.resolve(activeUser.getUsername() + extension);
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                if (rentalService.updateAvatar(activeUser.getUsername(), destPath.toString())) {
                    activeUser.setAvatarUrl(destPath.toString());
                    loadAvatar();
                    JOptionPane.showMessageDialog(this, "Foto profil berhasil diperbarui.");
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan path avatar ke database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal memproses file gambar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField oldPf = new JPasswordField(20); JPasswordField newPf = new JPasswordField(20); JPasswordField confPf = new JPasswordField(20);
        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Password Lama:"), gbc); gbc.gridx=1; panel.add(oldPf, gbc);
        gbc.gridy=1; gbc.gridx=0; panel.add(new JLabel("Password Baru:"), gbc); gbc.gridx=1; panel.add(newPf, gbc);
        gbc.gridy=2; gbc.gridx=0; panel.add(new JLabel("Konfirmasi Password Baru:"), gbc); gbc.gridx=1; panel.add(confPf, gbc);
        if (JOptionPane.showConfirmDialog(this, panel, "Ubah Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String o = new String(oldPf.getPassword()); String n = new String(newPf.getPassword()); String c = new String(confPf.getPassword());
            if (o.isEmpty() || n.isEmpty() || c.isEmpty()) { JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (!n.equals(c)) { JOptionPane.showMessageDialog(this, "Password baru dan konfirmasi tidak cocok!", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (rentalService.ubahPassword(activeUser.getUsername(), o, n)) JOptionPane.showMessageDialog(this, "Password berhasil diubah.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(this, "Gagal mengubah password. Pastikan password lama Anda benar.", "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteAccountDialog() {
        JPasswordField pf = new JPasswordField(20);
        JPanel p = new JPanel(new BorderLayout(5,5));
        p.add(new JLabel("Untuk konfirmasi, masukkan password Anda:"), BorderLayout.NORTH);
        p.add(pf, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, p, "Konfirmasi Hapus Akun", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            if (rentalService.deleteAccount(activeUser.getUsername(), new String(pf.getPassword()))) {
                JOptionPane.showMessageDialog(null, "Akun Anda telah berhasil dihapus.", "Akun Dihapus", JOptionPane.INFORMATION_MESSAGE);
                logoutAction.run();
            } else JOptionPane.showMessageDialog(this, "Gagal menghapus akun. Pastikan password Anda benar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- HELPER UNTUK UI ---

    public void refreshContent() {
        loadAvatar();
        usernameInfoLabel.setText(activeUser.getUsername());
        roleInfoLabel.setText("Login sebagai: " + activeUser.getRole());
        refreshActivityHistory();
    }

    private void refreshActivityHistory() {
        if (historyTableModel != null) {
            historyTableModel.setRowCount(0);
            List<LoginHistory> history = rentalService.getLoginHistory(activeUser.getUsername());
            for (LoginHistory item : history) {
                historyTableModel.addRow(new Object[]{item.getFormattedLoginTime(), item.getStatus()});
            }
        }
    }

    private void loadAvatar() {
        String path = activeUser.getAvatarUrl();
        ImageIcon icon;
        int size = 120;
        if (path != null && !path.isEmpty() && new File(path).exists()) {
            icon = new ImageIcon(path);
        } else {
            JLabel temp = new JLabel("👤"); temp.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
            icon = new ImageIcon();
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics(); temp.paint(g2d); g2d.dispose();
            icon.setImage(img);
        }
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        avatarLabel.setIcon(new ImageIcon(scaled));
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        avatarLabel.setPreferredSize(new Dimension(size + 2, size + 2));
    }

    private JPanel createTitledPanel(String title) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(t); p.add(Box.createVerticalStrut(15)); return p;
    }

    private JButton createActionButton(String text, String icon) {
        JLabel i = new JLabel(icon); i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JButton b = new JButton(text); b.setIcon(i.getIcon());
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); return b;
    }

    private static class NavListCellRenderer extends DefaultListCellRenderer {
        private final Border selBorder = new MatteBorder(0, 5, 0, 0, new Color(25, 118, 210));
        private final Border empty = new EmptyBorder(5, 15, 5, 15);
        public Component getListCellRendererComponent(JList<?> l, Object val, int i, boolean isSel, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(l, val, i, isSel, cellHasFocus);
            label.setBorder(empty);
            if (isSel) {
                label.setBorder(BorderFactory.createCompoundBorder(selBorder, empty));
                label.setBackground(new Color(236, 244, 255));
                label.setForeground(new Color(25, 118, 210));
            } else {
                label.setBackground(new Color(250, 251, 252));
                label.setForeground(UIManager.getColor("Label.foreground"));
            }
            return label;
        }
    }

    private JPanel createFormRow(String labelText, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row.add(label, BorderLayout.NORTH);
        row.add(component, BorderLayout.CENTER);
        return row;
    }
}