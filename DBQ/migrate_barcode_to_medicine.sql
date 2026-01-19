-- ============================================
-- BARCODE DOMAIN MODEL FIX - DATABASE MIGRATION
-- ============================================
-- This script migrates the database to the correct domain model
-- where barcode (GTIN/EAN) identifies the Medicine product,
-- not individual stock units.
--
-- Date: 2024
-- Purpose: Fix barcode domain modeling issue
-- ============================================

-- Step 1: Add barcode column to medicines table (if not exists)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'medicines' AND column_name = 'barcode'
    ) THEN
        ALTER TABLE medicines ADD COLUMN barcode VARCHAR(50);
        RAISE NOTICE 'Added barcode column to medicines table';
    ELSE
        RAISE NOTICE 'Barcode column already exists in medicines table';
    END IF;
END $$;

-- Step 2: Create index on barcode for fast lookups
CREATE INDEX IF NOT EXISTS idx_medicine_barcode ON medicines(barcode);

-- Step 3: Migrate existing data (if any)
-- If you have existing barcodes in stock_barcodes table, 
-- extract unique barcodes per medicine and update medicines table
-- 
-- NOTE: This assumes that barcodes in stock_barcodes are actually
-- product barcodes (GTIN/EAN), not serial numbers.
-- If they are serial numbers, skip this step.
DO $$
DECLARE
    medicine_record RECORD;
    barcode_value VARCHAR(50);
BEGIN
    -- For each medicine, find the most common barcode from its batches
    FOR medicine_record IN 
        SELECT DISTINCT m.id, m.name
        FROM medicines m
        INNER JOIN batches b ON b.medicine_id = m.id
        INNER JOIN stock_barcodes sb ON sb.batch_id = b.id
        WHERE m.barcode IS NULL OR m.barcode = ''
    LOOP
        -- Get the most frequently occurring barcode for this medicine
        SELECT sb.barcode INTO barcode_value
        FROM stock_barcodes sb
        INNER JOIN batches b ON b.id = sb.batch_id
        WHERE b.medicine_id = medicine_record.id
        GROUP BY sb.barcode
        ORDER BY COUNT(*) DESC
        LIMIT 1;
        
        -- Update medicine with the barcode
        IF barcode_value IS NOT NULL THEN
            UPDATE medicines 
            SET barcode = barcode_value 
            WHERE id = medicine_record.id;
            
            RAISE NOTICE 'Updated medicine % (ID: %) with barcode: %', 
                medicine_record.name, medicine_record.id, barcode_value;
        END IF;
    END LOOP;
END $$;

-- Step 4: Remove unique constraint from stock_barcodes.barcode if it exists
-- (This allows same serial number across batches if needed)
-- NOTE: Only do this if stock_barcodes contains serial numbers, not product barcodes
-- If stock_barcodes.barcode contains product barcodes (GTIN/EAN), 
-- you may want to keep the constraint or remove it based on your needs.

-- Uncomment the following if you want to remove unique constraint:
-- ALTER TABLE stock_barcodes DROP CONSTRAINT IF EXISTS idx_barcode_unique;
-- DROP INDEX IF EXISTS idx_barcode_unique;

-- Step 5: Verify the migration
SELECT 
    'Migration Summary' as info,
    COUNT(*) as total_medicines,
    COUNT(barcode) as medicines_with_barcode,
    COUNT(*) - COUNT(barcode) as medicines_without_barcode
FROM medicines;

-- Show sample of medicines with barcodes
SELECT 
    id,
    name,
    barcode,
    hsn_code
FROM medicines
WHERE barcode IS NOT NULL
LIMIT 10;

-- ============================================
-- MIGRATION COMPLETE
-- ============================================
-- After running this script:
-- 1. Restart your Spring Boot application
-- 2. Hibernate will detect the new column structure
-- 3. The application will use the corrected domain model
-- ============================================

