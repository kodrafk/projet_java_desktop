-- NutriLife DB schema — run once in nutrilife_db
CREATE TABLE IF NOT EXISTS `user` (
    `id`                           INT AUTO_INCREMENT PRIMARY KEY,
    `email`                        VARCHAR(180)  NOT NULL UNIQUE,
    `password`                     VARCHAR(255)  NOT NULL,
    `roles`                        VARCHAR(50)   NOT NULL DEFAULT 'ROLE_USER',
    `is_active`                    TINYINT(1)    NOT NULL DEFAULT 1,
    `created_at`                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `reset_token`                  VARCHAR(255)  DEFAULT NULL,
    `reset_token_expires_at`       DATETIME      DEFAULT NULL,
    `verification_code`            VARCHAR(6)    DEFAULT NULL,
    `verification_code_expires_at` DATETIME      DEFAULT NULL,
    `face_descriptor`              TEXT          DEFAULT NULL,
    `face_id_enrolled_at`          DATETIME      DEFAULT NULL,
    `welcome_message`              TEXT          DEFAULT NULL,
    `google_id`                    VARCHAR(255)  DEFAULT NULL,
    `photo_filename`               VARCHAR(255)  DEFAULT NULL,
    `first_name`                   VARCHAR(100)  NOT NULL,
    `last_name`                    VARCHAR(100)  NOT NULL,
    `birthday`                     DATE          NOT NULL,
    `weight`                       DOUBLE        NOT NULL DEFAULT 70,
    `height`                       DOUBLE        NOT NULL DEFAULT 170
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Badge tables
CREATE TABLE IF NOT EXISTS `badge` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `name`             VARCHAR(100) NOT NULL,
    `description`      TEXT,
    `condition_type`   VARCHAR(50),
    `condition_value`  INT DEFAULT 0,
    `icon`             VARCHAR(10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_badge` (
    `id`            INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`       INT NOT NULL,
    `badge_id`      INT NOT NULL,
    `unlocked`      TINYINT(1) NOT NULL DEFAULT 0,
    `unlocked_at`   DATETIME DEFAULT NULL,
    `current_value` INT NOT NULL DEFAULT 0,
    `is_vitrine`    TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`badge_id`) REFERENCES `badge`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
