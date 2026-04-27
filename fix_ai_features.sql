-- Fix AI Features for Complaint System
-- This script ensures all necessary columns exist for AI features to work

USE nutrilife_db;

-- ============================================
-- STEP 1: Fix the 'rate' column (if corrupted)
-- ============================================
SET @ate_exists = (SELECT COUNT(*) 
                   FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = 'nutrilife_db' 
                   AND TABLE_NAME = 'complaint' 
                   AND COLUMN_NAME = 'ate');

SET @sql1 = IF(@ate_exists > 0,
    'ALTER TABLE complaint CHANGE COLUMN `ate` `rate` INT DEFAULT 0',
    'SELECT "Rate column already correct" AS message');

PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- ============================================
-- STEP 2: Add emotion analysis columns
-- ============================================

-- Add detected_emotion column
SET @emotion_exists = (SELECT COUNT(*) 
                       FROM information_schema.COLUMNS 
                       WHERE TABLE_SCHEMA = 'nutrilife_db' 
                       AND TABLE_NAME = 'complaint' 
                       AND COLUMN_NAME = 'detected_emotion');

SET @sql2 = IF(@emotion_exists = 0,
    'ALTER TABLE complaint ADD COLUMN `detected_emotion` VARCHAR(50) DEFAULT ''NEUTRAL'' AFTER `incident_date`',
    'SELECT "detected_emotion column already exists" AS message');

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Add emotion_score column
SET @score_exists = (SELECT COUNT(*) 
                     FROM information_schema.COLUMNS 
                     WHERE TABLE_SCHEMA = 'nutrilife_db' 
                     AND TABLE_NAME = 'complaint' 
                     AND COLUMN_NAME = 'emotion_score');

SET @sql3 = IF(@score_exists = 0,
    'ALTER TABLE complaint ADD COLUMN `emotion_score` DOUBLE DEFAULT 0 AFTER `detected_emotion`',
    'SELECT "emotion_score column already exists" AS message');

PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Add urgency_level column
SET @urgency_exists = (SELECT COUNT(*) 
                       FROM information_schema.COLUMNS 
                       WHERE TABLE_SCHEMA = 'nutrilife_db' 
                       AND TABLE_NAME = 'complaint' 
                       AND COLUMN_NAME = 'urgency_level');

SET @sql4 = IF(@urgency_exists = 0,
    'ALTER TABLE complaint ADD COLUMN `urgency_level` INT DEFAULT 1 AFTER `emotion_score`',
    'SELECT "urgency_level column already exists" AS message');

PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Add emotion_recommendation column
SET @recommendation_exists = (SELECT COUNT(*) 
                              FROM information_schema.COLUMNS 
                              WHERE TABLE_SCHEMA = 'nutrilife_db' 
                              AND TABLE_NAME = 'complaint' 
                              AND COLUMN_NAME = 'emotion_recommendation');

SET @sql5 = IF(@recommendation_exists = 0,
    'ALTER TABLE complaint ADD COLUMN `emotion_recommendation` TEXT AFTER `urgency_level`',
    'SELECT "emotion_recommendation column already exists" AS message');

PREPARE stmt5 FROM @sql5;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

-- ============================================
-- STEP 3: Verify the schema
-- ============================================
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_DEFAULT,
    IS_NULLABLE
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'nutrilife_db' 
AND TABLE_NAME = 'complaint' 
AND COLUMN_NAME IN ('rate', 'detected_emotion', 'emotion_score', 'urgency_level', 'emotion_recommendation')
ORDER BY ORDINAL_POSITION;

-- ============================================
-- STEP 4: Update existing complaints with default values
-- ============================================
UPDATE complaint 
SET 
    detected_emotion = COALESCE(detected_emotion, 'NEUTRAL'),
    emotion_score = COALESCE(emotion_score, 0),
    urgency_level = COALESCE(urgency_level, 1)
WHERE detected_emotion IS NULL OR emotion_score IS NULL OR urgency_level IS NULL;

SELECT 'AI Features database schema fixed successfully!' AS status;
SELECT COUNT(*) AS total_complaints FROM complaint;
SELECT COUNT(*) AS analyzed_complaints FROM complaint WHERE urgency_level > 1 OR emotion_score > 0;
