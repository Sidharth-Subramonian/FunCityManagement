package com.funcity.ui.admin;

import com.funcity.model.User;
import com.funcity.service.AuthenticationService;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final AuthenticationService authService;
    private DefaultTableModel tableModel;
    private JTable userTable;

    public UserManagementPanel() {
        this.authService = new AuthenticationService();
        initUI();
        refreshUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("🛡️ Staff & Technician User Accounts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton addBtn = UIHelper.createPrimaryButton("+ Create User Account");
        addBtn.addActionListener(e -> openAddUserDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{
                "User ID", "Username", "Full Name", "Email", "Assigned Role", "Status", "Created Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        UIHelper.styleTable(userTable);

        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton toggleBtn = new JButton("Toggle Active/Inactive");
        toggleBtn.addActionListener(e -> handleToggleStatus());

        actions.add(toggleBtn);
        add(actions, BorderLayout.SOUTH);
    }

    private void refreshUsers() {
        tableModel.setRowCount(0);
        List<User> list = authService.getAllUsers();
        for (User u : list) {
            tableModel.addRow(new Object[]{
                    u.getUserId(), u.getUsername(), u.getFullName(), u.getEmail(),
                    u.getRole(), u.getStatus(), UIHelper.formatDate(u.getCreatedAt())
            });
        }
    }

    private void handleToggleStatus() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a user from the table.");
            return;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        if ("admin".equalsIgnoreCase(username)) {
            UIHelper.showError(this, "The primary administrator account cannot be deactivated.");
            return;
        }

        List<User> users = authService.getAllUsers();
        users.stream().filter(u -> u.getUserId() == userId).findFirst().ifPresent(u -> {
            u.setStatus("ACTIVE".equalsIgnoreCase(u.getStatus()) ? "INACTIVE" : "ACTIVE");
            authService.updateUser(u);
            refreshUsers();
        });
    }

    private void openAddUserDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create System Account", true);
        dlg.setSize(400, 340);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleCb = new JComboBox<>(new String[]{"STAFF", "TECHNICIAN", "ADMIN"});

        p.add(new JLabel("Username:")); p.add(usernameField);
        p.add(new JLabel("Password:")); p.add(passField);
        p.add(new JLabel("Full Name:")); p.add(nameField);
        p.add(new JLabel("Email Address:")); p.add(emailField);
        p.add(new JLabel("Role:")); p.add(roleCb);

        JButton saveBtn = UIHelper.createPrimaryButton("Create Account");
        saveBtn.addActionListener(e -> {
            try {
                authService.registerUser(
                        usernameField.getText().trim(),
                        new String(passField.getPassword()),
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        (String) roleCb.getSelectedItem()
                );
                UIHelper.showSuccess(dlg, "User account created successfully!");
                dlg.dispose();
                refreshUsers();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });

        p.add(new JLabel("")); p.add(saveBtn);
        dlg.add(p);
        dlg.setVisible(true);
    }
}

