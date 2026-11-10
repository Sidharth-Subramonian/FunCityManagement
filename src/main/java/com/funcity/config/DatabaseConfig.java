package com.funcity.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile HikariDataSource dataSource;
    private static boolean isH2Fallback = false;

    private DatabaseConfig() {}

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Properties props = loadProperties();

        String url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/funcity");
        }

        String user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = props.getProperty("db.user", "root");
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = props.getProperty("db.password", "Changeme@315");
        }

        String driver = System.getenv("DB_DRIVER");
        if (driver == null || driver.trim().isEmpty()) {
            driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        }

        boolean allowH2Fallback = Boolean.parseBoolean(props.getProperty("db.fallback.h2", "true"));

        try {
            logger.info("Connecting to primary database at: {}", url);
            dataSource = createDataSource(url, user, password, driver, props);
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Successfully connected to primary database: {}", conn.getMetaData().getDatabaseProductName());
                isH2Fallback = false;
            }
        } catch (Exception ex) {
            logger.warn("Primary database connection failed: {}", ex.getMessage());
            if (allowH2Fallback) {
                logger.info("Initializing embedded H2 database (MySQL compatibility mode)...");
                initializeH2Fallback();
            } else {
                throw new RuntimeException("Could not connect to database and H2 fallback is disabled.", ex);
            }
        }

        // Run migrations
        DatabaseMigration.runMigrations(getConnection());
    }

    public static synchronized void initializeH2Fallback() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        String h2Url = "jdbc:h2:mem:funcity;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(h2Url);
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("FunCity-H2-Pool");

        dataSource = new HikariDataSource(config);
        isH2Fallback = true;
        logger.info("H2 in-memory fallback pool initialized.");
    }

    public static synchronized void initializeForTesting(String testJdbcUrl) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(testJdbcUrl);
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);
        config.setPoolName("FunCity-Test-Pool");

        dataSource = new HikariDataSource(config);
        isH2Fallback = true;
        DatabaseMigration.runMigrations(getConnection());
    }

    private static HikariDataSource createDataSource(String url, String user, String password, String driver, Properties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName(driver);

        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("pool.minimumIdle", "2")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("pool.idleTimeout", "30000")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("pool.connectionTimeout", "5000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("pool.maxLifetime", "1800000")));
        config.setPoolName("FunCity-HikariPool");

        // MySQL specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            logger.warn("Could not load db.properties from classpath, using defaults.");
        }
        return props;
    }

    public static Connection getConnection() {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Failed to acquire connection from pool: {}", e.getMessage());
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
    }

    public static boolean isH2Fallback() {
        return isH2Fallback;
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariCP database pool...");
            dataSource.close();
        }
    }
}

