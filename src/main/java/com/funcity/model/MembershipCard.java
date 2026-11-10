package com.funcity.model;

import java.sql.Timestamp;

public class MembershipCard {
    private int cardId;
    private String cardNumber; // Simulated RFID UID
    private int customerId;
    private String cardStatus = "ACTIVE"; // "ACTIVE", "BLOCKED", "EXPIRED"
    private Timestamp issuedAt;
    private Timestamp lastUsedAt;

    // Joined fields
    private String customerName;
    private String customerCode;
    private String customerPhone;

    public MembershipCard() {}

    public MembershipCard(int cardId, String cardNumber, int customerId, String cardStatus) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.cardStatus = cardStatus;
    }

    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCardStatus() { return cardStatus; }
    public void setCardStatus(String cardStatus) { this.cardStatus = cardStatus; }

    public Timestamp getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Timestamp issuedAt) { this.issuedAt = issuedAt; }

    public Timestamp getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Timestamp lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(cardStatus);
    }

    public boolean isBlocked() {
        return "BLOCKED".equalsIgnoreCase(cardStatus);
    }

    @Override
    public String toString() {
        return cardNumber + " (" + cardStatus + ")";
    }
}

