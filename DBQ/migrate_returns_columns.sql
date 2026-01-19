-- Migration script to rename returns table columns to match Hibernate naming strategy
-- This fixes the issue where Hibernate generates snake_case column names but database has camelCase

-- Rename originalBill_id to original_bill_id
ALTER TABLE returns 
RENAME COLUMN "originalBill_id" TO original_bill_id;

-- Rename processedBy_id to processed_by_id  
ALTER TABLE returns 
RENAME COLUMN "processedBy_id" TO processed_by_id;

-- Update the foreign key constraint names if needed (optional)
-- The constraints should still work, but you can rename them for consistency:
-- ALTER TABLE returns RENAME CONSTRAINT fk_return_bill TO fk_return_bill;
-- ALTER TABLE returns RENAME CONSTRAINT fk_return_user TO fk_return_user;

-- Update the index name to match the new column name
DROP INDEX IF EXISTS idx_return_bill;
CREATE INDEX IF NOT EXISTS idx_return_bill ON returns(original_bill_id);

