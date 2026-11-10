package com.funcity.model;

import java.sql.Timestamp;

public class PrizeRedemption {
    private int redemptionId;
    private String redemptionReference;
    private int customerId;
    private int prizeId;
    private int quantity = 1;
    private int ticketsSpent;
    private Integer staffId;
    private Timestamp redeemedAt;

    // Joined fields
    private String customerName;
    private String customerCode;
    private String prizeName;
    private String prizeCategory;
    private String staffUsername;

    public PrizeRedemption() {}

    public PrizeRedemption(int redemptionId, String redemptionReference, int customerId, int prizeId,
                           int quantity, int ticketsSpent, Integer staffId) {
        this.redemptionId = redemptionId;
        this.redemptionReference = redemptionReference;
        this.customerId = customerId;
        this.prizeId = prizeId;
        this.quantity = quantity;
        this.ticketsSpent = ticketsSpent;
        this.staffId = staffId;
    }

    public int getRedemptionId() { return redemptionId; }
    public void setRedemptionId(int redemptionId) { this.redemptionId = redemptionId; }

    public String getRedemptionReference() { return redemptionReference; }
    public void setRedemptionReference(String redemptionReference) { this.redemptionReference = redemptionReference; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getPrizeId() { return prizeId; }
    public void setPrizeId(int prizeId) { this.prizeId = prizeId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTicketsSpent() { return ticketsSpent; }
    public void setTicketsSpent(int ticketsSpent) { this.ticketsSpent = ticketsSpent; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public Timestamp getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(Timestamp redeemedAt) { this.redeemedAt = redeemedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }

    public String getPrizeCategory() { return prizeCategory; }
    public void setPrizeCategory(String prizeCategory) { this.prizeCategory = prizeCategory; }

    public String getStaffUsername() { return staffUsername; }
    public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }

    @Override
    public String toString() {
        return redemptionReference + " - " + prizeName + " x" + quantity + " (" + ticketsSpent + " tickets)";
    }
}

