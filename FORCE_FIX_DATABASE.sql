-- ============================================================================
-- CORRECTION FORCÉE DE LA BASE DE DONNÉES
-- ============================================================================

USE nutrilife_db;

-- Désactiver les contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer complètement les tables problématiques
DROP TABLE IF EXISTS weight_log;
DROP TABLE IF EXISTS weight_objective;
DROP TABLE IF EXISTS progress_photo;
DROP TABLE IF EXISTS message;

-- Réactiver les contraintes
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- RECRÉER weight_log AVEC LA BONNE STRUCTURE
-- ============================================================================

CREATE TABLE weight_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    photo VARCHAR(255),
    note TEXT,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_logged (user_id, logged_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- RECRÉER weight_objective AVEC LA BONNE STRUCTURE
-- ============================================================================

CREATE TABLE weight_objective (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    start_weight DECIMAL(5,2) NOT NULL,
    target_weight DECIMAL(5,2) NOT NULL,
    start_date DATE NOT NULL,
    target_date DATE NOT NULL,
    start_photo VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_active (user_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- CRÉER progress_photo
-- ============================================================================

CREATE TABLE progress_photo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    caption TEXT,
    weight DECIMAL(5,2),
    taken_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_taken (user_id, taken_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- CRÉER message
-- ============================================================================

CREATE TABLE message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read TINYINT(1) DEFAULT 0,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_conversation (sender_id, receiver_id, sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- VÉRIFICATION
-- ============================================================================

SHOW TABLES;

SELECT '✅ Base de données corrigée avec succès!' AS Status;

-- ============================================================================
-- RÉSUMÉ
-- ============================================================================
-- 
-- Tables créées:
--   ✅ weight_log (avec photo, note, logged_at)
--   ✅ weight_objective (avec start_weight, start_photo, is_active)
--   ✅ progress_photo (nouvelle)
--   ✅ message (nouvelle)
-- 
-- Vous pouvez maintenant:
--   1. Relancer l'application
--   2. Ajouter des données dans le front office
--   3. Les voir dans le back office admin
-- 
-- ============================================================================
