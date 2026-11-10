package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameDAO {

    private static final Logger logger = LoggerFactory.getLogger(GameDAO.class);

    public Optional<Game> findById(int gameId) {
        String sql = "SELECT g.*, COUNT(m.machine_id) AS machine_count " +
                     "FROM games g " +
                     "LEFT JOIN machines m ON g.game_id = m.game_id " +
                     "WHERE g.game_id = ? GROUP BY g.game_id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding game by id {}: {}", gameId, e.getMessage());
            throw new RuntimeException("Database error finding game", e);
        }
        return Optional.empty();
    }

    public List<Game> findAll() {
        String sql = "SELECT g.*, COUNT(m.machine_id) AS machine_count " +
                     "FROM games g " +
                     "LEFT JOIN machines m ON g.game_id = m.game_id " +
                     "GROUP BY g.game_id ORDER BY g.game_id ASC";
        List<Game> games = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all games: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving games", e);
        }
        return games;
    }

    public List<Game> findActive() {
        String sql = "SELECT g.*, COUNT(m.machine_id) AS machine_count " +
                     "FROM games g " +
                     "LEFT JOIN machines m ON g.game_id = m.game_id " +
                     "WHERE g.status = 'ACTIVE' GROUP BY g.game_id ORDER BY g.name ASC";
        List<Game> games = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving active games: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving active games", e);
        }
        return games;
    }

    public Game create(Game game) {
        String sql = "INSERT INTO games (name, category, description, price_per_play, dispenses_tickets, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getCategory());
            ps.setString(3, game.getDescription());
            ps.setBigDecimal(4, game.getPricePerPlay());
            ps.setBoolean(5, game.isDispensesTickets());
            ps.setString(6, game.getStatus() != null ? game.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    game.setGameId(rs.getInt(1));
                }
            }
            return game;
        } catch (SQLException e) {
            logger.error("Error creating game {}: {}", game.getName(), e.getMessage());
            throw new RuntimeException("Database error creating game: " + e.getMessage(), e);
        }
    }

    public boolean update(Game game) {
        String sql = "UPDATE games SET name = ?, category = ?, description = ?, price_per_play = ?, dispenses_tickets = ?, status = ? WHERE game_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getCategory());
            ps.setString(3, game.getDescription());
            ps.setBigDecimal(4, game.getPricePerPlay());
            ps.setBoolean(5, game.isDispensesTickets());
            ps.setString(6, game.getStatus());
            ps.setInt(7, game.getGameId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating game {}: {}", game.getGameId(), e.getMessage());
            throw new RuntimeException("Database error updating game", e);
        }
    }

    public boolean delete(int gameId) {
        String sql = "DELETE FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting game {}: {}", gameId, e.getMessage());
            throw new RuntimeException("Cannot delete game (it may be linked to machines or play sessions): " + e.getMessage(), e);
        }
    }

    private Game mapResultSetToGame(ResultSet rs) throws SQLException {
        Game g = new Game();
        g.setGameId(rs.getInt("game_id"));
        g.setName(rs.getString("name"));
        g.setCategory(rs.getString("category"));
        g.setDescription(rs.getString("description"));
        g.setPricePerPlay(rs.getBigDecimal("price_per_play"));
        g.setDispensesTickets(rs.getBoolean("dispenses_tickets"));
        g.setStatus(rs.getString("status"));
        g.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            g.setMachineCount(rs.getInt("machine_count"));
        } catch (SQLException ignored) {}
        return g;
    }
}

