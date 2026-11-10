package com.funcity.service;

import com.funcity.config.DatabaseConfig;
import com.funcity.dao.*;
import com.funcity.model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusinessWorkflowTest {

    private static AuthenticationService authService;
    private static CustomerService customerService;
    private static MembershipService membershipService;
    private static WalletService walletService;
    private static GameService gameService;
    private static MachineService machineService;
    private static PlayService playService;
    private static TicketService ticketService;
    private static RedemptionService redemptionService;
    private static RewardService rewardService;
    private static MaintenanceService maintenanceService;

    @BeforeAll
    static void setUp() {
        DatabaseConfig.initializeForTesting("jdbc:h2:mem:test_funcity;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");

        authService = new AuthenticationService();
        customerService = new CustomerService();
        membershipService = new MembershipService();
        walletService = new WalletService();
        gameService = new GameService();
        machineService = new MachineService();
        playService = new PlayService();
        ticketService = new TicketService();
        redemptionService = new RedemptionService();
        rewardService = new RewardService();
        maintenanceService = new MaintenanceService();
    }

    @Test
    @Order(1)
    void testAuthenticationSuccessAndRoleAccess() {
        User admin = authService.authenticate("admin", "Admin@123");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertFalse(admin.isStaff());

        User staff = authService.authenticate("staff1", "Staff@123");
        assertNotNull(staff);
        assertTrue(staff.isStaff());

        User tech = authService.authenticate("tech1", "Tech@123");
        assertNotNull(tech);
        assertTrue(tech.isTechnician());
    }

    @Test
    @Order(2)
    void testAuthenticationFailure() {
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate("admin", "WrongPassword"));
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate("nonexistent_user", "AnyPass@123"));
    }

    @Test
    @Order(3)
    void testCustomerRegistrationWithAutoCard() {
        Customer c = customerService.registerCustomer("Test Gamer", "test.gamer@example.com", "+15559998877", true);
        assertNotNull(c);
        assertTrue(c.getCustomerId() > 0);
        assertNotNull(c.getCustomerCode());
        assertNotNull(c.getActiveCardNumber());

        // Verify duplicate prevention
        assertThrows(IllegalArgumentException.class, () ->
            customerService.registerCustomer("Duplicate Person", "test.gamer@example.com", "+15550001122", false)
        );
    }

    @Test
    @Order(4)
    void testMembershipCardValidationAndStatus() {
        Customer c = customerService.registerCustomer("Card Tester", "card.tester@example.com", "+15558887766", true);
        String cardNum = c.getActiveCardNumber();

        MembershipCard card = membershipService.validateCardForUse(cardNum);
        assertNotNull(card);
        assertTrue(card.isActive());

        // Block card
        membershipService.blockCard(card.getCardId(), "Simulated stolen card");
        assertThrows(IllegalStateException.class, () -> membershipService.validateCardForUse(cardNum));

        // Re-activate
        membershipService.activateCard(card.getCardId());
        MembershipCard reactivated = membershipService.validateCardForUse(cardNum);
        assertTrue(reactivated.isActive());
    }

    @Test
    @Order(5)
    void testWalletRechargeAndLoyaltyBonus() {
        Customer c = customerService.registerCustomer("Wallet Tester", "wallet.tester@example.com", "+15557776655", true);
        assertEquals(BigDecimal.ZERO.setScale(2), c.getWalletBalance().setScale(2));

        WalletTransaction tx = walletService.rechargeWallet(c.getCustomerId(), null, new BigDecimal("50.00"), "Initial recharge", 1);
        assertNotNull(tx);
        assertEquals(new BigDecimal("50.00"), tx.getClosingBalance());

        Customer reloaded = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(new BigDecimal("50.00"), reloaded.getWalletBalance());
        assertEquals(50, reloaded.getLoyaltyPoints()); // 1 pt per $1 recharge
    }

    @Test
    @Order(6)
    void testPlaySessionWorkflowSuccess() {
        // Customer setup with $10 balance
        Customer c = customerService.registerCustomer("Player One", "player1@example.com", "+15556665544", true);
        walletService.rechargeWallet(c.getCustomerId(), null, new BigDecimal("10.00"), "Setup", 1);

        // Find operational machine (e.g. MCH-01, MotoGP simulator, $2.50)
        Machine mch = machineService.getMachineByCode("MCH-01").orElseThrow();
        assertTrue(mch.isOperational());

        // 1. Initiate Play
        PlaySession session = playService.initiatePlaySession(c.getActiveCardNumber(), mch.getMachineId(), 1);
        assertNotNull(session);
        assertEquals("INITIATED", session.getStatus());
        assertEquals(new BigDecimal("2.50"), session.getGameFee());

        // Verify balance debited
        Customer afterDebit = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(new BigDecimal("7.50"), afterDebit.getWalletBalance());

        // 2. Complete Play & Award 50 Tickets
        boolean completed = playService.completePlaySession(session.getSessionId(), 50, 1);
        assertTrue(completed);

        // Verify tickets credited
        Customer afterPlay = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(50, afterPlay.getTicketBalance());
    }

    @Test
    @Order(7)
    void testPlaySessionRejectedForInsufficientBalance() {
        Customer brokePlayer = customerService.registerCustomer("Broke Player", "broke@example.com", "+15555554433", true);
        // Balance is $0.00
        Machine mch = machineService.getMachineByCode("MCH-01").orElseThrow();

        assertThrows(IllegalStateException.class, () ->
            playService.initiatePlaySession(brokePlayer.getActiveCardNumber(), mch.getMachineId(), 1)
        );
    }

    @Test
    @Order(8)
    void testPlaySessionRejectedForMachineUnderMaintenance() {
        Customer c = customerService.registerCustomer("Maintenance Player", "maint.player@example.com", "+15554443322", true);
        walletService.rechargeWallet(c.getCustomerId(), null, new BigDecimal("20.00"), "Load", 1);

        // Machine MCH-05 is UNDER_MAINTENANCE in sample data
        Machine mch = machineService.getMachineByCode("MCH-05").orElseThrow();
        assertFalse(mch.isOperational());

        assertThrows(IllegalStateException.class, () ->
            playService.initiatePlaySession(c.getActiveCardNumber(), mch.getMachineId(), 1)
        );
    }

    @Test
    @Order(9)
    void testPlaySessionCancellationAndRefund() {
        Customer c = customerService.registerCustomer("Refund Tester", "refund@example.com", "+15553332211", true);
        walletService.rechargeWallet(c.getCustomerId(), null, new BigDecimal("10.00"), "Load", 1);

        Machine mch = machineService.getMachineByCode("MCH-01").orElseThrow();
        PlaySession session = playService.initiatePlaySession(c.getActiveCardNumber(), mch.getMachineId(), 1);

        // Balance became $7.50
        Customer mid = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(new BigDecimal("7.50"), mid.getWalletBalance());

        // Cancel
        boolean cancelled = playService.cancelPlaySession(session.getSessionId(), "Customer changed mind", 1);
        assertTrue(cancelled);

        // Balance should be refunded to $10.00
        Customer afterRefund = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(new BigDecimal("10.00"), afterRefund.getWalletBalance());
    }

    @Test
    @Order(10)
    void testPrizeRedemptionLifecycle() {
        // Customer with 500 tickets
        Customer c = customerService.registerCustomer("Redemption Player", "redeem@example.com", "+15552221100", true);
        ticketService.adjustTickets(c.getCustomerId(), 500, "Bonus tickets for test", 1);

        // Find prize PRZ-003: LED Strobe Yo-Yo Pro, 120 tickets, stock 45
        List<Prize> prizes = redemptionService.getAvailablePrizes();
        Prize yoyo = prizes.stream().filter(p -> p.getPrizeCode().equals("PRZ-003")).findFirst().orElseThrow();
        int initialStock = yoyo.getInventoryStock();

        // 1. Redeem 2 yo-yos (240 tickets)
        PrizeRedemption red = redemptionService.redeemPrize(c.getCustomerId(), yoyo.getPrizeId(), 2, 1);
        assertNotNull(red);
        assertEquals(240, red.getTicketsSpent());

        // Verify remaining tickets: 500 - 240 = 260
        Customer afterRedeem = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(260, afterRedeem.getTicketBalance());

        // Verify stock reduced
        Prize updatedPrize = redemptionService.getAvailablePrizes().stream()
                .filter(p -> p.getPrizeId() == yoyo.getPrizeId()).findFirst().orElseThrow();
        assertEquals(initialStock - 2, updatedPrize.getInventoryStock());

        // 2. Attempt redemption exceeding remaining tickets (260 < 1500 for giant plush)
        Prize plush = prizes.stream().filter(p -> p.getPrizeCode().equals("PRZ-001")).findFirst().orElseThrow();
        assertThrows(IllegalStateException.class, () ->
            redemptionService.redeemPrize(c.getCustomerId(), plush.getPrizeId(), 1, 1)
        );
    }

    @Test
    @Order(11)
    void testMaintenanceLifecycle() {
        // Machine MCH-04 is operational
        Machine mch = machineService.getMachineByCode("MCH-04").orElseThrow();
        assertTrue(mch.isOperational());

        // Report fault
        MaintenanceRecord record = maintenanceService.reportFault(mch.getMachineId(), 1, "Coin mechanism stuck", "HIGH");
        assertNotNull(record);
        assertEquals("REPORTED", record.getStatus());

        // Machine should now be UNDER_MAINTENANCE
        Machine maintMch = machineService.getMachineById(mch.getMachineId()).orElseThrow();
        assertTrue(maintMch.isUnderMaintenance());

        // Complete maintenance
        maintenanceService.updateMaintenanceProgress(record.getRecordId(), "COMPLETED", "Cleared obstruction and oiled mechanism", 3);

        // Machine should now be restored to OPERATIONAL
        Machine restoredMch = machineService.getMachineById(mch.getMachineId()).orElseThrow();
        assertTrue(restoredMch.isOperational());
    }

    @Test
    @Order(12)
    void testConcurrentWalletDebits() throws Exception {
        Customer c = customerService.registerCustomer("Concurrent Tester", "concurrent@example.com", "+15551110099", true);
        // Load exactly $10.00
        walletService.rechargeWallet(c.getCustomerId(), null, new BigDecimal("10.00"), "Load", 1);

        // Try 10 concurrent debits of $2.50 each
        // Exactly 4 should succeed, and 6 must fail due to insufficient balance ($10 / $2.50 = 4)
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        ConcurrentLinkedQueue<Boolean> successResults = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    try (java.sql.Connection conn = DatabaseConfig.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            walletService.debitWallet(conn, c.getCustomerId(), null, new BigDecimal("2.50"), "Concurrent play", 1);
                            conn.commit();
                            successResults.add(true);
                        } catch (Exception ex) {
                            conn.rollback();
                            failures.add(ex);
                        }
                    }
                } catch (Exception e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(4, successResults.size(), "Exactly 4 transactions should succeed for $10 balance with $2.50 debit");
        assertEquals(6, failures.size(), "Remaining 6 transactions should fail with insufficient balance");

        Customer finalCust = customerService.getCustomerById(c.getCustomerId()).orElseThrow();
        assertEquals(BigDecimal.ZERO.setScale(2), finalCust.getWalletBalance().setScale(2));
    }
}

