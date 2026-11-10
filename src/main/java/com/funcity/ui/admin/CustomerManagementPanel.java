package com.funcity.ui.admin;

import com.funcity.model.Customer;
import com.funcity.model.MembershipCard;
import com.funcity.model.WalletTransaction;
import com.funcity.service.CustomerService;
import com.funcity.service.MembershipService;
import com.funcity.service.WalletService;
import com.funcity.ui.components.SearchTextField;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class CustomerManagementPanel extends JPanel {

    private final CustomerService customerService;
    private final MembershipService membershipService;
    private final WalletService walletService;

    private SearchTextField searchField;
    private DefaultTableModel tableModel;
    private JTable customerTable;

    public CustomerManagementPanel() {
        this.customerService = new CustomerService();
        this.membershipService = new MembershipService();
        this.walletService = new WalletService();

        initUI();
        refreshCustomers("");
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // Header and Search bar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel title = new JLabel("👥 Customer & Membership Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBar.setOpaque(false);

        searchField = new SearchTextField("Search by Name, Code, Phone, RFID Card...", 25);
        JButton searchBtn = new JButton("🔍 Search");
        searchBtn.addActionListener(e -> refreshCustomers(searchField.getText().trim()));

        JButton clearBtn = new JButton("Reset");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            refreshCustomers("");
        });

        JButton newCustBtn = UIHelper.createPrimaryButton("+ Add Customer");
        newCustBtn.addActionListener(e -> openCustomerDialog(null));

        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);
        searchBar.add(newCustBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(searchBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{
                "Code", "Name", "Email", "Phone", "Active RFID Card", "Wallet Balance", "Tickets", "Loyalty Pts", "Status", "ID"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        customerTable = new JTable(tableModel);
        UIHelper.styleTable(customerTable);
        customerTable.removeColumn(customerTable.getColumnModel().getColumn(9));

        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Bottom action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        JButton issueCardBtn = UIHelper.createSuccessButton("💳 Issue / Replace RFID Card");
        issueCardBtn.addActionListener(e -> handleIssueCard());

        JButton blockCardBtn = UIHelper.createDangerButton("🚫 Block RFID Card");
        blockCardBtn.addActionListener(e -> handleBlockCard());

        JButton viewLedgerBtn = new JButton("📜 View Ledger");
        viewLedgerBtn.addActionListener(e -> handleViewLedger());

        bottomPanel.add(issueCardBtn);
        bottomPanel.add(blockCardBtn);
        bottomPanel.add(viewLedgerBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshCustomers(String query) {
        tableModel.setRowCount(0);
        List<Customer> list = customerService.searchCustomers(query);
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                    c.getCustomerCode(), c.getName(), c.getEmail(), c.getPhone(),
                    c.getActiveCardNumber() != null ? c.getActiveCardNumber() : "(None)",
                    UIHelper.formatCurrency(c.getWalletBalance()),
                    c.getTicketBalance(),
                    c.getLoyaltyPoints(),
                    c.getStatus(),
                    c.getCustomerId()
            });
        }
    }

    private void handleIssueCard() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a customer first.");
            return;
        }
        int custId = (int) tableModel.getValueAt(row, 9);
        String name = (String) tableModel.getValueAt(row, 1);

        String customCard = JOptionPane.showInputDialog(this, "Enter simulated RFID UID (or leave empty to auto-generate):", "");
        if (customCard == null) return; // Cancelled

        try {
            MembershipCard card = membershipService.issueNewCard(custId, customCard);
            UIHelper.showSuccess(this, "Card " + card.getCardNumber() + " successfully issued to " + name + "!");
            refreshCustomers(searchField.getText());
        } catch (Exception ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void handleBlockCard() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a customer first.");
            return;
        }
        int custId = (int) tableModel.getValueAt(row, 9);
        Optional<MembershipCard> cardOpt = membershipService.getActiveCardForCustomer(custId);
        if (cardOpt.isEmpty()) {
            UIHelper.showError(this, "Selected customer has no active card to block.");
            return;
        }

        if (UIHelper.confirm(this, "Are you sure you want to block RFID card " + cardOpt.get().getCardNumber() + "?", "Block Card")) {
            membershipService.blockCard(cardOpt.get().getCardId(), "Blocked by administrator");
            UIHelper.showSuccess(this, "Card blocked.");
            refreshCustomers(searchField.getText());
        }
    }

    private void handleViewLedger() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a customer first.");
            return;
        }
        int custId = (int) tableModel.getValueAt(row, 9);
        String name = (String) tableModel.getValueAt(row, 1);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Transaction History - " + name, true);
        dlg.setSize(700, 400);
        dlg.setLocationRelativeTo(this);

        DefaultTableModel tm = new DefaultTableModel(new String[]{
                "Tx Reference", "Type", "Amount", "Balance After", "Description", "Date"
        }, 0);
        JTable table = new JTable(tm);
        UIHelper.styleTable(table);

        List<WalletTransaction> txs = walletService.getCustomerTransactions(custId);
        for (WalletTransaction tx : txs) {
            tm.addRow(new Object[]{
                    tx.getTransactionReference(), tx.getTransactionType(),
                    UIHelper.formatCurrency(tx.getAmount()), UIHelper.formatCurrency(tx.getClosingBalance()),
                    tx.getDescription(), UIHelper.formatDate(tx.getCreatedAt())
            });
        }

        dlg.add(new JScrollPane(table));
        dlg.setVisible(true);
    }

    private void openCustomerDialog(Customer existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Customer", true);
        dlg.setSize(380, 280);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        p.add(new JLabel("Full Name:")); p.add(nameField);
        p.add(new JLabel("Email Address:")); p.add(emailField);
        p.add(new JLabel("Phone:")); p.add(phoneField);

        JButton saveBtn = UIHelper.createPrimaryButton("Save Customer");
        saveBtn.addActionListener(e -> {
            try {
                customerService.registerCustomer(nameField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim(), true);
                UIHelper.showSuccess(dlg, "Customer created with active membership card!");
                dlg.dispose();
                refreshCustomers("");
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });

        p.add(new JLabel("")); p.add(saveBtn);
        dlg.add(p);
        dlg.setVisible(true);
    }
}

