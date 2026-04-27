-- ============================================================================
-- INSERTION DIRECTE DES UTILISATEURS - COPIER/COLLER DANS PHPMYADMIN
-- ============================================================================
-- Instructions:
-- 1. Ouvrir http://localhost/phpmyadmin
-- 2. Cliquer sur "nutrilife" à gauche
-- 3. Cliquer sur l'onglet "SQL"
-- 4. Copier TOUT ce fichier et coller dans la zone SQL
-- 5. Cliquer "Exécuter"
-- ============================================================================

USE nutrilife;

-- Supprimer les utilisateurs de test s'ils existent déjà (pour éviter les doublons)
DELETE FROM user WHERE email IN (
    'john.doe@nutrilife.com',
    'jane.smith@nutrilife.com',
    'bob.johnson@nutrilife.com',
    'alice.williams@nutrilife.com',
    'charlie.brown@nutrilife.com'
);

-- Insérer 5 utilisateurs de test avec ROLE_USER
INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at, birthday, weight, height)
VALUES 
('john.doe@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 'John', 'Doe', 1, NOW(), '1990-05-15', 75.5, 175.0),
('jane.smith@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 'Jane', 'Smith', 1, NOW(), '1988-08-22', 62.0, 165.0),
('bob.johnson@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 'Bob', 'Johnson', 1, NOW(), '1992-03-10', 82.0, 180.0),
('alice.williams@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 'Alice', 'Williams', 1, NOW(), '1995-11-30', 58.5, 160.0),
('charlie.brown@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 'Charlie', 'Brown', 1, NOW(), '1987-07-18', 78.0, 178.0);

-- Vérifier que les utilisateurs ont été ajoutés
SELECT 'SUCCESS: 5 users added!' as Status;
SELECT id, email, roles, first_name, last_name, is_active FROM user WHERE roles = 'ROLE_USER';
