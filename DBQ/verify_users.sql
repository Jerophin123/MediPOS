-- ============================================
-- VERIFY USERS CREATED
-- ============================================
-- Run this query to verify all users were created successfully
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

-- ============================================
-- EXPECTED OUTPUT: 7 users
-- ============================================
-- 1. admin (ADMIN)
-- 2. cashier (CASHIER)
-- 3. stockmonitor (STOCK_MONITOR)
-- 4. stockkeeper (STOCK_KEEPER)
-- 5. customersupport (CUSTOMER_SUPPORT)
-- 6. analyst (ANALYST)
-- 7. manager (MANAGER)
-- ============================================

