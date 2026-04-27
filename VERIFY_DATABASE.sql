-- ============================================================================
-- VÉRIFICATION DE LA STRUCTURE DE LA BASE DE DONNÉES
-- ============================================================================

USE nutrilife_db;

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '  VÉRIFICATION DE LA STRUCTURE DE LA BASE DE DONNÉES' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';

-- Vérifier que les tables existent
SELECT '📋 Tables existantes:' AS '';
SHOW TABLES;
SELECT '' AS '';

-- Vérifier la structure de weight_log
SELECT '📊 Structure de weight_log:' AS '';
DESCRIBE weight_log;
SELECT '' AS '';

-- Vérifier la structure de weight_objective
SELECT '🎯 Structure de weight_objective:' AS '';
DESCRIBE weight_objective;
SELECT '' AS '';

-- Vérifier la structure de progress_photo
SELECT '📸 Structure de progress_photo:' AS '';
DESCRIBE progress_photo;
SELECT '' AS '';

-- Vérifier la structure de message
SELECT '💬 Structure de message:' AS '';
DESCRIBE message;
SELECT '' AS '';

-- Compter les données
SELECT '📈 Nombre de données:' AS '';
SELECT 
    (SELECT COUNT(*) FROM weight_log) AS weight_logs,
    (SELECT COUNT(*) FROM weight_objective) AS weight_objectives,
    (SELECT COUNT(*) FROM progress_photo) AS progress_photos,
    (SELECT COUNT(*) FROM message) AS messages;
SELECT '' AS '';

-- Vérifier les colonnes critiques
SELECT '✅ Vérification des colonnes critiques:' AS '';

SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = 'nutrilife_db' 
            AND TABLE_NAME = 'weight_log' 
            AND COLUMN_NAME IN ('photo', 'note', 'logged_at')
            GROUP BY TABLE_NAME
            HAVING COUNT(*) = 3
        ) THEN '✅ weight_log a les bonnes colonnes (photo, note, logged_at)'
        ELSE '❌ weight_log a des colonnes manquantes!'
    END AS weight_log_check;

SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = 'nutrilife_db' 
            AND TABLE_NAME = 'weight_objective' 
            AND COLUMN_NAME IN ('start_weight', 'start_photo', 'is_active')
            GROUP BY TABLE_NAME
            HAVING COUNT(*) = 3
        ) THEN '✅ weight_objective a les bonnes colonnes (start_weight, start_photo, is_active)'
        ELSE '❌ weight_objective a des colonnes manquantes!'
    END AS weight_objective_check;

SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.TABLES 
            WHERE TABLE_SCHEMA = 'nutrilife_db' 
            AND TABLE_NAME = 'progress_photo'
        ) THEN '✅ Table progress_photo existe'
        ELSE '❌ Table progress_photo manquante!'
    END AS progress_photo_check;

SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.TABLES 
            WHERE TABLE_SCHEMA = 'nutrilife_db' 
            AND TABLE_NAME = 'message'
        ) THEN '✅ Table message existe'
        ELSE '❌ Table message manquante!'
    END AS message_check;

SELECT '' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '  FIN DE LA VÉRIFICATION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
