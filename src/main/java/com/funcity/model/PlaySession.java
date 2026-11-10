package com.funcity.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PlaySession {
    private int sessionId;
    private String sessionReference;
    private int customerId;
    private int cardId;
    private int machineId;
    private int gameId;
    private BigDecimal gameFee = BigDecimal.ZERO;
    private int ticketsEarned = 0;
    private String status = "INITIATED"; // "INITIATED", "COMPLETED", "CANCELLED"
    private Integer walletTransactionId;
    private Timestamp startedAt;
    private Timestamp completedAt;
    private Integer staffId;

    // Joined fields
    private String customerName;
    private String cardNumber;
    private String machineCode;
    private String gameName;
    private String staffUsername;

    public PlaySession() {}

    public PlaySession(int sessionId, String sessionReference, int customerId, int cardId, int machineId,
                       int gameId, BigDecimal gameFee, int ticketsEarned, String status,
                       Integer walletTransactionId, Timestamp startedAt, Timestamp completedAt, Integer staffId) {
        this.sessionId = sessionId;
        this.sessionReference = sessionReference;
        this.customerId = customerId;
        this.cardId = cardId;
        this.machineId = machineId;
        this.gameId = gameId;
        this.gameFee = gameFee;
        this.ticketsEarned = ticketsEarned;
        this.status = status;
        this.walletTransactionId = walletTransactionId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.staffId = staffId;
    }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getSessionReference() { return sessionReference; }
    public void setSessionReference(String sessionReference) { this.sessionReference = sessionReference; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public BigDecimal getGameFee() { return gameFee; }
    public void setGameFee(BigDecimal gameFee) { this.gameFee = gameFee; }

    public int getTicketsEarned() { return ticketsEarned; }
    public void setTicketsEarned(int ticketsEarned) { this.ticketsEarned = ticketsEarned; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getWalletTransactionId() { return walletTransactionId; }
    public void setWalletTransactionId(Integer walletTransactionId) { this.walletTransactionId = walletTransactionId; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getStaffUsername() { return staffUsername; }
    public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }

    public boolean isInitiated() {
        return "INITIATED".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return sessionReference + " (" + gameName + " - " + status + ")";
    }
}

