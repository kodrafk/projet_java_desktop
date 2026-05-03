@echo off
cls
color 0A
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         📦 INSTALLATION SERVEUR FACE RECOGNITION 📦           ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Installation des dependances Python...
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python n'est pas installe!
    echo.
    echo Telechargez Python depuis: https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

echo ✅ Python detecte
echo.

REM Create virtual environment
echo Creation de l'environnement virtuel...
python -m venv venv

REM Activate virtual environment
call venv\Scripts\activate.bat

REM Upgrade pip
echo.
echo Mise a jour de pip...
python -m pip install --upgrade pip

REM Install requirements
echo.
echo Installation des packages...
pip install flask flask-cors numpy opencv-python Pillow

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                    ✅ INSTALLATION TERMINEE ✅                 ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Pour demarrer le serveur, executez: START_SERVER.bat
echo.
pause
