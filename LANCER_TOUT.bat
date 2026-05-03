@echo off
cls
color 0B
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🚀 LANCEMENT COMPLET - FACE ID SYSTEM 🚀              ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Ce script va:
echo   1. Demarrer le serveur Python Face Recognition
echo   2. Demarrer l'application Java
echo.
echo ════════════════════════════════════════════════════════════════
echo.

REM Check if Python server is already running
echo Verification du serveur Python...
curl -s http://localhost:5000/health >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Serveur Python deja en cours d'execution
) else (
    echo 🚀 Demarrage du serveur Python...
    start "Python Face Server" cmd /k "cd python_face_server && START_SERVER.bat"
    echo Attente du demarrage du serveur...
    timeout /t 5 >nul
)

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo 🚀 Demarrage de l'application Java...
echo.

mvn javafx:run

pause
