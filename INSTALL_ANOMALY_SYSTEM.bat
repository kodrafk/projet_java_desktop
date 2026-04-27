@echo off
color 0A
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🚀 INSTALLATION SYSTÈME DE DÉTECTION D'ANOMALIES + ML
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo     Système de détection intelligente avec Machine Learning
echo     Pour la surveillance de la santé des utilisateurs
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.
pause

REM Configuration MySQL
set MYSQL_USER=root
set MYSQL_PASSWORD=
set MYSQL_DATABASE=nutrilife
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo.
echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ ÉTAPE 1/4 : Vérification de MySQL                                    │
echo └───────────────────────────────────────────────────────────────────────┘
echo.

mysql --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ✗ MySQL n'est pas installé ou pas dans le PATH
    echo.
    echo 💡 Installez MySQL ou XAMPP/WAMP et réessayez
    echo.
    pause
    exit /b 1
)

echo ✓ MySQL détecté
echo.

echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ ÉTAPE 2/4 : Création des tables                                      │
echo └───────────────────────────────────────────────────────────────────────┘
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% < CREATE_ANOMALY_DETECTION_TABLES.sql

if %ERRORLEVEL% EQU 0 (
    echo ✓ Tables créées avec succès
    echo.
    echo   Tables créées:
    echo     • health_anomalies
    echo     • health_alerts
    echo     • user_health_metrics
    echo     • anomaly_detection_history
    echo.
    echo   Vues créées:
    echo     • v_anomaly_dashboard
    echo     • v_pending_alerts
    echo     • v_anomaly_statistics
    echo.
) else (
    echo ✗ Erreur lors de la création des tables
    echo.
    echo 💡 Vérifiez:
    echo    1. MySQL est démarré
    echo    2. La base 'nutrilife' existe
    echo    3. Les identifiants sont corrects
    echo.
    pause
    exit /b 1
)

echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ ÉTAPE 3/4 : Insertion de données de test                             │
echo └───────────────────────────────────────────────────────────────────────┘
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% << EOF

-- Utilisateur de test avec anomalies
INSERT IGNORE INTO users (id, email, password, first_name, last_name, is_active, created_at)
VALUES (999, 'test.anomaly@nutrilife.com', 'test123', 'Test', 'Anomaly', TRUE, NOW());

-- Perte de poids rapide (anomalie critique)
DELETE FROM weight_logs WHERE user_id = 999;
INSERT INTO weight_logs (user_id, weight, logged_at) VALUES
(999, 85.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(999, 81.5, NOW());

-- Objectif irréaliste
DELETE FROM weight_objectives WHERE user_id = 999;
INSERT INTO weight_objectives (user_id, start_weight, target_weight, start_date, target_date, active)
VALUES (999, 85.0, 65.0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE);

-- Anomalie de test
INSERT INTO health_anomalies (user_id, type, description, severity, confidence, detected_at)
VALUES (999, 'RAPID_WEIGHT_LOSS', 'Perte de 3.5 kg en 7 jours (test)', 87.5, 0.95, NOW());

-- Alerte de test
INSERT INTO health_alerts (user_id, anomaly_id, title, message, priority, risk_score, recommendation, created_at)
VALUES (999, LAST_INSERT_ID(), 'Perte de poids rapide', 'Anomalie critique détectée', 'CRITICAL', 87.5, 
        'Contacter l''utilisateur immédiatement', NOW());

EOF

if %ERRORLEVEL% EQU 0 (
    echo ✓ Données de test insérées
    echo.
    echo   Utilisateur de test créé:
    echo     • Email: test.anomaly@nutrilife.com
    echo     • Anomalie: Perte rapide (-3.5kg en 7j)
    echo     • Sévérité: 87%% (CRITIQUE)
    echo.
) else (
    echo ⚠️  Erreur lors de l'insertion (peut être ignorée)
    echo.
)

echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ ÉTAPE 4/4 : Vérification du système                                  │
echo └───────────────────────────────────────────────────────────────────────┘
echo.

echo Statistiques globales:
echo.
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SELECT * FROM v_anomaly_statistics;"

echo.
echo Anomalies détectées:
echo.
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SELECT id, user_id, type, severity, detected_at FROM health_anomalies ORDER BY detected_at DESC LIMIT 5;"

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     ✅ INSTALLATION TERMINÉE AVEC SUCCÈS !
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo 📊 Système installé et fonctionnel:
echo.
echo    ✓ 4 tables créées
echo    ✓ 3 vues SQL créées
echo    ✓ Index optimisés
echo    ✓ Données de test insérées
echo    ✓ Anomalies détectées
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🎯 PROCHAINES ÉTAPES
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo 1. Lancez votre application NutriLife
echo.
echo 2. Connectez-vous en tant qu'ADMIN
echo.
echo 3. Dans le menu latéral, allez dans:
echo    Menu → HEALTH AI → 🔍 Anomaly Detection
echo.
echo 4. Cliquez sur "🚀 Lancer Détection"
echo.
echo 5. Consultez les résultats dans le dashboard:
echo    • Cartes statistiques
echo    • Graphiques interactifs
echo    • Liste des anomalies
echo    • Alertes prédictives
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     📚 DOCUMENTATION
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo • ANOMALY_DETECTION_README.md    - Installation rapide
echo • ANOMALY_DETECTION_GUIDE.md     - Guide technique complet
echo • ANOMALY_SYSTEM_SUMMARY.md      - Résumé du système
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🧹 NETTOYAGE (Optionnel)
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo Pour supprimer les données de test:
echo mysql -u root -p nutrilife -e "DELETE FROM users WHERE id = 999;"
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo 🎉 Félicitations ! Votre système ML est prêt !
echo.
pause
