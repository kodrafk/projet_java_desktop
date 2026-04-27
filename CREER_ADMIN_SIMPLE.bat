@echo off
chcp 65001 >nul
color 0A
cls

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           🔐 CRÉATION COMPTE ADMIN SIMPLE                                ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.
echo.

echo Création du compte admin...
echo.
echo Entrez le mot de passe MySQL root (appuyez sur Entrée si vide):

mysql -u root -p < CREATE_SIMPLE_ADMIN.sql

if %errorlevel% equ 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════════════════════════╗
    echo ║                                                                           ║
    echo ║           ✅ COMPTE ADMIN CRÉÉ AVEC SUCCÈS!                              ║
    echo ║                                                                           ║
    echo ╚═══════════════════════════════════════════════════════════════════════════╝
    echo.
    echo.
    echo ═══════════════════════════════════════════════════════════════════════════
    echo 📧 IDENTIFIANTS À UTILISER
    echo ═══════════════════════════════════════════════════════════════════════════
    echo.
    echo     Email:    admin123@nutrilife.com
    echo     Password: admin123
    echo.
    echo ═══════════════════════════════════════════════════════════════════════════
    echo.
    echo.
    echo 💡 POUR VOUS CONNECTER:
    echo.
    echo    1. Ouvrez votre application NutriLife
    echo    2. Entrez: admin123@nutrilife.com
    echo    3. Mot de passe: admin123
    echo    4. Cliquez sur "Sign In"
    echo.
    echo.
) else (
    echo.
    echo ❌ Erreur lors de la création du compte
    echo.
    echo 💡 Vérifiez que:
    echo    - MySQL est démarré (WAMP/XAMPP)
    echo    - La base 'nutrilife' existe
    echo    - Le mot de passe root est correct
    echo.
)

echo.
pause
