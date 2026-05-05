-- ============================================================
-- Blog Tables Migration
-- Run this script in your nutrilife_db database
-- to enable all blog/publication features
-- ============================================================

-- Publication Table
CREATE TABLE IF NOT EXISTS `publication` (
    `id`             INT AUTO_INCREMENT PRIMARY KEY,
    `titre`          VARCHAR(150)  NOT NULL,
    `contenu`        TEXT          NOT NULL,
    `description`    TEXT          DEFAULT NULL,
    `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `author_name`    VARCHAR(150)  DEFAULT NULL,
    `author_avatar`  VARCHAR(255)  DEFAULT NULL,
    `is_admin`       TINYINT(1)    NOT NULL DEFAULT 0,
    `image`          VARCHAR(255)  DEFAULT NULL,
    `view_count`     INT           NOT NULL DEFAULT 0,
    `share_count`    INT           NOT NULL DEFAULT 0,
    `visibility`     VARCHAR(20)   NOT NULL DEFAULT 'public',
    `scheduled_at`   DATETIME      DEFAULT NULL,
    `shared_from_id` INT           DEFAULT NULL,
    `user_id`        INT           NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Publication Comment Table
CREATE TABLE IF NOT EXISTS `publication_comment` (
    `id`            INT AUTO_INCREMENT PRIMARY KEY,
    `publication_id` INT          NOT NULL,
    `user_id`       INT           NOT NULL,
    `contenu`       TEXT          NOT NULL,
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `author_name`   VARCHAR(150)  DEFAULT NULL,
    `author_avatar` VARCHAR(255)  DEFAULT NULL,
    `is_admin`      TINYINT(1)    NOT NULL DEFAULT 0,
    FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Publication Like/Dislike Table
CREATE TABLE IF NOT EXISTS `publication_like` (
    `id`             INT AUTO_INCREMENT PRIMARY KEY,
    `publication_id` INT          NOT NULL,
    `user_id`        INT          NOT NULL,
    `is_like`        TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `unique_user_pub` (`publication_id`, `user_id`),
    FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Publication Report Table (Signalement)
CREATE TABLE IF NOT EXISTS `publication_report` (
    `id`             INT AUTO_INCREMENT PRIMARY KEY,
    `publication_id` INT          NOT NULL,
    `user_id`        INT          NOT NULL,
    `reason`         VARCHAR(255) NOT NULL DEFAULT 'Contenu inapproprie',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `unique_user_report` (`publication_id`, `user_id`),
    FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
