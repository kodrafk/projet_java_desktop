@echo off
title DIAGNOSTIC CAMERA COMPLET
color 0A
echo ========================================
echo    DIAGNOSTIC CAMERA FACE ID COMPLET
echo ========================================
echo.

cd /d "%~dp0"

echo [1/6] Verification des processus...
echo Applications qui utilisent la camera:
tasklist | findstr /i "teams zoom skype chrome msedge obs" 2>nul
if %errorlevel% equ 0 (
    echo ⚠️  ATTENTION: Applications detectees qui utilisent la camera!
    echo Fermeture automatique...
    taskkill /f /im Teams.exe 2>nul
    taskkill /f /im Zoom.exe 2>nul
    taskkill /f /im Skype.exe 2>nul
    taskkill /f /im chrome.exe 2>nul
    taskkill /f /im msedge.exe 2>nul
    taskkill /f /im obs64.exe 2>nul
    timeout /t 3 /nobreak >nul
) else (
    echo ✅ Aucune application conflictuelle detectee
)
echo.

echo [2/6] Arret des anciens serveurs Python...
taskkill /f /im python.exe 2>nul
taskkill /f /im pythonw.exe 2>nul
timeout /t 2 /nobreak >nul
echo ✅ Serveurs arretes
echo.

echo [3/6] Test Python et OpenCV...
python -c "import cv2; print('✅ OpenCV version:', cv2.__version__)" 2>nul
if %errorlevel% neq 0 (
    echo ❌ ERREUR: OpenCV non installe!
    echo Installez avec: pip install opencv-python
    pause
    exit /b 1
)
echo.

echo [4/6] Test direct de la camera...
python test_camera_direct.py
echo.

echo [5/6] Demarrage du serveur camera FORCE...
start /b python camera_server_force.py
timeout /t 5 /nobreak >nul

echo [6/6] Test de connexion au serveur...
python -c "import socket; s=socket.socket(); s.connect(('127.0.0.1',7654)); s.send(b'STATUS\n'); print('✅ Serveur:', s.recv(1024).decode().strip()); s.close()" 2>nul
if %errorlevel% equ 0 (
    echo ✅ Serveur camera operationnel!
) else (
    echo ❌ Serveur camera non accessible
)

echo.
echo ========================================
echo    DIAGNOSTIC TERMINE
echo ========================================
echo.
echo INSTRUCTIONS:
echo 1. Si tout est ✅, relancez Face ID
echo 2. Si erreurs ❌, contactez le support
echo 3. Le serveur camera tourne en arriere-plan
echo.
pause