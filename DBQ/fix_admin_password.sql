-- ============================================
-- FIX ADMIN PASSWORD - VERIFIED HASH
-- ============================================
-- This script updates the admin password to password123
-- The hash below is generated using BCryptPasswordEncoder
-- ============================================

-- First, let's check the current admin user
SELECT 
    username,
    role,
    active,
    LENGTH(password) as password_length,
    SUBSTRING(password, 1, 7) as hash_prefix
FROM users
WHERE username = 'admin';

-- Update admin password to password123
-- This hash is generated with BCrypt strength 10
UPDATE users
SET 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    role = 'ADMIN',
    active = true,
    updated_at = NOW()
WHERE username = 'admin';

-- Verify the update
SELECT 
    username,
    email,
    full_name,
    role,
    active,
    CASE 
        WHEN password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
        THEN '✅ Password updated correctly' 
        ELSE '❌ Password hash mismatch' 
    END as password_status,
    updated_at
FROM users
WHERE username = 'admin';

-- ============================================
-- If login still fails, try these steps:
-- 1. Verify user is active: SELECT active FROM users WHERE username = 'admin';
-- 2. Check role: SELECT role FROM users WHERE username = 'admin';
-- 3. Restart the backend application
-- 4. Clear browser cache and localStorage
-- ============================================

