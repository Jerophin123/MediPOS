-- Check the actual column names in return_items table
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'return_items'
ORDER BY ordinal_position;

