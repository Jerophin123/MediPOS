-- Fix return_items table: Rename batchnumber to batch_number
-- This ensures the column name matches Hibernate's expected naming

-- Step 1: Check current column name
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'return_items'
AND (column_name LIKE '%batch%' OR column_name = 'batchnumber' OR column_name = 'batch_number')
ORDER BY column_name;

-- Step 2: Rename batchnumber to batch_number if it exists
DO $$
BEGIN
    -- Check if batchnumber exists (no underscore)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'return_items' 
        AND column_name = 'batchnumber'
    ) THEN
        -- Check if batch_number already exists
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'return_items' 
            AND column_name = 'batch_number'
        ) THEN
            -- Both exist - drop the old one
            ALTER TABLE return_items DROP COLUMN "batchnumber";
            RAISE NOTICE 'Dropped duplicate batchnumber column - batch_number already exists';
        ELSE
            -- Rename to batch_number
            ALTER TABLE return_items RENAME COLUMN "batchnumber" TO batch_number;
            RAISE NOTICE 'Renamed batchnumber to batch_number';
        END IF;
    ELSE
        RAISE NOTICE 'batchnumber column does not exist';
    END IF;
END $$;

-- Step 3: Verify the column now exists with correct name
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'return_items'
ORDER BY ordinal_position;

