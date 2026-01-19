-- ============================================
-- CREATE ALL TABLES - FRESH SCHEMA
-- ============================================
-- This script creates all tables for the Medical Store POS system
-- with the corrected domain model (barcode in medicines table)
-- ============================================

-- ============================================
-- 1. USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_email ON users(email);

-- ============================================
-- 2. MEDICINES TABLE (with barcode column)
-- ============================================
CREATE TABLE IF NOT EXISTS medicines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    manufacturer VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    barcode VARCHAR(50), -- GTIN/EAN - identifies product, NOT unique per unit
    hsn_code VARCHAR(20) NOT NULL UNIQUE,
    gst_percentage NUMERIC(5,2) NOT NULL,
    prescription_required BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0 -- Optimistic locking
);

CREATE INDEX IF NOT EXISTS idx_medicine_name ON medicines(name);
CREATE INDEX IF NOT EXISTS idx_medicine_hsn ON medicines(hsn_code);
CREATE INDEX IF NOT EXISTS idx_medicine_status ON medicines(status);
CREATE INDEX IF NOT EXISTS idx_medicine_barcode ON medicines(barcode);

-- ============================================
-- 3. BATCHES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS batches (
    id BIGSERIAL PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    purchase_price NUMERIC(10,2) NOT NULL,
    selling_price NUMERIC(10,2) NOT NULL,
    quantity_available INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0 -- Optimistic locking
);

ALTER TABLE batches 
ADD CONSTRAINT fk_batch_medicine 
FOREIGN KEY (medicine_id) REFERENCES medicines(id);

CREATE INDEX IF NOT EXISTS idx_batch_medicine ON batches(medicine_id);
CREATE INDEX IF NOT EXISTS idx_batch_expiry ON batches(expiry_date);
CREATE INDEX IF NOT EXISTS idx_batch_number ON batches(batch_number);
CREATE INDEX IF NOT EXISTS idx_batch_medicine_expiry ON batches(medicine_id, expiry_date);

-- ============================================
-- 4. STOCK_BARCODES TABLE (for serial number tracking, optional)
-- ============================================
CREATE TABLE IF NOT EXISTS stock_barcodes (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    barcode VARCHAR(100) NOT NULL UNIQUE,
    sold BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

ALTER TABLE stock_barcodes 
ADD CONSTRAINT fk_barcode_batch 
FOREIGN KEY (batch_id) REFERENCES batches(id);

CREATE INDEX IF NOT EXISTS idx_barcode_unique ON stock_barcodes(barcode);
CREATE INDEX IF NOT EXISTS idx_barcode_batch ON stock_barcodes(batch_id);
CREATE INDEX IF NOT EXISTS idx_barcode_sold ON stock_barcodes(sold);

-- ============================================
-- 5. BILLS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    bill_date TIMESTAMP NOT NULL,
    cashier_id BIGINT NOT NULL,
    customer_name VARCHAR(200),
    customer_phone VARCHAR(20),
    subtotal NUMERIC(10,2) NOT NULL,
    total_gst NUMERIC(10,2) NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    cancelled BOOLEAN NOT NULL DEFAULT false,
    cancellation_reason TEXT
);

ALTER TABLE bills 
ADD CONSTRAINT fk_bill_cashier 
FOREIGN KEY (cashier_id) REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_bill_number ON bills(bill_number);
CREATE INDEX IF NOT EXISTS idx_bill_date ON bills(bill_date);
CREATE INDEX IF NOT EXISTS idx_bill_cashier ON bills(cashier_id);

-- ============================================
-- 6. BILL_ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS bill_items (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    gst_percentage NUMERIC(10,2) NOT NULL,
    gst_amount NUMERIC(10,2) NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL
);

ALTER TABLE bill_items 
ADD CONSTRAINT fk_bill_item_bill 
FOREIGN KEY (bill_id) REFERENCES bills(id);

ALTER TABLE bill_items 
ADD CONSTRAINT fk_bill_item_medicine 
FOREIGN KEY (medicine_id) REFERENCES medicines(id);

ALTER TABLE bill_items 
ADD CONSTRAINT fk_bill_item_batch 
FOREIGN KEY (batch_id) REFERENCES batches(id);

CREATE INDEX IF NOT EXISTS idx_bill_item_bill ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_item_batch ON bill_items(batch_id);

-- ============================================
-- 7. PAYMENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    mode VARCHAR(20) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE payments 
ADD CONSTRAINT fk_payment_bill 
FOREIGN KEY (bill_id) REFERENCES bills(id);

CREATE INDEX IF NOT EXISTS idx_payment_bill ON payments(bill_id);
CREATE INDEX IF NOT EXISTS idx_payment_reference ON payments(payment_reference);
CREATE INDEX IF NOT EXISTS idx_payment_date ON payments(payment_date);

-- ============================================
-- 8. RETURNS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    "originalBill_id" BIGINT NOT NULL,
    "processedBy_id" BIGINT NOT NULL,
    return_date TIMESTAMP NOT NULL,
    refund_amount NUMERIC(10,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    return_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE returns 
ADD CONSTRAINT fk_return_bill 
FOREIGN KEY ("originalBill_id") REFERENCES bills(id);

ALTER TABLE returns 
ADD CONSTRAINT fk_return_user 
FOREIGN KEY ("processedBy_id") REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_return_bill ON returns("originalBill_id");
CREATE INDEX IF NOT EXISTS idx_return_date ON returns(return_date);
CREATE INDEX IF NOT EXISTS idx_return_number ON returns(return_number);

-- ============================================
-- 9. RETURN_ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS return_items (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    refund_amount NUMERIC(10,2) NOT NULL
);

ALTER TABLE return_items 
ADD CONSTRAINT fk_return_item_return 
FOREIGN KEY (return_id) REFERENCES returns(id);

ALTER TABLE return_items 
ADD CONSTRAINT fk_return_item_medicine 
FOREIGN KEY (medicine_id) REFERENCES medicines(id);

ALTER TABLE return_items 
ADD CONSTRAINT fk_return_item_batch 
FOREIGN KEY (batch_id) REFERENCES batches(id);

CREATE INDEX IF NOT EXISTS idx_return_item_return ON return_items(return_id);
CREATE INDEX IF NOT EXISTS idx_return_item_batch ON return_items(batch_id);

-- ============================================
-- 10. AUDIT_LOGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    description TEXT,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL,
    ip_address VARCHAR(50)
);

ALTER TABLE audit_logs 
ADD CONSTRAINT fk_audit_user 
FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_logs(timestamp);

-- ============================================
-- VERIFICATION
-- ============================================
SELECT 
    'Schema Creation Complete' as status,
    COUNT(*) as total_tables
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_type = 'BASE TABLE';

-- List all created tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- ============================================
-- SCHEMA CREATION COMPLETE
-- ============================================
-- All tables have been created with:
-- - Correct foreign key relationships
-- - All indexes for performance
-- - Barcode column in medicines table (not unique)
-- - Optimistic locking (version columns)
-- ============================================

