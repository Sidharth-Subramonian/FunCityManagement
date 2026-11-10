# 🎡 Fun City 2.0: Smart Arcade & Amusement Centre Management System

[![Java Version](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2024-orange.svg)](https://www.oracle.com/java/)
[![Build Status](https://img.shields.io/badge/Build-Maven%20Passing-brightgreen.svg)](https://maven.apache.org/)
[![UI](https://img.shields.io/badge/UI-FlatLaf%20%2B%20JFreeChart-blue.svg)](https://www.formdev.com/flatlaf/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0%20%7C%20H2-teal.svg)](https://www.mysql.com/)

Fun City 2.0 is a production-grade, modular, cashless arcade and amusement centre management system inspired by industry platforms such as **Semnox Parafait**, **Embed**, and **Intercard**.

The system completely modernizes legacy desktop arcade software by introducing **simulated RFID membership cards**, **atomic digital wallet transactions**, **server-authoritative play sessions**, **electronic ticket ledgers**, **prize inventory redemption**, **machine maintenance tracking**, and **real-time business analytics**.

---

## 📌 Table of Contents
1. [Architectural Overview](#-architectural-overview)
2. [Key Features by Role](#-key-features-by-role)
3. [Technology Stack](#-technology-stack)
4. [Database Design & Normalization](#-database-design--normalization)
5. [Cashless Arcade Workflows](#-cashless-arcade-workflows)
6. [Default Login Accounts](#-default-login-accounts)
7. [Getting Started & Installation](#-getting-started--installation)
8. [Configuration & Migration Strategy](#-configuration--migration-strategy)
9. [Automated Testing](#-automated-testing)
10. [Authors & Credits](#-authors--credits)

---

## 🏗️ Architectural Overview

Fun City 2.0 follows a strict **Layered Architecture** adhering to SOLID principles and separation of concerns:

```
┌────────────────────────────────────────────────────────┐
│                      UI LAYER                          │
│ FlatLaf Look & Feel | Modern Sidebar & Top Nav         │
│   ├── ui.auth        (LoginForm, Role Preset Bar)      │
│   ├── ui.dashboard   (JFreeChart Analytics, StatCards) │
│   ├── ui.admin       (Games, Machines, Staff, Ledgers) │
│   ├── ui.staff       (POS: RFID Tap, Wallet, Prizes)   │
│   ├── ui.technician  (Maintenance & Machine Console)   │
│   └── ui.components  (StatCard, SearchField, UIHelper) │
└───────────────────────────┬────────────────────────────┘
                            │ (Calls asynchronously via SwingWorker)
┌───────────────────────────▼────────────────────────────┐
│                    SERVICE LAYER                       │
│ Business Logic, Validations, Transaction Orchestration │
│   ├── AuthenticationService                            │
│   ├── CustomerService & MembershipService              │
│   ├── WalletService (Atomic Recharge / Debit / Refund) │
│   ├── GameService & MachineService                     │
│   ├── PlayService (Complete Play Session Lifecycle)    │
│   ├── TicketService & RedemptionService                │
│   ├── RewardService & MaintenanceService               │
│   └── ReportService (Analytics Aggregations)           │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                      DAO LAYER                         │
│ Clean JDBC, PreparedStatements, Explicit Transactions   │
│ Row-Level Locking (SELECT ... FOR UPDATE)              │
│   ├── CustomerDAO, MembershipCardDAO, UserDAO          │
│   ├── GameDAO, MachineDAO, PlaySessionDAO              │
│   ├── WalletDAO, TicketDAO, PrizeDAO, RewardDAO        │
│   └── MaintenanceDAO, ReportDAO                        │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                 DATA & PERSISTENCE                     │
│ HikariCP Connection Pool | Externalized db.properties   │
│ MySQL 8.0 (Production) / H2 Embedded (Offline/Tests)   │
└────────────────────────────────────────────────────────┘
```

---

## 🌟 Key Features by Role

### 👑 Administrator
- **Business Dashboard**: Real-time KPI stat cards (Total Customers, Active RFID Cards, Operational Machines, Today's Revenue, Play Sessions Today) and JFreeChart visual graphs (7-Day Revenue, Game Popularity, Machine Status Pie, Prize Redemptions).
- **Customer Directory**: Customer profiles, search by name/code/phone/card, card issuance, card blocking/unblocking, and ledger audits.
- **Game Management**: Configure game titles, categories (Arcade, Simulator, VR, Redemption), prices per play, ticket eligibility, and status.
- **Physical Machine Management**: Link physical machines to game titles, track floor locations, serial numbers, and operational statuses (`OPERATIONAL`, `UNDER_MAINTENANCE`, `OUT_OF_SERVICE`).
- **Unified Ledgers**: Full immutable audit trails for Digital Wallet Transactions, Electronic Ticket Ledgers, and Prize Redemption logs.
- **User Accounts**: Create and manage Staff and Technician accounts with role-based access.

### 🎟️ Arcade Staff (POS Cashier Terminal)
- **Simulated RFID Card Tap**: Instant card lookup displaying customer identity, card status, wallet balance, ticket balance, and loyalty points.
- **Fast Wallet Recharge**: Quick preset buttons (`+$10`, `+$20`, `+$50`, `+$100`) or custom amounts with atomic database transactions and automatic loyalty points calculation.
- **Play Session Initiation**: Tap-to-play with operational machine selection, server-side game price retrieval, and wallet debit.
- **Ticket Awarding Console**: Log actual tickets won by players after gameplay, crediting electronic ticket accounts instantly.
- **Prize Redemption Store**: Redeem prizes from inventory stock (Plush toys, Electronics, Candy), checking ticket balances and decrementing inventory atomically.
- **Customer Registration**: Quick modal to register new customers and auto-issue RFID membership cards.

### 🔧 Maintenance Technician
- **Floor Machine Monitoring**: Live grid displaying the status of all arcade machines across the floor.
- **Work Order Management**: View reported faults, update technician repair notes, set status to `IN_PROGRESS`, and mark `COMPLETED`.
- **Automatic Machine Availability Sync**: Reporting a fault automatically sets machine to `UNDER_MAINTENANCE` (blocking new play sessions); completing the repair automatically restores it to `OPERATIONAL`.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 17 / 21 / 24 | Modern Object-Oriented Java with strong encapsulation |
| **GUI Framework** | Java Swing + FlatLaf 3.4.1 | Clean, responsive POS and desktop interface |
| **Data Visualization** | JFreeChart 1.5.4 | Embedded analytics charts |
| **Database** | MySQL 8.0 & H2 | MySQL 8 for production; embedded H2 for self-contained testing |
| **Connection Pool**| HikariCP 5.1.0 | High-performance JDBC connection pooling |
| **Password Security**| jBCrypt 0.4 | Industry-standard salted BCrypt password hashing |
| **Logging** | SLF4J + Logback | Structured logging with rotation and level control |
| **Build Tool** | Apache Maven | Dependency management, compilation, and shade packaging |
| **Testing** | JUnit 5 | Comprehensive unit, integration, and concurrency tests |

---

## 🗄️ Database Design & Normalization

The database is normalized to 3NF and enforces relational integrity via foreign keys, unique constraints, and check constraints across 12 core tables:

1. `users`: System staff, technicians, and administrators with BCrypt password hashes.
2. `customers`: Customer profiles with unique customer codes, emails, phones, and cached balance summaries.
3. `membership_cards`: Simulated RFID cards (`card_number` UID) linked to customers with status (`ACTIVE`, `BLOCKED`, `EXPIRED`).
4. `games`: Game software/types, prices per play (`DECIMAL(10,2)`), ticket eligibility, and categories.
5. `machines`: Physical hardware units linked to games, locations, and operational statuses.
6. `wallet_transactions`: Ledger of all financial movements (`RECHARGE`, `GAME_DEBIT`, `REFUND`) with opening and closing balances.
7. `play_sessions`: Complete session records (`INITIATED`, `COMPLETED`, `CANCELLED`) tracking card, machine, game fee, and tickets earned.
8. `ticket_transactions`: Ledger of electronic ticket awards (`EARNED`, `REDEEMED`, `ADJUSTED`).
9. `prizes`: Prize redemption catalog with ticket costs, inventory stock, and categories.
10. `prize_redemptions`: Audit logs of prize redemptions.
11. `reward_transactions`: Customer loyalty points ledger (distinct from arcade tickets).
12. `maintenance_records`: Machine fault tickets, technician notes, priorities, and resolution timestamps.

---

## 🔄 Cashless Arcade Workflows

### 1. Play Session Workflow
```
[Staff Scans Card] ──► [System Validates Card: ACTIVE?]
                              │ (Yes)
                              ▼
                     [Select Operational Machine]
                              │
                              ▼
                     [Fetch Game Price from DB]
                              │
                              ▼
                 [Validate Wallet Balance >= Price]
                              │ (Atomic Transaction)
                              ├─► Deduct Game Fee from Wallet
                              ├─► Record Wallet Transaction (GAME_DEBIT)
                              └─► Create Play Session (INITIATED)
                              │
                              ▼
                       [Player Plays Game]
                              │
                              ▼
           [Staff Enters Actual Tickets Won & Submits]
                              │ (Atomic Transaction)
                              ├─► Update Session (COMPLETED)
                              ├─► Credit Tickets to Customer Account
                              └─► Record Ticket Transaction (EARNED)
```

### 2. Prize Redemption Workflow
```
[Select Customer & Prize] ──► [Check Prize: ACTIVE?]
                                     │ (Yes)
                                     ▼
                           [Check Inventory >= Qty]
                                     │ (Yes)
                                     ▼
                       [Check Tickets >= Ticket Cost]
                                     │ (Atomic Transaction)
                                     ├─► Deduct Tickets from Customer
                                     ├─► Decrement Prize Stock
                                     ├─► Record Ticket Transaction (REDEEMED)
                                     └─► Record Prize Redemption Entry
```

---

## 🔑 Default Login Accounts

The system seeds three pre-configured accounts out-of-the-box (with quick-select buttons on the login screen for testing):

| Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `Admin@123` | Full access to all modules, analytics, configuration, ledgers, accounts |
| **Staff** | `staff1` | `Staff@123` | POS Terminal, Customer lookup, Wallet recharge, Play sessions, Prize redemption |
| **Technician** | `tech1` | `Tech@123` | Machine status monitoring, Maintenance work orders, Fault reporting |

---

## 🚀 Getting Started & Installation

### Prerequisites
- **Java JDK 17** or higher (Java 17, 21, or 24 supported).
- **Apache Maven 3.8+** (or use the included `mvnw.cmd` wrapper).
- **MySQL 8.0** (optional; the application includes an automatic in-memory H2 fallback for instant offline evaluation).

### Build & Package
To compile and package the fat executable JAR:
```bash
mvn clean package
```

### Running the Application
Run the packaged JAR:
```bash
java -jar target/funcity-management-2.0.0.jar
```

Or run directly from Maven:
```bash
mvn compile exec:java -Dexec.mainClass="com.funcity.Main"
```

---

## ⚙️ Configuration & Migration Strategy

Database connection settings are externalized in `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/funcity?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=Changeme@315
db.driver=com.mysql.cj.jdbc.Driver

pool.maximumPoolSize=10
pool.minimumIdle=2

# Fallback to embedded H2 if MySQL is unavailable
db.fallback.h2=true
```

You can also override these settings using environment variables:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_DRIVER`

### Automated Migrations
On startup, `DatabaseMigration.java` automatically runs `schema.sql` idempotently. If the database is fresh, it automatically populates `sample_data.sql` with games, machines, prizes, customers, and seed users.

---

## 🧪 Automated Testing

Run the full JUnit 5 test suite:
```bash
mvn test
```

### Test Coverage Highlights
- `testAuthenticationSuccessAndRoleAccess`: Validates BCrypt verification for all three roles.
- `testCustomerRegistrationWithAutoCard`: Verifies unique constraints and RFID card issuance.
- `testMembershipCardValidationAndStatus`: Verifies card lifecycle (`ACTIVE` -> `BLOCKED` -> `ACTIVE`).
- `testWalletRechargeAndLoyaltyBonus`: Tests atomic balance updates and loyalty point accumulation.
- `testPlaySessionWorkflowSuccess`: Complete play lifecycle from fee deduction to ticket allocation.
- `testPlaySessionRejectedForInsufficientBalance`: Validates negative balance prevention.
- `testPlaySessionRejectedForMachineUnderMaintenance`: Enforces machine availability constraints.
- `testPlaySessionCancellationAndRefund`: Tests atomic refund workflow.
- `testPrizeRedemptionLifecycle`: Validates ticket debit, inventory decrement, and balance checks.
- `testMaintenanceLifecycle`: Validates fault reporting, automatic status toggle, and resolution.
- `testConcurrentWalletDebits`: Multithreaded concurrent debit test verifying pessimistic row-level locking.

---

## 👨‍💻 Developers & Credits
- **Sidharth Subramonian** & **Skandan Kamal**  
  *B.E. CSE, Rajalakshmi Engineering College*  
- **Architecture & Modernization:** Fun City 2.0 Architectural Redesign

