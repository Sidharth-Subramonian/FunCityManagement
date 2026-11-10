package com.funcity.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class WalletTransaction {
    private int transactionId;
    private String transactionReference;
    private int customerId;
    private Integer cardId;
    private String transactionType; // "RECHARGE", "GAME_DEBIT", "REFUND"
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private BigDecimal closingBalance = BigDecimal.ZERO;
    private String description;
    private Integer createdBy;
    private Timestamp createdAt;

    // Joined fields
    private String customerName;
    private String customerCode;
    private String cardNumber;
    private String createdByUsername;

    public WalletTransaction() {}

    public WalletTransaction(int transactionId, String transactionReference, int customerId, Integer cardId,
                             String transactionType, BigDecimal amount, BigDecimal openingBalance,
                             BigDecimal closingBalance, String description, Integer createdBy) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.customerId = customerId;
        this.cardId = cardId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.description = description;
        this.createdBy = createdBy;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Integer getCardId() { return cardId; }
    public void setCardId(Integer cardId) { this.cardId = cardId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    @Override
    public String toString() {
        return transactionReference + " [" + transactionType + " $" + amount + "]";
    }
}

