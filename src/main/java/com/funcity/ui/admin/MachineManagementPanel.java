package com.funcity.ui.admin;

import com.funcity.model.Game;
import com.funcity.model.Machine;
import com.funcity.service.GameService;
import com.funcity.service.MachineService;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class MachineManagementPanel extends JPanel {

    private final MachineService machineService;
    private final GameService gameService;
    private DefaultTableModel tableModel;
    private JTable machineTable;

    public MachineManagementPanel() {
        this.machineService = new MachineService();
        this.gameService = new GameService();
        initUI();
        refreshMachines();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("🕹️ Physical Arcade Machines Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton addBtn = UIHelper.createPrimaryButton("+ Register New Machine");
        addBtn.addActionListener(e -> openAddMachineDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{
                "Machine Code", "Game Title", "Floor Location", "Serial Number", "Operational Status", "Installed Date", "ID"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        machineTable = new JTable(tableModel);
        UIHelper.styleTable(machineTable);
        machineTable.removeColumn(machineTable.getColumnModel().getColumn(6));

        add(new JScrollPane(machineTable), BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton setStatusBtn = new JButton("Change Status");
        setStatusBtn.addActionListener(e -> handleChangeStatus());

        JButton deleteBtn = UIHelper.createDangerButton("🗑 Delete Machine");
        deleteBtn.addActionListener(e -> handleDeleteMachine());

        actions.add(setStatusBtn);
        actions.add(deleteBtn);

        add(actions, BorderLayout.SOUTH);
    }

    private void refreshMachines() {
        tableModel.setRowCount(0);
        List<Machine> list = machineService.getAllMachines();
        for (Machine m : list) {
            tableModel.addRow(new Object[]{
                    m.getMachineCode(), m.getGameName(), m.getFloorLocation(),
                    m.getSerialNumber(), m.getStatus(), UIHelper.formatDate(m.getInstalledAt()),
                    m.getMachineId()
            });
        }
    }

    private void handleChangeStatus() {
        int row = machineTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a machine from the table.");
            return;
        }
        int machineId = (int) tableModel.getValueAt(row, 6);
        String current = (String) tableModel.getValueAt(row, 4);

        String[] statuses = {"OPERATIONAL", "UNDER_MAINTENANCE", "OUT_OF_SERVICE"};
        String chosen = (String) JOptionPane.showInputDialog(
                this, "Select new status for machine:", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, current
        );

        if (chosen != null && !chosen.equals(current)) {
            machineService.updateStatus(machineId, chosen);
            refreshMachines();
        }
    }

    private void handleDeleteMachine() {
        int row = machineTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a machine to delete.");
            return;
        }
        int machineId = (int) tableModel.getValueAt(row, 6);
        String code = (String) tableModel.getValueAt(row, 0);

        if (UIHelper.confirm(this, "Are you sure you want to delete machine " + code + "?", "Delete Machine")) {
            try {
                machineService.deleteMachine(machineId);
                UIHelper.showSuccess(this, "Machine deleted.");
                refreshMachines();
            } catch (Exception ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        }
    }

    private void openAddMachineDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Machine", true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField codeField = new JTextField("MCH-" + String.format("%02d", machineService.getAllMachines().size() + 1));
        JComboBox<GameItem> gameCb = new JComboBox<>();
        List<Game> games = gameService.getAllGames();
        for (Game g : games) {
            gameCb.addItem(new GameItem(g));
        }

        JTextField serialField = new JTextField("SN-" + System.currentTimeMillis() % 100000);
        JTextField locationField = new JTextField("Zone A - Lane 1");

        p.add(new JLabel("Machine Code:")); p.add(codeField);
        p.add(new JLabel("Game Title:")); p.add(gameCb);
        p.add(new JLabel("Serial Number:")); p.add(serialField);
        p.add(new JLabel("Floor Location:")); p.add(locationField);

        JButton saveBtn = UIHelper.createPrimaryButton("Register Machine");
        saveBtn.addActionListener(e -> {
            GameItem selected = (GameItem) gameCb.getSelectedItem();
            if (selected == null) {
                UIHelper.showError(dlg, "No game selected.");
                return;
            }
            try {
                machineService.addMachine(
                        codeField.getText().trim(),
                        selected.game.getGameId(),
                        serialField.getText().trim(),
                        locationField.getText().trim(),
                        "OPERATIONAL"
                );
                UIHelper.showSuccess(dlg, "Machine registered successfully!");
                dlg.dispose();
                refreshMachines();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });

        p.add(new JLabel("")); p.add(saveBtn);
        dlg.add(p);
        dlg.setVisible(true);
    }

    private static class GameItem {
        final Game game;
        GameItem(Game game) { this.game = game; }
        @Override
        public String toString() {
            return game.getName() + " (" + UIHelper.formatCurrency(game.getPricePerPlay()) + ")";
        }
    }
}

