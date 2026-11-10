package com.funcity.service;

import com.funcity.config.DatabaseConfig;
import com.funcity.dao.CustomerDAO;
import com.funcity.dao.MembershipCardDAO;
import com.funcity.model.Customer;
import com.funcity.model.MembershipCard;
import com.funcity.util.IDGenerator;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerDAO customerDAO;
    private final MembershipCardDAO membershipCardDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        this.membershipCardDAO = new MembershipCardDAO();
    }

    public CustomerService(CustomerDAO customerDAO, MembershipCardDAO membershipCardDAO) {
        this.customerDAO = customerDAO;
        this.membershipCardDAO = membershipCardDAO;
    }

    public Customer registerCustomer(String name, String email, String phone, boolean autoIssueCard) {
        ValidationUtil.requireNonEmpty(name, "Customer Name");
        ValidationUtil.requireValidEmail(email);
        ValidationUtil.requireValidPhone(phone);

        if (customerDAO.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("A customer with this email address already exists.");
        }
        if (customerDAO.findByPhone(phone.trim()).isPresent()) {
            throw new IllegalArgumentException("A customer with this phone number already exists.");
        }

        String customerCode = IDGenerator.generateCustomerCode();
        Customer customer = new Customer(0, customerCode, name.trim(), email.trim(), phone.trim(),
                BigDecimal.ZERO, 0, 0, "ACTIVE");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                customer = customerDAO.create(conn, customer);

                if (autoIssueCard) {
                    String cardNumber = IDGenerator.generateCardNumber();
                    MembershipCard card = new MembershipCard(0, cardNumber, customer.getCustomerId(), "ACTIVE");
                    membershipCardDAO.create(conn, card);
                    customer.setActiveCardNumber(cardNumber);
                }

                conn.commit();
                logger.info("Registered customer {} ({})", customer.getName(), customer.getCustomerCode());
                return customer;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Failed to register customer: {}", e.getMessage());
                throw new RuntimeException("Error registering customer: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during customer registration", e);
        }
    }

    public boolean updateCustomer(Customer customer) {
        ValidationUtil.requireNonEmpty(customer.getName(), "Customer Name");
        ValidationUtil.requireValidEmail(customer.getEmail());
        ValidationUtil.requireValidPhone(customer.getPhone());
        return customerDAO.update(customer);
    }

    public Optional<Customer> getCustomerById(int customerId) {
        return customerDAO.findById(customerId);
    }

    public Optional<Customer> getCustomerByCode(String code) {
        return customerDAO.findByCode(code.trim());
    }

    public Optional<Customer> getCustomerByCardNumber(String cardNumber) {
        return customerDAO.findByCardNumber(cardNumber.trim());
    }

    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return customerDAO.findAll();
        }
        return customerDAO.search(query.trim());
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }
}

