package com.funcity.service;

import com.funcity.dao.CustomerDAO;
import com.funcity.dao.MembershipCardDAO;
import com.funcity.model.Customer;
import com.funcity.model.MembershipCard;
import com.funcity.util.IDGenerator;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class MembershipService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipService.class);
    private final MembershipCardDAO membershipCardDAO;
    private final CustomerDAO customerDAO;

    public MembershipService() {
        this.membershipCardDAO = new MembershipCardDAO();
        this.customerDAO = new CustomerDAO();
    }

    public MembershipService(MembershipCardDAO membershipCardDAO, CustomerDAO customerDAO) {
        this.membershipCardDAO = membershipCardDAO;
        this.customerDAO = customerDAO;
    }

    public MembershipCard issueNewCard(int customerId, String customCardNumber) {
        Optional<Customer> custOpt = customerDAO.findById(customerId);
        if (custOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer does not exist with ID: " + customerId);
        }

        // Deactivate previous active card if exists
        Optional<MembershipCard> existingActive = membershipCardDAO.findActiveByCustomerId(customerId);
        existingActive.ifPresent(card -> {
            logger.info("Deactivating previous card {} for customer {}", card.getCardNumber(), customerId);
            membershipCardDAO.updateStatus(card.getCardId(), "EXPIRED");
        });

        String cardNumber = (customCardNumber != null && !customCardNumber.trim().isEmpty())
                ? customCardNumber.trim()
                : IDGenerator.generateCardNumber();

        if (membershipCardDAO.findByCardNumber(cardNumber).isPresent()) {
            throw new IllegalArgumentException("Card number already in use: " + cardNumber);
        }

        MembershipCard newCard = new MembershipCard(0, cardNumber, customerId, "ACTIVE");
        newCard = membershipCardDAO.create(newCard);
        logger.info("Issued card {} to customer {}", cardNumber, customerId);
        return newCard;
    }

    public boolean blockCard(int cardId, String reason) {
        logger.warn("Blocking membership card ID {}. Reason: {}", cardId, reason);
        return membershipCardDAO.updateStatus(cardId, "BLOCKED");
    }

    public boolean activateCard(int cardId) {
        logger.info("Activating membership card ID {}", cardId);
        return membershipCardDAO.updateStatus(cardId, "ACTIVE");
    }

    public MembershipCard validateCardForUse(String cardNumber) {
        ValidationUtil.requireNonEmpty(cardNumber, "Card Number");
        Optional<MembershipCard> cardOpt = membershipCardDAO.findByCardNumber(cardNumber.trim());
        if (cardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card not found: " + cardNumber);
        }

        MembershipCard card = cardOpt.get();
        if (!card.isActive()) {
            throw new IllegalStateException("Card is " + card.getCardStatus() + ". Cannot authorize transaction.");
        }

        Optional<Customer> custOpt = customerDAO.findById(card.getCustomerId());
        if (custOpt.isEmpty() || !custOpt.get().isActive()) {
            throw new IllegalStateException("Associated customer account is blocked or inactive.");
        }

        membershipCardDAO.updateLastUsed(card.getCardId());
        return card;
    }

    public Optional<MembershipCard> getCardByNumber(String cardNumber) {
        return membershipCardDAO.findByCardNumber(cardNumber);
    }

    public Optional<MembershipCard> getActiveCardForCustomer(int customerId) {
        return membershipCardDAO.findActiveByCustomerId(customerId);
    }

    public List<MembershipCard> getAllCards() {
        return membershipCardDAO.findAll();
    }
}

