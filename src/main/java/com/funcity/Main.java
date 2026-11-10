package com.funcity;

import com.formdev.flatlaf.FlatLightLaf;
import com.funcity.config.DatabaseConfig;
import com.funcity.ui.auth.LoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Fun City 2.0 - Arcade & Amusement Centre Management System...");

        // 1. Install Modern FlatLaf Look and Feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception e) {
            logger.warn("Failed to initialize FlatLaf theme, falling back to system L&F: {}", e.getMessage());
        }

        // 2. Initialize Database Configuration & Schema Migrations
        try {
            DatabaseConfig.initialize();
        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                    "Database Connection Notice:\n" + e.getMessage() +
                    "\n\nPlease ensure MySQL is running or check db.properties.",
                    "Database Warning", JOptionPane.WARNING_MESSAGE);
        }

        // 3. Register Shutdown Hook for clean connection pool cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutting down; releasing database connections...");
            DatabaseConfig.shutdown();
        }));

        // 4. Launch Login Form on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}

