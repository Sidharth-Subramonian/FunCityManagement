package com.funcity.model;

import java.sql.Timestamp;

public class TicketTransaction {
    private int ticketTransactionId;
    private String transactionReference;
    private int customerId;
    private Integer sessionId;
    private String transactionType; // "EARNED", "REDEEMED", "ADJUSTED"
    private int tickets; // positive if earned/adjusted up, negative if redeemed
    private int openingBalance;
    private int closingBalance;
    private String description;
    private Integer createdBy;
    private Timestamp createdAt;

    // Joined fields
    private String customerName;
    private String customerCode;
    private String createdByUsername;

    public TicketTransaction() {}

    public TicketTransaction(int ticketTransactionId, String transactionReference, int customerId, Integer sessionId,
                             String transactionType, int tickets, int openingBalance, int closingBalance,
                             String description, Integer createdBy) {
        this.ticketTransactionId = ticketTransactionId;
        this.transactionReference = transactionReference;
        this.customerId = customerId;
        this.sessionId = sessionId;
        this.transactionType = transactionType;
        this.tickets = tickets;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.description = description;
        this.createdBy = createdBy;
    }

    public int getTicketTransactionId() { return ticketTransactionId; }
    public void setTicketTransactionId(int ticketTransactionId) { this.ticketTransactionId = ticketTransactionId; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getTickets() { return tickets; }
    public void setTickets(int tickets) { this.tickets = tickets; }

    public int getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(int openingBalance) { this.openingBalance = openingBalance; }

    public int getClosingBalance() { return closingBalance; }
    public void setClosingBalance(int closingBalance) { this.closingBalance = closingBalance; }

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

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    @Override
    public String toString() {
        return transactionReference + " [" + transactionType + " " + tickets + " tickets]";
    }
}

