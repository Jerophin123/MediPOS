-- Cleanup duplicate columns in return_items table
-- Drop refundamount (no underscore) since refund_amount (with underscore) exists

ALTER TABLE return_items DROP COLUMN IF EXISTS "refundamount";

-- Verify only correct columns remain
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns
WHERE table_name = 'return_items'
ORDER BY ordinal_position;

