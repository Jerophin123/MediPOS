-- Fix foreign key constraint to reference the correct column name
-- The constraint fk_return_bill currently references originalBill_id (camelCase)
-- but the column is now original_bill_id (snake_case)

-- Step 1: Drop the old foreign key constraint
ALTER TABLE returns DROP CONSTRAINT IF EXISTS fk_return_bill;

-- Step 2: Recreate the foreign key constraint with the correct column name
ALTER TABLE returns 
ADD CONSTRAINT fk_return_bill 
FOREIGN KEY (original_bill_id) REFERENCES bills(id);

-- Step 3: Verify the constraint was created correctly
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

