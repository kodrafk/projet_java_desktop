@echo off
title CAMERA FORCE SERVER
echo ========================================
echo    SERVEUR CAMERA FORCE - DEMARRAGE
echo ========================================
echo.

cd /d "%~dp0"

echo Fermeture des anciens processus...
taskkill /f /im python.exe 2>nul
timeout /t 2 /nobreak >nul

echo Lancement du serveur camera FORCE...
python camera_server_force.py

pause