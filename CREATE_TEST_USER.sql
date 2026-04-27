-- ============================================================================
-- Créer un compte USER de test avec mot de passe en clair
-- ============================================================================

USE nutrilife_db;

-- Supprimer l'ancien user s'il existe
DELETE FROM user WHERE email = 'test@user.com';

-- Créer un nouveau compte USER avec mot de passe en clair
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
    'test@user.com',           -- Email
    'user123',                 -- Password en CLAIR
    'ROLE_USER',               -- Rôle USER
    1,                         -- Actif
    'Marie',                   -- Prénom
    'Dupont',                  -- Nom
    '1998-03-20',              -- Date de naissance
    65.0,                      -- Poids (kg)
    168.0,                     -- Taille (cm)
    NOW()                      -- Date de création
);

-- Vérifier que le compte a été créé
SELECT 
    id,
    email,
    password,
    roles,
    first_name,
    last_name,
    is_active,
    created_at
FROM user 
WHERE email = 'test@user.com';

-- ============================================================================
-- COMPTE USER CRÉÉ !
-- ============================================================================
-- 
-- 📧 Email    : test@user.com
-- 🔑 Password : user123
-- 👤 Rôle     : ROLE_USER
-- ✅ Statut   : Actif
-- 
-- ============================================================================

SELECT '✅ Compte user créé avec succès !' AS Status;
