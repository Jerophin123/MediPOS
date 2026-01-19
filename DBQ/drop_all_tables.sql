-- ============================================
-- DROP ALL TABLES AND RESET SCHEMA
-- ============================================
-- WARNING: This will DELETE ALL DATA permanently!
-- Use this only if you want to start fresh.
-- ============================================

-- Disable foreign key checks temporarily (PostgreSQL doesn't need this, but for safety)
-- Drop all tables in reverse dependency order (children first, parents last)

-- Drop child tables first
DROP TABLE IF EXISTS return_items CASCADE;
DROP TABLE IF EXISTS return_batches CASCADE;
DROP TABLE IF EXISTS returns CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS bill_items CASCADE;
DROP TABLE IF EXISTS bills CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS stock_barcodes CASCADE;
DROP TABLE IF EXISTS batches CASCADE;

-- Drop parent tables
DROP TABLE IF EXISTS medicines CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop sequences if they exist (PostgreSQL auto-creates sequences for IDENTITY columns)
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;
DROP SEQUENCE IF EXISTS medicines_id_seq CASCADE;
DROP SEQUENCE IF EXISTS batches_id_seq CASCADE;
DROP SEQUENCE IF EXISTS bills_id_seq CASCADE;
DROP SEQUENCE IF EXISTS bill_items_id_seq CASCADE;
DROP SEQUENCE IF EXISTS payments_id_seq CASCADE;
DROP SEQUENCE IF EXISTS returns_id_seq CASCADE;
DROP SEQUENCE IF EXISTS return_items_id_seq CASCADE;
DROP SEQUENCE IF EXISTS audit_logs_id_seq CASCADE;
DROP SEQUENCE IF EXISTS stock_barcodes_id_seq CASCADE;

-- Verify all tables are dropped
SELECT 
    'Tables remaining:' as info,
    COUNT(*) as count
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_type = 'BASE TABLE';

-- Show any remaining tables (should be empty)
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- ============================================
-- RESET COMPLETE
-- ============================================
-- Next steps:
-- 1. Restart your Spring Boot application
-- 2. Hibernate will automatically recreate all tables with ddl-auto: update
-- 3. All tables will be created with the correct schema including barcode in medicines
-- ============================================

