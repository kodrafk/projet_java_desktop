@echo off
color 0E
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🔧 FIX CAMERA BLACK SCREEN - FACE ID
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo [ETAPE 1] Fermeture des applications utilisant la camera...
echo.
taskkill /F /IM python.exe 2>nul
taskkill /F /IM pythonw.exe 2>nul
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul
timeout /t 2 >nul

echo.
echo [ETAPE 2] Verification de Python...
echo.
where python >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Python n'est pas installe ou pas dans le PATH
    echo.
    echo 💡 Installez Python : https://www.python.org/downloads/
    echo    Cochez "Add Python to PATH" pendant l'installation
    echo.
    pause
    exit /b 1
)

python --version
echo.

echo [ETAPE 3] Installation d'OpenCV...
echo.
python -m pip install --upgrade opencv-python
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ✗ Erreur d'installation d'OpenCV
    echo.
    pause
    exit /b 1
)

echo.
echo [ETAPE 4] Test du serveur camera...
echo.
echo Demarrage du serveur camera (appuyez sur Ctrl+C pour arreter)...
echo.
python camera_server.py 7654

pause
