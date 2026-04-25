USE nutrilife_db;

CREATE TABLE IF NOT EXISTS `nutrition_objective` (
  `id`                  INT AUTO_INCREMENT PRIMARY KEY,
  `title`               VARCHAR(255) NOT NULL,
  `description`         TEXT DEFAULT NULL,
  `goal_type`           VARCHAR(50) DEFAULT 'custom',
  `plan_level`          VARCHAR(50) DEFAULT NULL,
  `target_calories`     INT NOT NULL DEFAULT 2000,
  `target_protein`      DOUBLE NOT NULL DEFAULT 150,
  `target_carbs`        DOUBLE NOT NULL DEFAULT 200,
  `target_fats`         DOUBLE NOT NULL DEFAULT 65,
  `target_water`        DOUBLE NOT NULL DEFAULT 2.5,
  `status`              VARCHAR(20) NOT NULL DEFAULT 'pending',
  `planned_start_date`  DATE DEFAULT NULL,
  `start_date`          DATETIME DEFAULT NULL,
  `end_date`            DATETIME DEFAULT NULL,
  `auto_activate`       TINYINT(1) NOT NULL DEFAULT 0,
  `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id`             INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `daily_log` (
  `id`                      INT AUTO_INCREMENT PRIMARY KEY,
  `nutrition_objective_id`  INT NOT NULL,
  `day_number`              INT NOT NULL DEFAULT 1,
  `date`                    DATE DEFAULT NULL,
  `completed`               TINYINT(1) NOT NULL DEFAULT 0,
  `calories_consumed`       INT DEFAULT NULL,
  `protein_consumed`        DOUBLE DEFAULT NULL,
  `carbs_consumed`          DOUBLE DEFAULT NULL,
  `fats_consumed`           DOUBLE DEFAULT NULL,
  `water_consumed`          DOUBLE DEFAULT NULL,
  `mood`                    VARCHAR(50) DEFAULT NULL,
  `notes`                   TEXT DEFAULT NULL,
  `meals`                   TEXT DEFAULT NULL,
  `selected_foods`          TEXT DEFAULT NULL,
  `custom_foods`            TEXT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ingredient` (
  `id`       INT AUTO_INCREMENT PRIMARY KEY,
  `name`     VARCHAR(255) NOT NULL,
  `calories` DOUBLE DEFAULT 0,
  `protein`  DOUBLE DEFAULT 0,
  `carbs`    DOUBLE DEFAULT 0,
  `fats`     DOUBLE DEFAULT 0,
  `unit`     VARCHAR(50) DEFAULT 'g'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_badge` (
  `id` int(11) AUTO_INCREMENT PRIMARY KEY,
  `unlocked` tinyint(4) NOT NULL DEFAULT 0,
  `unlocked_at` datetime DEFAULT NULL,
  `current_value` int(11) NOT NULL DEFAULT 0,
  `is_vitrine` tinyint(4) NOT NULL DEFAULT 0,
  `user_id` int(11) NOT NULL,
  `badge_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `complaint` (
  `id` int(11) AUTO_INCREMENT PRIMARY KEY,
  `user_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `rate` int(11) DEFAULT 0,
  `date_of_complaint` datetime DEFAULT CURRENT_TIMESTAMP,
  `incident_date` date DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'PENDING',
  `emotion_tone` varchar(50) DEFAULT NULL,
  `image_path` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT 'Tables recreated successfully' AS status;
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'nutrilife_db';
