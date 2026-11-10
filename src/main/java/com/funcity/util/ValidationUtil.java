package com.funcity.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{7,15}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[\\s-()]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    public static boolean isNonEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNonNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isPositive(int number) {
        return number > 0;
    }

    public static boolean isNonNegative(int number) {
        return number >= 0;
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (!isNonEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
    }

    public static void requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    public static void requireValidPhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }
    }

    public static void requirePositive(BigDecimal amount, String fieldName) {
        if (!isPositive(amount)) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    public static void requirePositive(int number, String fieldName) {
        if (!isPositive(number)) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }
}

