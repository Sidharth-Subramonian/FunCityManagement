package com.funcity.service;

import com.funcity.dao.MachineDAO;
import com.funcity.dao.MaintenanceDAO;
import com.funcity.model.Machine;
import com.funcity.model.MaintenanceRecord;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class MaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final MaintenanceDAO maintenanceDAO;
    private final MachineDAO machineDAO;

    public MaintenanceService() {
        this.maintenanceDAO = new MaintenanceDAO();
        this.machineDAO = new MachineDAO();
    }

    public MaintenanceService(MaintenanceDAO maintenanceDAO, MachineDAO machineDAO) {
        this.maintenanceDAO = maintenanceDAO;
        this.machineDAO = machineDAO;
    }

    public MaintenanceRecord reportFault(int machineId, int reportedByUserId, String description, String priority) {
        ValidationUtil.requireNonEmpty(description, "Issue description");

        Optional<Machine> machineOpt = machineDAO.findById(machineId);
        if (machineOpt.isEmpty()) {
            throw new IllegalArgumentException("Machine not found with ID: " + machineId);
        }

        // Automatically set machine under maintenance so it stops accepting play sessions
        machineDAO.updateStatus(machineId, "UNDER_MAINTENANCE");

        MaintenanceRecord record = new MaintenanceRecord(
                0,
                machineId,
                reportedByUserId,
                null,
                description.trim(),
                null,
                "REPORTED",
                priority != null ? priority : "MEDIUM"
        );
        MaintenanceRecord created = maintenanceDAO.create(record);
        logger.warn("Machine {} reported with fault. Placed UNDER_MAINTENANCE. Ticket #{}", machineId, created.getRecordId());
        return created;
    }

    public boolean updateMaintenanceProgress(int recordId, String status, String notes, Integer technicianId) {
        ValidationUtil.requireNonEmpty(status, "Status");
        Optional<MaintenanceRecord> recordOpt = maintenanceDAO.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Maintenance record not found with ID: " + recordId);
        }
        MaintenanceRecord record = recordOpt.get();

        boolean updated = maintenanceDAO.updateStatusAndNotes(recordId, status, notes, technicianId);

        // If completed, check if machine can be set back to OPERATIONAL
        if ("COMPLETED".equalsIgnoreCase(status)) {
            machineDAO.updateStatus(record.getMachineId(), "OPERATIONAL");
            logger.info("Maintenance #{} completed. Machine {} restored to OPERATIONAL.", recordId, record.getMachineId());
        } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            machineDAO.updateStatus(record.getMachineId(), "UNDER_MAINTENANCE");
        }

        return updated;
    }

    public List<MaintenanceRecord> getActiveRequests() {
        return maintenanceDAO.findActive();
    }

    public List<MaintenanceRecord> getAllRecords() {
        return maintenanceDAO.findAll();
    }

    public List<MaintenanceRecord> getRecordsByTechnician(int techId) {
        return maintenanceDAO.findByTechnicianId(techId);
    }
}

