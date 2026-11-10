package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.DashboardMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    public DashboardMetrics getDashboardMetrics() {
        DashboardMetrics m = new DashboardMetrics();
        try (Connection conn = DatabaseConfig.getConnection()) {
            m.setTotalCustomers(countQuery(conn, "SELECT COUNT(*) FROM customers"));
            m.setTotalActiveCards(countQuery(conn, "SELECT COUNT(*) FROM membership_cards WHERE card_status = 'ACTIVE'"));
            m.setTotalGames(countQuery(conn, "SELECT COUNT(*) FROM games WHERE status = 'ACTIVE'"));
            m.setTotalMachines(countQuery(conn, "SELECT COUNT(*) FROM machines"));
            m.setOperationalMachines(countQuery(conn, "SELECT COUNT(*) FROM machines WHERE status = 'OPERATIONAL'"));
            m.setMachinesUnderMaintenance(countQuery(conn, "SELECT COUNT(*) FROM machines WHERE status = 'UNDER_MAINTENANCE'"));

            // Today's metrics (using CURRENT_DATE or date function)
            String revSql = "SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions " +
                            "WHERE transaction_type = 'RECHARGE' AND CAST(created_at AS DATE) = CURRENT_DATE";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(revSql)) {
                if (rs.next()) {
                    m.setTodayRevenue(rs.getBigDecimal(1));
                }
            }

            m.setTotalPlaySessionsToday(countQuery(conn, "SELECT COUNT(*) FROM play_sessions WHERE CAST(started_at AS DATE) = CURRENT_DATE"));
            m.setTicketsIssuedToday(countQuery(conn, "SELECT COALESCE(SUM(tickets), 0) FROM ticket_transactions WHERE transaction_type = 'EARNED' AND CAST(created_at AS DATE) = CURRENT_DATE"));
            m.setPrizeRedemptionsToday(countQuery(conn, "SELECT COUNT(*) FROM prize_redemptions WHERE CAST(redeemed_at AS DATE) = CURRENT_DATE"));
        } catch (SQLException e) {
            logger.error("Error gathering dashboard metrics: {}", e.getMessage());
        }
        return m;
    }

    public Map<String, BigDecimal> getDailyRevenue(int days) {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        String sql = "SELECT CAST(created_at AS DATE) AS tx_date, SUM(amount) AS daily_rev " +
                     "FROM wallet_transactions " +
                     "WHERE transaction_type = 'RECHARGE' " +
                     "GROUP BY CAST(created_at AS DATE) " +
                     "ORDER BY tx_date DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("tx_date");
                    BigDecimal rev = rs.getBigDecimal("daily_rev");
                    data.put(d != null ? d.toString() : "Unknown", rev != null ? rev : BigDecimal.ZERO);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving daily revenue: {}", e.getMessage());
        }
        return data;
    }

    public Map<String, Integer> getMostPopularGames() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT g.name, COUNT(ps.session_id) AS play_count " +
                     "FROM games g " +
                     "LEFT JOIN play_sessions ps ON g.game_id = ps.game_id " +
                     "GROUP BY g.game_id, g.name " +
                     "ORDER BY play_count DESC, g.name ASC LIMIT 10";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("name"), rs.getInt("play_count"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving popular games: {}", e.getMessage());
        }
        return data;
    }

    public Map<String, Integer> getMachineStatusDistribution() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM machines GROUP BY status";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving machine status distribution: {}", e.getMessage());
        }
        return data;
    }

    public Map<String, Integer> getPrizeRedemptionDistribution() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT p.name, COALESCE(SUM(pr.quantity), 0) AS total_redeemed " +
                     "FROM prizes p " +
                     "LEFT JOIN prize_redemptions pr ON p.prize_id = pr.prize_id " +
                     "GROUP BY p.prize_id, p.name " +
                     "ORDER BY total_redeemed DESC LIMIT 10";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("name"), rs.getInt("total_redeemed"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving prize redemption stats: {}", e.getMessage());
        }
        return data;
    }

    private int countQuery(Connection conn, String sql) {
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warn("Count query error [{}]: {}", sql, e.getMessage());
        }
        return 0;
    }
}

