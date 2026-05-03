@echo off
title TEST CAMERA SARXOS
color 0A
echo ========================================
echo    TEST CAMERA DIRECT SARXOS
echo ========================================
echo.

cd /d "%~dp0"

echo Fermeture des applications conflictuelles...
taskkill /f /im Teams.exe 2>nul
taskkill /f /im Zoom.exe 2>nul
taskkill /f /im Skype.exe 2>nul
taskkill /f /im chrome.exe 2>nul
taskkill /f /im msedge.exe 2>nul
timeout /t 2 /nobreak >nul

echo Compilation du test...
javac -cp "lib/*;target/classes" TestDirectCamera.java
if %errorlevel% neq 0 (
    echo ❌ ERREUR DE COMPILATION!
    echo Vérifiez que les librairies sarxos sont dans lib/
    pause
    exit /b 1
)

echo Lancement du test...
java -cp ".;lib/*;target/classes" TestDirectCamera

echo.
if exist test_camera_sarxos.jpg (
    echo ✅ IMAGE CAPTURÉE: test_camera_sarxos.jpg
    echo La caméra fonctionne! Face ID devrait marcher maintenant.
) else (
    echo ❌ Pas d'image capturée - problème de caméra
)

echo.
pause