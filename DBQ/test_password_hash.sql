-- ============================================
-- TEST PASSWORD HASH
-- ============================================
-- This script helps verify if the password hash is working
-- ============================================

-- Check current admin user status
SELECT 
    username,
    role,
    active,
    CASE 
        WHEN active = false THEN '❌ USER IS INACTIVE - Cannot login!'
        WHEN role != 'ADMIN' THEN '❌ ROLE IS WRONG: ' || role
        ELSE '✅ User settings look correct'
    END as status
FROM users
WHERE username = 'admin';

-- ============================================
-- IMPORTANT: The password hash must be generated
-- by the Spring Boot application using BCryptPasswordEncoder
-- ============================================
-- 
-- To get the correct hash, you can:
-- 1. Start the Spring Boot application
-- 2. The DataInitializer will automatically update the admin password
-- 3. OR use the application's password encoder to generate a new hash
--
-- The hash format should be: $2a$10$... (60 characters total)
-- ============================================



