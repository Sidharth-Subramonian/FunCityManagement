package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.MaintenanceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceDAO {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceDAO.class);

    public MaintenanceRecord create(MaintenanceRecord record) {
        String sql = "INSERT INTO maintenance_records (machine_id, reported_by, assigned_technician_id, " +
                     "issue_description, technician_notes, status, priority) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getMachineId());
            ps.setInt(2, record.getReportedBy());
            if (record.getAssignedTechnicianId() != null) {
                ps.setInt(3, record.getAssignedTechnicianId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, record.getIssueDescription());
            ps.setString(5, record.getTechnicianNotes());
            ps.setString(6, record.getStatus() != null ? record.getStatus() : "REPORTED");
            ps.setString(7, record.getPriority() != null ? record.getPriority() : "MEDIUM");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setRecordId(rs.getInt(1));
                }
            }
            return record;
        } catch (SQLException e) {
            logger.error("Error creating maintenance record: {}", e.getMessage());
            throw new RuntimeException("Database error creating maintenance record: " + e.getMessage(), e);
        }
    }

    public Optional<MaintenanceRecord> findById(int recordId) {
        String sql = getMaintenanceSelectQuery() + " WHERE mr.record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMaintenanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding maintenance record by id {}: {}", recordId, e.getMessage());
            throw new RuntimeException("Database error finding maintenance record", e);
        }
        return Optional.empty();
    }

    public List<MaintenanceRecord> findAll() {
        String sql = getMaintenanceSelectQuery() + " ORDER BY mr.record_id DESC";
        List<MaintenanceRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMaintenanceRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all maintenance records: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving maintenance records", e);
        }
        return list;
    }

    public List<MaintenanceRecord> findActive() {
        String sql = getMaintenanceSelectQuery() + " WHERE mr.status IN ('REPORTED', 'IN_PROGRESS') ORDER BY mr.record_id DESC";
        List<MaintenanceRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMaintenanceRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving active maintenance records: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving active maintenance records", e);
        }
        return list;
    }

    public List<MaintenanceRecord> findByTechnicianId(int technicianId) {
        String sql = getMaintenanceSelectQuery() + " WHERE mr.assigned_technician_id = ? ORDER BY mr.record_id DESC";
        List<MaintenanceRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMaintenanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving records for technician {}: {}", technicianId, e.getMessage());
            throw new RuntimeException("Database error retrieving records for technician", e);
        }
        return list;
    }

    public boolean updateStatusAndNotes(int recordId, String status, String notes, Integer technicianId) {
        String sql = "UPDATE maintenance_records SET status = ?, technician_notes = ?, " +
                     "assigned_technician_id = COALESCE(?, assigned_technician_id), " +
                     "resolved_at = CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE resolved_at END " +
                     "WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            if (technicianId != null) {
                ps.setInt(3, technicianId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, status);
            ps.setInt(5, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating maintenance record {}: {}", recordId, e.getMessage());
            throw new RuntimeException("Database error updating maintenance record", e);
        }
    }

    private String getMaintenanceSelectQuery() {
        return "SELECT mr.*, m.machine_code, m.floor_location, g.name AS game_name, " +
               "u1.username AS reported_by_username, u2.full_name AS technician_name " +
               "FROM maintenance_records mr " +
               "JOIN machines m ON mr.machine_id = m.machine_id " +
               "JOIN games g ON m.game_id = g.game_id " +
               "JOIN users u1 ON mr.reported_by = u1.user_id " +
               "LEFT JOIN users u2 ON mr.assigned_technician_id = u2.user_id";
    }

    private MaintenanceRecord mapResultSetToMaintenanceRecord(ResultSet rs) throws SQLException {
        MaintenanceRecord mr = new MaintenanceRecord();
        mr.setRecordId(rs.getInt("record_id"));
        mr.setMachineId(rs.getInt("machine_id"));
        mr.setReportedBy(rs.getInt("reported_by"));
        int techId = rs.getInt("assigned_technician_id");
        if (!rs.wasNull()) {
            mr.setAssignedTechnicianId(techId);
        }
        mr.setIssueDescription(rs.getString("issue_description"));
        mr.setTechnicianNotes(rs.getString("technician_notes"));
        mr.setStatus(rs.getString("status"));
        mr.setPriority(rs.getString("priority"));
        mr.setReportedAt(rs.getTimestamp("reported_at"));
        mr.setResolvedAt(rs.getTimestamp("resolved_at"));
        try {
            mr.setMachineCode(rs.getString("machine_code"));
            mr.setFloorLocation(rs.getString("floor_location"));
            mr.setGameName(rs.getString("game_name"));
            mr.setReportedByUsername(rs.getString("reported_by_username"));
            mr.setTechnicianName(rs.getString("technician_name"));
        } catch (SQLException ignored) {}
        return mr;
    }
}

