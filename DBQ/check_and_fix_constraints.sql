-- Check and fix all constraints on the returns table
-- The error message shows constraint "originalBill_id" but column is "original_bill_id"

-- Step 1: Check all constraints on returns table
SELECT
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    cc.check_clause
FROM information_schema.table_constraints AS tc
LEFT JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
LEFT JOIN information_schema.check_constraints AS cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'returns'
ORDER BY tc.constraint_type, tc.constraint_name;

-- Step 2: Check NOT NULL constraints (these are implicit, not named constraints)
SELECT
    column_name,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'returns'
AND (column_name LIKE '%bill%' OR column_name LIKE '%processed%')
ORDER BY column_name;

-- Step 3: If there are any CHECK constraints with old column names, drop and recreate them
-- (Usually NOT NULL is a column property, not a separate constraint)

-- Step 4: Verify the column properties are correct
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'returns'
ORDER BY ordinal_position;

