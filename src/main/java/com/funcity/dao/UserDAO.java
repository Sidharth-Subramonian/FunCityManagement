package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username {}: {}", username, e.getMessage());
            throw new RuntimeException("Database error finding user", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id {}: {}", userId, e.getMessage());
            throw new RuntimeException("Database error finding user", e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all users: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving users", e);
        }
        return users;
    }

    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? AND status = 'ACTIVE' ORDER BY full_name ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving users by role {}: {}", role, e.getMessage());
            throw new RuntimeException("Database error retrieving users by role", e);
        }
        return users;
    }

    public User create(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            logger.error("Error creating user {}: {}", user.getUsername(), e.getMessage());
            throw new RuntimeException("Database error creating user: " + e.getMessage(), e);
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Database error updating user", e);
        }
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Database error updating password", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}

