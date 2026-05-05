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
