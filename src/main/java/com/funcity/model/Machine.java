package com.funcity.model;

import java.sql.Timestamp;

public class Machine {
    private int machineId;
    private String machineCode; // e.g. "MCH-01"
    private int gameId;
    private String serialNumber;
    private String floorLocation;
    private String status = "OPERATIONAL"; // "OPERATIONAL", "UNDER_MAINTENANCE", "OUT_OF_SERVICE"
    private Timestamp installedAt;
    private Timestamp lastMaintenanceDate;

    // Joined fields
    private String gameName;
    private String gameCategory;
    private java.math.BigDecimal gamePrice;

    public Machine() {}

    public Machine(int machineId, String machineCode, int gameId, String serialNumber, String floorLocation, String status) {
        this.machineId = machineId;
        this.machineCode = machineCode;
        this.gameId = gameId;
        this.serialNumber = serialNumber;
        this.floorLocation = floorLocation;
        this.status = status;
    }

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getFloorLocation() { return floorLocation; }
    public void setFloorLocation(String floorLocation) { this.floorLocation = floorLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getInstalledAt() { return installedAt; }
    public void setInstalledAt(Timestamp installedAt) { this.installedAt = installedAt; }

    public Timestamp getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(Timestamp lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getGameCategory() { return gameCategory; }
    public void setGameCategory(String gameCategory) { this.gameCategory = gameCategory; }

    public java.math.BigDecimal getGamePrice() { return gamePrice; }
    public void setGamePrice(java.math.BigDecimal gamePrice) { this.gamePrice = gamePrice; }

    public boolean isOperational() {
        return "OPERATIONAL".equalsIgnoreCase(status);
    }

    public boolean isUnderMaintenance() {
        return "UNDER_MAINTENANCE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return machineCode + " - " + (gameName != null ? gameName : "Game #" + gameId) + " (" + status + ")";
    }
}

