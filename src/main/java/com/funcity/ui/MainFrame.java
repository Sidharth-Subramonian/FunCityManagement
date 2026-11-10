package com.funcity.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.funcity.model.User;
import com.funcity.ui.admin.*;
import com.funcity.ui.auth.LoginForm;
import com.funcity.ui.dashboard.DashboardPanel;
import com.funcity.ui.staff.StaffPOSPanel;
import com.funcity.ui.technician.TechnicianPanel;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane mainTabbedPane;

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Fun City 2.0 - Arcade & Amusement Centre Management System");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());

        // 1. TOP HEADER BAR
        JPanel headerBar = new JPanel(new BorderLayout(15, 0));
        headerBar.setBackground(Color.WHITE);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));

        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        JLabel brandLogo = new JLabel("🎡 FUN CITY 2.0");
        brandLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLogo.setForeground(UIHelper.PRIMARY_COLOR);

        JLabel brandSub = new JLabel("| Smart Cashless Arcade System");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        brandSub.setForeground(new Color(100, 116, 139));

        brandPanel.add(brandLogo);
        brandPanel.add(brandSub);

        // User info & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        User currentUser = UserSession.getCurrentUser();
        String displayName = (currentUser != null) ? currentUser.getFullName() : "Administrator";
        String role = (currentUser != null) ? currentUser.getRole() : "ADMIN";

        JLabel userLabel = new JLabel("👤 " + displayName);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel roleBadge = new JLabel("  " + role + "  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setOpaque(true);

        if ("ADMIN".equalsIgnoreCase(role)) {
            roleBadge.setBackground(new Color(239, 246, 255));
            roleBadge.setForeground(UIHelper.PRIMARY_COLOR);
            roleBadge.setBorder(BorderFactory.createLineBorder(UIHelper.PRIMARY_COLOR));
        } else if ("STAFF".equalsIgnoreCase(role)) {
            roleBadge.setBackground(new Color(240, 253, 244));
            roleBadge.setForeground(UIHelper.SUCCESS_COLOR);
            roleBadge.setBorder(BorderFactory.createLineBorder(UIHelper.SUCCESS_COLOR));
        } else {
            roleBadge.setBackground(new Color(254, 243, 199));
            roleBadge.setForeground(UIHelper.WARNING_COLOR);
            roleBadge.setBorder(BorderFactory.createLineBorder(UIHelper.WARNING_COLOR));
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutBtn.addActionListener(e -> handleLogout());

        userPanel.add(userLabel);
        userPanel.add(roleBadge);
        userPanel.add(logoutBtn);

        headerBar.add(brandPanel, BorderLayout.WEST);
        headerBar.add(userPanel, BorderLayout.EAST);
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // 2. MAIN NAVIGATION TABS (Role-based)
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        if (UserSession.isAdmin()) {
            mainTabbedPane.addTab("📊 Analytics Dashboard", new DashboardPanel());
            mainTabbedPane.addTab("🎟️ POS Cashier Terminal", new StaffPOSPanel());
            mainTabbedPane.addTab("👥 Customers & Cards", new CustomerManagementPanel());
            mainTabbedPane.addTab("🎮 Games & Pricing", new GameManagementPanel());
            mainTabbedPane.addTab("🕹️ Machines", new MachineManagementPanel());
            mainTabbedPane.addTab("🔧 Maintenance Console", new TechnicianPanel());
            mainTabbedPane.addTab("📜 All Ledgers", new AllTransactionsPanel());
            mainTabbedPane.addTab("🛡️ User Accounts", new UserManagementPanel());
        } else if (UserSession.isStaff()) {
            mainTabbedPane.addTab("🎟️ POS Cashier Terminal", new StaffPOSPanel());
            mainTabbedPane.addTab("👥 Customers & Cards", new CustomerManagementPanel());
            mainTabbedPane.addTab("📜 Permitted Ledgers", new AllTransactionsPanel());
        } else if (UserSession.isTechnician()) {
            mainTabbedPane.addTab("🔧 Maintenance & Machine Console", new TechnicianPanel());
        } else {
            // Default fallback
            mainTabbedPane.addTab("🎟️ POS Cashier Terminal", new StaffPOSPanel());
        }

        rootPanel.add(mainTabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    private void handleLogout() {
        if (UIHelper.confirm(this, "Are you sure you want to log out of Fun City 2.0?", "Confirm Logout")) {
            UserSession.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}

