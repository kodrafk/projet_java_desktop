@echo off
chcp 65001 >nul
color 0C
cls

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           🔧 RÉINITIALISATION DES MOTS DE PASSE ADMIN                    ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.
echo.
echo Ce script va réinitialiser TOUS les mots de passe admin à: admin123
echo.
echo Appuyez sur une touche pour continuer...
pause >nul

echo.
echo Réinitialisation en cours...
echo.

REM Trouver MySQL
set MYSQL_PATH=
if exist "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" set MYSQL_PATH=C:\wamp64\bin\mysql\mysql8.0.31\bin\
if exist "C:\xampp\mysql\bin\mysql.exe" set MYSQL_PATH=C:\xampp\mysql\bin\
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\

if "%MYSQL_PATH%"=="" (
    echo Tentative avec mysql dans le PATH...
    mysql -u root nutrilife < RESET_ADMIN_PASSWORD.sql
) else (
    echo Utilisation de MySQL trouvé dans: %MYSQL_PATH%
    "%MYSQL_PATH%mysql.exe" -u root nutrilife < RESET_ADMIN_PASSWORD.sql
)

if %errorlevel% equ 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════════════════════════╗
    echo ║                                                                           ║
    echo ║           ✅ MOTS DE PASSE RÉINITIALISÉS AVEC SUCCÈS!                    ║
    echo ║                                                                           ║
    echo ╚═══════════════════════════════════════════════════════════════════════════╝
    echo.
    echo.
    echo Tous les comptes admin ont maintenant le mot de passe: admin123
    echo.
    echo Vous pouvez maintenant vous connecter avec N'IMPORTE QUEL email admin
    echo et le mot de passe: admin123
    echo.
) else (
    echo.
    echo ❌ Erreur lors de la réinitialisation
    echo.
    echo Essayez manuellement:
    echo 1. Ouvrez MySQL Workbench ou phpMyAdmin
    echo 2. Exécutez le fichier: RESET_ADMIN_PASSWORD.sql
    echo.
)

echo.
pause
