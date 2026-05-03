@echo off
cls
color 0A
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🧪 TEST SERVEUR PYTHON - FACE ID 🧪                   ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Test 1: Python installed
echo Test 1: Python installe...
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python n'est pas installe
    echo    Installez depuis: https://www.python.org/downloads/
    goto :end
) else (
    python --version
    echo ✅ Python OK
)

echo.
echo Test 2: Packages Python...
python -c "import flask" 2>nul
if errorlevel 1 (
    echo ❌ Flask non installe
    echo    Executez: INSTALL.bat
) else (
    echo ✅ Flask OK
)

python -c "import cv2" 2>nul
if errorlevel 1 (
    echo ❌ OpenCV non installe
    echo    Executez: INSTALL.bat
) else (
    echo ✅ OpenCV OK
)

python -c "import numpy" 2>nul
if errorlevel 1 (
    echo ❌ NumPy non installe
    echo    Executez: INSTALL.bat
) else (
    echo ✅ NumPy OK
)

python -c "import PIL" 2>nul
if errorlevel 1 (
    echo ❌ Pillow non installe
    echo    Executez: INSTALL.bat
) else (
    echo ✅ Pillow OK
)

echo.
echo Test 3: Serveur en cours...
curl -s http://localhost:5000/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️ Serveur non demarre
    echo    Demarrez avec: START_SERVER.bat
) else (
    echo ✅ Serveur en cours d'execution
    echo.
    echo Reponse du serveur:
    curl -s http://localhost:5000/health
)

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo RÉSUMÉ:
echo   Si tous les tests sont OK, le serveur est pret!
echo   Sinon, executez INSTALL.bat puis START_SERVER.bat
echo.

:end
pause
