-- Fix all foreign key constraints to reference the correct column names (snake_case)

-- Step 1: Drop old foreign key constraints
ALTER TABLE returns DROP CONSTRAINT IF EXISTS fk_return_bill;
ALTER TABLE returns DROP CONSTRAINT IF EXISTS fk_return_user;

-- Step 2: Recreate foreign key constraints with correct column names
ALTER TABLE returns 
ADD CONSTRAINT fk_return_bill 
FOREIGN KEY (original_bill_id) REFERENCES bills(id);

ALTER TABLE returns 
ADD CONSTRAINT fk_return_user 
FOREIGN KEY (processed_by_id) REFERENCES users(id);

-- Step 3: Verify all constraints were created correctly
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

