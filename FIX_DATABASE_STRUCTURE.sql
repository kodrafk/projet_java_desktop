-- ============================================================================
-- Script pour corriger la structure de la base de données
-- ============================================================================

USE nutrilife_db;

-- ============================================================================
-- 1. CORRIGER LA TABLE weight_log
-- ============================================================================

-- Supprimer l'ancienne table si elle existe
DROP TABLE IF EXISTS weight_log;

-- Créer la nouvelle table avec la bonne structure
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
-- 2. CORRIGER LA TABLE weight_objective
-- ============================================================================

-- Supprimer l'ancienne table si elle existe
DROP TABLE IF EXISTS weight_objective;

-- Créer la nouvelle table avec la bonne structure
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
-- 3. CRÉER LA TABLE progress_photo
-- ============================================================================

CREATE TABLE IF NOT EXISTS progress_photo (
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
-- 4. CRÉER LA TABLE message
-- ============================================================================

CREATE TABLE IF NOT EXISTS message (
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
-- 4. VÉRIFIER LES TABLES
-- ============================================================================

SHOW TABLES;

SELECT '✅ Structure de la base de données corrigée !' AS Status;

-- ============================================================================
-- RÉSUMÉ DES MODIFICATIONS
-- ============================================================================
-- 
-- weight_log:
--   - Ajouté: photo (VARCHAR 255)
--   - Ajouté: note (TEXT)
--   - Ajouté: logged_at (TIMESTAMP)
--   - Supprimé: log_date, notes
-- 
-- weight_objective:
--   - Ajouté: start_weight (DECIMAL 5,2)
--   - Ajouté: start_photo (VARCHAR 255)
--   - Ajouté: is_active (TINYINT 1)
--   - Modifié: status → is_active
-- 
-- progress_photo:
--   - Nouvelle table créée
-- 
-- message:
--   - Nouvelle table créée pour le chat admin-user
-- 
-- ============================================================================
