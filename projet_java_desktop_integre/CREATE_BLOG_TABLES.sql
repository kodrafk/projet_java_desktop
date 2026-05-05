-- ═══════════════════════════════════════════════════════════════════
-- SCRIPT DE CRÉATION DES TABLES POUR LE BLOG
-- ═══════════════════════════════════════════════════════════════════
-- Exécutez ce script dans votre base de données MySQL
-- ═══════════════════════════════════════════════════════════════════

-- Supprimer les tables si elles existent déjà (pour réinitialiser)
-- ATTENTION: Cela supprimera toutes les données existantes!
-- Commentez ces lignes si vous voulez conserver les données

DROP TABLE IF EXISTS publication_report;
DROP TABLE IF EXISTS publication_like;
DROP TABLE IF EXISTS publication_comment;
DROP TABLE IF EXISTS publication;

-- ═══════════════════════════════════════════════════════════════════
-- TABLE: publication
-- ═══════════════════════════════════════════════════════════════════
-- Stocke les publications du blog

CREATE TABLE publication (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(100) NOT NULL,
    contenu TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    author_name VARCHAR(100),
    author_avatar VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE,
    image VARCHAR(255),
    view_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    visibility VARCHAR(20) DEFAULT 'public',
    scheduled_at TIMESTAMP NULL,
    shared_from_id INT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════════
-- TABLE: publication_comment
-- ═══════════════════════════════════════════════════════════════════
-- Stocke les commentaires sur les publications

CREATE TABLE publication_comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    author_name VARCHAR(100),
    author_avatar VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════════
-- TABLE: publication_like
-- ═══════════════════════════════════════════════════════════════════
-- Stocke les likes et dislikes sur les publications

CREATE TABLE publication_like (
    id INT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    is_like BOOLEAN NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_publication (publication_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════════
-- TABLE: publication_report
-- ═══════════════════════════════════════════════════════════════════
-- Stocke les signalements de publications

CREATE TABLE publication_report (
    id INT AUTO_INCREMENT PRIMARY KEY,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_report (publication_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════════
-- VÉRIFICATION
-- ═══════════════════════════════════════════════════════════════════
-- Exécutez cette requête pour vérifier que les tables ont été créées

SHOW TABLES LIKE 'publication%';

-- Vous devriez voir 4 tables:
-- publication
-- publication_comment
-- publication_like
-- publication_report

-- ═══════════════════════════════════════════════════════════════════
-- DONNÉES DE TEST (OPTIONNEL)
-- ═══════════════════════════════════════════════════════════════════
-- Décommentez ces lignes pour insérer des données de test

/*
-- Insérer une publication de test (remplacez user_id par un ID valide)
INSERT INTO publication (titre, contenu, author_name, is_admin, user_id, visibility)
VALUES ('Bienvenue sur le blog!', 'Ceci est la première publication de test. #blog #test', 'Admin', TRUE, 1, 'public');

-- Insérer un commentaire de test
INSERT INTO publication_comment (contenu, author_name, is_admin, publication_id, user_id)
VALUES ('Super publication!', 'Utilisateur Test', FALSE, 1, 1);

-- Insérer un like de test
INSERT INTO publication_like (publication_id, user_id, is_like)
VALUES (1, 1, TRUE);
*/

-- ═══════════════════════════════════════════════════════════════════
-- FIN DU SCRIPT
-- ═══════════════════════════════════════════════════════════════════

SELECT 'Tables créées avec succès!' AS Status;
