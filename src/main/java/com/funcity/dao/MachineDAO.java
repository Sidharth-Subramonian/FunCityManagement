package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.Machine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MachineDAO {

    private static final Logger logger = LoggerFactory.getLogger(MachineDAO.class);

    public Optional<Machine> findById(int machineId) {
        String sql = "SELECT m.*, g.name AS game_name, g.category AS game_category, g.price_per_play AS game_price " +
                     "FROM machines m JOIN games g ON m.game_id = g.game_id WHERE m.machine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, machineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMachine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding machine by id {}: {}", machineId, e.getMessage());
            throw new RuntimeException("Database error finding machine", e);
        }
        return Optional.empty();
    }

    public Optional<Machine> findByCode(String code) {
        String sql = "SELECT m.*, g.name AS game_name, g.category AS game_category, g.price_per_play AS game_price " +
                     "FROM machines m JOIN games g ON m.game_id = g.game_id WHERE m.machine_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMachine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding machine by code {}: {}", code, e.getMessage());
            throw new RuntimeException("Database error finding machine", e);
        }
        return Optional.empty();
    }

    public List<Machine> findAll() {
        String sql = "SELECT m.*, g.name AS game_name, g.category AS game_category, g.price_per_play AS game_price " +
                     "FROM machines m JOIN games g ON m.game_id = g.game_id ORDER BY m.machine_id ASC";
        List<Machine> machines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                machines.add(mapResultSetToMachine(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all machines: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving machines", e);
        }
        return machines;
    }

    public List<Machine> findOperational() {
        String sql = "SELECT m.*, g.name AS game_name, g.category AS game_category, g.price_per_play AS game_price " +
                     "FROM machines m JOIN games g ON m.game_id = g.game_id " +
                     "WHERE m.status = 'OPERATIONAL' AND g.status = 'ACTIVE' ORDER BY g.name ASC, m.machine_code ASC";
        List<Machine> machines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                machines.add(mapResultSetToMachine(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving operational machines: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving operational machines", e);
        }
        return machines;
    }

    public List<Machine> findByGameId(int gameId) {
        String sql = "SELECT m.*, g.name AS game_name, g.category AS game_category, g.price_per_play AS game_price " +
                     "FROM machines m JOIN games g ON m.game_id = g.game_id WHERE m.game_id = ? ORDER BY m.machine_id ASC";
        List<Machine> machines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    machines.add(mapResultSetToMachine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving machines for game {}: {}", gameId, e.getMessage());
            throw new RuntimeException("Database error retrieving machines for game", e);
        }
        return machines;
    }

    public Machine create(Machine machine) {
        String sql = "INSERT INTO machines (machine_code, game_id, serial_number, floor_location, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, machine.getMachineCode());
            ps.setInt(2, machine.getGameId());
            ps.setString(3, machine.getSerialNumber());
            ps.setString(4, machine.getFloorLocation());
            ps.setString(5, machine.getStatus() != null ? machine.getStatus() : "OPERATIONAL");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    machine.setMachineId(rs.getInt(1));
                }
            }
            return machine;
        } catch (SQLException e) {
            logger.error("Error creating machine {}: {}", machine.getMachineCode(), e.getMessage());
            throw new RuntimeException("Database error creating machine: " + e.getMessage(), e);
        }
    }

    public boolean update(Machine machine) {
        String sql = "UPDATE machines SET game_id = ?, serial_number = ?, floor_location = ?, status = ? WHERE machine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, machine.getGameId());
            ps.setString(2, machine.getSerialNumber());
            ps.setString(3, machine.getFloorLocation());
            ps.setString(4, machine.getStatus());
            ps.setInt(5, machine.getMachineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating machine {}: {}", machine.getMachineId(), e.getMessage());
            throw new RuntimeException("Database error updating machine", e);
        }
    }

    public boolean updateStatus(int machineId, String status) {
        String sql = "UPDATE machines SET status = ? WHERE machine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, machineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for machine {}: {}", machineId, e.getMessage());
            throw new RuntimeException("Database error updating machine status", e);
        }
    }

    public boolean delete(int machineId) {
        String sql = "DELETE FROM machines WHERE machine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, machineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting machine {}: {}", machineId, e.getMessage());
            throw new RuntimeException("Cannot delete machine (it may be linked to play sessions or maintenance records): " + e.getMessage(), e);
        }
    }

    private Machine mapResultSetToMachine(ResultSet rs) throws SQLException {
        Machine m = new Machine();
        m.setMachineId(rs.getInt("machine_id"));
        m.setMachineCode(rs.getString("machine_code"));
        m.setGameId(rs.getInt("game_id"));
        m.setSerialNumber(rs.getString("serial_number"));
        m.setFloorLocation(rs.getString("floor_location"));
        m.setStatus(rs.getString("status"));
        m.setInstalledAt(rs.getTimestamp("installed_at"));
        m.setLastMaintenanceDate(rs.getTimestamp("last_maintenance_date"));
        try {
            m.setGameName(rs.getString("game_name"));
            m.setGameCategory(rs.getString("game_category"));
            m.setGamePrice(rs.getBigDecimal("game_price"));
        } catch (SQLException ignored) {}
        return m;
    }
}

