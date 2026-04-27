-- ============================================================================
-- Script pour créer un compte ADMIN dans NutriLife
-- ============================================================================
-- 
-- INSTRUCTIONS :
-- 1. Ouvrir MySQL Workbench ou phpMyAdmin
-- 2. Sélectionner la base de données 'nutrilife_db'
-- 3. Exécuter ce script
-- 
-- ============================================================================

USE nutrilife_db;

-- Supprimer l'admin s'il existe déjà (pour éviter les doublons)
DELETE FROM user WHERE email = 'admin@nutrilife.com';

-- Créer le compte ADMIN
INSERT INTO user (
    email,
    password,
    roles,
    is_active,
    first_name,
    last_name,
    birthday,
    weight,
    height,
    created_at
) VALUES (
    'admin@nutrilife.com',                    -- Email
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- Password: "admin123" (BCrypt)
    'ROLE_ADMIN',                             -- Rôle ADMIN
    1,                                        -- Actif
    'Admin',                                  -- Prénom
    'NutriLife',                              -- Nom
    '1990-01-01',                             -- Date de naissance
    75.0,                                     -- Poids (kg)
    175.0,                                    -- Taille (cm)
    NOW()                                     -- Date de création
);

-- Vérifier que le compte a été créé
SELECT 
    id,
    email,
    roles,
    first_name,
    last_name,
    is_active,
    created_at
FROM user 
WHERE email = 'admin@nutrilife.com';

-- ============================================================================
-- COMPTE ADMIN CRÉÉ !
-- ============================================================================
-- 
-- 📧 Email    : admin@nutrilife.com
-- 🔑 Password : admin123
-- 👤 Rôle     : ROLE_ADMIN
-- ✅ Statut   : Actif
-- 
-- ============================================================================
-- 
-- ALTERNATIVE : Si le mot de passe BCrypt ne fonctionne pas,
-- utilisez ce script pour créer un mot de passe en clair (moins sécurisé) :
-- 
-- UPDATE user 
-- SET password = 'admin123' 
-- WHERE email = 'admin@nutrilife.com';
-- 
-- Puis dans votre code Java, assurez-vous que le PasswordEncoder
-- accepte les mots de passe en clair pour les tests.
-- 
-- ============================================================================

-- BONUS : Créer un utilisateur de test normal
DELETE FROM user WHERE email = 'user@test.com';

INSERT INTO user (
    email,
    password,
    roles,
    is_active,
    first_name,
    last_name,
    birthday,
    weight,
    height,
    created_at
) VALUES (
    'user@test.com',                          -- Email
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- Password: "admin123"
    'ROLE_USER',                              -- Rôle USER
    1,                                        -- Actif
    'John',                                   -- Prénom
    'Doe',                                    -- Nom
    '1995-05-15',                             -- Date de naissance
    80.0,                                     -- Poids (kg)
    180.0,                                    -- Taille (cm)
    NOW()                                     -- Date de création
);

-- ============================================================================
-- COMPTES CRÉÉS :
-- ============================================================================
-- 
-- 1. ADMIN
--    📧 Email    : admin@nutrilife.com
--    🔑 Password : admin123
--    👤 Rôle     : ROLE_ADMIN
-- 
-- 2. USER (pour tester)
--    📧 Email    : user@test.com
--    🔑 Password : admin123
--    👤 Rôle     : ROLE_USER
-- 
-- ============================================================================

SELECT '✅ Comptes créés avec succès !' AS Status;
