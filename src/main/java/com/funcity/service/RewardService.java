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

public class RewardService {

    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);
    private final RewardDAO rewardDAO;
    private final CustomerDAO customerDAO;
    private final WalletDAO walletDAO;

    public RewardService() {
        this.rewardDAO = new RewardDAO();
        this.customerDAO = new CustomerDAO();
        this.walletDAO = new WalletDAO();
    }

    public RewardService(RewardDAO rewardDAO, CustomerDAO customerDAO, WalletDAO walletDAO) {
        this.rewardDAO = rewardDAO;
        this.customerDAO = customerDAO;
        this.walletDAO = walletDAO;
    }

    public RewardTransaction awardPoints(int customerId, int points, String activityType, String description) {
        ValidationUtil.requirePositive(points, "Points");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
                if (custOpt.isEmpty()) {
                    throw new IllegalArgumentException("Customer not found.");
                }
                Customer customer = custOpt.get();

                int opening = customer.getLoyaltyPoints();
                int closing = opening + points;
                customerDAO.updateBalances(conn, customerId, customer.getWalletBalance(), customer.getTicketBalance(), closing);

                RewardTransaction rt = new RewardTransaction(
                        0,
                        IDGenerator.generateRewardReference(),
                        customerId,
                        points,
                        opening,
                        closing,
                        activityType != null ? activityType : "SPECIAL_AWARD",
                        description
                );
                rewardDAO.recordTransaction(conn, rt);

                conn.commit();
                logger.info("Awarded {} points to customer {} ({})", points, customerId, activityType);
                return rt;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to award points to customer {}: {}", customerId, e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Failed to award points", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error awarding loyalty points", e);
        }
    }

    public boolean convertPointsToWalletCredit(int customerId, int pointsToConvert, Integer staffId) {
        // Exchange rate: 100 points = $5.00 wallet bonus
        if (pointsToConvert < 100 || pointsToConvert % 100 != 0) {
            throw new IllegalArgumentException("Points must be redeemed in increments of 100 (100 pts = $5.00).");
        }

        BigDecimal bonusCredit = BigDecimal.valueOf((pointsToConvert / 100) * 5L).setScale(2, RoundingMode.HALF_UP);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
                if (custOpt.isEmpty()) {
                    throw new IllegalArgumentException("Customer not found.");
                }
                Customer customer = custOpt.get();

                if (customer.getLoyaltyPoints() < pointsToConvert) {
                    throw new IllegalStateException("Insufficient loyalty points. Has: " + customer.getLoyaltyPoints() + ", Needed: " + pointsToConvert);
                }

                int openingPts = customer.getLoyaltyPoints();
                int closingPts = openingPts - pointsToConvert;

                BigDecimal openingBal = customer.getWalletBalance();
                BigDecimal closingBal = openingBal.add(bonusCredit);

                customerDAO.updateBalances(conn, customerId, closingBal, customer.getTicketBalance(), closingPts);

                // Record reward transaction
                RewardTransaction rt = new RewardTransaction(
                        0,
                        IDGenerator.generateRewardReference(),
                        customerId,
                        -pointsToConvert,
                        openingPts,
                        closingPts,
                        "POINTS_REDEMPTION",
                        "Converted " + pointsToConvert + " points to $" + bonusCredit + " wallet credit"
                );
                rewardDAO.recordTransaction(conn, rt);

                // Record wallet transaction
                WalletTransaction wt = new WalletTransaction(
                        0,
                        IDGenerator.generateWalletTxReference(),
                        customerId,
                        null,
                        "RECHARGE",
                        bonusCredit,
                        openingBal,
                        closingBal,
                        "Loyalty points conversion bonus (" + pointsToConvert + " pts)",
                        staffId
                );
                walletDAO.recordTransaction(conn, wt);

                conn.commit();
                logger.info("Converted {} points to ${} wallet bonus for customer {}", pointsToConvert, bonusCredit, customerId);
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to convert points for customer {}: {}", customerId, e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Conversion failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error converting points", e);
        }
    }

    public List<RewardTransaction> getCustomerRewardHistory(int customerId) {
        return rewardDAO.findByCustomerId(customerId);
    }
}

