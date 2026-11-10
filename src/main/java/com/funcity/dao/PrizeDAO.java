package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.Prize;
import com.funcity.model.PrizeRedemption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrizeDAO {

    private static final Logger logger = LoggerFactory.getLogger(PrizeDAO.class);

    public Optional<Prize> findById(int prizeId) {
        String sql = "SELECT * FROM prizes WHERE prize_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prizeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrize(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding prize by id {}: {}", prizeId, e.getMessage());
            throw new RuntimeException("Database error finding prize", e);
        }
        return Optional.empty();
    }

    public Optional<Prize> findByIdForUpdate(Connection conn, int prizeId) throws SQLException {
        String sql = "SELECT * FROM prizes WHERE prize_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prizeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrize(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Prize> findAll() {
        String sql = "SELECT * FROM prizes ORDER BY prize_id ASC";
        List<Prize> prizes = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prizes.add(mapResultSetToPrize(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all prizes: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving prizes", e);
        }
        return prizes;
    }

    public List<Prize> findAvailable() {
        String sql = "SELECT * FROM prizes WHERE status = 'ACTIVE' AND inventory_stock > 0 ORDER BY ticket_cost ASC";
        List<Prize> prizes = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prizes.add(mapResultSetToPrize(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving available prizes: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving available prizes", e);
        }
        return prizes;
    }

    public Prize create(Prize prize) {
        String sql = "INSERT INTO prizes (prize_code, name, category, description, ticket_cost, inventory_stock, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, prize.getPrizeCode());
            ps.setString(2, prize.getName());
            ps.setString(3, prize.getCategory());
            ps.setString(4, prize.getDescription());
            ps.setInt(5, prize.getTicketCost());
            ps.setInt(6, prize.getInventoryStock());
            ps.setString(7, prize.getStatus() != null ? prize.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    prize.setPrizeId(rs.getInt(1));
                }
            }
            return prize;
        } catch (SQLException e) {
            logger.error("Error creating prize {}: {}", prize.getName(), e.getMessage());
            throw new RuntimeException("Database error creating prize: " + e.getMessage(), e);
        }
    }

    public boolean update(Prize prize) {
        String sql = "UPDATE prizes SET name = ?, category = ?, description = ?, ticket_cost = ?, inventory_stock = ?, status = ? WHERE prize_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prize.getName());
            ps.setString(2, prize.getCategory());
            ps.setString(3, prize.getDescription());
            ps.setInt(4, prize.getTicketCost());
            ps.setInt(5, prize.getInventoryStock());
            ps.setString(6, prize.getStatus());
            ps.setInt(7, prize.getPrizeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating prize {}: {}", prize.getPrizeId(), e.getMessage());
            throw new RuntimeException("Database error updating prize", e);
        }
    }

    public boolean updateStock(Connection conn, int prizeId, int newStock) throws SQLException {
        String sql = "UPDATE prizes SET inventory_stock = ? WHERE prize_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, prizeId);
            return ps.executeUpdate() > 0;
        }
    }

    public PrizeRedemption recordRedemption(Connection conn, PrizeRedemption red) throws SQLException {
        String sql = "INSERT INTO prize_redemptions (redemption_reference, customer_id, prize_id, quantity, tickets_spent, staff_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, red.getRedemptionReference());
            ps.setInt(2, red.getCustomerId());
            ps.setInt(3, red.getPrizeId());
            ps.setInt(4, red.getQuantity());
            ps.setInt(5, red.getTicketsSpent());
            if (red.getStaffId() != null) {
                ps.setInt(6, red.getStaffId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    red.setRedemptionId(rs.getInt(1));
                }
            }
            return red;
        }
    }

    public List<PrizeRedemption> findAllRedemptions(int limit) {
        String sql = "SELECT pr.*, c.name AS customer_name, c.customer_code, p.name AS prize_name, p.category AS prize_category, u.username AS staff_username " +
                     "FROM prize_redemptions pr " +
                     "JOIN customers c ON pr.customer_id = c.customer_id " +
                     "JOIN prizes p ON pr.prize_id = p.prize_id " +
                     "LEFT JOIN users u ON pr.staff_id = u.user_id " +
                     "ORDER BY pr.redemption_id DESC LIMIT ?";
        List<PrizeRedemption> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRedemption(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving redemptions: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving redemptions", e);
        }
        return list;
    }

    private Prize mapResultSetToPrize(ResultSet rs) throws SQLException {
        Prize p = new Prize();
        p.setPrizeId(rs.getInt("prize_id"));
        p.setPrizeCode(rs.getString("prize_code"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setTicketCost(rs.getInt("ticket_cost"));
        p.setInventoryStock(rs.getInt("inventory_stock"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }

    private PrizeRedemption mapResultSetToRedemption(ResultSet rs) throws SQLException {
        PrizeRedemption pr = new PrizeRedemption();
        pr.setRedemptionId(rs.getInt("redemption_id"));
        pr.setRedemptionReference(rs.getString("redemption_reference"));
        pr.setCustomerId(rs.getInt("customer_id"));
        pr.setPrizeId(rs.getInt("prize_id"));
        pr.setQuantity(rs.getInt("quantity"));
        pr.setTicketsSpent(rs.getInt("tickets_spent"));
        int staffId = rs.getInt("staff_id");
        if (!rs.wasNull()) {
            pr.setStaffId(staffId);
        }
        pr.setRedeemedAt(rs.getTimestamp("redeemed_at"));
        try {
            pr.setCustomerName(rs.getString("customer_name"));
            pr.setCustomerCode(rs.getString("customer_code"));
            pr.setPrizeName(rs.getString("prize_name"));
            pr.setPrizeCategory(rs.getString("prize_category"));
            pr.setStaffUsername(rs.getString("staff_username"));
        } catch (SQLException ignored) {}
        return pr;
    }
}

