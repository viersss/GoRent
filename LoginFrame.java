// =================================================================================
// File 10: src/LoginFrame.java
// =================================================================================
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class LoginFrame extends JFrame {
    private final RentalService rentalService = RentalService.getInstance();
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login - GoRent");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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

        JLabel welcomeText = new JLabel("<html><center>Solusi Rental Kendaraan<br>Terbaik dan Terpercaya</center></html>");
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
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 20, 0);

        JLabel loginTitle = new JLabel("Login ke Akun Anda");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(loginTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy++; gbc.gridx = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; usernameField = new JTextField(20); panel.add(usernameField, gbc);

        gbc.gridy++; gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; passwordField = new JPasswordField(20); panel.add(passwordField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Belum punya akun? Register");
        registerButton.putClientProperty( "JButton.buttonType", "borderless" );
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(loginButton); buttonPanel.add(registerButton);
        panel.add(buttonPanel, gbc);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        return panel;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User loggedInUser = rentalService.login(username, password);
        if (loggedInUser != null) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    if (loggedInUser.getRole() == Role.ADMIN) new RentalKendaraanApp(loggedInUser).setVisible(true);
                    else if (loggedInUser.getRole() == Role.USER) new UserFrame(loggedInUser).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Gagal membuka jendela utama:\n" + ex.getMessage(), "Error Kritis", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Username atau password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}