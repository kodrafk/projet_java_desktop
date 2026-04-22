-- Run this in your nutrilife_db database before launching the app
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

-- Complaint Table
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

-- Complaint Response Table
CREATE TABLE IF NOT EXISTS `complaint_response` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `complaint_id`     INT NOT NULL,
    `response_content` TEXT NOT NULL,
    `response_date`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`complaint_id`) REFERENCES `complaint`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
