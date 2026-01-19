-- ============================================
-- UPDATE ALL USERS TO NEW PASSWORD
-- ============================================
-- This script updates all existing users to use password123
-- Useful if you have old users with different passwords
-- ============================================

-- BCrypt hash for password123
-- This is the same hash used in create_users.sql
UPDATE users
SET 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    updated_at = NOW()
WHERE password != '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- Verify all users have the new password
SELECT 
    username,
    role,
    active,
    CASE 
        WHEN password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
        THEN 'password123' 
        ELSE 'DIFFERENT PASSWORD' 
    END as password_status
FROM users
ORDER BY role, username;

-- ============================================
-- All users should now have password: password123
-- ============================================

