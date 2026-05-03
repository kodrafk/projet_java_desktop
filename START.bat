@echo off
title FACE ID - Demarrage Automatique
cls
color 0B

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║              🚀 FACE ID - DEMARRAGE RAPIDE 🚀                 ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Ce script va tout faire automatiquement:
echo   ✅ Verifier Python
echo   ✅ Installer les dependances si necessaire
echo   ✅ Demarrer le serveur Python
echo   ✅ Compiler le projet Java
echo   ✅ Lancer l'application
echo.
echo ════════════════════════════════════════════════════════════════
echo.

REM Step 1: Check Python
echo [1/5] Verification de Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ❌ ERREUR: Python n'est pas installe!
    echo.
    echo Veuillez installer Python 3.10+ depuis:
    echo https://www.python.org/downloads/
    echo.
    echo IMPORTANT: Cochez "Add Python to PATH" pendant l'installation
    echo.
    pause
    exit /b 1
)
for /f "tokens=2" %%i in ('python --version 2^>^&1') do set PYTHON_VERSION=%%i
echo ✅ Python %PYTHON_VERSION% detecte
echo.

REM Step 2: Check/Install dependencies
echo [2/5] Verification des dependances Python...
if not exist python_face_server\venv (
    echo ⚠️ Environnement virtuel non trouve
    echo 📦 Installation des dependances...
    cd python_face_server
    call INSTALL.bat
    if errorlevel 1 (
        echo ❌ Erreur lors de l'installation
        cd ..
        pause
        exit /b 1
    )
    cd ..
    echo ✅ Dependances installees
) else (
    echo ✅ Dependances deja installees
)
echo.

REM Step 3: Start Python server
echo [3/5] Demarrage du serveur Python...
curl -s http://localhost:5000/health >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Serveur Python deja en cours
) else (
    echo 🚀 Lancement du serveur...
    start "🐍 Python Face Recognition Server" cmd /k "cd /d "%~dp0python_face_server" && START_SERVER.bat"
    
    echo ⏳ Attente du demarrage (15 secondes)...
    timeout /t 15 >nul
    
    REM Verify server started
    curl -s http://localhost:5000/health >nul 2>&1
    if errorlevel 1 (
        echo.
        echo ❌ Le serveur Python n'a pas demarre
        echo Verifiez la fenetre "Python Face Recognition Server"
        echo.
        pause
        exit /b 1
    )
    echo ✅ Serveur Python pret
)
echo.

REM Step 4: Compile Java project
echo [4/5] Compilation du projet Java...
call mvn clean compile -q
if errorlevel 1 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)
echo ✅ Compilation reussie
echo.

REM Step 5: Launch application
echo [5/5] Lancement de l'application...
echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo 🎮 APPLICATION PRETE!
echo.
echo POUR VOIR L'INTERFACE PROFESSIONNELLE:
echo   1. Cliquez sur "Face ID Login" (bouton bleu)
echo   2. La camera s'ouvre avec interface moderne
echo   3. Positionnez votre visage dans le cercle
echo.
echo PREMIERE FOIS?
echo   - Inscrivez-vous d'abord
echo   - Allez dans Profil ^> Enroll Face ID
echo   - Ensuite utilisez Face ID Login
echo.
echo ════════════════════════════════════════════════════════════════
echo.

call mvn javafx:run

echo.
echo Application fermee.
pause
