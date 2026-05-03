@echo off
cls
color 0B
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🚀 DEMARRAGE AUTOMATIQUE - FACE ID 🚀                 ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Compile the project first
echo 📦 Compilation du projet Java...
call mvn clean compile -q
if errorlevel 1 (
    echo ❌ Erreur de compilation!
    pause
    exit /b 1
)
echo ✅ Compilation reussie
echo.

REM Check if Python server is running
echo 🔍 Verification du serveur Python...
curl -s http://localhost:5000/health >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Serveur Python deja en cours
) else (
    echo 🚀 Demarrage du serveur Python...
    
    REM Check if Python is installed
    python --version >nul 2>&1
    if errorlevel 1 (
        echo.
        echo ❌ Python n'est pas installe!
        echo.
        echo Installez Python depuis: https://www.python.org/downloads/
        echo IMPORTANT: Cochez "Add Python to PATH"
        echo.
        pause
        exit /b 1
    )
    
    REM Check if virtual environment exists
    if not exist python_face_server\venv (
        echo.
        echo ⚠️ Environnement virtuel non trouve
        echo Installation des dependances...
        cd python_face_server
        call INSTALL.bat
        cd ..
    )
    
    REM Start Python server in background
    start "Python Face Server" cmd /k "cd python_face_server && START_SERVER.bat"
    
    echo ⏳ Attente du demarrage du serveur (10 secondes)...
    timeout /t 10 >nul
    
    REM Verify server started
    curl -s http://localhost:5000/health >nul 2>&1
    if errorlevel 1 (
        echo ❌ Le serveur Python n'a pas demarre correctement
        echo Verifiez la fenetre du serveur Python
        pause
        exit /b 1
    )
    echo ✅ Serveur Python demarre
)

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo 🎮 Demarrage de l'application...
echo.
echo INSTRUCTIONS:
echo   1. Cliquez sur "Face ID Login" (bouton bleu)
echo   2. La camera va s'ouvrir avec interface professionnelle
echo   3. Positionnez votre visage dans le cercle
echo   4. Le systeme va automatiquement vous authentifier
echo.
echo NOTE: Si c'est votre premiere fois:
echo   - Inscrivez-vous d'abord
echo   - Allez dans Profil pour enroller votre Face ID
echo.
echo ════════════════════════════════════════════════════════════════
echo.

call mvn javafx:run

pause
