package com.funcity.ui.staff;

import com.funcity.model.*;
import com.funcity.service.*;
import com.funcity.ui.UserSession;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class StaffPOSPanel extends JPanel {

    private final CustomerService customerService;
    private final MembershipService membershipService;
    private final WalletService walletService;
    private final PlayService playService;
    private final MachineService machineService;
    private final RedemptionService redemptionService;
    private final TicketService ticketService;

    // Active Customer State
    private Customer activeCustomer;
    private MembershipCard activeCard;

    // Header Components
    private JTextField cardSearchField;
    private JLabel custNameLabel;
    private JLabel custCodeLabel;
    private JLabel cardStatusLabel;
    private JLabel walletBalanceLabel;
    private JLabel ticketBalanceLabel;
    private JLabel loyaltyPointsLabel;

    // Recharge Components
    private JTextField customAmountField;

    // Play Session Components
    private JComboBox<MachineItem> machineComboBox;
    private DefaultTableModel activeSessionsModel;
    private JTable activeSessionsTable;

    // Prize Redemption Components
    private DefaultTableModel prizeTableModel;
    private JTable prizeTable;
    private JSpinner prizeQtySpinner;

    public StaffPOSPanel() {
        this.customerService = new CustomerService();
        this.membershipService = new MembershipService();
        this.walletService = new WalletService();
        this.playService = new PlayService();
        this.machineService = new MachineService();
        this.redemptionService = new RedemptionService();
        this.ticketService = new TicketService();

        initUI();
        refreshOperationalMachines();
        refreshPrizes();
        refreshActiveSessions();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // ==========================================
        // 1. TOP HEADER: Card Tap & Customer Profile
        // ==========================================
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Search / Tap input
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel tapIcon = new JLabel("💳 Tap / Scan RFID Card:");
        tapIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cardSearchField = new JTextField(15);
        cardSearchField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardSearchField.setText("FC-A1001001"); // Default demo card for quick testing
        cardSearchField.addActionListener(e -> lookupCard());

        JButton searchBtn = UIHelper.createPrimaryButton("Lookup Card");
        searchBtn.addActionListener(e -> lookupCard());

        JButton newCustBtn = UIHelper.createSuccessButton("+ New Customer");
        newCustBtn.addActionListener(e -> openCustomerRegistrationDialog());

        searchPanel.add(tapIcon);
        searchPanel.add(cardSearchField);
        searchPanel.add(searchBtn);
        searchPanel.add(newCustBtn);

        // Profile details cards
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        profilePanel.setOpaque(false);

        custNameLabel = new JLabel("No Card Scanned");
        custNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        custCodeLabel = new JLabel("-");
        custCodeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        custCodeLabel.setForeground(new Color(100, 116, 139));

        cardStatusLabel = new JLabel("");
        cardStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        walletBalanceLabel = new JLabel("$0.00");
        walletBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        walletBalanceLabel.setForeground(UIHelper.SUCCESS_COLOR);

        ticketBalanceLabel = new JLabel("0 🎟️");
        ticketBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        ticketBalanceLabel.setForeground(new Color(147, 51, 234)); // Purple

        loyaltyPointsLabel = new JLabel("0 pts");
        loyaltyPointsLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loyaltyPointsLabel.setForeground(UIHelper.WARNING_COLOR);

        JPanel nameBox = new JPanel(new GridLayout(2, 1));
        nameBox.setOpaque(false);
        nameBox.add(custNameLabel);
        nameBox.add(custCodeLabel);

        JPanel balBox = new JPanel(new GridLayout(2, 1));
        balBox.setOpaque(false);
        balBox.add(new JLabel("Wallet Balance:"));
        balBox.add(walletBalanceLabel);

        JPanel ticketBox = new JPanel(new GridLayout(2, 1));
        ticketBox.setOpaque(false);
        ticketBox.add(new JLabel("Tickets Available:"));
        ticketBox.add(ticketBalanceLabel);

        JPanel ptBox = new JPanel(new GridLayout(2, 1));
        ptBox.setOpaque(false);
        ptBox.add(new JLabel("Loyalty Points:"));
        ptBox.add(loyaltyPointsLabel);

        profilePanel.add(nameBox);
        profilePanel.add(cardStatusLabel);
        profilePanel.add(balBox);
        profilePanel.add(ticketBox);
        profilePanel.add(ptBox);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(profilePanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. TABS: POS Cashier Operations
        // ==========================================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("💵 Wallet Recharge", createRechargeTab());
        tabbedPane.addTab("🎮 Initiate Play", createPlayTab());
        tabbedPane.addTab("🎟️ Complete Play & Log Tickets", createActiveSessionsTab());
        tabbedPane.addTab("🎁 Prize Redemption", createRedemptionTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Auto-load demo customer
        lookupCard();
    }

    private JPanel createRechargeTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Select Cash Recharge Amount");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        p.add(title, gbc);

        // Quick amount buttons
        int[] quickAmounts = {10, 20, 50, 100};
        gbc.gridy = 1; gbc.gridwidth = 1;
        for (int i = 0; i < quickAmounts.length; i++) {
            final int amt = quickAmounts[i];
            JButton btn = new JButton("$" + amt);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setPreferredSize(new Dimension(100, 45));
            btn.addActionListener(e -> executeRecharge(BigDecimal.valueOf(amt)));
            gbc.gridx = i;
            p.add(btn, gbc);
        }

        // Custom amount row
        JLabel customLabel = new JLabel("Or Custom Amount ($):");
        customLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        p.add(customLabel, gbc);

        customAmountField = new JTextField(10);
        customAmountField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 1;
        p.add(customAmountField, gbc);

        JButton customBtn = UIHelper.createPrimaryButton("Process Recharge");
        customBtn.addActionListener(e -> {
            try {
                BigDecimal amt = new BigDecimal(customAmountField.getText().trim());
                executeRecharge(amt);
            } catch (Exception ex) {
                UIHelper.showError(this, "Please enter a valid numeric dollar amount.");
            }
        });
        gbc.gridx = 3; gbc.gridy = 2; gbc.gridwidth = 1;
        p.add(customBtn, gbc);

        return p;
    }

    private JPanel createPlayTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Initiate Arcade Game Session");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        p.add(title, gbc);

        JLabel selectLabel = new JLabel("Select Operational Machine:");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        p.add(selectLabel, gbc);

        machineComboBox = new JComboBox<>();
        machineComboBox.setPreferredSize(new Dimension(350, 36));
        machineComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 1;
        p.add(machineComboBox, gbc);

        JButton playBtn = UIHelper.createSuccessButton("🎮 Authorize & Tap Play");
        playBtn.setPreferredSize(new Dimension(220, 44));
        playBtn.addActionListener(e -> executePlayInitiation());
        gbc.gridx = 1; gbc.gridy = 2;
        p.add(playBtn, gbc);

        return p;
    }

    private JPanel createActiveSessionsTab() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        activeSessionsModel = new DefaultTableModel(new String[]{
                "Session Ref", "Machine", "Game", "Fee", "Started At", "Customer", "Session ID"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        activeSessionsTable = new JTable(activeSessionsModel);
        UIHelper.styleTable(activeSessionsTable);
        // Hide Session ID column from visual display
        activeSessionsTable.removeColumn(activeSessionsTable.getColumnModel().getColumn(6));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setOpaque(false);

        JLabel ticketLabel = new JLabel("Tickets Won:");
        ticketLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JTextField ticketsWonField = new JTextField("50", 6);
        ticketsWonField.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton completeBtn = UIHelper.createSuccessButton("✓ Complete & Award Tickets");
        completeBtn.addActionListener(e -> {
            int selectedRow = activeSessionsTable.getSelectedRow();
            if (selectedRow < 0) {
                UIHelper.showError(this, "Please select an active session from the table.");
                return;
            }
            int sessionId = (int) activeSessionsModel.getValueAt(selectedRow, 6);
            try {
                int tickets = Integer.parseInt(ticketsWonField.getText().trim());
                Integer staffId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;
                playService.completePlaySession(sessionId, tickets, staffId);
                UIHelper.showSuccess(this, "Session completed successfully! Awarded " + tickets + " tickets.");
                refreshActiveSessions();
                lookupCard();
            } catch (Exception ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        });

        JButton cancelBtn = UIHelper.createDangerButton("✕ Cancel & Refund");
        cancelBtn.addActionListener(e -> {
            int selectedRow = activeSessionsTable.getSelectedRow();
            if (selectedRow < 0) {
                UIHelper.showError(this, "Please select an active session from the table.");
                return;
            }
            int sessionId = (int) activeSessionsModel.getValueAt(selectedRow, 6);
            String reason = JOptionPane.showInputDialog(this, "Enter reason for cancellation & refund:", "Machine Glitch");
            if (reason != null && !reason.trim().isEmpty()) {
                Integer staffId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;
                playService.cancelPlaySession(sessionId, reason, staffId);
                UIHelper.showSuccess(this, "Session cancelled. Game fee refunded to customer wallet.");
                refreshActiveSessions();
                lookupCard();
            }
        });

        JButton refreshSessionsBtn = new JButton("🔄 Refresh Sessions");
        refreshSessionsBtn.addActionListener(e -> refreshActiveSessions());

        controlPanel.add(ticketLabel);
        controlPanel.add(ticketsWonField);
        controlPanel.add(completeBtn);
        controlPanel.add(cancelBtn);
        controlPanel.add(refreshSessionsBtn);

        p.add(new JScrollPane(activeSessionsTable), BorderLayout.CENTER);
        p.add(controlPanel, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createRedemptionTab() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        prizeTableModel = new DefaultTableModel(new String[]{
                "Prize Code", "Prize Name", "Category", "Ticket Cost", "Stock Available", "Prize ID"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        prizeTable = new JTable(prizeTableModel);
        UIHelper.styleTable(prizeTable);
        prizeTable.removeColumn(prizeTable.getColumnModel().getColumn(5));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setOpaque(false);

        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        prizeQtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        prizeQtySpinner.setPreferredSize(new Dimension(60, 30));

        JButton redeemBtn = UIHelper.createSuccessButton("🎁 Redeem Selected Prize");
        redeemBtn.addActionListener(e -> executeRedemption());

        JButton refreshPrizesBtn = new JButton("🔄 Refresh Prizes");
        refreshPrizesBtn.addActionListener(e -> refreshPrizes());

        controlPanel.add(qtyLabel);
        controlPanel.add(prizeQtySpinner);
        controlPanel.add(redeemBtn);
        controlPanel.add(refreshPrizesBtn);

        p.add(new JScrollPane(prizeTable), BorderLayout.CENTER);
        p.add(controlPanel, BorderLayout.SOUTH);

        return p;
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    private void lookupCard() {
        String input = cardSearchField.getText().trim();
        if (input.isEmpty()) return;

        Optional<Customer> custOpt = Optional.empty();
        if (input.toUpperCase().startsWith("FC-")) {
            custOpt = customerService.getCustomerByCardNumber(input);
        } else if (input.toUpperCase().startsWith("CUST-")) {
            custOpt = customerService.getCustomerByCode(input);
        } else {
            List<Customer> list = customerService.searchCustomers(input);
            if (!list.isEmpty()) {
                custOpt = Optional.of(list.get(0));
            }
        }

        if (custOpt.isPresent()) {
            activeCustomer = custOpt.get();
            custNameLabel.setText(activeCustomer.getName());
            custCodeLabel.setText(activeCustomer.getCustomerCode() + " | " + activeCustomer.getPhone());
            walletBalanceLabel.setText(UIHelper.formatCurrency(activeCustomer.getWalletBalance()));
            ticketBalanceLabel.setText(activeCustomer.getTicketBalance() + " 🎟️");
            loyaltyPointsLabel.setText(activeCustomer.getLoyaltyPoints() + " pts");

            Optional<MembershipCard> cardOpt = membershipService.getActiveCardForCustomer(activeCustomer.getCustomerId());
            if (cardOpt.isPresent()) {
                activeCard = cardOpt.get();
                cardStatusLabel.setText("ACTIVE");
                cardStatusLabel.setForeground(UIHelper.SUCCESS_COLOR);
            } else {
                activeCard = null;
                cardStatusLabel.setText("NO ACTIVE CARD");
                cardStatusLabel.setForeground(UIHelper.DANGER_COLOR);
            }
        } else {
            activeCustomer = null;
            activeCard = null;
            custNameLabel.setText("Customer Not Found");
            custCodeLabel.setText("-");
            cardStatusLabel.setText("");
            walletBalanceLabel.setText("$0.00");
            ticketBalanceLabel.setText("0 🎟️");
            loyaltyPointsLabel.setText("0 pts");
        }
    }

    private void executeRecharge(BigDecimal amount) {
        if (activeCustomer == null) {
            UIHelper.showError(this, "Please scan or lookup a customer first.");
            return;
        }
        Integer staffId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;
        Integer cardId = activeCard != null ? activeCard.getCardId() : null;

        try {
            walletService.rechargeWallet(activeCustomer.getCustomerId(), cardId, amount, "POS Counter Cash", staffId);
            UIHelper.showSuccess(this, "Recharged $" + amount + " successfully!");
            lookupCard();
        } catch (Exception ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void executePlayInitiation() {
        if (activeCustomer == null || activeCard == null) {
            UIHelper.showError(this, "Please scan an active membership card to initiate play.");
            return;
        }
        MachineItem selected = (MachineItem) machineComboBox.getSelectedItem();
        if (selected == null) {
            UIHelper.showError(this, "No operational machine selected.");
            return;
        }

        Integer staffId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;
        try {
            PlaySession session = playService.initiatePlaySession(activeCard.getCardNumber(), selected.machine.getMachineId(), staffId);
            UIHelper.showSuccess(this, "Play session authorized! Session Ref: " + session.getSessionReference());
            refreshActiveSessions();
            lookupCard();
        } catch (Exception ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void executeRedemption() {
        if (activeCustomer == null) {
            UIHelper.showError(this, "Please lookup a customer first.");
            return;
        }
        int selectedRow = prizeTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showError(this, "Please select a prize from the catalog.");
            return;
        }
        int prizeId = (int) prizeTableModel.getValueAt(selectedRow, 5);
        int qty = (int) prizeQtySpinner.getValue();
        Integer staffId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;

        try {
            PrizeRedemption red = redemptionService.redeemPrize(activeCustomer.getCustomerId(), prizeId, qty, staffId);
            UIHelper.showSuccess(this, "Redemption successful! Ref: " + red.getRedemptionReference() + "\nSpent: " + red.getTicketsSpent() + " tickets.");
            refreshPrizes();
            lookupCard();
        } catch (Exception ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void refreshOperationalMachines() {
        machineComboBox.removeAllItems();
        List<Machine> list = machineService.getOperationalMachines();
        for (Machine m : list) {
            machineComboBox.addItem(new MachineItem(m));
        }
    }

    private void refreshPrizes() {
        prizeTableModel.setRowCount(0);
        List<Prize> list = redemptionService.getAvailablePrizes();
        for (Prize p : list) {
            prizeTableModel.addRow(new Object[]{
                    p.getPrizeCode(), p.getName(), p.getCategory(), p.getTicketCost(), p.getInventoryStock(), p.getPrizeId()
            });
        }
    }

    private void refreshActiveSessions() {
        activeSessionsModel.setRowCount(0);
        List<PlaySession> list = playService.getActiveInitiatedSessions();
        for (PlaySession s : list) {
            activeSessionsModel.addRow(new Object[]{
                    s.getSessionReference(), s.getMachineCode(), s.getGameName(),
                    UIHelper.formatCurrency(s.getGameFee()), UIHelper.formatDate(s.getStartedAt()),
                    s.getCustomerName(), s.getSessionId()
            });
        }
    }

    private void openCustomerRegistrationDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Customer", true);
        dlg.setSize(400, 320);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JCheckBox issueCardCheck = new JCheckBox("Issue RFID Card", true);

        p.add(new JLabel("Full Name:")); p.add(nameField);
        p.add(new JLabel("Email Address:")); p.add(emailField);
        p.add(new JLabel("Phone Number:")); p.add(phoneField);
        p.add(new JLabel("Membership:")); p.add(issueCardCheck);

        JButton saveBtn = UIHelper.createPrimaryButton("Register Customer");
        saveBtn.addActionListener(e -> {
            try {
                Customer c = customerService.registerCustomer(
                        nameField.getText().trim(), emailField.getText().trim(),
                        phoneField.getText().trim(), issueCardCheck.isSelected()
                );
                UIHelper.showSuccess(dlg, "Customer registered successfully!\nCode: " + c.getCustomerCode() +
                        (c.getActiveCardNumber() != null ? "\nRFID Card: " + c.getActiveCardNumber() : ""));
                dlg.dispose();
                if (c.getActiveCardNumber() != null) {
                    cardSearchField.setText(c.getActiveCardNumber());
                } else {
                    cardSearchField.setText(c.getCustomerCode());
                }
                lookupCard();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });

        p.add(new JLabel("")); p.add(saveBtn);
        dlg.add(p);
        dlg.setVisible(true);
    }

    private static class MachineItem {
        final Machine machine;
        MachineItem(Machine machine) { this.machine = machine; }
        @Override
        public String toString() {
            return machine.getMachineCode() + " - " + machine.getGameName() + " (" + UIHelper.formatCurrency(machine.getGamePrice()) + ")";
        }
    }
}

