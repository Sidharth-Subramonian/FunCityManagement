-- =============================================================================
-- Fun City 2.0: Smart Arcade & Amusement Centre Management System
-- Database Schema (MySQL 8.0 & ANSI SQL Compliant)
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'STAFF', 'TECHNICIAN'
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    wallet_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    ticket_balance INT NOT NULL DEFAULT 0,
    loyalty_points INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'BLOCKED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wallet_balance CHECK (wallet_balance >= 0.00),
    CONSTRAINT chk_ticket_balance CHECK (ticket_balance >= 0),
    CONSTRAINT chk_loyalty_points CHECK (loyalty_points >= 0)
);

CREATE TABLE IF NOT EXISTS membership_cards (
    card_id INT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(50) NOT NULL UNIQUE, -- Simulated RFID UID
    customer_id INT NOT NULL,
    card_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'BLOCKED', 'EXPIRED'
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS games (
    game_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL, -- 'ARCADE', 'SIMULATOR', 'REDEMPTION', 'VR'
    description TEXT,
    price_per_play DECIMAL(10,2) NOT NULL,
    dispenses_tickets BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_game_price CHECK (price_per_play >= 0.00)
);

CREATE TABLE IF NOT EXISTS machines (
    machine_id INT AUTO_INCREMENT PRIMARY KEY,
    machine_code VARCHAR(30) NOT NULL UNIQUE,
    game_id INT NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    floor_location VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPERATIONAL', -- 'OPERATIONAL', 'UNDER_MAINTENANCE', 'OUT_OF_SERVICE'
    installed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_maintenance_date TIMESTAMP NULL,
    FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    card_id INT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'RECHARGE', 'GAME_DEBIT', 'REFUND'
    amount DECIMAL(10,2) NOT NULL,
    opening_balance DECIMAL(10,2) NOT NULL,
    closing_balance DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    created_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (card_id) REFERENCES membership_cards(card_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS play_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    session_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    card_id INT NOT NULL,
    machine_id INT NOT NULL,
    game_id INT NOT NULL,
    game_fee DECIMAL(10,2) NOT NULL,
    tickets_earned INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED', -- 'INITIATED', 'COMPLETED', 'CANCELLED'
    wallet_transaction_id INT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    staff_id INT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (card_id) REFERENCES membership_cards(card_id) ON DELETE RESTRICT,
    FOREIGN KEY (machine_id) REFERENCES machines(machine_id) ON DELETE RESTRICT,
    FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE RESTRICT,
    FOREIGN KEY (wallet_transaction_id) REFERENCES wallet_transactions(transaction_id) ON DELETE SET NULL,
    FOREIGN KEY (staff_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_tickets_earned CHECK (tickets_earned >= 0)
);

CREATE TABLE IF NOT EXISTS ticket_transactions (
    ticket_transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    session_id INT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'EARNED', 'REDEEMED', 'ADJUSTED'
    tickets INT NOT NULL,
    opening_balance INT NOT NULL,
    closing_balance INT NOT NULL,
    description VARCHAR(255),
    created_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (session_id) REFERENCES play_sessions(session_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS prizes (
    prize_id INT AUTO_INCREMENT PRIMARY KEY,
    prize_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'PLUSH', 'ELECTRONICS', 'TOYS', 'CANDY'
    description TEXT,
    ticket_cost INT NOT NULL,
    inventory_stock INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ticket_cost CHECK (ticket_cost > 0),
    CONSTRAINT chk_inventory_stock CHECK (inventory_stock >= 0)
);

CREATE TABLE IF NOT EXISTS prize_redemptions (
    redemption_id INT AUTO_INCREMENT PRIMARY KEY,
    redemption_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    prize_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    tickets_spent INT NOT NULL,
    staff_id INT NULL,
    redeemed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (prize_id) REFERENCES prizes(prize_id) ON DELETE RESTRICT,
    FOREIGN KEY (staff_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_redemption_qty CHECK (quantity > 0),
    CONSTRAINT chk_tickets_spent CHECK (tickets_spent > 0)
);

CREATE TABLE IF NOT EXISTS reward_transactions (
    reward_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    points INT NOT NULL,
    opening_points INT NOT NULL,
    closing_points INT NOT NULL,
    activity_type VARCHAR(50) NOT NULL, -- 'RECHARGE_BONUS', 'PLAY_BONUS', 'POINTS_REDEMPTION'
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS maintenance_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    machine_id INT NOT NULL,
    reported_by INT NOT NULL,
    assigned_technician_id INT NULL,
    issue_description TEXT NOT NULL,
    technician_notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED', -- 'REPORTED', 'IN_PROGRESS', 'COMPLETED'
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (machine_id) REFERENCES machines(machine_id) ON DELETE RESTRICT,
    FOREIGN KEY (reported_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_technician_id) REFERENCES users(user_id) ON DELETE SET NULL
);

