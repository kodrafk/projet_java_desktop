@echo off
chcp 65001 >nul
color 0A
cls

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           🚀 LANCEMENT COMPLET DE L'APPLICATION                          ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.

echo [1/4] Création des comptes admin...
echo.

REM Essayer de créer les comptes (ignorer les erreurs si déjà existants)
mysql -u root nutrilife < create_admin_now.sql 2>nul

if %errorlevel% equ 0 (
    echo ✅ Comptes admin créés/vérifiés
) else (
    echo ⚠️  Comptes peut-être déjà existants
)

echo.
echo [2/4] Installation des tables d'anomalies...
echo.

if exist "CREATE_ANOMALY_DETECTION_TABLES.sql" (
    mysql -u root nutrilife < CREATE_ANOMALY_DETECTION_TABLES.sql 2>nul
    if %errorlevel% equ 0 (
        echo ✅ Tables d'anomalies installées
    ) else (
        echo ⚠️  Tables peut-être déjà installées
    )
)

echo.
echo [3/4] Compilation du projet...
echo.

call mvn clean compile -q

if %errorlevel% neq 0 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)

echo ✅ Compilation réussie
echo.

echo [4/4] Lancement de l'application...
echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║   📧 IDENTIFIANTS À UTILISER:                                            ║
echo ║                                                                           ║
echo ║   Email:    admin123@nutrilife.com                                       ║
echo ║   Password: admin123                                                     ║
echo ║                                                                           ║
echo ║   OU                                                                     ║
echo ║                                                                           ║
echo ║   Email:    admin@nutrilife.com                                          ║
echo ║   Password: admin123                                                     ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.
echo L'application va se lancer dans 3 secondes...
timeout /t 3 >nul

call mvn javafx:run

pause
