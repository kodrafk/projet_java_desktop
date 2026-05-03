@echo off
cls
color 0B
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🚀 LANCEMENT DE L'APPLICATION NUTRILIFE 🚀            ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Demarrage de l'application...
echo.
echo Fonctionnalites disponibles:
echo   ✅ Connexion par mot de passe
echo   ✅ Connexion Face ID (camera professionnelle)
echo   ✅ Connexion Google
echo.
echo L'application va s'ouvrir dans quelques secondes...
echo.

cd /d "%~dp0"

call mvn javafx:run

pause
