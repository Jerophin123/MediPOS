-- ============================================
-- CHECK ALL USERS
-- ============================================
-- Run this to see all users and verify they exist
-- ============================================

SELECT 
    id,
    username,
    email,
    full_name,
    role,
    active,
    created_at
FROM users
ORDER BY 
    CASE role
        WHEN 'ADMIN' THEN 1
        WHEN 'CASHIER' THEN 2
        WHEN 'STOCK_MONITOR' THEN 3
        WHEN 'STOCK_KEEPER' THEN 4
        WHEN 'CUSTOMER_SUPPORT' THEN 5
        WHEN 'ANALYST' THEN 6
        WHEN 'MANAGER' THEN 7
        ELSE 8
    END,
    username;

-- Expected: 7 users total
-- If you see fewer, run create_users.sql again
-- The script uses ON CONFLICT DO NOTHING, so it's safe to run multiple times

