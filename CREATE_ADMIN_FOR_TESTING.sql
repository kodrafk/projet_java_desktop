-- ═══════════════════════════════════════════════════════════════════════════
-- CRÉATION D'UN COMPTE ADMIN POUR TESTER LE SYSTÈME
-- ═══════════════════════════════════════════════════════════════════════════

-- Supprimer l'ancien compte de test s'il existe
DELETE FROM users WHERE email = 'admin.test@nutrilife.com';

-- Créer un nouveau compte admin de test
-- Mot de passe: admin123
INSERT INTO users (
    email, 
    password, 
    first_name, 
    last_name, 
    role, 
    is_active, 
    created_at
) VALUES (
    'admin.test@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ',
    'Admin',
    'Test',
    'ROLE_ADMIN',
    TRUE,
    NOW()
);

-- Afficher le compte créé
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '✅ COMPTE ADMIN CRÉÉ AVEC SUCCÈS' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';
SELECT '📧 Email: admin.test@nutrilife.com' as 'Identifiants';
SELECT '🔐 Mot de passe: admin123' as '';
SELECT '👤 Nom: Admin Test' as '';
SELECT '🔑 Rôle: ROLE_ADMIN' as '';
SELECT '' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '💡 POUR VOUS CONNECTER' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';
SELECT '1. Lancez votre application NutriLife' as '';
SELECT '2. Email: admin.test@nutrilife.com' as '';
SELECT '3. Password: admin123' as '';
SELECT '4. Cliquez sur Login' as '';
SELECT '' as '';
SELECT '5. Menu latéral → HEALTH AI → Anomaly Detection' as '';
SELECT '' as '';

-- Afficher tous les comptes admin existants
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '📋 TOUS LES COMPTES ADMIN DISPONIBLES' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';

SELECT 
    id,
    email,
    CONCAT(first_name, ' ', last_name) as nom_complet,
    role,
    is_active,
    created_at
FROM users 
WHERE role = 'ROLE_ADMIN'
ORDER BY created_at DESC;
