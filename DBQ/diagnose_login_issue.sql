-- ============================================
-- DIAGNOSE LOGIN ISSUE
-- ============================================
-- Run this to check why login might be failing
-- ============================================

-- 1. Check if admin user exists and is active
SELECT 
    id,
    username,
    email,
    full_name,
    role,
    active,
    LENGTH(password) as password_length,
    SUBSTRING(password, 1, 7) as hash_prefix,
    created_at,
    updated_at
FROM users
WHERE username = 'admin';

-- 2. Check if user is active
SELECT 
    CASE 
        WHEN active = true THEN '✅ User is active'
        ELSE '❌ User is INACTIVE - This will prevent login!'
    END as status_check
FROM users
WHERE username = 'admin';

-- 3. Check if role is correct
SELECT 
    CASE 
        WHEN role = 'ADMIN' THEN '✅ Role is correct (ADMIN)'
        ELSE '❌ Role is incorrect: ' || role || ' (should be ADMIN)'
    END as role_check
FROM users
WHERE username = 'admin';

-- 4. Verify password hash format (BCrypt should start with $2a$ or $2b$)
SELECT 
    CASE 
        WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' THEN '✅ Password hash format is correct'
        ELSE '❌ Password hash format is incorrect!'
    END as password_format_check,
    LENGTH(password) as hash_length
FROM users
WHERE username = 'admin';

-- 5. Update admin user to ensure correct settings
UPDATE users
SET 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    role = 'ADMIN',
    active = true,
    email = 'admin@medicalstore.com',
    full_name = 'System Administrator',
    updated_at = NOW()
WHERE username = 'admin';

-- 6. Final verification
SELECT 
    username,
    role,
    active,
    CASE 
        WHEN password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
        THEN '✅ Password updated to password123' 
        ELSE '❌ Password hash does not match'
    END as password_status
FROM users
WHERE username = 'admin';

-- ============================================
-- TROUBLESHOOTING STEPS:
-- ============================================
-- 1. If user is inactive: UPDATE users SET active = true WHERE username = 'admin';
-- 2. If role is wrong: UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
-- 3. If password hash is wrong: Run the UPDATE above
-- 4. After fixing, restart the backend application
-- 5. Clear browser cache and localStorage
-- 6. Try logging in again with: admin / password123
-- ============================================

