package com.funcity.dao;

import com.funcity.config.DatabaseConfig;
import com.funcity.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);

    public Optional<Customer> findById(int customerId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return findById(conn, customerId);
        } catch (SQLException e) {
            logger.error("Error finding customer by id {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Database error finding customer", e);
        }
    }

    public Optional<Customer> findById(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "WHERE c.customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByIdForUpdate(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "WHERE c.customer_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByCode(String code) {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "WHERE c.customer_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by code {}: {}", code, e.getMessage());
            throw new RuntimeException("Database error finding customer", e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "WHERE c.phone = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by phone {}: {}", phone, e.getMessage());
            throw new RuntimeException("Database error finding customer", e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "WHERE c.email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by email {}: {}", email, e.getMessage());
            throw new RuntimeException("Database error finding customer", e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByCardNumber(String cardNumber) {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "JOIN membership_cards mc ON c.customer_id = mc.customer_id " +
                     "WHERE mc.card_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by card number {}: {}", cardNumber, e.getMessage());
            throw new RuntimeException("Database error finding customer", e);
        }
        return Optional.empty();
    }

    public List<Customer> search(String query) {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "WHERE c.name LIKE ? OR c.customer_code LIKE ? OR c.phone LIKE ? OR c.email LIKE ? OR mc.card_number LIKE ? " +
                     "ORDER BY c.customer_id DESC";
        List<Customer> list = new ArrayList<>();
        String wildcard = "%" + query.trim() + "%";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);
            ps.setString(5, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching customers with query {}: {}", query, e.getMessage());
            throw new RuntimeException("Database error searching customers", e);
        }
        return list;
    }

    public List<Customer> findAll() {
        String sql = "SELECT c.*, mc.card_number FROM customers c " +
                     "LEFT JOIN membership_cards mc ON c.customer_id = mc.customer_id AND mc.card_status = 'ACTIVE' " +
                     "ORDER BY c.customer_id DESC";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all customers: {}", e.getMessage());
            throw new RuntimeException("Database error retrieving customers", e);
        }
        return list;
    }

    public Customer create(Customer customer) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return create(conn, customer);
        } catch (SQLException e) {
            logger.error("Error creating customer {}: {}", customer.getName(), e.getMessage());
            throw new RuntimeException("Database error creating customer: " + e.getMessage(), e);
        }
    }

    public Customer create(Connection conn, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_code, name, email, phone, wallet_balance, ticket_balance, loyalty_points, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getCustomerCode());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setBigDecimal(5, customer.getWalletBalance() != null ? customer.getWalletBalance() : BigDecimal.ZERO);
            ps.setInt(6, customer.getTicketBalance());
            ps.setInt(7, customer.getLoyaltyPoints());
            ps.setString(8, customer.getStatus() != null ? customer.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setCustomerId(rs.getInt(1));
                }
            }
            return customer;
        }
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, status = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getStatus());
            ps.setInt(5, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating customer {}: {}", customer.getCustomerId(), e.getMessage());
            throw new RuntimeException("Database error updating customer", e);
        }
    }

    public boolean updateBalances(Connection conn, int customerId, BigDecimal newWalletBalance, int newTicketBalance, int newLoyaltyPoints) throws SQLException {
        String sql = "UPDATE customers SET wallet_balance = ?, ticket_balance = ?, loyalty_points = ? WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newWalletBalance);
            ps.setInt(2, newTicketBalance);
            ps.setInt(3, newLoyaltyPoints);
            ps.setInt(4, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setCustomerCode(rs.getString("customer_code"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setWalletBalance(rs.getBigDecimal("wallet_balance"));
        c.setTicketBalance(rs.getInt("ticket_balance"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        try {
            c.setActiveCardNumber(rs.getString("card_number"));
        } catch (SQLException ignored) {}
        return c;
    }
}

