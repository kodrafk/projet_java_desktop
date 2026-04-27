@echo off
echo ═══════════════════════════════════════════════════════════════════════════
echo CRÉATION DES TABLES - SYSTÈME DE DÉTECTION D'ANOMALIES
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Configuration MySQL
set MYSQL_USER=root
set MYSQL_PASSWORD=
set MYSQL_DATABASE=nutrilife
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo [1/3] Connexion à MySQL...
echo.

REM Exécuter le script SQL
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% < CREATE_ANOMALY_DETECTION_TABLES.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Tables créées avec succès!
    echo.
    echo Tables créées:
    echo   - health_anomalies
    echo   - health_alerts
    echo   - user_health_metrics
    echo   - anomaly_detection_history
    echo.
    echo Vues créées:
    echo   - v_anomaly_dashboard
    echo   - v_pending_alerts
    echo   - v_anomaly_statistics
    echo.
    echo [2/3] Vérification des tables...
    mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SHOW TABLES LIKE 'health%%';"
    echo.
    echo [3/3] Système prêt!
    echo.
    echo ═══════════════════════════════════════════════════════════════════════════
    echo SYSTÈME DE DÉTECTION D'ANOMALIES INSTALLÉ
    echo ═══════════════════════════════════════════════════════════════════════════
) else (
    echo.
    echo ✗ Erreur lors de la création des tables
    echo.
    echo Vérifiez:
    echo   1. MySQL est démarré
    echo   2. Les identifiants sont corrects
    echo   3. La base de données 'nutrilife' existe
    echo.
)

echo.
pause
