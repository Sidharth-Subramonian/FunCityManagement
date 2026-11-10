package com.funcity.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testHashAndVerify() {
        String raw = "Admin@123";
        String hash = PasswordUtil.hashPassword(raw);
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
        assertTrue(PasswordUtil.checkPassword(raw, hash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hash));

        System.out.println("HASH_ADMIN: " + PasswordUtil.hashPassword("Admin@123"));
        System.out.println("HASH_STAFF: " + PasswordUtil.hashPassword("Staff@123"));
        System.out.println("HASH_TECH: " + PasswordUtil.hashPassword("Tech@123"));
    }
}
