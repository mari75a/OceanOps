package GUI;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

// Placeholder for text field
class HintTextField extends JTextField {
    private final String hint;

    public HintTextField(String hint) {
        this.hint = hint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !hasFocus()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            g2.setColor(new Color(160, 160, 160));
            g2.drawString(hint, getInsets().left + 5, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 4);
            g2.dispose();
        }
    }
}

// Placeholder for password field
class HintPasswordField extends JPasswordField {
    private final String hint;

    public HintPasswordField(String hint) {
        this.hint = hint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !hasFocus()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            g2.setColor(new Color(160, 160, 160));
            g2.drawString(hint, getInsets().left + 5, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 4);
            g2.dispose();
        }
    }
}

public class Register extends JFrame {

    private JTextField fullNameField, emailField, usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton;
    private JLabel logoLabel;
private JButton goToLoginButton;

    public Register() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("OceanOps - Register");
        setSize(420, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(11, 30, 45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Logo (optional)
        logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoLabel);
        setLogo("/img/oceanOpsLogo (1).png");

        mainPanel.add(Box.createVerticalStrut(20));

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(30));

        // Full Name
        fullNameField = new HintTextField("Full Name");
        styleTextField(fullNameField);
        mainPanel.add(fullNameField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Email
        emailField = new HintTextField("Email");
        styleTextField(emailField);
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Username
        usernameField = new HintTextField("Username");
        styleTextField(usernameField);
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Password
        passwordField = new HintPasswordField("Password");
        styleTextField(passwordField);
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Confirm Password
        confirmPasswordField = new HintPasswordField("Confirm Password");
        styleTextField(confirmPasswordField);
        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createVerticalStrut(25));

        // Register Button
        registerButton = new JButton("Register");
        
        registerButton.addActionListener(e -> {
    String fullName = fullNameField.getText().trim();
    String email = emailField.getText().trim();
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();
    String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

    if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (!password.equals(confirmPassword)) {
        JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        // Check if username already exists
        java.sql.ResultSet rs = model.Mysql.search("SELECT * FROM users WHERE username = '" + username + "'");
        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Username already exists. Choose another one.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert user
        String query = "INSERT INTO users (username, password, role) VALUES ('" + username + "', '" + password + "', 'operator')";
        model.Mysql.iud(query);

        JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Optional: Redirect to login
        dispose();
        new Login();

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
});

        
        styleButton(registerButton);
        mainPanel.add(registerButton);
        mainPanel.add(Box.createVerticalStrut(10));

goToLoginButton = new JButton("Already have an account? Login");
styleFlatButton(goToLoginButton);
mainPanel.add(goToLoginButton);

goToLoginButton.addActionListener(e -> {
    dispose(); // Close register window
    new Login(); // Open login window (make sure Login class exists)
});

        setContentPane(mainPanel);
        setVisible(true);
    }
private void styleFlatButton(JButton button) {
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setBackground(new Color(0, 0, 0, 0)); // Transparent
    button.setForeground(new Color(130, 180, 250)); // Light blue text
    button.setFont(new Font("Inter", Font.PLAIN, 13));
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}
    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBackground(new Color(21, 44, 62));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void styleButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(45, 156, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setLogo(String imagePath) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image image = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            System.err.println("Logo not found: " + imagePath);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Register::new);
    }
}
