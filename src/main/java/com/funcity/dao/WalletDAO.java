package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.WalletTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WalletDAO {

    private static final Logger logger = LoggerFactory.getLogger(WalletDAO.class);

    public WalletTransaction recordTransaction(Connection conn, WalletTransaction tx) throws SQLException {
        String sql = "INSERT INTO wallet_transactions (transaction_reference, customer_id, card_id, transaction_type, " +
                     "amount, opening_balance, closing_balance, description, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getTransactionReference());
            ps.setInt(2, tx.getCustomerId());
            if (tx.getCardId() != null) {
                ps.setInt(3, tx.getCardId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, tx.getTransactionType());
            ps.setBigDecimal(5, tx.getAmount());
            ps.setBigDecimal(6, tx.getOpeningBalance());
            ps.setBigDecimal(7, tx.getClosingBalance());
            ps.setString(8, tx.getDescription());
            if (tx.getCreatedBy() != null) {
                ps.setInt(9, tx.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setTransactionId(rs.getInt(1));
                }
            }
            return tx;
        }
    }

    public List<WalletTransaction> findByCustomerId(int customerId) {
        String sql = "SELECT wt.*, c.name AS customer_name, c.customer_code, mc.card_number, u.username AS created_by_username " +
                     "FROM wallet_transactions wt " +
                     "JOIN customers c ON wt.customer_id = c.customer_id " +
                     "LEFT JOIN membership_cards mc ON wt.card_id = mc.card_id " +
                     "LEFT JOIN users u ON wt.created_by = u.user_id " +
                     "WHERE wt.customer_id = ? ORDER BY wt.transaction_id DESC";
        List<WalletTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToWalletTx(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving wallet transactions for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Database error retrieving wallet transactions", e);
        }
        return list;
    }

    public List<WalletTransaction> findRecent(int limit) {
        String sql = "SELECT wt.*, c.name AS customer_name, c.customer_code, mc.card_number, u.username AS created_by_username " +
                     "FROM wallet_transactions wt " +
                     "JOIN customers c ON wt.customer_id = c.customer_id " +
                     "LEFT JOIN membership_cards mc ON wt.card_id = mc.card_id " +
                     "LEFT JOIN users u ON wt.created_by = u.user_id " +
                     "ORDER BY wt.transaction_id DESC LIMIT ?";
        List<WalletTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToWalletTx(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recent wallet transactions: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving wallet transactions", e);
        }
        return list;
    }

    public BigDecimal calculateRevenue(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions " +
                     "WHERE transaction_type = 'RECHARGE' AND created_at >= ? AND created_at <= ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, startDate);
            ps.setTimestamp(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating revenue: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private WalletTransaction mapResultSetToWalletTx(ResultSet rs) throws SQLException {
        WalletTransaction wt = new WalletTransaction();
        wt.setTransactionId(rs.getInt("transaction_id"));
        wt.setTransactionReference(rs.getString("transaction_reference"));
        wt.setCustomerId(rs.getInt("customer_id"));
        int cardId = rs.getInt("card_id");
        if (!rs.wasNull()) {
            wt.setCardId(cardId);
        }
        wt.setTransactionType(rs.getString("transaction_type"));
        wt.setAmount(rs.getBigDecimal("amount"));
        wt.setOpeningBalance(rs.getBigDecimal("opening_balance"));
        wt.setClosingBalance(rs.getBigDecimal("closing_balance"));
        wt.setDescription(rs.getString("description"));
        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) {
            wt.setCreatedBy(createdBy);
        }
        wt.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            wt.setCustomerName(rs.getString("customer_name"));
            wt.setCustomerCode(rs.getString("customer_code"));
            wt.setCardNumber(rs.getString("card_number"));
            wt.setCreatedByUsername(rs.getString("created_by_username"));
        } catch (SQLException ignored) {}
        return wt;
    }
}

