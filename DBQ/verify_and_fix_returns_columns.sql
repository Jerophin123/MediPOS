-- Verification and Fix Script for Returns Table Columns
-- This script checks the current column names and fixes them if needed

-- Step 1: Check current column names
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'returns' 
AND column_name LIKE '%bill%' OR column_name LIKE '%processed%'
ORDER BY column_name;

-- Step 2: If columns are still camelCase, rename them to snake_case
-- Uncomment and run these if the columns are still named "originalBill_id" and "processedBy_id"

-- ALTER TABLE returns RENAME COLUMN "originalBill_id" TO original_bill_id;
-- ALTER TABLE returns RENAME COLUMN "processedBy_id" TO processed_by_id;

-- Step 3: Verify the rename worked
-- SELECT column_name, data_type 
-- FROM information_schema.columns 
-- WHERE table_name = 'returns' 
-- AND (column_name = 'original_bill_id' OR column_name = 'processed_by_id');

-- Step 4: Recreate the index if needed
-- DROP INDEX IF EXISTS idx_return_bill;
-- CREATE INDEX IF NOT EXISTS idx_return_bill ON returns(original_bill_id);

-- Step 5: Verify foreign key constraints still work
-- SELECT
--     tc.constraint_name, 
--     tc.table_name, 
--     kcu.column_name, 
--     ccu.table_name AS foreign_table_name,
--     ccu.column_name AS foreign_column_name 
-- FROM information_schema.table_constraints AS tc 
-- JOIN information_schema.key_column_usage AS kcu
--   ON tc.constraint_name = kcu.constraint_name
-- JOIN information_schema.constraint_column_usage AS ccu
--   ON ccu.constraint_name = tc.constraint_name
-- WHERE tc.constraint_type = 'FOREIGN KEY' 
-- AND tc.table_name = 'returns';

