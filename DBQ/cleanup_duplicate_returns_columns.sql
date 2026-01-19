-- Cleanup script to remove duplicate columns from returns table
-- Keep only the snake_case versions, drop the camelCase and no-underscore versions

-- Step 1: Drop duplicate camelCase and no-underscore columns
ALTER TABLE returns DROP COLUMN IF EXISTS "originalBill_id";
ALTER TABLE returns DROP COLUMN IF EXISTS "createdat";
ALTER TABLE returns DROP COLUMN IF EXISTS "refundamount";
ALTER TABLE returns DROP COLUMN IF EXISTS "returndate";
ALTER TABLE returns DROP COLUMN IF EXISTS "returnnumber";
ALTER TABLE returns DROP COLUMN IF EXISTS "returntype";

-- Step 2: Verify only the correct columns remain
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'returns'
ORDER BY ordinal_position;

-- Step 3: Verify foreign key constraints point to correct columns
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

