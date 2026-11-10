package com.funcity.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Customer {
    private int customerId;
    private String customerCode;
    private String name;
    private String email;
    private String phone;
    private BigDecimal walletBalance = BigDecimal.ZERO;
    private int ticketBalance = 0;
    private int loyaltyPoints = 0;
    private String status = "ACTIVE"; // "ACTIVE", "BLOCKED"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Optional transient fields for display
    private String activeCardNumber;

    public Customer() {}

    public Customer(int customerId, String customerCode, String name, String email, String phone,
                    BigDecimal walletBalance, int ticketBalance, int loyaltyPoints, String status) {
        this.customerId = customerId;
        this.customerCode = customerCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.walletBalance = walletBalance != null ? walletBalance : BigDecimal.ZERO;
        this.ticketBalance = ticketBalance;
        this.loyaltyPoints = loyaltyPoints;
        this.status = status;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }

    public int getTicketBalance() { return ticketBalance; }
    public void setTicketBalance(int ticketBalance) { this.ticketBalance = ticketBalance; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getActiveCardNumber() { return activeCardNumber; }
    public void setActiveCardNumber(String activeCardNumber) { this.activeCardNumber = activeCardNumber; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return name + " (" + customerCode + ")";
    }
}

