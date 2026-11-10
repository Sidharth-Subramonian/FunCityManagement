package com.funcity.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Game {
    private int gameId;
    private String name;
    private String category; // "ARCADE", "SIMULATOR", "REDEMPTION", "VR"
    private String description;
    private BigDecimal pricePerPlay = BigDecimal.ZERO;
    private boolean dispensesTickets = true;
    private String status = "ACTIVE"; // "ACTIVE", "INACTIVE"
    private Timestamp createdAt;

    // Joined fields
    private int machineCount;

    public Game() {}

    public Game(int gameId, String name, String category, String description, BigDecimal pricePerPlay, boolean dispensesTickets, String status) {
        this.gameId = gameId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.pricePerPlay = pricePerPlay != null ? pricePerPlay : BigDecimal.ZERO;
        this.dispensesTickets = dispensesTickets;
        this.status = status;
    }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerPlay() { return pricePerPlay; }
    public void setPricePerPlay(BigDecimal pricePerPlay) { this.pricePerPlay = pricePerPlay; }

    public boolean isDispensesTickets() { return dispensesTickets; }
    public void setDispensesTickets(boolean dispensesTickets) { this.dispensesTickets = dispensesTickets; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getMachineCount() { return machineCount; }
    public void setMachineCount(int machineCount) { this.machineCount = machineCount; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return name + " ($" + pricePerPlay + ")";
    }
}

