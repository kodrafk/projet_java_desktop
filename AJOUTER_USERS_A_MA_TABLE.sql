-- ═══════════════════════════════════════════════════════════════════════════
-- Ajouter des utilisateurs à VOTRE table user existante
-- Ce script utilise votre base de données et votre table actuelles
-- Il AJOUTE seulement des utilisateurs, ne supprime rien
-- ═══════════════════════════════════════════════════════════════════════════

-- Vérifier d'abord ce que vous avez
SELECT '=== VOTRE TABLE USER ACTUELLE ===' AS info;
SELECT id, email, roles, first_name, last_name, is_active FROM user;

SELECT '=== STATISTIQUES ===' AS info;
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN roles = 'ROLE_ADMIN' OR roles LIKE '%ADMIN%' THEN 1 ELSE 0 END) as admins,
    SUM(CASE WHEN roles = 'ROLE_USER' OR roles NOT LIKE '%ADMIN%' THEN 1 ELSE 0 END) as regular_users
FROM user;

-- Ajouter 5 utilisateurs normaux (seulement s'ils n'existent pas déjà)
SELECT '=== AJOUT DE 5 UTILISATEURS ===' AS info;

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at)
SELECT 
    'john.doe@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    'John',
    'Doe',
    1,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'john.doe@nutrilife.com');

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at)
SELECT 
    'jane.smith@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    'Jane',
    'Smith',
    1,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'jane.smith@nutrilife.com');

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at)
SELECT 
    'bob.johnson@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    'Bob',
    'Johnson',
    1,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'bob.johnson@nutrilife.com');

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at)
SELECT 
    'alice.williams@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    'Alice',
    'Williams',
    1,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'alice.williams@nutrilife.com');

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at)
SELECT 
    'charlie.brown@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    'Charlie',
    'Brown',
    1,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'charlie.brown@nutrilife.com');

-- Vérifier le résultat
SELECT '=== APRÈS AJOUT ===' AS info;
SELECT id, email, roles, first_name, last_name, is_active FROM user ORDER BY id;

SELECT '=== NOUVELLES STATISTIQUES ===' AS info;
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN roles = 'ROLE_ADMIN' OR roles LIKE '%ADMIN%' THEN 1 ELSE 0 END) as admins,
    SUM(CASE WHEN roles = 'ROLE_USER' OR roles NOT LIKE '%ADMIN%' THEN 1 ELSE 0 END) as regular_users
FROM user;

-- ═══════════════════════════════════════════════════════════════════════════
-- RÉSUMÉ
-- ═══════════════════════════════════════════════════════════════════════════
-- Ce script a ajouté 5 utilisateurs à votre table user existante :
-- - john.doe@nutrilife.com (password: password123)
-- - jane.smith@nutrilife.com (password: password123)
-- - bob.johnson@nutrilife.com (password: password123)
-- - alice.williams@nutrilife.com (password: password123)
-- - charlie.brown@nutrilife.com (password: password123)
--
-- Vos données existantes n'ont PAS été modifiées.
-- Seuls ces 5 nouveaux utilisateurs ont été ajoutés.
-- ═══════════════════════════════════════════════════════════════════════════
