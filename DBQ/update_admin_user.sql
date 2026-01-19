-- ============================================
-- UPDATE EXISTING ADMIN USER
-- ============================================
-- This script updates the existing admin user to use the new password
-- and ensures it has the correct ADMIN role
-- ============================================

-- Update admin user password to password123 (BCrypt hashed)
UPDATE users
SET 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
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
    updated_at
FROM users
WHERE username = 'admin';

-- ============================================
-- After running this:
-- - Admin username: admin
-- - Admin password: password123
-- - Admin role: ADMIN (all pages access)
-- ============================================

