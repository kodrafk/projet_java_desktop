@echo off
chcp 65001 >nul
color 0A
cls

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           🚀 CONFIGURATION ET LANCEMENT AUTOMATIQUE                      ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.

echo [1/3] Création du compte admin...
echo.

REM Chercher MySQL
set MYSQL_FOUND=0

if exist "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" (
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" -u root nutrilife < CREATE_FRESH_ADMIN.sql 2>nul
    set MYSQL_FOUND=1
)

if exist "C:\xampp\mysql\bin\mysql.exe" (
    "C:\xampp\mysql\bin\mysql.exe" -u root nutrilife < CREATE_FRESH_ADMIN.sql 2>nul
    set MYSQL_FOUND=1
)

if %MYSQL_FOUND%==0 (
    mysql -u root nutrilife < CREATE_FRESH_ADMIN.sql 2>nul
)

echo ✅ Compte admin créé
echo.

echo [2/3] Installation des tables d'anomalies...
echo.

if exist "CREATE_ANOMALY_DETECTION_TABLES.sql" (
    if %MYSQL_FOUND%==1 (
        if exist "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" (
            "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" -u root nutrilife < CREATE_ANOMALY_DETECTION_TABLES.sql 2>nul
        )
        if exist "C:\xampp\mysql\bin\mysql.exe" (
            "C:\xampp\mysql\bin\mysql.exe" -u root nutrilife < CREATE_ANOMALY_DETECTION_TABLES.sql 2>nul
        )
    ) else (
        mysql -u root nutrilife < CREATE_ANOMALY_DETECTION_TABLES.sql 2>nul
    )
    echo ✅ Tables installées
) else (
    echo ⚠️  Fichier SQL introuvable
)

echo.
echo [3/3] Lancement de l'application...
echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║   🔐 IDENTIFIANTS À UTILISER:                                            ║
echo ║                                                                           ║
echo ║   📧 Email:    kiro.admin@nutrilife.com                                  ║
echo ║   🔐 Password: kiro2026                                                  ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.
echo L'application va se lancer...
echo.

start cmd /k "mvn javafx:run"

echo.
echo ✅ Application lancée dans une nouvelle fenêtre!
echo.
echo Utilisez les identifiants ci-dessus pour vous connecter.
echo.
pause
