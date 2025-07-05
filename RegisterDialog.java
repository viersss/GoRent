// =================================================================================
// File 11: src/RegisterDialog.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class RegisterDialog extends JDialog {
    private final RentalService rentalService = RentalService.getInstance();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<Role> roleComboBox;

    public RegisterDialog(Frame owner) {
        super(owner, "Register Akun Baru - GoRent", true);
        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createBrandingPanel(), createFormPanel());
        splitPane.setDividerLocation(350);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(0);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(35, 40, 50));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);
        JLabel appName = new JLabel("GoRent");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 48));
        appName.setForeground(Color.WHITE);
        panel.add(appName, gbc);
        JLabel welcomeText = new JLabel("<html><center>Satu Langkah Lagi untuk<br>Memulai Petualangan Anda</center></html>");
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeText.setForeground(new Color(200, 200, 200));
        welcomeText.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(welcomeText, gbc);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel registerTitle = new JLabel("Buat Akun Baru");
        registerTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(registerTitle, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++; gbc.gridx = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; usernameField = new JTextField(20); panel.add(usernameField, gbc);
        gbc.gridy++; gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; passwordField = new JPasswordField(20); panel.add(passwordField, gbc);
        gbc.gridy++; gbc.gridx = 0; panel.add(new JLabel("Konfirmasi Password:"), gbc);
        gbc.gridx = 1; confirmPasswordField = new JPasswordField(20); panel.add(confirmPasswordField, gbc);
        gbc.gridy++; gbc.gridx = 0; panel.add(new JLabel("Daftar Sebagai:"), gbc);
        gbc.gridx = 1; roleComboBox = new JComboBox<>(new Role[]{Role.USER, Role.ADMIN}); panel.add(roleComboBox, gbc);
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton registerButton = new JButton("Daftarkan Akun");
        JButton backButton = new JButton("Kembali ke Login");
        backButton.putClientProperty("JButton.buttonType", "borderless");
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(registerButton); buttonPanel.add(backButton);
        panel.add(buttonPanel, gbc);
        registerButton.addActionListener(e -> attemptRegister());
        backButton.addActionListener(e -> dispose());
        return panel;
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        Role role = (Role) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi tidak cocok!", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        if (rentalService.register(username, password, role)) {
            JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username '" + username + "' sudah digunakan.", "Registrasi Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}