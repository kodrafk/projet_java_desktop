-- Run this in your MySQL client to create the face recognition tables
-- USE nutrilife_db;

CREATE TABLE IF NOT EXISTS `face_embeddings` (
    `id`                   INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`              INT NOT NULL UNIQUE,
    `embedding_encrypted`  TEXT NOT NULL,
    `encryption_iv`        VARCHAR(255) NOT NULL,
    `encryption_tag`       VARCHAR(255) NOT NULL,
    `is_active`            TINYINT(1) NOT NULL DEFAULT 1,
    `consent_given_at`     DATETIME DEFAULT NULL,
    `consent_ip`           VARCHAR(45) DEFAULT NULL,
    `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_used_at`         DATETIME DEFAULT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `face_verification_attempts` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`          INT DEFAULT NULL,
    `email`            VARCHAR(255) DEFAULT NULL,
    `ip_address`       VARCHAR(45) NOT NULL DEFAULT '127.0.0.1',
    `success`          TINYINT(1) NOT NULL DEFAULT 0,
    `similarity_score` DOUBLE DEFAULT NULL,
    `attempted_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT 'Tables created successfully!' AS status;
