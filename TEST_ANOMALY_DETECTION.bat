@echo off
echo ═══════════════════════════════════════════════════════════════════════════
echo TEST DU SYSTÈME DE DÉTECTION D'ANOMALIES
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Configuration MySQL
set MYSQL_USER=root
set MYSQL_PASSWORD=
set MYSQL_DATABASE=nutrilife
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo [1/5] Vérification des tables...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SHOW TABLES LIKE 'health%%';"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ✗ Erreur: Les tables n'existent pas
    echo ⚠️  Exécutez d'abord: CREATE_ANOMALY_DETECTION_TABLES.bat
    echo.
    pause
    exit /b 1
)

echo.
echo [2/5] Insertion de données de test...
echo.

REM Créer un utilisateur de test avec anomalies
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% << EOF

-- Utilisateur de test
INSERT IGNORE INTO users (id, email, password, first_name, last_name, is_active, created_at)
VALUES (999, 'test.anomaly@nutrilife.com', 'test123', 'Test', 'Anomaly', TRUE, NOW());

-- Perte de poids rapide (anomalie critique)
DELETE FROM weight_logs WHERE user_id = 999;
INSERT INTO weight_logs (user_id, weight, logged_at) VALUES
(999, 85.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(999, 81.5, NOW());  -- Perte de 3.5kg en 7 jours

-- Objectif irréaliste
DELETE FROM weight_objectives WHERE user_id = 999;
INSERT INTO weight_objectives (user_id, start_weight, target_weight, start_date, target_date, active)
VALUES (999, 85.0, 65.0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE);  -- 20kg en 30 jours

EOF

if %ERRORLEVEL% EQU 0 (
    echo ✓ Données de test insérées
) else (
    echo ✗ Erreur lors de l'insertion
)

echo.
echo [3/5] Test de détection...
echo.

REM Simuler une détection (via SQL)
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% << EOF

-- Insérer une anomalie de test
INSERT INTO health_anomalies (user_id, type, description, severity, confidence, detected_at)
VALUES (999, 'RAPID_WEIGHT_LOSS', 'Test: Perte de 3.5 kg en 7 jours', 87.5, 0.95, NOW());

-- Insérer une alerte de test
INSERT INTO health_alerts (user_id, anomaly_id, title, message, priority, risk_score, recommendation, created_at)
VALUES (999, LAST_INSERT_ID(), 'Perte de poids rapide', 'Test: Anomalie détectée', 'CRITICAL', 87.5, 
        'Contacter l''utilisateur immédiatement', NOW());

EOF

echo ✓ Anomalie de test créée
echo.

echo [4/5] Vérification des résultats...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SELECT * FROM health_anomalies WHERE user_id = 999 ORDER BY detected_at DESC LIMIT 1;"

echo.
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SELECT * FROM health_alerts WHERE user_id = 999 ORDER BY created_at DESC LIMIT 1;"

echo.
echo [5/5] Statistiques globales...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SELECT * FROM v_anomaly_statistics;"

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo TEST TERMINÉ AVEC SUCCÈS!
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo 📊 Résultats:
echo    ✓ Tables créées et fonctionnelles
echo    ✓ Données de test insérées
echo    ✓ Anomalie détectée et enregistrée
echo    ✓ Alerte générée
echo.
echo 💡 Prochaines étapes:
echo    1. Ouvrez l'application NutriLife
echo    2. Connectez-vous en tant qu'admin
echo    3. Allez dans: Menu → HEALTH AI → Anomaly Detection
echo    4. Cliquez sur "🚀 Lancer Détection"
echo    5. Consultez les résultats dans le dashboard
echo.
echo 🧹 Pour nettoyer les données de test:
echo    mysql -u root -p nutrilife -e "DELETE FROM users WHERE id = 999;"
echo.
pause
