-- ============================================
-- Migration: Separate admin_response into its own table
-- Run this on your existing nutrilife_db database
-- ============================================

-- Step 1: Create the new complaint_response table
CREATE TABLE IF NOT EXISTS `complaint_response` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `complaint_id`     INT NOT NULL,
    `response_content` TEXT NOT NULL,
    `response_date`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`complaint_id`) REFERENCES `complaint`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Migrate existing responses (if any) from complaint to complaint_response
INSERT INTO `complaint_response` (`complaint_id`, `response_content`, `response_date`)
SELECT `id`, `admin_response`, NOW()
FROM `complaint`
WHERE `admin_response` IS NOT NULL AND `admin_response` != '';

-- Step 3: Drop the old column from complaint
ALTER TABLE `complaint` DROP COLUMN `admin_response`;
