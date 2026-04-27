-- Réinitialiser le mot de passe des comptes admin existants
-- Nouveau mot de passe: admin123

USE nutrilife;

-- Mettre à jour tous les comptes admin avec le nouveau mot de passe
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ'
WHERE role = 'ROLE_ADMIN';

-- Afficher les comptes mis à jour
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '✅ MOTS DE PASSE RÉINITIALISÉS!' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';
SELECT 'Tous les comptes admin ont maintenant le mot de passe: admin123' as '';
SELECT '' as '';
SELECT '📋 Comptes admin disponibles:' as '';
SELECT email, first_name, last_name FROM users WHERE role = 'ROLE_ADMIN';
SELECT '' as '';
SELECT '🔐 Mot de passe pour TOUS: admin123' as '';
