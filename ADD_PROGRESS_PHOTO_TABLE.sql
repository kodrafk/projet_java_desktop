-- ============================================================================
-- Ajouter la table progress_photo pour les photos de progression
-- ============================================================================

USE nutrilife_db;

-- Créer la table progress_photo si elle n'existe pas
CREATE TABLE IF NOT EXISTS progress_photo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    caption TEXT,
    weight DECIMAL(5,2),
    taken_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_taken_at (taken_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Vérifier que la table a été créée
SHOW TABLES LIKE 'progress_photo';

SELECT '✅ Table progress_photo créée avec succès !' AS Status;

-- ============================================================================
-- STRUCTURE DE LA TABLE
-- ============================================================================
-- 
-- id          : ID unique de la photo
-- user_id     : ID de l'utilisateur (lié à la table user)
-- filename    : Nom du fichier photo (stocké dans uploads/progress/)
-- caption     : Légende/description de la photo (optionnel)
-- weight      : Poids au moment de la photo (optionnel)
-- taken_at    : Date et heure de la photo
-- created_at  : Date de création de l'enregistrement
-- 
-- ============================================================================
