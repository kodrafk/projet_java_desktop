@echo off
cls
color 0B
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🚀 DEMARRAGE SERVEUR FACE RECOGNITION 🚀              ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Activate virtual environment
if exist venv\Scripts\activate.bat (
    call venv\Scripts\activate.bat
    echo ✅ Environnement virtuel active
) else (
    echo ⚠️ Environnement virtuel non trouve
    echo Executez d'abord: INSTALL.bat
    pause
    exit /b 1
)

echo.
echo Demarrage du serveur sur http://localhost:5000
echo.
echo Endpoints disponibles:
echo   - GET  /health      : Verification du serveur
echo   - POST /detect      : Detection de visage
echo   - POST /enroll      : Enregistrement de visage
echo   - POST /verify      : Verification de visage
echo   - POST /extract     : Extraction d'encoding
echo   - GET  /enrolled    : Liste des utilisateurs
echo.
echo ════════════════════════════════════════════════════════════════
echo.

python face_recognition_server.py
