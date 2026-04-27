-- ============================================================
--  NutriLife DB — Full Schema
--  Run this in phpMyAdmin on the nutrilife_db database
-- ============================================================

USE nutrilife_db;

-- ── user (required as FK for nutrition_objective) ──────────
CREATE TABLE IF NOT EXISTS `user` (
    `id`               INT          NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(100) NOT NULL DEFAULT 'Default User',
    `email`            VARCHAR(150) NOT NULL DEFAULT 'user@nutrilife.com',
    `password`         VARCHAR(255) NOT NULL DEFAULT '',
    `roles`            VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER',
    `is_active`        TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `first_name`       VARCHAR(100) NOT NULL DEFAULT '',
    `last_name`        VARCHAR(100) NOT NULL DEFAULT '',
    `birthday`         DATE         NOT NULL DEFAULT '2000-01-01',
    `weight`           FLOAT                 DEFAULT NULL,
    `height`           FLOAT                 DEFAULT NULL,
    `phone_number`     VARCHAR(20)           DEFAULT NULL,
    `phone_verified`   TINYINT(1)   NOT NULL DEFAULT 0,
    `photo_filename`   VARCHAR(255)          DEFAULT NULL,
    `welcome_message`  TEXT                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert a default admin user
INSERT IGNORE INTO `user` (`id`, `name`, `email`, `roles`, `is_active`, `first_name`, `last_name`)
VALUES (1, 'Admin User', 'admin@nutrilife.com', 'ROLE_ADMIN', 1, 'Admin', 'User');

-- ── ingredient ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ingredient` (
    `id`               INT            NOT NULL AUTO_INCREMENT,
    `nom`              VARCHAR(150)   NOT NULL,
    `nom_en`           VARCHAR(150)            DEFAULT NULL,
    `categorie`        VARCHAR(100)            DEFAULT NULL,
    `quantite`         DOUBLE         NOT NULL DEFAULT 0,
    `unite`            VARCHAR(50)             DEFAULT NULL,
    `date_peremption`  DATE                    DEFAULT NULL,
    `notes`            TEXT                    DEFAULT NULL,
    `image`            VARCHAR(255)            DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── nutrition_objective ────────────────────────────────────
CREATE TABLE IF NOT EXISTS `nutrition_objective` (
    `id`                  INT            NOT NULL AUTO_INCREMENT,
    `user_id`             INT            NOT NULL,
    `title`               VARCHAR(255)   NOT NULL,
    `description`         TEXT                    DEFAULT NULL,
    `goal_type`           VARCHAR(50)             DEFAULT NULL,
    `plan_level`          VARCHAR(50)             DEFAULT NULL,
    `target_calories`     INT            NOT NULL DEFAULT 2000,
    `target_protein`      DOUBLE         NOT NULL DEFAULT 0,
    `target_carbs`        DOUBLE         NOT NULL DEFAULT 0,
    `target_fats`         DOUBLE         NOT NULL DEFAULT 0,
    `target_water`        DOUBLE         NOT NULL DEFAULT 0,
    `status`              VARCHAR(20)    NOT NULL DEFAULT 'pending',
    `planned_start_date`  DATE                    DEFAULT NULL,
    `start_date`          DATETIME                DEFAULT NULL,
    `end_date`            DATETIME                DEFAULT NULL,
    `auto_activate`       TINYINT(1)     NOT NULL DEFAULT 0,
    `created_at`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_objective_user` (`user_id`),
    CONSTRAINT `fk_objective_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── daily_log ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `daily_log` (
    `id`                       INT        NOT NULL AUTO_INCREMENT,
    `nutrition_objective_id`   INT        NOT NULL,
    `day_number`               INT        NOT NULL DEFAULT 1,
    `date`                     DATE                DEFAULT NULL,
    `completed`                TINYINT(1) NOT NULL DEFAULT 0,
    `calories_consumed`        INT                 DEFAULT NULL,
    `protein_consumed`         DOUBLE              DEFAULT NULL,
    `carbs_consumed`           DOUBLE              DEFAULT NULL,
    `fats_consumed`            DOUBLE              DEFAULT NULL,
    `water_consumed`           DOUBLE              DEFAULT NULL,
    `mood`                     VARCHAR(50)         DEFAULT NULL,
    `notes`                    TEXT                DEFAULT NULL,
    `meals`                    LONGTEXT            DEFAULT NULL,
    `selected_foods`           LONGTEXT            DEFAULT NULL,
    `custom_foods`             LONGTEXT            DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_log_objective` (`nutrition_objective_id`),
    CONSTRAINT `fk_log_objective` FOREIGN KEY (`nutrition_objective_id`)
        REFERENCES `nutrition_objective` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
