package com.funcity.service;

import com.funcity.dao.UserDAO;
import com.funcity.model.User;
import com.funcity.util.PasswordUtil;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User authenticate(String username, String plainPassword) {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(plainPassword, "Password");

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: user '{}' not found", username);
            throw new IllegalArgumentException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Authentication rejected: user '{}' is inactive", username);
            throw new IllegalStateException("Your account is currently inactive. Contact an administrator.");
        }

        boolean matches = PasswordUtil.checkPassword(plainPassword, user.getPasswordHash());
        if (!matches) {
            logger.warn("Authentication failed: invalid password for user '{}'", username);
            throw new IllegalArgumentException("Invalid username or password.");
        }

        logger.info("User '{}' ({}) authenticated successfully", user.getUsername(), user.getRole());
        return user;
    }

    public User registerUser(String username, String plainPassword, String fullName, String email, String role) {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(plainPassword, "Password");
        ValidationUtil.requireNonEmpty(fullName, "Full name");
        ValidationUtil.requireValidEmail(email);
        ValidationUtil.requireNonEmpty(role, "Role");

        if (userDAO.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        User user = new User(0, username.trim(), passwordHash, fullName.trim(), email.trim(), role.toUpperCase(), "ACTIVE");
        return userDAO.create(user);
    }

    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        ValidationUtil.requireNonEmpty(newPassword, "New password");
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match.");
        }
        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getTechnicians() {
        return userDAO.findByRole("TECHNICIAN");
    }

    public boolean updateUser(User user) {
        ValidationUtil.requireNonEmpty(user.getFullName(), "Full name");
        ValidationUtil.requireValidEmail(user.getEmail());
        return userDAO.update(user);
    }
}

