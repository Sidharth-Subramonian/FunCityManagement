package com.funcity.ui.technician;

import com.funcity.model.Machine;
import com.funcity.model.MaintenanceRecord;
import com.funcity.service.MachineService;
import com.funcity.service.MaintenanceService;
import com.funcity.ui.UserSession;
import com.funcity.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TechnicianPanel extends JPanel {

    private final MaintenanceService maintenanceService;
    private final MachineService machineService;

    private DefaultTableModel machineTableModel;
    private JTable machineTable;

    private DefaultTableModel maintenanceTableModel;
    private JTable maintenanceTable;

    public TechnicianPanel() {
        this.maintenanceService = new MaintenanceService();
        this.machineService = new MachineService();

        initUI();
        refreshAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIHelper.BG_LIGHT);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🔧 Arcade Machine Monitoring & Maintenance Console");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.TEXT_DARK);

        JButton refreshBtn = new JButton("🔄 Refresh Status");
        refreshBtn.addActionListener(e -> refreshAllData());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Split into Top (Machine Status) and Bottom (Maintenance Records)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setContinuousLayout(true);

        // Top: Machines
        JPanel machinesPanel = new JPanel(new BorderLayout(5, 5));
        machinesPanel.setBackground(Color.WHITE);
        machinesPanel.setBorder(BorderFactory.createTitledBorder("Floor Arcade Machines Health"));

        machineTableModel = new DefaultTableModel(new String[]{
                "Machine Code", "Game Title", "Location", "Serial Number", "Operational Status", "Machine ID"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        machineTable = new JTable(machineTableModel);
        UIHelper.styleTable(machineTable);
        machineTable.removeColumn(machineTable.getColumnModel().getColumn(5));

        JPanel machineActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        machineActions.setOpaque(false);
        JButton reportFaultBtn = UIHelper.createDangerButton("⚠ Report Machine Fault");
        reportFaultBtn.addActionListener(e -> openReportFaultDialog());
        machineActions.add(reportFaultBtn);

        machinesPanel.add(new JScrollPane(machineTable), BorderLayout.CENTER);
        machinesPanel.add(machineActions, BorderLayout.SOUTH);

        // Bottom: Maintenance Work Orders
        JPanel maintPanel = new JPanel(new BorderLayout(5, 5));
        maintPanel.setBackground(Color.WHITE);
        maintPanel.setBorder(BorderFactory.createTitledBorder("Active Maintenance Work Orders & Faults"));

        maintenanceTableModel = new DefaultTableModel(new String[]{
                "Record #", "Machine", "Game", "Priority", "Status", "Issue Description", "Technician Notes", "Reported At", "ID"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        maintenanceTable = new JTable(maintenanceTableModel);
        UIHelper.styleTable(maintenanceTable);
        maintenanceTable.removeColumn(maintenanceTable.getColumnModel().getColumn(8));

        JPanel maintActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        maintActions.setOpaque(false);

        JButton inProgressBtn = UIHelper.createPrimaryButton("🛠 Start Repair (IN_PROGRESS)");
        inProgressBtn.addActionListener(e -> updateSelectedStatus("IN_PROGRESS"));

        JButton completeBtn = UIHelper.createSuccessButton("✓ Complete Repair & Restore Machine");
        completeBtn.addActionListener(e -> updateSelectedStatus("COMPLETED"));

        JButton addNotesBtn = new JButton("📝 Update Notes");
        addNotesBtn.addActionListener(e -> addNotesToSelected());

        maintActions.add(inProgressBtn);
        maintActions.add(completeBtn);
        maintActions.add(addNotesBtn);

        maintPanel.add(new JScrollPane(maintenanceTable), BorderLayout.CENTER);
        maintPanel.add(maintActions, BorderLayout.SOUTH);

        splitPane.setTopComponent(machinesPanel);
        splitPane.setBottomComponent(maintPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshAllData() {
        // Refresh Machines
        machineTableModel.setRowCount(0);
        List<Machine> machines = machineService.getAllMachines();
        for (Machine m : machines) {
            machineTableModel.addRow(new Object[]{
                    m.getMachineCode(), m.getGameName(), m.getFloorLocation(),
                    m.getSerialNumber(), m.getStatus(), m.getMachineId()
            });
        }

        // Refresh Maintenance Orders
        maintenanceTableModel.setRowCount(0);
        List<MaintenanceRecord> records = maintenanceService.getAllRecords();
        for (MaintenanceRecord r : records) {
            maintenanceTableModel.addRow(new Object[]{
                    "MNT-" + r.getRecordId(), r.getMachineCode(), r.getGameName(),
                    r.getPriority(), r.getStatus(), r.getIssueDescription(),
                    r.getTechnicianNotes() != null ? r.getTechnicianNotes() : "-",
                    UIHelper.formatDate(r.getReportedAt()), r.getRecordId()
            });
        }
    }

    private void updateSelectedStatus(String newStatus) {
        int row = maintenanceTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a maintenance work order from the table.");
            return;
        }
        int recordId = (int) maintenanceTableModel.getValueAt(row, 8);
        String currentNotes = (String) maintenanceTableModel.getValueAt(row, 6);
        if ("-".equals(currentNotes)) currentNotes = "";

        String inputNotes = JOptionPane.showInputDialog(this, "Technician Repair Notes:", currentNotes);
        if (inputNotes == null) return; // User cancelled

        Integer techId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;
        try {
            maintenanceService.updateMaintenanceProgress(recordId, newStatus, inputNotes, techId);
            UIHelper.showSuccess(this, "Work order updated to " + newStatus + "!");
            refreshAllData();
        } catch (Exception ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void addNotesToSelected() {
        int row = maintenanceTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a maintenance work order.");
            return;
        }
        int recordId = (int) maintenanceTableModel.getValueAt(row, 8);
        String currentStatus = (String) maintenanceTableModel.getValueAt(row, 4);
        String currentNotes = (String) maintenanceTableModel.getValueAt(row, 6);
        if ("-".equals(currentNotes)) currentNotes = "";

        String newNotes = JOptionPane.showInputDialog(this, "Update Notes for Work Order #" + recordId + ":", currentNotes);
        if (newNotes != null) {
            Integer techId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : null;
            try {
                maintenanceService.updateMaintenanceProgress(recordId, currentStatus, newNotes, techId);
                UIHelper.showSuccess(this, "Notes updated.");
                refreshAllData();
            } catch (Exception ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        }
    }

    private void openReportFaultDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Report Machine Fault", true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<MachineItem> machineCb = new JComboBox<>();
        List<Machine> machines = machineService.getAllMachines();
        for (Machine m : machines) {
            machineCb.addItem(new MachineItem(m));
        }

        JComboBox<String> priorityCb = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});
        priorityCb.setSelectedItem("HIGH");

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);

        p.add(new JLabel("Machine:")); p.add(machineCb);
        p.add(new JLabel("Fault Priority:")); p.add(priorityCb);
        p.add(new JLabel("Fault Description:")); p.add(new JScrollPane(descArea));

        JButton submitBtn = UIHelper.createDangerButton("Submit Fault Ticket");
        submitBtn.addActionListener(e -> {
            MachineItem item = (MachineItem) machineCb.getSelectedItem();
            if (item == null || descArea.getText().trim().isEmpty()) {
                UIHelper.showError(dlg, "Please provide a fault description.");
                return;
            }
            Integer userId = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getUserId() : 1;
            try {
                maintenanceService.reportFault(
                        item.machine.getMachineId(),
                        userId,
                        descArea.getText().trim(),
                        (String) priorityCb.getSelectedItem()
                );
                UIHelper.showSuccess(dlg, "Fault ticket created! Machine placed UNDER_MAINTENANCE.");
                dlg.dispose();
                refreshAllData();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });

        p.add(new JLabel("")); p.add(submitBtn);
        dlg.add(p);
        dlg.setVisible(true);
    }

    private static class MachineItem {
        final Machine machine;
        MachineItem(Machine machine) { this.machine = machine; }
        @Override
        public String toString() {
            return machine.getMachineCode() + " - " + machine.getGameName() + " (" + machine.getStatus() + ")";
        }
    }
}

