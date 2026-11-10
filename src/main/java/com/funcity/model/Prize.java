package com.funcity.model;

import java.sql.Timestamp;

public class Prize {
    private int prizeId;
    private String prizeCode;
    private String name;
    private String category; // "PLUSH", "ELECTRONICS", "TOYS", "CANDY"
    private String description;
    private int ticketCost;
    private int inventoryStock;
    private String status = "ACTIVE"; // "ACTIVE", "INACTIVE"
    private Timestamp createdAt;

    public Prize() {}

    public Prize(int prizeId, String prizeCode, String name, String category, String description, int ticketCost, int inventoryStock, String status) {
        this.prizeId = prizeId;
        this.prizeCode = prizeCode;
        this.name = name;
        this.category = category;
        this.description = description;
        this.ticketCost = ticketCost;
        this.inventoryStock = inventoryStock;
        this.status = status;
    }

    public int getPrizeId() { return prizeId; }
    public void setPrizeId(int prizeId) { this.prizeId = prizeId; }

    public String getPrizeCode() { return prizeCode; }
    public void setPrizeCode(String prizeCode) { this.prizeCode = prizeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTicketCost() { return ticketCost; }
    public void setTicketCost(int ticketCost) { this.ticketCost = ticketCost; }

    public int getInventoryStock() { return inventoryStock; }
    public void setInventoryStock(int inventoryStock) { this.inventoryStock = inventoryStock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean isAvailable() {
        return isActive() && inventoryStock > 0;
    }

    @Override
    public String toString() {
        return name + " (" + ticketCost + " tickets, stock: " + inventoryStock + ")";
    }
}

