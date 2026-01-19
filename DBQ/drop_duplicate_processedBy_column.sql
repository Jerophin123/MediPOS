-- Drop duplicate processedBy_id column (camelCase)
-- This script removes the old camelCase column since we now have processed_by_id (snake_case)

-- Step 1: Check if processedBy_id column exists
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'returns' 
AND column_name IN ('processedBy_id', 'processed_by_id', 'originalBill_id', 'original_bill_id')
ORDER BY column_name;

-- Step 2: Drop the old camelCase column if it exists
-- WARNING: Make sure processed_by_id exists and has the correct data before running this!
DO $$
BEGIN
    -- Check if processedBy_id exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'returns' 
        AND column_name = 'processedBy_id'
    ) THEN
        -- Drop the old camelCase column
        ALTER TABLE returns DROP COLUMN "processedBy_id";
        RAISE NOTICE 'Dropped old processedBy_id column (camelCase)';
    ELSE
        RAISE NOTICE 'processedBy_id column does not exist - nothing to drop';
    END IF;
END $$;

-- Step 3: Verify only the correct columns remain
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'returns' 
AND (column_name LIKE '%bill%' OR column_name LIKE '%processed%')
ORDER BY column_name;

-- Step 4: Verify foreign key constraints point to correct columns
SELECT
    tc.constraint_name, 
    kcu.column_name, 
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' 
AND tc.table_name = 'returns'
ORDER BY kcu.column_name;

