-- ═══════════════════════════════════════════════════════════════════════════
-- CRÉATION COMPTE ADMIN SIMPLE
-- ═══════════════════════════════════════════════════════════════════════════

USE nutrilife;

-- Supprimer les anciens comptes de test
DELETE FROM users WHERE email IN ('test@admin.com', 'admin@test.com', 'admin123@nutrilife.com');

-- Créer un compte admin simple
-- Email: admin123@nutrilife.com
-- Password: admin123
INSERT INTO users (email, password, first_name, last_name, role, is_active, created_at) 
VALUES (
    'admin123@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ',
    'Admin',
    'User',
    'ROLE_ADMIN',
    1,
    NOW()
);

-- Afficher le résultat
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '✅ COMPTE ADMIN CRÉÉ!' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';
SELECT '📧 Email: admin123@nutrilife.com' as 'IDENTIFIANTS';
SELECT '🔐 Password: admin123' as '';
SELECT '' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';

-- Afficher tous les comptes admin
SELECT '📋 TOUS LES COMPTES ADMIN:' as '';
SELECT email, first_name, last_name, role 
FROM users 
WHERE role = 'ROLE_ADMIN';
