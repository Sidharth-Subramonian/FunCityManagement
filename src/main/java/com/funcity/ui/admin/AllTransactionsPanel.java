package com.funcity.ui.admin;

import com.funcity.model.PrizeRedemption;
import com.funcity.model.TicketTransaction;
import com.funcity.model.WalletTransaction;
import com.funcity.service.RedemptionService;
import com.funcity.service.TicketService;
import com.funcity.service.WalletService;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AllTransactionsPanel extends JPanel {

    private final WalletService walletService;
    private final TicketService ticketService;
    private final RedemptionService redemptionService;

    private DefaultTableModel walletModel;
    private DefaultTableModel ticketModel;
    private DefaultTableModel redemptionModel;

    public AllTransactionsPanel() {
        this.walletService = new WalletService();
        this.ticketService = new TicketService();
        this.redemptionService = new RedemptionService();

        initUI();
        refreshAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("📜 Financial & Electronic Ticket Ledgers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton refreshBtn = new JButton("🔄 Refresh Ledgers");
        refreshBtn.addActionListener(e -> refreshAllData());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Tabbed Ledgers
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // 1. Wallet
        walletModel = new DefaultTableModel(new String[]{
                "Reference", "Customer", "Type", "Amount", "Opening Bal", "Closing Bal", "Description", "Staff", "Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable walletTable = new JTable(walletModel);
        UIHelper.styleTable(walletTable);
        tabs.addTab("💵 Digital Wallet Transactions", new JScrollPane(walletTable));

        // 2. Tickets
        ticketModel = new DefaultTableModel(new String[]{
                "Reference", "Customer", "Type", "Tickets (+/-)", "Opening Bal", "Closing Bal", "Description", "Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable ticketTable = new JTable(ticketModel);
        UIHelper.styleTable(ticketTable);
        tabs.addTab("🎟️ Electronic Ticket Ledger", new JScrollPane(ticketTable));

        // 3. Prize Redemptions
        redemptionModel = new DefaultTableModel(new String[]{
                "Reference", "Customer", "Prize Item", "Quantity", "Tickets Spent", "Staff", "Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable redTable = new JTable(redemptionModel);
        UIHelper.styleTable(redTable);
        tabs.addTab("🎁 Prize Redemptions Log", new JScrollPane(redTable));

        add(tabs, BorderLayout.CENTER);
    }

    private void refreshAllData() {
        // Wallet transactions
        walletModel.setRowCount(0);
        List<WalletTransaction> wtxs = walletService.getRecentTransactions(100);
        for (WalletTransaction tx : wtxs) {
            walletModel.addRow(new Object[]{
                    tx.getTransactionReference(), tx.getCustomerName(), tx.getTransactionType(),
                    UIHelper.formatCurrency(tx.getAmount()), UIHelper.formatCurrency(tx.getOpeningBalance()),
                    UIHelper.formatCurrency(tx.getClosingBalance()), tx.getDescription(),
                    tx.getCreatedByUsername() != null ? tx.getCreatedByUsername() : "System",
                    UIHelper.formatDate(tx.getCreatedAt())
            });
        }

        // Ticket transactions
        ticketModel.setRowCount(0);
        List<TicketTransaction> ttxs = ticketService.getRecentTicketTransactions(100);
        for (TicketTransaction tx : ttxs) {
            ticketModel.addRow(new Object[]{
                    tx.getTransactionReference(), tx.getCustomerName(), tx.getTransactionType(),
                    (tx.getTickets() > 0 ? "+" : "") + tx.getTickets(),
                    tx.getOpeningBalance(), tx.getClosingBalance(), tx.getDescription(),
                    UIHelper.formatDate(tx.getCreatedAt())
            });
        }

        // Prize redemptions
        redemptionModel.setRowCount(0);
        List<PrizeRedemption> reds = redemptionService.getRecentRedemptions(100);
        for (PrizeRedemption r : reds) {
            redemptionModel.addRow(new Object[]{
                    r.getRedemptionReference(), r.getCustomerName(), r.getPrizeName(),
                    r.getQuantity(), r.getTicketsSpent() + " 🎟️",
                    r.getStaffUsername() != null ? r.getStaffUsername() : "System",
                    UIHelper.formatDate(r.getRedeemedAt())
            });
        }
    }
}

