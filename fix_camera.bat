@echo off
echo ========================================
echo    REPARATION CAMERA FACE ID
echo ========================================
echo.

echo 1. Fermeture des applications qui utilisent la camera...
taskkill /f /im Teams.exe 2>nul
taskkill /f /im Zoom.exe 2>nul
taskkill /f /im Skype.exe 2>nul
taskkill /f /im chrome.exe 2>nul
taskkill /f /im msedge.exe 2>nul

echo 2. Arret du serveur camera Python...
taskkill /f /im python.exe 2>nul
taskkill /f /im pythonw.exe 2>nul

echo 3. Attente 3 secondes...
timeout /t 3 /nobreak >nul

echo 4. Test de la camera avec Python...
cd /d "%~dp0"
python -c "import cv2; cap = cv2.VideoCapture(0, cv2.CAP_DSHOW); print('Camera OK:', cap.isOpened()); cap.release()"

echo 5. Demarrage du serveur camera...
start /b python camera_server.py

echo 6. Attente du serveur...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo CAMERA REPAREE ! Relancez Face ID.
echo ========================================
pause