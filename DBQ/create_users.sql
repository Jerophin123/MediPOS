-- ============================================
-- CREATE USERS FOR EACH ROLE
-- ============================================
-- This script creates users for each department/role
-- Passwords are BCrypt hashed (default password for all: password123)
-- 
-- IMPORTANT: Change passwords after first login!
-- ============================================

-- Admin User (All pages access)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'admin@medicalstore.com',
    'System Administrator',
    'ADMIN',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Cashier (Billing access only)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'cashier',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'cashier@medicalstore.com',
    'Cashier User',
    'CASHIER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Stock Monitor (Inventory access only)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'stockmonitor',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'stockmonitor@medicalstore.com',
    'Stock Monitor',
    'STOCK_MONITOR',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Stock Keeper (Medicines access only)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'stockkeeper',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'stockkeeper@medicalstore.com',
    'Stock Keeper',
    'STOCK_KEEPER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Customer Support (Returns access only)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'customersupport',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'customersupport@medicalstore.com',
    'Customer Support Team',
    'CUSTOMER_SUPPORT',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Analyst (Reports access only)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'analyst',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'analyst@medicalstore.com',
    'Data Analyst',
    'ANALYST',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Manager (Reports + Purchase History access)
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
VALUES (
    'manager',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'manager@medicalstore.com',
    'Manager',
    'MANAGER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- ============================================
-- LOGIN CREDENTIALS SUMMARY
-- ============================================
-- All users have default password: password123
-- 
-- Role          | Username          | Access
-- --------------|-------------------|------------------------------------------
-- Admin         | admin             | All pages
-- Cashier       | cashier           | Billing
-- Stock Monitor | stockmonitor      | Inventory
-- Stock Keeper  | stockkeeper       | Medicines
-- Customer Sup. | customersupport   | Returns
-- Analyst       | analyst           | Reports
-- Manager       | manager           | Reports + Purchase History
-- ============================================

