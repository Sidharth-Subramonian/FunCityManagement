package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.RewardTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RewardDAO {

    private static final Logger logger = LoggerFactory.getLogger(RewardDAO.class);

    public RewardTransaction recordTransaction(Connection conn, RewardTransaction tx) throws SQLException {
        String sql = "INSERT INTO reward_transactions (transaction_reference, customer_id, points, " +
                     "opening_points, closing_points, activity_type, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getTransactionReference());
            ps.setInt(2, tx.getCustomerId());
            ps.setInt(3, tx.getPoints());
            ps.setInt(4, tx.getOpeningPoints());
            ps.setInt(5, tx.getClosingPoints());
            ps.setString(6, tx.getActivityType());
            ps.setString(7, tx.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setRewardId(rs.getInt(1));
                }
            }
            return tx;
        }
    }

    public List<RewardTransaction> findByCustomerId(int customerId) {
        String sql = "SELECT rt.*, c.name AS customer_name, c.customer_code " +
                     "FROM reward_transactions rt " +
                     "JOIN customers c ON rt.customer_id = c.customer_id " +
                     "WHERE rt.customer_id = ? ORDER BY rt.reward_id DESC";
        List<RewardTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRewardTx(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving reward transactions for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Database error retrieving reward transactions", e);
        }
        return list;
    }

    public List<RewardTransaction> findRecent(int limit) {
        String sql = "SELECT rt.*, c.name AS customer_name, c.customer_code " +
                     "FROM reward_transactions rt " +
                     "JOIN customers c ON rt.customer_id = c.customer_id " +
                     "ORDER BY rt.reward_id DESC LIMIT ?";
        List<RewardTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRewardTx(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recent reward transactions: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving reward transactions", e);
        }
        return list;
    }

    private RewardTransaction mapResultSetToRewardTx(ResultSet rs) throws SQLException {
        RewardTransaction rt = new RewardTransaction();
        rt.setRewardId(rs.getInt("reward_id"));
        rt.setTransactionReference(rs.getString("transaction_reference"));
        rt.setCustomerId(rs.getInt("customer_id"));
        rt.setPoints(rs.getInt("points"));
        rt.setOpeningPoints(rs.getInt("opening_points"));
        rt.setClosingPoints(rs.getInt("closing_points"));
        rt.setActivityType(rs.getString("activity_type"));
        rt.setDescription(rs.getString("description"));
        rt.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            rt.setCustomerName(rs.getString("customer_name"));
            rt.setCustomerCode(rs.getString("customer_code"));
        } catch (SQLException ignored) {}
        return rt;
    }
}

