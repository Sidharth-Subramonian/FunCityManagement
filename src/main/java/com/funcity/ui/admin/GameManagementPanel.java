package com.funcity.ui.admin;

import com.funcity.model.Game;
import com.funcity.service.GameService;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class GameManagementPanel extends JPanel {

    private final GameService gameService;
    private DefaultTableModel tableModel;
    private JTable gameTable;

    public GameManagementPanel() {
        this.gameService = new GameService();
        initUI();
        refreshGames();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("🎮 Arcade Games & Pricing Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton addBtn = UIHelper.createPrimaryButton("+ Add New Game");
        addBtn.addActionListener(e -> openGameDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{
                "Game ID", "Game Title", "Category", "Price / Play", "Dispenses Tickets", "Machines", "Status", "Description"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        gameTable = new JTable(tableModel);
        UIHelper.styleTable(gameTable);

        add(new JScrollPane(gameTable), BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton editBtn = new JButton("✏ Edit Game");
        editBtn.addActionListener(e -> handleEditGame());

        JButton toggleStatusBtn = new JButton("Toggle Active/Inactive");
        toggleStatusBtn.addActionListener(e -> handleToggleStatus());

        JButton deleteBtn = UIHelper.createDangerButton("🗑 Delete Game");
        deleteBtn.addActionListener(e -> handleDeleteGame());

        actions.add(editBtn);
        actions.add(toggleStatusBtn);
        actions.add(deleteBtn);

        add(actions, BorderLayout.SOUTH);
    }

    private void refreshGames() {
        tableModel.setRowCount(0);
        List<Game> list = gameService.getAllGames();
        for (Game g : list) {
            tableModel.addRow(new Object[]{
                    g.getGameId(), g.getName(), g.getCategory(),
                    UIHelper.formatCurrency(g.getPricePerPlay()),
                    g.isDispensesTickets() ? "Yes 🎟️" : "No",
                    g.getMachineCount(),
                    g.getStatus(),
                    g.getDescription()
            });
        }
    }

    private void handleEditGame() {
        int row = gameTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a game from the table.");
            return;
        }
        int gameId = (int) tableModel.getValueAt(row, 0);
        Optional<Game> gameOpt = gameService.getGameById(gameId);
        gameOpt.ifPresent(this::openGameDialog);
    }

    private void handleToggleStatus() {
        int row = gameTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a game.");
            return;
        }
        int gameId = (int) tableModel.getValueAt(row, 0);
        Optional<Game> gameOpt = gameService.getGameById(gameId);
        if (gameOpt.isPresent()) {
            Game g = gameOpt.get();
            g.setStatus("ACTIVE".equalsIgnoreCase(g.getStatus()) ? "INACTIVE" : "ACTIVE");
            gameService.updateGame(g);
            refreshGames();
        }
    }

    private void handleDeleteGame() {
        int row = gameTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a game to delete.");
            return;
        }
        int gameId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        if (UIHelper.confirm(this, "Are you sure you want to delete game '" + name + "'?\n(Deletion will fail if machines or sessions are linked)", "Confirm Delete")) {
            try {
                gameService.deleteGame(gameId);
                UIHelper.showSuccess(this, "Game deleted successfully.");
                refreshGames();
            } catch (Exception ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        }
    }

    private void openGameDialog(Game existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Game" : "Add Game", true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(isEdit ? existing.getName() : "");
        JComboBox<String> catCb = new JComboBox<>(new String[]{"ARCADE", "SIMULATOR", "REDEMPTION", "VR", "SPORTS"});
        if (isEdit) catCb.setSelectedItem(existing.getCategory());

        JTextField priceField = new JTextField(isEdit ? existing.getPricePerPlay().toString() : "2.00");
        JCheckBox ticketCheck = new JCheckBox("Dispenses Tickets", isEdit ? existing.isDispensesTickets() : true);
        JTextArea descArea = new JTextArea(isEdit ? existing.getDescription() : "", 3, 20);

        p.add(new JLabel("Game Name:")); p.add(nameField);
        p.add(new JLabel("Category:")); p.add(catCb);
        p.add(new JLabel("Price Per Play ($):")); p.add(priceField);
        p.add(new JLabel("Ticket Eligible:")); p.add(ticketCheck);
        p.add(new JLabel("Description:")); p.add(new JScrollPane(descArea));

        JButton saveBtn = UIHelper.createPrimaryButton(isEdit ? "Update Game" : "Create Game");
        saveBtn.addActionListener(e -> {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (isEdit) {
                    existing.setName(nameField.getText().trim());
                    existing.setCategory((String) catCb.getSelectedItem());
                    existing.setPricePerPlay(price);
                    existing.setDispensesTickets(ticketCheck.isSelected());
                    existing.setDescription(descArea.getText().trim());
                    gameService.updateGame(existing);
                } else {
                    gameService.addGame(
                            nameField.getText().trim(),
                            (String) catCb.getSelectedItem(),
                            descArea.getText().trim(),
                            price,
                            ticketCheck.isSelected()
                    );
                }
                UIHelper.showSuccess(dlg, "Game saved successfully!");
                dlg.dispose();
                refreshGames();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });

        p.add(new JLabel("")); p.add(saveBtn);
        dlg.add(p);
        dlg.setVisible(true);
    }
}

