package com.funcity.model;

import java.sql.Timestamp;

public class RewardTransaction {
    private int rewardId;
    private String transactionReference;
    private int customerId;
    private int points; // positive if earned, negative if redeemed
    private int openingPoints;
    private int closingPoints;
    private String activityType; // "RECHARGE_BONUS", "PLAY_BONUS", "POINTS_REDEMPTION"
    private String description;
    private Timestamp createdAt;

    // Joined fields
    private String customerName;
    private String customerCode;

    public RewardTransaction() {}

    public RewardTransaction(int rewardId, String transactionReference, int customerId, int points,
                             int openingPoints, int closingPoints, String activityType, String description) {
        this.rewardId = rewardId;
        this.transactionReference = transactionReference;
        this.customerId = customerId;
        this.points = points;
        this.openingPoints = openingPoints;
        this.closingPoints = closingPoints;
        this.activityType = activityType;
        this.description = description;
    }

    public int getRewardId() { return rewardId; }
    public void setRewardId(int rewardId) { this.rewardId = rewardId; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getOpeningPoints() { return openingPoints; }
    public void setOpeningPoints(int openingPoints) { this.openingPoints = openingPoints; }

    public int getClosingPoints() { return closingPoints; }
    public void setClosingPoints(int closingPoints) { this.closingPoints = closingPoints; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    @Override
    public String toString() {
        return transactionReference + " [" + activityType + " " + points + " pts]";
    }
}

