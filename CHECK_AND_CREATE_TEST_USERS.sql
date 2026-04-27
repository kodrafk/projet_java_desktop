-- ═══════════════════════════════════════════════════════════════════════════
-- Script de vérification et création d'utilisateurs de test
-- ═══════════════════════════════════════════════════════════════════════════

-- Vérifier les utilisateurs existants
SELECT '=== EXISTING USERS ===' AS info;
SELECT id, email, roles, first_name, last_name, is_active, phone 
FROM user 
ORDER BY id;

-- Compter les utilisateurs par rôle
SELECT '=== USER COUNT BY ROLE ===' AS info;
SELECT 
    roles,
    COUNT(*) as count
FROM user
GROUP BY roles;

-- Créer des utilisateurs de test si nécessaire
-- (Seulement si moins de 3 utilisateurs non-admin existent)

SET @user_count = (SELECT COUNT(*) FROM user WHERE roles != 'ROLE_ADMIN');

SELECT CONCAT('Current non-admin users: ', @user_count) AS info;

-- Créer utilisateur 1
INSERT INTO user (email, password, roles, first_name, last_name, is_active, phone, created_at, weight, height, birthday)
SELECT 
    'john.doe@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password123
    'ROLE_USER',
    'John',
    'Doe',
    1,
    '+33612345678',
    NOW(),
    75.5,
    175,
    '1990-05-15'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'john.doe@nutrilife.com');

-- Créer utilisateur 2
INSERT INTO user (email, password, roles, first_name, last_name, is_active, phone, created_at, weight, height, birthday)
SELECT 
    'jane.smith@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password123
    'ROLE_USER',
    'Jane',
    'Smith',
    1,
    '+33698765432',
    NOW(),
    62.0,
    165,
    '1992-08-22'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'jane.smith@nutrilife.com');

-- Créer utilisateur 3
INSERT INTO user (email, password, roles, first_name, last_name, is_active, phone, created_at, weight, height, birthday)
SELECT 
    'bob.johnson@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password123
    'ROLE_USER',
    'Bob',
    'Johnson',
    1,
    NULL,
    NOW(),
    82.3,
    180,
    '1988-12-10'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'bob.johnson@nutrilife.com');

-- Créer utilisateur 4
INSERT INTO user (email, password, roles, first_name, last_name, is_active, phone, created_at, weight, height, birthday)
SELECT 
    'alice.williams@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password123
    'ROLE_USER',
    'Alice',
    'Williams',
    1,
    '+33687654321',
    NOW(),
    58.5,
    160,
    '1995-03-18'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'alice.williams@nutrilife.com');

-- Créer utilisateur 5
INSERT INTO user (email, password, roles, first_name, last_name, is_active, phone, created_at, weight, height, birthday)
SELECT 
    'charlie.brown@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password123
    'ROLE_USER',
    'Charlie',
    'Brown',
    1,
    '+33676543210',
    NOW(),
    70.0,
    172,
    '1991-07-25'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'charlie.brown@nutrilife.com');

-- Vérifier les utilisateurs après création
SELECT '=== USERS AFTER CREATION ===' AS info;
SELECT id, email, roles, first_name, last_name, is_active, phone 
FROM user 
ORDER BY id;

-- Résumé
SELECT '=== SUMMARY ===' AS info;
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN roles = 'ROLE_ADMIN' THEN 1 ELSE 0 END) as admins,
    SUM(CASE WHEN roles != 'ROLE_ADMIN' THEN 1 ELSE 0 END) as regular_users,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_users,
    SUM(CASE WHEN phone IS NOT NULL THEN 1 ELSE 0 END) as users_with_phone
FROM user;
