package com.funcity.ui.auth;

import com.funcity.model.User;
import com.funcity.service.AuthenticationService;
import com.funcity.ui.MainFrame;
import com.funcity.ui.UserSession;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {

    private final AuthenticationService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton loginButton;

    public LoginForm() {
        this.authService = new AuthenticationService();
        initUI();
    }

    private void initUI() {
        setTitle("Fun City 2.0 - Arcade Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(460, 560);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel logoLabel = new JLabel("🎡 FUN CITY 2.0", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(UIHelper.PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Cashless Arcade Management Platform", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField("admin");
        usernameField.setPreferredSize(new Dimension(360, 38));
        usernameField.setMaximumSize(new Dimension(360, 38));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("Admin@123");
        passwordField.setPreferredSize(new Dimension(360, 38));
        passwordField.setMaximumSize(new Dimension(360, 38));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(UIHelper.DANGER_COLOR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton = UIHelper.createPrimaryButton("Sign In");
        loginButton.setPreferredSize(new Dimension(360, 42));
        loginButton.setMaximumSize(new Dimension(360, 42));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        // Enter key submits form
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> performLogin());

        // Quick Role Switcher for review/testing convenience
        JPanel quickPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        quickPanel.setOpaque(false);
        quickPanel.setMaximumSize(new Dimension(360, 32));

        JButton btnAdmin = new JButton("👑 Admin");
        btnAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAdmin.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("Admin@123");
            errorLabel.setText(" ");
        });

        JButton btnStaff = new JButton("🎟️ Staff");
        btnStaff.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnStaff.addActionListener(e -> {
            usernameField.setText("staff1");
            passwordField.setText("Staff@123");
            errorLabel.setText(" ");
        });

        JButton btnTech = new JButton("🔧 Tech");
        btnTech.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnTech.addActionListener(e -> {
            usernameField.setText("tech1");
            passwordField.setText("Tech@123");
            errorLabel.setText(" ");
        });

        quickPanel.add(btnAdmin);
        quickPanel.add(btnStaff);
        quickPanel.add(btnTech);

        JLabel quickLabel = new JLabel("Quick Login Preset:");
        quickLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        quickLabel.setForeground(new Color(148, 163, 184));
        quickLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Layout construction
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));

        mainPanel.add(userLabel);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(15));

        mainPanel.add(passLabel);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(errorLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(25));

        mainPanel.add(quickLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(quickPanel);

        add(mainPanel);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        loginButton.setEnabled(false);
        errorLabel.setText("Authenticating...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.authenticate(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    UserSession.login(user);
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}

