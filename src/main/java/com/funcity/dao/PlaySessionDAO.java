package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.PlaySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaySessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(PlaySessionDAO.class);

    public PlaySession create(Connection conn, PlaySession session) throws SQLException {
        String sql = "INSERT INTO play_sessions (session_reference, customer_id, card_id, machine_id, game_id, " +
                     "game_fee, tickets_earned, status, wallet_transaction_id, staff_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, session.getSessionReference());
            ps.setInt(2, session.getCustomerId());
            ps.setInt(3, session.getCardId());
            ps.setInt(4, session.getMachineId());
            ps.setInt(5, session.getGameId());
            ps.setBigDecimal(6, session.getGameFee());
            ps.setInt(7, session.getTicketsEarned());
            ps.setString(8, session.getStatus() != null ? session.getStatus() : "INITIATED");
            if (session.getWalletTransactionId() != null) {
                ps.setInt(9, session.getWalletTransactionId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            if (session.getStaffId() != null) {
                ps.setInt(10, session.getStaffId());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    session.setSessionId(rs.getInt(1));
                }
            }
            return session;
        }
    }

    public Optional<PlaySession> findById(int sessionId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return findById(conn, sessionId);
        } catch (SQLException e) {
            logger.error("Error finding session by id {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Database error finding session", e);
        }
    }

    public Optional<PlaySession> findById(Connection conn, int sessionId) throws SQLException {
        String sql = getSessionSelectQuery() + " WHERE ps.session_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPlaySession(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<PlaySession> findInitiatedSessions() {
        String sql = getSessionSelectQuery() + " WHERE ps.status = 'INITIATED' ORDER BY ps.session_id DESC";
        List<PlaySession> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPlaySession(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving initiated play sessions: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving initiated play sessions", e);
        }
        return list;
    }

    public List<PlaySession> findRecent(int limit) {
        String sql = getSessionSelectQuery() + " ORDER BY ps.session_id DESC LIMIT ?";
        List<PlaySession> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPlaySession(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recent play sessions: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving play sessions", e);
        }
        return list;
    }

    public boolean completeSession(Connection conn, int sessionId, int ticketsEarned) throws SQLException {
        String sql = "UPDATE play_sessions SET status = 'COMPLETED', tickets_earned = ?, completed_at = CURRENT_TIMESTAMP WHERE session_id = ? AND status = 'INITIATED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketsEarned);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelSession(Connection conn, int sessionId) throws SQLException {
        String sql = "UPDATE play_sessions SET status = 'CANCELLED', completed_at = CURRENT_TIMESTAMP WHERE session_id = ? AND status = 'INITIATED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    private String getSessionSelectQuery() {
        return "SELECT ps.*, c.name AS customer_name, mc.card_number, m.machine_code, g.name AS game_name, u.username AS staff_username " +
               "FROM play_sessions ps " +
               "JOIN customers c ON ps.customer_id = c.customer_id " +
               "JOIN membership_cards mc ON ps.card_id = mc.card_id " +
               "JOIN machines m ON ps.machine_id = m.machine_id " +
               "JOIN games g ON ps.game_id = g.game_id " +
               "LEFT JOIN users u ON ps.staff_id = u.user_id";
    }

    private PlaySession mapResultSetToPlaySession(ResultSet rs) throws SQLException {
        PlaySession s = new PlaySession();
        s.setSessionId(rs.getInt("session_id"));
        s.setSessionReference(rs.getString("session_reference"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setCardId(rs.getInt("card_id"));
        s.setMachineId(rs.getInt("machine_id"));
        s.setGameId(rs.getInt("game_id"));
        s.setGameFee(rs.getBigDecimal("game_fee"));
        s.setTicketsEarned(rs.getInt("tickets_earned"));
        s.setStatus(rs.getString("status"));
        int wtxId = rs.getInt("wallet_transaction_id");
        if (!rs.wasNull()) {
            s.setWalletTransactionId(wtxId);
        }
        s.setStartedAt(rs.getTimestamp("started_at"));
        s.setCompletedAt(rs.getTimestamp("completed_at"));
        int staffId = rs.getInt("staff_id");
        if (!rs.wasNull()) {
            s.setStaffId(staffId);
        }
        try {
            s.setCustomerName(rs.getString("customer_name"));
            s.setCardNumber(rs.getString("card_number"));
            s.setMachineCode(rs.getString("machine_code"));
            s.setGameName(rs.getString("game_name"));
            s.setStaffUsername(rs.getString("staff_username"));
        } catch (SQLException ignored) {}
        return s;
    }
}

