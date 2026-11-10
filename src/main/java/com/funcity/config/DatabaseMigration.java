package com.funcity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    public static void runMigrations(Connection conn) {
        if (conn == null) {
            logger.warn("Cannot run migrations: connection is null.");
            return;
        }

        try {
            logger.info("Executing database schema migration...");
            executeSqlScript(conn, "schema.sql");

            // Check if seed data is needed (if users table is empty)
            if (isTableEmpty(conn, "users")) {
                logger.info("Database appears fresh; loading sample seed data...");
                executeSqlScript(conn, "sample_data.sql");
            }
            logger.info("Database migrations completed successfully.");
        } catch (Exception e) {
            logger.error("Database migration encountered an error: {}", e.getMessage(), e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {}
        }
    }

    public static void executeSqlScript(Connection conn, String scriptPath) throws Exception {
        InputStream in = DatabaseMigration.class.getClassLoader().getResourceAsStream(scriptPath);
        if (in == null) {
            logger.warn("SQL script not found on classpath: {}", scriptPath);
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Strip inline comment if present
                int commentIdx = line.indexOf("--");
                if (commentIdx >= 0) {
                    line = line.substring(0, commentIdx).trim();
                }
                if (line.startsWith("//") || line.startsWith("/*") || line.isEmpty()) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty()) {
                        executeSingleStatement(conn, sql);
                    }
                    sb.setLength(0);
                }
            }
        }
    }

    private static void executeSingleStatement(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Ignore benign index/table already exists errors
            if (e.getMessage() != null && (e.getMessage().contains("already exists") || e.getMessage().contains("Duplicate"))) {
                logger.debug("Benign DDL warning: {}", e.getMessage());
            } else {
                logger.warn("SQL statement warning [{}]: {}", e.getMessage(), sql.length() > 80 ? sql.substring(0, 80) + "..." : sql);
            }
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            logger.debug("Could not count table {}: {}", tableName, e.getMessage());
        }
        return true;
    }
}
