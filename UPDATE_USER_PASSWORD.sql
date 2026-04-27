-- ============================================================================
-- Script pour corriger le mot de passe du USER
-- ============================================================================

USE nutrilife_db;

-- Option 1: Mot de passe en clair 'user123'
UPDATE user 
SET password = 'user123' 
WHERE email = 'user@test.com';

-- Vérifier le compte
SELECT 
    id,
    email,
    password,
    roles,
    first_name,
    last_name,
    is_active
FROM user 
WHERE email = 'user@test.com';

-- ============================================================================
-- COMPTE USER MIS À JOUR !
-- ============================================================================
-- 
-- 📧 Email:    user@test.com
-- 🔑 Password: user123
-- 👤 Role:     ROLE_USER
-- ✅ Status:   Active
-- 
-- ============================================================================
