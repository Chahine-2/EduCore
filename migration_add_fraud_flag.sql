s-- Migration script to add fraud detection support to existing EDUCORE databases
-- Run this script if you have an existing educore database

USE educore;

-- Add the fraude_detecte column to the resultat table if it doesn't exist
ALTER TABLE resultat
ADD COLUMN fraude_detecte BOOLEAN DEFAULT FALSE;

-- Verify the column was added
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'resultat' AND COLUMN_NAME = 'fraude_detecte';

