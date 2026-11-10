package com.funcity.service;

import com.funcity.config.DatabaseConfig;
import com.funcity.dao.CustomerDAO;
import com.funcity.dao.PrizeDAO;
import com.funcity.dao.TicketDAO;
import com.funcity.model.Customer;
import com.funcity.model.Prize;
import com.funcity.model.PrizeRedemption;
import com.funcity.model.TicketTransaction;
import com.funcity.util.IDGenerator;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RedemptionService {

    private static final Logger logger = LoggerFactory.getLogger(RedemptionService.class);
    private final PrizeDAO prizeDAO;
    private final CustomerDAO customerDAO;
    private final TicketDAO ticketDAO;

    public RedemptionService() {
        this.prizeDAO = new PrizeDAO();
        this.customerDAO = new CustomerDAO();
        this.ticketDAO = new TicketDAO();
    }

    public RedemptionService(PrizeDAO prizeDAO, CustomerDAO customerDAO, TicketDAO ticketDAO) {
        this.prizeDAO = prizeDAO;
        this.customerDAO = customerDAO;
        this.ticketDAO = ticketDAO;
    }

    public PrizeRedemption redeemPrize(int customerId, int prizeId, int quantity, Integer staffId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Redemption quantity must be at least 1.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lock and validate prize
                Optional<Prize> prizeOpt = prizeDAO.findByIdForUpdate(conn, prizeId);
                if (prizeOpt.isEmpty()) {
                    throw new IllegalArgumentException("Prize not found with ID: " + prizeId);
                }
                Prize prize = prizeOpt.get();
                if (!prize.isActive()) {
                    throw new IllegalStateException("Prize '" + prize.getName() + "' is inactive and unavailable for redemption.");
                }
                if (prize.getInventoryStock() < quantity) {
                    throw new IllegalStateException("Insufficient prize inventory. Available: " + prize.getInventoryStock() + ", Requested: " + quantity);
                }

                int totalTicketCost = prize.getTicketCost() * quantity;

                // 2. Lock and validate customer
                Optional<Customer> custOpt = customerDAO.findByIdForUpdate(conn, customerId);
                if (custOpt.isEmpty()) {
                    throw new IllegalArgumentException("Customer not found with ID: " + customerId);
                }
                Customer customer = custOpt.get();
                if (!customer.isActive()) {
                    throw new IllegalStateException("Customer account is blocked.");
                }
                if (customer.getTicketBalance() < totalTicketCost) {
                    throw new IllegalStateException("Insufficient tickets. Required: " + totalTicketCost + ", Customer has: " + customer.getTicketBalance());
                }

                // 3. Deduct tickets
                int openingTickets = customer.getTicketBalance();
                int closingTickets = openingTickets - totalTicketCost;
                customerDAO.updateBalances(conn, customerId, customer.getWalletBalance(), closingTickets, customer.getLoyaltyPoints());

                // 4. Decrement prize stock
                int newStock = prize.getInventoryStock() - quantity;
                prizeDAO.updateStock(conn, prizeId, newStock);

                // 5. Record ticket ledger entry
                TicketTransaction tt = new TicketTransaction(
                        0,
                        IDGenerator.generateTicketTxReference(),
                        customerId,
                        null,
                        "REDEEMED",
                        -totalTicketCost,
                        openingTickets,
                        closingTickets,
                        "Redeemed " + quantity + "x " + prize.getName(),
                        staffId
                );
                ticketDAO.recordTransaction(conn, tt);

                // 6. Record prize redemption record
                PrizeRedemption redemption = new PrizeRedemption(
                        0,
                        IDGenerator.generateRedemptionReference(),
                        customerId,
                        prizeId,
                        quantity,
                        totalTicketCost,
                        staffId
                );
                redemption = prizeDAO.recordRedemption(conn, redemption);

                conn.commit();
                logger.info("Successfully processed redemption {} for customer {}: {}x {} for {} tickets",
                        redemption.getRedemptionReference(), customerId, quantity, prize.getName(), totalTicketCost);
                return redemption;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Failed to redeem prize: {}", e.getMessage());
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Redemption failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during redemption", e);
        }
    }

    public Prize addPrize(String name, String category, String description, int ticketCost, int stock) {
        ValidationUtil.requireNonEmpty(name, "Prize Name");
        ValidationUtil.requireNonEmpty(category, "Category");
        ValidationUtil.requirePositive(ticketCost, "Ticket Cost");
        if (stock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }

        String code = IDGenerator.generatePrizeCode(prizeDAO.findAll().size() + 1);
        Prize prize = new Prize(0, code, name.trim(), category.trim(), description, ticketCost, stock, "ACTIVE");
        return prizeDAO.create(prize);
    }

    public boolean updatePrize(Prize prize) {
        ValidationUtil.requireNonEmpty(prize.getName(), "Prize Name");
        ValidationUtil.requirePositive(prize.getTicketCost(), "Ticket Cost");
        return prizeDAO.update(prize);
    }

    public List<Prize> getAvailablePrizes() {
        return prizeDAO.findAvailable();
    }

    public List<Prize> getAllPrizes() {
        return prizeDAO.findAll();
    }

    public List<PrizeRedemption> getRecentRedemptions(int limit) {
        return prizeDAO.findAllRedemptions(limit);
    }
}

