package com.funcity.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class IDGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateCustomerCode() {
        return "CUST-" + String.format("%04d", RANDOM.nextInt(9000) + 1000);
    }

    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder("FC-");
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateWalletTxReference() {
        return "WTX-" + LocalDateTime.now().format(DATE_FORMAT) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generatePlaySessionReference() {
        return "SES-" + LocalDateTime.now().format(DATE_FORMAT) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateTicketTxReference() {
        return "TTX-" + LocalDateTime.now().format(DATE_FORMAT) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateRedemptionReference() {
        return "RED-" + LocalDateTime.now().format(DATE_FORMAT) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateRewardReference() {
        return "REW-" + LocalDateTime.now().format(DATE_FORMAT) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateMachineCode(int nextNum) {
        return String.format("MCH-%02d", nextNum);
    }

    public static String generatePrizeCode(int nextNum) {
        return String.format("PRZ-%03d", nextNum);
    }
}
