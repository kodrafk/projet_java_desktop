-- ============================================================================
-- SCRIPT COMPLET - BASE DE DONNÉES NUTRILIFE
-- ============================================================================
-- 
-- Ce fichier contient TOUT ce dont vous avez besoin pour recréer
-- la base de données complète de NutriLife
-- 
-- UTILISATION:
-- 1. Ouvrez phpMyAdmin ou MySQL Workbench
-- 2. Exécutez ce script complet
-- 3. La base de données sera créée avec toutes les tables et les comptes
-- 
-- ============================================================================

-- Supprimer la base si elle existe (ATTENTION: perte de données)
DROP DATABASE IF EXISTS nutrilife_db;

-- Créer la base de données
CREATE DATABASE nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE nutrilife_db;

-- ============================================================================
-- TABLE: user
-- ============================================================================
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(50) DEFAULT 'ROLE_USER',
    is_active TINYINT(1) DEFAULT 1,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    birthday DATE,
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    gender VARCHAR(10),
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    profile_picture VARCHAR(255),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    email_verified TINYINT(1) DEFAULT 0,
    verification_token VARCHAR(255),
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_roles (roles),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: badge
-- ============================================================================
CREATE TABLE badge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_path VARCHAR(255),
    requirement_type VARCHAR(50),
    requirement_value INT,
    points INT DEFAULT 0,
    rarity VARCHAR(20) DEFAULT 'COMMON',
    category VARCHAR(50),
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_requirement_type (requirement_type),
    INDEX idx_rarity (rarity),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: user_badge
-- ============================================================================
CREATE TABLE user_badge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    badge_id INT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress INT DEFAULT 0,
    is_displayed TINYINT(1) DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_badge (user_id, badge_id),
    INDEX idx_user_id (user_id),
    INDEX idx_badge_id (badge_id),
    INDEX idx_earned_at (earned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: weight_log
-- ============================================================================
CREATE TABLE weight_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    log_date DATE NOT NULL,
    notes TEXT,
    bmi DECIMAL(4,2),
    body_fat_percentage DECIMAL(4,2),
    muscle_mass DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_log_date (log_date),
    UNIQUE KEY unique_user_date (user_id, log_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: weight_objective
-- ============================================================================
CREATE TABLE weight_objective (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    target_weight DECIMAL(5,2) NOT NULL,
    start_weight DECIMAL(5,2),
    start_date DATE NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    objective_type VARCHAR(20) DEFAULT 'LOSE_WEIGHT',
    weekly_goal DECIMAL(4,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_target_date (target_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: ingredient
-- ============================================================================
CREATE TABLE ingredient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    calories DECIMAL(6,2),
    proteins DECIMAL(6,2),
    carbohydrates DECIMAL(6,2),
    fats DECIMAL(6,2),
    fiber DECIMAL(6,2),
    sugar DECIMAL(6,2),
    sodium DECIMAL(6,2),
    serving_size VARCHAR(50),
    unit VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: face_embedding
-- ============================================================================
CREATE TABLE face_embedding (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    embedding BLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active TINYINT(1) DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- DONNÉES INITIALES: Compte ADMIN
-- ============================================================================
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
    gender,
    email_verified
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
    'OTHER',
    1
);

-- ============================================================================
-- DONNÉES INITIALES: Compte USER de test
-- ============================================================================
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
    gender,
    email_verified
) VALUES (
    'user@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    1,
    'John',
    'Doe',
    '1995-05-15',
    80.0,
    180.0,
    'MALE',
    1
);

-- ============================================================================
-- DONNÉES INITIALES: Badges
-- ============================================================================
INSERT INTO badge (name, description, icon_path, requirement_type, requirement_value, points, rarity, category) VALUES
('Débutant', 'Première connexion à NutriLife', '/images/badges/beginner.png', 'LOGIN', 1, 10, 'COMMON', 'GENERAL'),
('Régulier', '7 jours consécutifs de connexion', '/images/badges/regular.png', 'LOGIN_STREAK', 7, 50, 'UNCOMMON', 'GENERAL'),
('Assidu', '30 jours consécutifs de connexion', '/images/badges/dedicated.png', 'LOGIN_STREAK', 30, 200, 'RARE', 'GENERAL'),
('Champion', '100 jours consécutifs de connexion', '/images/badges/champion.png', 'LOGIN_STREAK', 100, 500, 'EPIC', 'GENERAL'),
('Légende', '365 jours consécutifs de connexion', '/images/badges/legend.png', 'LOGIN_STREAK', 365, 1000, 'LEGENDARY', 'GENERAL'),

('Premier Pas', 'Premier enregistrement de poids', '/images/badges/first_weight.png', 'WEIGHT_LOG', 1, 10, 'COMMON', 'WEIGHT'),
('Suivi Régulier', '10 enregistrements de poids', '/images/badges/weight_tracker.png', 'WEIGHT_LOG', 10, 50, 'UNCOMMON', 'WEIGHT'),
('Objectif Atteint', 'Premier objectif de poids atteint', '/images/badges/goal_reached.png', 'GOAL_COMPLETED', 1, 100, 'RARE', 'WEIGHT'),
('Transformation', '5 objectifs de poids atteints', '/images/badges/transformation.png', 'GOAL_COMPLETED', 5, 300, 'EPIC', 'WEIGHT'),

('Nutritionniste Amateur', '10 ingrédients consultés', '/images/badges/nutrition_beginner.png', 'INGREDIENT_VIEW', 10, 20, 'COMMON', 'NUTRITION'),
('Expert Nutrition', '100 ingrédients consultés', '/images/badges/nutrition_expert.png', 'INGREDIENT_VIEW', 100, 150, 'RARE', 'NUTRITION');

-- ============================================================================
-- VÉRIFICATION
-- ============================================================================
SELECT 'Base de données créée avec succès !' AS Status;

SELECT 
    'Utilisateurs créés:' AS Info,
    COUNT(*) AS Total
FROM user;

SELECT 
    email,
    roles,
    first_name,
    last_name,
    is_active
FROM user;

SELECT 
    'Badges créés:' AS Info,
    COUNT(*) AS Total
FROM badge;

-- ============================================================================
-- INFORMATIONS DE CONNEXION
-- ============================================================================
SELECT '
========================================
  BASE DE DONNÉES CRÉÉE AVEC SUCCÈS !
========================================

Base de données: nutrilife_db

COMPTES CRÉÉS:

1. ADMIN
   📧 Email    : admin@nutrilife.com
   🔑 Password : admin123
   👤 Rôle     : ROLE_ADMIN

2. USER (test)
   📧 Email    : user@test.com
   🔑 Password : admin123
   👤 Rôle     : ROLE_USER

TABLES CRÉÉES:
- user              (utilisateurs)
- badge             (badges)
- user_badge        (badges des utilisateurs)
- weight_log        (historique de poids)
- weight_objective  (objectifs de poids)
- ingredient        (ingrédients)
- face_embedding    (reconnaissance faciale)

PROCHAINES ÉTAPES:
1. Vérifiez que toutes les tables existent
2. Lancez votre application Java
3. Connectez-vous avec admin@nutrilife.com / admin123

========================================
' AS Information;

-- ============================================================================
-- FIN DU SCRIPT
-- ============================================================================
