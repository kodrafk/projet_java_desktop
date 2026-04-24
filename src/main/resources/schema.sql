-- NutriLife DB — Full Integrated Schema
-- Contains User Management, Complaints, and Nutrition/Recipe features

-- 1. USER TABLE
CREATE TABLE IF NOT EXISTS `user` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `email`            VARCHAR(180)  NOT NULL UNIQUE,
    `password`         VARCHAR(255)  NOT NULL,
    `roles`            VARCHAR(50)   NOT NULL DEFAULT 'ROLE_USER',
    `is_active`        TINYINT(1)    NOT NULL DEFAULT 1,
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `first_name`       VARCHAR(100)  NOT NULL,
    `last_name`        VARCHAR(100)  NOT NULL,
    `birthday`         DATE          NOT NULL,
    `weight`           FLOAT         DEFAULT NULL,
    `height`           FLOAT         DEFAULT NULL,
    `phone_number`     VARCHAR(20)   DEFAULT NULL,
    `phone_verified`   TINYINT(1)    NOT NULL DEFAULT 0,
    `photo_filename`   VARCHAR(255)  DEFAULT NULL,
    `welcome_message`  TEXT          DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. COMPLAINT TABLES
CREATE TABLE IF NOT EXISTS `complaint` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`          INT NOT NULL,
    `title`            VARCHAR(150) NOT NULL,
    `description`      TEXT NOT NULL,
    `phone_number`     VARCHAR(20),
    `rate`             INT DEFAULT 0,
    `date_of_complaint` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`           VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `image_path`       VARCHAR(255),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `complaint_response` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `complaint_id`     INT NOT NULL,
    `response_content` TEXT NOT NULL,
    `response_date`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`complaint_id`) REFERENCES `complaint`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. NUTRITION & RECIPE TABLES (from Friends)
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

CREATE TABLE IF NOT EXISTS `recette` (
    `id`               INT            NOT NULL AUTO_INCREMENT,
    `nom`              VARCHAR(150)   NOT NULL,
    `description`      TEXT,
    `temps_preparation` INT,
    `difficulte`       VARCHAR(50),
    `image`            VARCHAR(255),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recette_ingredient` (
    `recette_id`       INT NOT NULL,
    `ingredient_id`    INT NOT NULL,
    `quantite`         DOUBLE NOT NULL,
    PRIMARY KEY (`recette_id`, `ingredient_id`),
    FOREIGN KEY (`recette_id`) REFERENCES `recette`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    PRIMARY KEY (`id`),
    FOREIGN KEY (`nutrition_objective_id`) REFERENCES `nutrition_objective` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
