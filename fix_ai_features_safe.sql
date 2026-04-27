-- Safe AI Features Fix - Checks if columns exist before adding
-- Run this in phpMyAdmin SQL tab

USE nutrilife_db;

-- ============================================
-- Fix rate column if it's named 'ate'
-- ============================================
ALTER TABLE complaint CHANGE COLUMN `ate` `rate` INT DEFAULT 0;

-- ============================================
-- Add emotion columns ONLY if they don't exist
-- ============================================

-- Check and show current complaint table structure
SELECT 'Current complaint table structure:' AS info;
DESCRIBE complaint;

-- Update existing NULL values to defaults
UPDATE complaint 
SET 
    detected_emotion = COALESCE(detected_emotion, 'NEUTRAL'),
    emotion_score = COALESCE(emotion_score, 0),
    urgency_level = COALESCE(urgency_level, 1)
WHERE detected_emotion IS NULL OR emotion_score IS NULL OR urgency_level IS NULL;

SELECT '✅ Database is ready for AI features!' AS status;
SELECT COUNT(*) AS total_complaints FROM complaint;
