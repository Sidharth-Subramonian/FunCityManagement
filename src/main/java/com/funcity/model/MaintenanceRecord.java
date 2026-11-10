package com.funcity.model;

import java.sql.Timestamp;

public class MaintenanceRecord {
    private int recordId;
    private int machineId;
    private int reportedBy;
    private Integer assignedTechnicianId;
    private String issueDescription;
    private String technicianNotes;
    private String status = "REPORTED"; // "REPORTED", "IN_PROGRESS", "COMPLETED"
    private String priority = "MEDIUM"; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private Timestamp reportedAt;
    private Timestamp resolvedAt;

    // Joined fields
    private String machineCode;
    private String gameName;
    private String floorLocation;
    private String reportedByUsername;
    private String technicianName;

    public MaintenanceRecord() {}

    public MaintenanceRecord(int recordId, int machineId, int reportedBy, Integer assignedTechnicianId,
                             String issueDescription, String technicianNotes, String status, String priority) {
        this.recordId = recordId;
        this.machineId = machineId;
        this.reportedBy = reportedBy;
        this.assignedTechnicianId = assignedTechnicianId;
        this.issueDescription = issueDescription;
        this.technicianNotes = technicianNotes;
        this.status = status;
        this.priority = priority;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }

    public int getReportedBy() { return reportedBy; }
    public void setReportedBy(int reportedBy) { this.reportedBy = reportedBy; }

    public Integer getAssignedTechnicianId() { return assignedTechnicianId; }
    public void setAssignedTechnicianId(Integer assignedTechnicianId) { this.assignedTechnicianId = assignedTechnicianId; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getTechnicianNotes() { return technicianNotes; }
    public void setTechnicianNotes(String technicianNotes) { this.technicianNotes = technicianNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Timestamp getReportedAt() { return reportedAt; }
    public void setReportedAt(Timestamp reportedAt) { this.reportedAt = reportedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getFloorLocation() { return floorLocation; }
    public void setFloorLocation(String floorLocation) { this.floorLocation = floorLocation; }

    public String getReportedByUsername() { return reportedByUsername; }
    public void setReportedByUsername(String reportedByUsername) { this.reportedByUsername = reportedByUsername; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public boolean isReported() {
        return "REPORTED".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "MNT-" + recordId + " [" + machineCode + " - " + status + " (" + priority + ")]";
    }
}

