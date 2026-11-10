package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.TicketTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private static final Logger logger = LoggerFactory.getLogger(TicketDAO.class);

    public TicketTransaction recordTransaction(Connection conn, TicketTransaction tx) throws SQLException {
        String sql = "INSERT INTO ticket_transactions (transaction_reference, customer_id, session_id, " +
                     "transaction_type, tickets, opening_balance, closing_balance, description, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getTransactionReference());
            ps.setInt(2, tx.getCustomerId());
            if (tx.getSessionId() != null) {
                ps.setInt(3, tx.getSessionId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, tx.getTransactionType());
            ps.setInt(5, tx.getTickets());
            ps.setInt(6, tx.getOpeningBalance());
            ps.setInt(7, tx.getClosingBalance());
            ps.setString(8, tx.getDescription());
            if (tx.getCreatedBy() != null) {
                ps.setInt(9, tx.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setTicketTransactionId(rs.getInt(1));
                }
            }
            return tx;
        }
    }

    public List<TicketTransaction> findByCustomerId(int customerId) {
        String sql = "SELECT tt.*, c.name AS customer_name, c.customer_code, u.username AS created_by_username " +
                     "FROM ticket_transactions tt " +
                     "JOIN customers c ON tt.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON tt.created_by = u.user_id " +
                     "WHERE tt.customer_id = ? ORDER BY tt.ticket_transaction_id DESC";
        List<TicketTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTicketTx(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving ticket transactions for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Database error retrieving ticket transactions", e);
        }
        return list;
    }

    public List<TicketTransaction> findRecent(int limit) {
        String sql = "SELECT tt.*, c.name AS customer_name, c.customer_code, u.username AS created_by_username " +
                     "FROM ticket_transactions tt " +
                     "JOIN customers c ON tt.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON tt.created_by = u.user_id " +
                     "ORDER BY tt.ticket_transaction_id DESC LIMIT ?";
        List<TicketTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTicketTx(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recent ticket transactions: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving ticket transactions", e);
        }
        return list;
    }

    private TicketTransaction mapResultSetToTicketTx(ResultSet rs) throws SQLException {
        TicketTransaction tt = new TicketTransaction();
        tt.setTicketTransactionId(rs.getInt("ticket_transaction_id"));
        tt.setTransactionReference(rs.getString("transaction_reference"));
        tt.setCustomerId(rs.getInt("customer_id"));
        int sessId = rs.getInt("session_id");
        if (!rs.wasNull()) {
            tt.setSessionId(sessId);
        }
        tt.setTransactionType(rs.getString("transaction_type"));
        tt.setTickets(rs.getInt("tickets"));
        tt.setOpeningBalance(rs.getInt("opening_balance"));
        tt.setClosingBalance(rs.getInt("closing_balance"));
        tt.setDescription(rs.getString("description"));
        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) {
            tt.setCreatedBy(createdBy);
        }
        tt.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            tt.setCustomerName(rs.getString("customer_name"));
            tt.setCustomerCode(rs.getString("customer_code"));
            tt.setCreatedByUsername(rs.getString("created_by_username"));
        } catch (SQLException ignored) {}
        return tt;
    }
}

