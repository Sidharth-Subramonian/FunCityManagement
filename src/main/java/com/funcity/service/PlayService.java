package com.funcity.service;

import com.funcity.config.DatabaseConfig;
import com.funcity.dao.*;
import com.funcity.model.*;
import com.funcity.util.IDGenerator;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PlayService {

    private static final Logger logger = LoggerFactory.getLogger(PlayService.class);
    private final PlaySessionDAO playSessionDAO;
    private final MembershipService membershipService;
    private final CustomerDAO customerDAO;
    private final MachineDAO machineDAO;
    private final GameDAO gameDAO;
    private final WalletService walletService;
    private final TicketDAO ticketDAO;

    public PlayService() {
        this.playSessionDAO = new PlaySessionDAO();
        this.membershipService = new MembershipService();
        this.customerDAO = new CustomerDAO();
        this.machineDAO = new MachineDAO();
        this.gameDAO = new GameDAO();
        this.walletService = new WalletService();
        this.ticketDAO = new TicketDAO();
    }

    public PlayService(PlaySessionDAO playSessionDAO, MembershipService membershipService,
                       CustomerDAO customerDAO, MachineDAO machineDAO, GameDAO gameDAO,
                       WalletService walletService, TicketDAO ticketDAO) {
        this.playSessionDAO = playSessionDAO;
        this.membershipService = membershipService;
        this.customerDAO = customerDAO;
        this.machineDAO = machineDAO;
        this.gameDAO = gameDAO;
        this.walletService = walletService;
        this.ticketDAO = ticketDAO;
    }

    public PlaySession initiatePlaySession(String cardNumber, int machineId, Integer staffId) {
        ValidationUtil.requireNonEmpty(cardNumber, "Card Number");

        // 1. Validate Card & Customer
        MembershipCard card = membershipService.validateCardForUse(cardNumber);
        int customerId = card.getCustomerId();

        // 2. Validate Machine
        Optional<Machine> machineOpt = machineDAO.findById(machineId);
        if (machineOpt.isEmpty()) {
            throw new IllegalArgumentException("Machine not found with ID: " + machineId);
        }
        Machine machine = machineOpt.get();
        if (!machine.isOperational()) {
            throw new IllegalStateException("Machine " + machine.getMachineCode() + " is " + machine.getStatus() + " and not accepting plays.");
        }

        // 3. Validate Game and retrieve Price from DB
        Optional<Game> gameOpt = gameDAO.findById(machine.getGameId());
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Associated game not found for machine: " + machine.getMachineCode());
        }
        Game game = gameOpt.get();
        if (!game.isActive()) {
            throw new IllegalStateException("Game '" + game.getName() + "' is currently inactive.");
        }

        BigDecimal price = game.getPricePerPlay();

        // 4. Atomic Execution: Debit Wallet & Create Play Session
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Debit wallet
                WalletTransaction debitTx = walletService.debitWallet(
                        conn,
                        customerId,
                        card.getCardId(),
                        price,
                        "Play " + game.getName() + " on " + machine.getMachineCode(),
                        staffId
                );

                // Create play session
                PlaySession session = new PlaySession(
                        0,
                        IDGenerator.generatePlaySessionReference(),
                        customerId,
                        card.getCardId(),
                        machineId,
                        game.getGameId(),
                        price,
                        0,
                        "INITIATED",
                        debitTx.getTransactionId(),
                        null,
                        null,
                        staffId
                );
                session = playSessionDAO.create(conn, session);

                conn.commit();
                logger.info("Initiated play session {} for customer {} on machine {}",
                        session.getSessionReference(), customerId, machine.getMachineCode());
                return session;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to initiate play session: {}", e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Could not initiate play: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during play session initiation", e);
        }
    }

    public boolean completePlaySession(int sessionId, int ticketsEarned, Integer staffId) {
        if (ticketsEarned < 0) {
            throw new IllegalArgumentException("Tickets earned cannot be negative.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<PlaySession> sessOpt = playSessionDAO.findById(conn, sessionId);
                if (sessOpt.isEmpty()) {
                    throw new IllegalArgumentException("Play session not found with ID: " + sessionId);
                }
                PlaySession session = sessOpt.get();
                if (!session.isInitiated()) {
                    throw new IllegalStateException("Session is already " + session.getStatus() + ". Cannot complete.");
                }

                int customerId = session.getCustomerId();
                Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
                if (custOpt.isEmpty()) {
                    throw new IllegalArgumentException("Customer not found.");
                }
                Customer customer = custOpt.get();

                // 1. Complete Session Record
                playSessionDAO.completeSession(conn, sessionId, ticketsEarned);

                // 2. Award Tickets if applicable
                if (ticketsEarned > 0) {
                    int openingTickets = customer.getTicketBalance();
                    int closingTickets = openingTickets + ticketsEarned;

                    customerDAO.updateBalances(conn, customerId, customer.getWalletBalance(), closingTickets, customer.getLoyaltyPoints());

                    TicketTransaction tt = new TicketTransaction(
                            0,
                            IDGenerator.generateTicketTxReference(),
                            customerId,
                            sessionId,
                            "EARNED",
                            ticketsEarned,
                            openingTickets,
                            closingTickets,
                            "Tickets won from " + session.getGameName() + " (" + session.getSessionReference() + ")",
                            staffId
                    );
                    ticketDAO.recordTransaction(conn, tt);
                }

                conn.commit();
                logger.info("Completed play session {}. Tickets awarded: {}", session.getSessionReference(), ticketsEarned);
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to complete play session {}: {}", sessionId, e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Completion failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error completing play session", e);
        }
    }

    public boolean cancelPlaySession(int sessionId, String reason, Integer staffId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<PlaySession> sessOpt = playSessionDAO.findById(conn, sessionId);
                if (sessOpt.isEmpty()) {
                    throw new IllegalArgumentException("Play session not found with ID: " + sessionId);
                }
                PlaySession session = sessOpt.get();
                if (!session.isInitiated()) {
                    throw new IllegalStateException("Session is already " + session.getStatus() + ". Cannot cancel.");
                }

                // 1. Mark session CANCELLED
                playSessionDAO.cancelSession(conn, sessionId);

                // 2. Refund wallet atomically
                walletService.refundWallet(
                        conn,
                        session.getCustomerId(),
                        session.getCardId(),
                        session.getGameFee(),
                        "Refund for cancelled session " + session.getSessionReference() + (reason != null ? " (" + reason + ")" : ""),
                        staffId
                );

                conn.commit();
                logger.info("Cancelled play session {} and refunded ${}", session.getSessionReference(), session.getGameFee());
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to cancel play session {}: {}", sessionId, e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Cancellation failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error cancelling play session", e);
        }
    }

    public List<PlaySession> getActiveInitiatedSessions() {
        return playSessionDAO.findInitiatedSessions();
    }

    public List<PlaySession> getRecentSessions(int limit) {
        return playSessionDAO.findRecent(limit);
    }
}

