package com.funcity.service;

import com.funcity.config.DatabaseConfig;
import com.funcity.dao.CustomerDAO;
import com.funcity.dao.RewardDAO;
import com.funcity.dao.WalletDAO;
import com.funcity.model.Customer;
import com.funcity.model.RewardTransaction;
import com.funcity.model.WalletTransaction;
import com.funcity.util.IDGenerator;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    private final WalletDAO walletDAO;
    private final CustomerDAO customerDAO;
    private final RewardDAO rewardDAO;

    public WalletService() {
        this.walletDAO = new WalletDAO();
        this.customerDAO = new CustomerDAO();
        this.rewardDAO = new RewardDAO();
    }

    public WalletService(WalletDAO walletDAO, CustomerDAO customerDAO, RewardDAO rewardDAO) {
        this.walletDAO = walletDAO;
        this.customerDAO = customerDAO;
        this.rewardDAO = rewardDAO;
    }

    public WalletTransaction rechargeWallet(int customerId, Integer cardId, BigDecimal amount, String description, Integer staffId) {
        ValidationUtil.requirePositive(amount, "Recharge Amount");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
                if (custOpt.isEmpty()) {
                    throw new IllegalArgumentException("Customer does not exist with ID: " + customerId);
                }
                Customer customer = custOpt.get();
                if (!customer.isActive()) {
                    throw new IllegalStateException("Customer account is blocked. Recharge not permitted.");
                }

                BigDecimal opening = customer.getWalletBalance();
                BigDecimal closing = opening.add(amount).setScale(2, RoundingMode.HALF_UP);

                // Award 1 loyalty point per whole dollar recharged
                int pointsEarned = amount.intValue();
                int openingPts = customer.getLoyaltyPoints();
                int closingPts = openingPts + pointsEarned;

                customerDAO.updateBalances(conn, customerId, closing, customer.getTicketBalance(), closingPts);

                WalletTransaction tx = new WalletTransaction(
                        0,
                        IDGenerator.generateWalletTxReference(),
                        customerId,
                        cardId,
                        "RECHARGE",
                        amount,
                        opening,
                        closing,
                        description != null ? description : "Wallet Cash Recharge",
                        staffId
                );
                walletDAO.recordTransaction(conn, tx);

                if (pointsEarned > 0) {
                    RewardTransaction rewTx = new RewardTransaction(
                            0,
                            IDGenerator.generateRewardReference(),
                            customerId,
                            pointsEarned,
                            openingPts,
                            closingPts,
                            "RECHARGE_BONUS",
                            "Bonus points for $" + amount + " recharge"
                    );
                    rewardDAO.recordTransaction(conn, rewTx);
                }

                conn.commit();
                logger.info("Successfully recharged wallet for customer {}: ${} (New Balance: ${})",
                        customerId, amount, closing);
                return tx;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to execute wallet recharge for customer {}: {}", customerId, e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Recharge failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during recharge", e);
        }
    }

    public WalletTransaction debitWallet(Connection conn, int customerId, Integer cardId, BigDecimal amount, String description, Integer staffId) throws SQLException {
        ValidationUtil.requirePositive(amount, "Debit Amount");

        Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
        if (custOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer does not exist with ID: " + customerId);
        }
        Customer customer = custOpt.get();
        if (!customer.isActive()) {
            throw new IllegalStateException("Customer account is blocked.");
        }

        BigDecimal opening = customer.getWalletBalance();
        if (opening.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient wallet balance. Required: $" + amount + ", Available: $" + opening);
        }

        BigDecimal closing = opening.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        customerDAO.updateBalances(conn, customerId, closing, customer.getTicketBalance(), customer.getLoyaltyPoints());

        WalletTransaction tx = new WalletTransaction(
                0,
                IDGenerator.generateWalletTxReference(),
                customerId,
                cardId,
                "GAME_DEBIT",
                amount,
                opening,
                closing,
                description,
                staffId
        );
        return walletDAO.recordTransaction(conn, tx);
    }

    public WalletTransaction refundWallet(Connection conn, int customerId, Integer cardId, BigDecimal amount, String description, Integer staffId) throws SQLException {
        ValidationUtil.requirePositive(amount, "Refund Amount");

        Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
        if (custOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer does not exist with ID: " + customerId);
        }
        Customer customer = custOpt.get();

        BigDecimal opening = customer.getWalletBalance();
        BigDecimal closing = opening.add(amount).setScale(2, RoundingMode.HALF_UP);

        customerDAO.updateBalances(conn, customerId, closing, customer.getTicketBalance(), customer.getLoyaltyPoints());

        WalletTransaction tx = new WalletTransaction(
                0,
                IDGenerator.generateWalletTxReference(),
                customerId,
                cardId,
                "REFUND",
                amount,
                opening,
                closing,
                description,
                staffId
        );
        return walletDAO.recordTransaction(conn, tx);
    }

    public List<WalletTransaction> getCustomerTransactions(int customerId) {
        return walletDAO.findByCustomerId(customerId);
    }

    public List<WalletTransaction> getRecentTransactions(int limit) {
        return walletDAO.findRecent(limit);
    }
}

