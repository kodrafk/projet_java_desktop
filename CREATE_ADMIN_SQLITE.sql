-- Script pour créer le compte ADMIN dans SQLite
-- Exécutez ce script dans DB Browser for SQLite ou via ligne de commande

-- Supprimer l'admin s'il existe déjà
DELETE FROM user WHERE email = 'admin@nutrilife.com';

-- Créer le compte ADMIN
-- Password: Admin@1234 (BCrypt hash)
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
    'admin@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_ADMIN',
    1,
    'Admin',
    'NutriLife',
    '1990-01-01',
    75.0,
    175.0,
    datetime('now')
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
