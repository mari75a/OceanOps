package GUI;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private JLabel logoLabel;

    public Login() {
        // Use FlatLaf modern dark theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
    }

    private void initComponents() {
        setTitle("OceanOps - Login");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(11, 30, 45)); // Dark blue

        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Logo
        logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoLabel);
        setLogoAutoResize("/img/oceanOpsLogo (1).png");

        mainPanel.add(Box.createVerticalStrut(20));

        // Title
        JLabel titleLabel = new JLabel("OceanOps");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 25));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        JLabel titleLabe2 = new JLabel("Login");
        titleLabe2.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabe2.setForeground(Color.WHITE);
        titleLabe2.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabe2);

        mainPanel.add(Box.createVerticalStrut(30));

        // Username
        usernameField = new JTextField();
        styleTextField(usernameField, "Username");
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Password
        passwordField = new JPasswordField();
        styleTextField(passwordField, "Password");
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(25));

        // Login Button
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        java.sql.ResultSet rs = model.Mysql.search("SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'");
        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new Dashboard(); // launch your main app
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
});

        styleButton(loginButton);
        mainPanel.add(loginButton);

        mainPanel.add(Box.createVerticalStrut(10));

        // Register Button
        registerButton = new JButton("Register");
        registerButton.addActionListener(e->{
        dispose();
        new Register();
        });
        styleButton(registerButton);
        registerButton.setBackground(new Color(74, 144, 226));
        mainPanel.add(registerButton);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBackground(new Color(21, 44, 62));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        field.setToolTipText(placeholder);
    }

    private void styleButton(JButton button) {
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // This is the key
    button.setAlignmentX(Component.CENTER_ALIGNMENT);            // Align to center
    button.setBackground(new Color(45, 156, 219));
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Inter", Font.BOLD, 14));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
}

    private void setLogoAutoResize(String imagePath) {
        ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
        logoLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));

        // Optional auto-scale on resize
        logoLabel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                int size = logoLabel.getWidth();
                Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}
