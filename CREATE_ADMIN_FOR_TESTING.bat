@echo off
color 0A
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🔑 CRÉATION D'UN COMPTE ADMIN POUR TESTER
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Configuration MySQL
set MYSQL_USER=root
set MYSQL_PASSWORD=
set MYSQL_DATABASE=nutrilife
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo Création du compte admin...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% < CREATE_ADMIN_FOR_TESTING.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ═══════════════════════════════════════════════════════════════════════════
    echo     ✅ COMPTE ADMIN CRÉÉ !
    echo ═══════════════════════════════════════════════════════════════════════════
    echo.
    echo 📧 Email: admin.test@nutrilife.com
    echo 🔐 Mot de passe: admin123
    echo.
    echo ═══════════════════════════════════════════════════════════════════════════
    echo     🚀 POUR VOUS CONNECTER
    echo ═══════════════════════════════════════════════════════════════════════════
    echo.
    echo 1. Lancez votre application NutriLife
    echo.
    echo 2. Sur l'écran de connexion, entrez :
    echo    Email: admin.test@nutrilife.com
    echo    Password: admin123
    echo.
    echo 3. Cliquez sur [Login]
    echo.
    echo 4. Dans le menu latéral gauche, cherchez :
    echo    HEALTH AI → 🔍 Anomaly Detection
    echo.
    echo 5. Cliquez sur "🚀 Lancer Détection"
    echo.
    echo ═══════════════════════════════════════════════════════════════════════════
) else (
    echo.
    echo ✗ Erreur lors de la création du compte
    echo.
    echo Vérifiez que MySQL est démarré et que la base 'nutrilife' existe
    echo.
)

pause
