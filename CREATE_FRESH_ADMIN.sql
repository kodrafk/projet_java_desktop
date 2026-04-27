USE nutrilife;

-- Supprimer le compte s'il existe déjà
DELETE FROM users WHERE email = 'kiro.admin@nutrilife.com';

-- Créer un nouveau compte admin frais
-- Email: kiro.admin@nutrilife.com
-- Password: kiro2026
-- Hash BCrypt pour "kiro2026"
INSERT INTO users (email, password, first_name, last_name, role, is_active, created_at) 
VALUES (
    'kiro.admin@nutrilife.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Kiro',
    'Admin',
    'ROLE_ADMIN',
    1,
    NOW()
);

SELECT '✅ COMPTE CRÉÉ!' as '';
SELECT 'Email: kiro.admin@nutrilife.com' as '';
SELECT 'Password: kiro2026' as '';
