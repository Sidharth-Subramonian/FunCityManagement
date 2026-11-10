package com.funcity.service;

import com.funcity.config.DatabaseConfig;
import com.funcity.dao.CustomerDAO;
import com.funcity.dao.TicketDAO;
import com.funcity.model.Customer;
import com.funcity.model.TicketTransaction;
import com.funcity.util.IDGenerator;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    private final TicketDAO ticketDAO;
    private final CustomerDAO customerDAO;

    public TicketService() {
        this.ticketDAO = new TicketDAO();
        this.customerDAO = new CustomerDAO();
    }

    public TicketService(TicketDAO ticketDAO, CustomerDAO customerDAO) {
        this.ticketDAO = ticketDAO;
        this.customerDAO = customerDAO;
    }

    public TicketTransaction adjustTickets(int customerId, int ticketDelta, String reason, Integer staffId) {
        if (ticketDelta == 0) {
            throw new IllegalArgumentException("Ticket adjustment delta cannot be zero.");
        }
        ValidationUtil.requireNonEmpty(reason, "Adjustment reason");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
                if (custOpt.isEmpty()) {
                    throw new IllegalArgumentException("Customer not found.");
                }
                Customer customer = custOpt.get();

                int opening = customer.getTicketBalance();
                int closing = opening + ticketDelta;
                if (closing < 0) {
                    throw new IllegalStateException("Adjustment would result in negative ticket balance. Current: " + opening + ", Adjustment: " + ticketDelta);
                }

                customerDAO.updateBalances(conn, customerId, customer.getWalletBalance(), closing, customer.getLoyaltyPoints());

                TicketTransaction tt = new TicketTransaction(
                        0,
                        IDGenerator.generateTicketTxReference(),
                        customerId,
                        null,
                        "ADJUSTED",
                        ticketDelta,
                        opening,
                        closing,
                        "Manual Adjustment: " + reason,
                        staffId
                );
                ticketDAO.recordTransaction(conn, tt);

                conn.commit();
                logger.info("Adjusted tickets for customer {}: {} (New: {})", customerId, ticketDelta, closing);
                return tt;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to adjust tickets for customer {}: {}", customerId, e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Ticket adjustment failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during ticket adjustment", e);
        }
    }

    public List<TicketTransaction> getCustomerTicketHistory(int customerId) {
        return ticketDAO.findByCustomerId(customerId);
    }

    public List<TicketTransaction> getRecentTicketTransactions(int limit) {
        return ticketDAO.findRecent(limit);
    }
}

