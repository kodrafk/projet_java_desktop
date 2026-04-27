-- ═══════════════════════════════════════════════════════════════════════════
-- Script de création de la table user_alerts
-- Système d'alertes/notifications pour les utilisateurs
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS user_alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    admin_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'INFO',
    category VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    read_at TIMESTAMP NULL,
    action_url VARCHAR(500) NULL,
    action_label VARCHAR(100) NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════════════════
-- Données de test (optionnel)
-- ═══════════════════════════════════════════════════════════════════════════

-- Exemple d'alerte INFO
INSERT INTO user_alerts (user_id, admin_id, title, message, type, category)
SELECT 
    (SELECT id FROM users WHERE roles = 'ROLE_USER' LIMIT 1),
    (SELECT id FROM users WHERE roles = 'ROLE_ADMIN' LIMIT 1),
    'Welcome to NutriLife!',
    'We are excited to have you on board. Start by setting your health goals in your profile.',
    'INFO',
    'SYSTEM'
WHERE EXISTS (SELECT 1 FROM users WHERE roles = 'ROLE_USER')
  AND EXISTS (SELECT 1 FROM users WHERE roles = 'ROLE_ADMIN');

-- Exemple d'alerte WARNING
INSERT INTO user_alerts (user_id, admin_id, title, message, type, category)
SELECT 
    (SELECT id FROM users WHERE roles = 'ROLE_USER' LIMIT 1),
    (SELECT id FROM users WHERE roles = 'ROLE_ADMIN' LIMIT 1),
    'Health Check Reminder',
    'It has been a while since your last health metrics update. Please update your weight and measurements.',
    'WARNING',
    'HEALTH'
WHERE EXISTS (SELECT 1 FROM users WHERE roles = 'ROLE_USER')
  AND EXISTS (SELECT 1 FROM users WHERE roles = 'ROLE_ADMIN');

-- Exemple d'alerte SUCCESS
INSERT INTO user_alerts (user_id, admin_id, title, message, type, category)
SELECT 
    (SELECT id FROM users WHERE roles = 'ROLE_USER' LIMIT 1),
    (SELECT id FROM users WHERE roles = 'ROLE_ADMIN' LIMIT 1),
    'Congratulations! 🎉',
    'You have reached your weekly goal! Keep up the great work!',
    'SUCCESS',
    'GOAL'
WHERE EXISTS (SELECT 1 FROM users WHERE roles = 'ROLE_USER')
  AND EXISTS (SELECT 1 FROM users WHERE roles = 'ROLE_ADMIN');

-- ═══════════════════════════════════════════════════════════════════════════
-- Vérification
-- ═══════════════════════════════════════════════════════════════════════════

SELECT 'Table user_alerts created successfully!' AS status;
SELECT COUNT(*) AS alert_count FROM user_alerts;
