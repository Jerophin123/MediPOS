-- Update audit_logs_action_check constraint to include all action types
-- Run this script on your PostgreSQL database

-- First, drop the existing constraint
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_action_check;

-- Recreate the constraint with all action types
ALTER TABLE audit_logs ADD CONSTRAINT audit_logs_action_check 
CHECK (action IN (
    'BILL_CREATED',
    'BILL_CANCELLED',
    'PAYMENT_RECEIVED',
    'REFUND_PROCESSED',
    'STOCK_ADJUSTED',
    'STOCK_UPDATED',
    'PRICE_OVERRIDE',
    'MEDICINE_ADDED',
    'MEDICINE_UPDATED',
    'MEDICINE_DELETED',
    'BATCH_ADDED',
    'BATCH_UPDATED',
    'BATCH_DELETED',
    'USER_LOGIN',
    'USER_LOGOUT'
));



