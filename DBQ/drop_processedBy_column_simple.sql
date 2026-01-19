-- Simple command to drop duplicate processedBy_id column
ALTER TABLE returns DROP COLUMN IF EXISTS "processedBy_id";

