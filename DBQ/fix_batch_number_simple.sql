-- Simple command to rename batchnumber to batch_number in return_items table
ALTER TABLE return_items RENAME COLUMN "batchnumber" TO batch_number;

