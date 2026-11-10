-- =============================================================================
-- Fun City 2.0: Smart Arcade & Amusement Centre Management System
-- Sample Seed Data
-- =============================================================================

-- Seed Users:
-- admin / Admin@123
-- staff1 / Staff@123
-- tech1 / Tech@123
INSERT INTO users (username, password_hash, full_name, email, role, status) VALUES
('admin', '$2a$12$7iJYFZqdOPaqwlZUMOQYeOYVud.R27Iabss3w7M6QM04s3zSsoBVS', 'System Administrator', 'admin@funcity.com', 'ADMIN', 'ACTIVE'),
('staff1', '$2a$12$iERDX75pf089pOCEeWPYkuJIGF1YQlrHVNw6analX5hjdo0FhUfuO', 'Alex Rivera (Staff)', 'staff1@funcity.com', 'STAFF', 'ACTIVE'),
('tech1', '$2a$12$Q7C/AGu/lnrwlPcbx0NVOuz82/jQHadn34AZwdGcGYthHTwHSe8Hi', 'Marcus Vance (Technician)', 'tech1@funcity.com', 'TECHNICIAN', 'ACTIVE');

-- Seed Games
INSERT INTO games (name, category, description, price_per_play, dispenses_tickets, status) VALUES
('MotoGP Racing Simulator', 'SIMULATOR', 'Ultra-realistic leaning motorcycle arcade experience with motion feedback.', 2.50, TRUE, 'ACTIVE'),
('Jurassic Park Arcade', 'ARCADE', 'Motion-cabinet environmental shooter saving dinosaurs on Isla Nublar.', 3.00, FALSE, 'ACTIVE'),
('Skee-Ball Classic', 'REDEMPTION', 'The timeless alley bowler skill game dispensing physical/digital redemption tickets.', 1.50, TRUE, 'ACTIVE'),
('Space Invaders Frenzy', 'ARCADE', 'Giant 8-foot LED screen retro revival dual shooter.', 2.00, TRUE, 'ACTIVE'),
('VR Hyper Ride 360', 'VR', 'Virtual reality full 360-degree coaster simulator.', 5.00, FALSE, 'ACTIVE'),
('Air Hockey Arena Pro', 'ARCADE', 'Tournament-grade high airflow puck battle table.', 2.00, FALSE, 'ACTIVE'),
('Ticket Monster Wheel', 'REDEMPTION', 'High-stakes spinning wheel redemption machine with monster jackpot tickets.', 2.00, TRUE, 'ACTIVE'),
('Basketball Shootout Pro', 'REDEMPTION', 'Timed arcade basketball hoop shootout challenge.', 1.50, TRUE, 'ACTIVE');

-- Seed Machines
INSERT INTO machines (machine_code, game_id, serial_number, floor_location, status) VALUES
('MCH-01', 1, 'SN-MOTO-001', 'Zone A - Simulator Bay 1', 'OPERATIONAL'),
('MCH-02', 1, 'SN-MOTO-002', 'Zone A - Simulator Bay 2', 'OPERATIONAL'),
('MCH-03', 2, 'SN-JP-001', 'Zone B - Action Alley', 'OPERATIONAL'),
('MCH-04', 3, 'SN-SKEE-001', 'Zone C - Carnival Row 1', 'OPERATIONAL'),
('MCH-05', 3, 'SN-SKEE-002', 'Zone C - Carnival Row 2', 'UNDER_MAINTENANCE'),
('MCH-06', 4, 'SN-SPINV-001', 'Zone B - Action Alley Center', 'OPERATIONAL'),
('MCH-07', 5, 'SN-VR-001', 'Zone VR - Pod 1', 'OPERATIONAL'),
('MCH-08', 8, 'SN-HOOP-001', 'Zone D - Sports Arena 1', 'OPERATIONAL');

-- Seed Customers
INSERT INTO customers (customer_code, name, email, phone, wallet_balance, ticket_balance, loyalty_points, status) VALUES
('CUST-1001', 'Alex Johnson', 'alex.j@example.com', '+15551234567', 45.00, 850, 150, 'ACTIVE'),
('CUST-1002', 'Samantha Lee', 'sam.lee@example.com', '+15552345678', 22.50, 1420, 80, 'ACTIVE'),
('CUST-1003', 'Michael Brown', 'michael.b@example.com', '+15553456789', 5.00, 120, 20, 'ACTIVE'),
('CUST-1004', 'Emily Davis', 'emily.d@example.com', '+15554567890', 80.00, 3500, 280, 'ACTIVE');

-- Seed Membership Cards (Simulated RFID UIDs)
INSERT INTO membership_cards (card_number, customer_id, card_status) VALUES
('FC-A1001001', 1, 'ACTIVE'),
('FC-B2002002', 2, 'ACTIVE'),
('FC-C3003003', 3, 'ACTIVE'),
('FC-D4004004', 4, 'ACTIVE');

-- Seed Prizes
INSERT INTO prizes (prize_code, name, category, description, ticket_cost, inventory_stock, status) VALUES
('PRZ-001', 'Giant Pikachu Plush 24"', 'PLUSH', 'Super soft collector edition jumbo plush toy.', 1500, 10, 'ACTIVE'),
('PRZ-002', 'Wireless RGB Gaming Headset', 'ELECTRONICS', '7.1 surround sound gaming headphones with boom mic.', 3000, 6, 'ACTIVE'),
('PRZ-003', 'LED Strobe Yo-Yo Pro', 'TOYS', 'High speed ball-bearing light up stunt yo-yo.', 120, 45, 'ACTIVE'),
('PRZ-004', 'Mega Sour Gummy Box (1 lb)', 'CANDY', 'Assorted gourmet sour gummy worms and bears.', 200, 30, 'ACTIVE'),
('PRZ-005', 'Waterproof Bluetooth Speaker', 'ELECTRONICS', 'Compact rugged outdoor portable speaker.', 2200, 8, 'ACTIVE'),
('PRZ-006', 'Fun City Championship Cap', 'TOYS', 'Embroidered snapback cap with holographic arcade brim.', 450, 25, 'ACTIVE');

-- Seed Sample Maintenance Record
INSERT INTO maintenance_records (machine_id, reported_by, assigned_technician_id, issue_description, technician_notes, status, priority) VALUES
(5, 2, 3, 'Ball return sensor jammed; balls not registering in scoring chute.', 'Inspected ball gate. Replacement optical sensor ordered and in progress.', 'IN_PROGRESS', 'HIGH');

