-- Final Fix Script for Returns Table Columns
-- Run this script to ensure columns match Hibernate's expected names

-- Step 1: Check if columns exist with old names (camelCase)
DO $$
BEGIN
    -- Check if originalBill_id exists (camelCase)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'returns' 
        AND column_name = 'originalBill_id'
    ) THEN
        -- Rename to snake_case
        ALTER TABLE returns RENAME COLUMN "originalBill_id" TO original_bill_id;
        RAISE NOTICE 'Renamed originalBill_id to original_bill_id';
    END IF;
    
    -- Check if processedBy_id exists (camelCase) - DROP it if processed_by_id already exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'returns' 
        AND column_name = 'processedBy_id'
    ) THEN
        -- Check if processed_by_id already exists
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'returns' 
            AND column_name = 'processed_by_id'
        ) THEN
            -- Drop the old camelCase column since snake_case already exists
            ALTER TABLE returns DROP COLUMN "processedBy_id";
            RAISE NOTICE 'Dropped duplicate processedBy_id column (camelCase) - processed_by_id already exists';
        ELSE
            -- Rename to snake_case if processed_by_id doesn't exist
            ALTER TABLE returns RENAME COLUMN "processedBy_id" TO processed_by_id;
            RAISE NOTICE 'Renamed processedBy_id to processed_by_id';
        END IF;
    END IF;
END $$;

-- Step 2: Verify the columns now exist with correct names
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'returns' 
AND (column_name = 'original_bill_id' OR column_name = 'processed_by_id')
ORDER BY column_name;

-- Step 3: Recreate index with correct column name
DROP INDEX IF EXISTS idx_return_bill;
CREATE INDEX IF NOT EXISTS idx_return_bill ON returns(original_bill_id);

-- Step 4: Verify foreign key constraints
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

