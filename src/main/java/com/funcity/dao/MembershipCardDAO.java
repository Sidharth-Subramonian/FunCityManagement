package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.MembershipCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembershipCardDAO {

    private static final Logger logger = LoggerFactory.getLogger(MembershipCardDAO.class);

    public Optional<MembershipCard> findByCardNumber(String cardNumber) {
        String sql = "SELECT mc.*, c.name AS customer_name, c.customer_code, c.phone AS customer_phone " +
                     "FROM membership_cards mc " +
                     "JOIN customers c ON mc.customer_id = c.customer_id " +
                     "WHERE mc.card_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCard(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding card by number {}: {}", cardNumber, e.getMessage());
            throw new RuntimeException("Database error finding card", e);
        }
        return Optional.empty();
    }

    public Optional<MembershipCard> findActiveByCustomerId(int customerId) {
        String sql = "SELECT mc.*, c.name AS customer_name, c.customer_code, c.phone AS customer_phone " +
                     "FROM membership_cards mc " +
                     "JOIN customers c ON mc.customer_id = c.customer_id " +
                     "WHERE mc.customer_id = ? AND mc.card_status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCard(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding active card for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Database error finding active card", e);
        }
        return Optional.empty();
    }

    public List<MembershipCard> findAllByCustomerId(int customerId) {
        String sql = "SELECT mc.*, c.name AS customer_name, c.customer_code, c.phone AS customer_phone " +
                     "FROM membership_cards mc " +
                     "JOIN customers c ON mc.customer_id = c.customer_id " +
                     "WHERE mc.customer_id = ? ORDER BY mc.card_id DESC";
        List<MembershipCard> cards = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToCard(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving cards for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Database error retrieving cards", e);
        }
        return cards;
    }

    public List<MembershipCard> findAll() {
        String sql = "SELECT mc.*, c.name AS customer_name, c.customer_code, c.phone AS customer_phone " +
                     "FROM membership_cards mc " +
                     "JOIN customers c ON mc.customer_id = c.customer_id " +
                     "ORDER BY mc.card_id DESC";
        List<MembershipCard> cards = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cards.add(mapResultSetToCard(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all cards: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving cards", e);
        }
        return cards;
    }

    public MembershipCard create(MembershipCard card) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return create(conn, card);
        } catch (SQLException e) {
            logger.error("Error creating card {}: {}", card.getCardNumber(), e.getMessage());
            throw new RuntimeException("Database error creating card: " + e.getMessage(), e);
        }
    }

    public MembershipCard create(Connection conn, MembershipCard card) throws SQLException {
        String sql = "INSERT INTO membership_cards (card_number, customer_id, card_status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, card.getCardNumber());
            ps.setInt(2, card.getCustomerId());
            ps.setString(3, card.getCardStatus() != null ? card.getCardStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    card.setCardId(rs.getInt(1));
                }
            }
            return card;
        }
    }

    public boolean updateStatus(int cardId, String newStatus) {
        String sql = "UPDATE membership_cards SET card_status = ? WHERE card_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, cardId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for card {}: {}", cardId, e.getMessage());
            throw new RuntimeException("Database error updating card status", e);
        }
    }

    public boolean updateLastUsed(int cardId) {
        String sql = "UPDATE membership_cards SET last_used_at = CURRENT_TIMESTAMP WHERE card_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating last_used for card {}: {}", cardId, e.getMessage());
            return false;
        }
    }

    private MembershipCard mapResultSetToCard(ResultSet rs) throws SQLException {
        MembershipCard mc = new MembershipCard();
        mc.setCardId(rs.getInt("card_id"));
        mc.setCardNumber(rs.getString("card_number"));
        mc.setCustomerId(rs.getInt("customer_id"));
        mc.setCardStatus(rs.getString("card_status"));
        mc.setIssuedAt(rs.getTimestamp("issued_at"));
        mc.setLastUsedAt(rs.getTimestamp("last_used_at"));
        try {
            mc.setCustomerName(rs.getString("customer_name"));
            mc.setCustomerCode(rs.getString("customer_code"));
            mc.setCustomerPhone(rs.getString("customer_phone"));
        } catch (SQLException ignored) {}
        return mc;
    }
}

