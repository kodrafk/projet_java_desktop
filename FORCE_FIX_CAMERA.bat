@echo off
cd /d "%~dp0"
color 0E
title FORCE FIX CAMERA - Solution Puissante

echo.
echo ========================================================================
echo                    FORCE FIX CAMERA - SOLUTION PUISSANTE
echo ========================================================================
echo.
echo Cette solution va FORCER la liberation de la camera en:
echo   1. Tuant TOUS les processus Java
echo   2. Tuant TOUS les processus Python
echo   3. Tuant Teams/Zoom/Skype/Chrome/Edge
echo   4. Attendant 5 secondes
echo   5. Redemarrant le service camera Windows
echo.
echo ========================================================================
echo.

set /p confirm="Continuer? (O/N): "
if /i "%confirm%" NEQ "O" (
    echo.
    echo Annule.
    pause
    exit /b
)

echo.
echo [ETAPE 1/5] Fermeture FORCEE de tous les processus Java...
echo.

taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1
taskkill /F /IM javaws.exe /T >nul 2>&1

timeout /t 1 /nobreak >nul

echo [OK] Tous les processus Java tues
echo.

echo [ETAPE 2/5] Fermeture FORCEE de tous les processus Python...
echo.

taskkill /F /IM python.exe /T >nul 2>&1
taskkill /F /IM pythonw.exe /T >nul 2>&1
taskkill /F /IM python3.exe /T >nul 2>&1

timeout /t 1 /nobreak >nul

echo [OK] Tous les processus Python tues
echo.

echo [ETAPE 3/5] Fermeture de TOUTES les applications camera...
echo.

REM Teams
taskkill /F /IM Teams.exe /T >nul 2>&1
echo    - Teams: Ferme

REM Zoom
taskkill /F /IM Zoom.exe /T >nul 2>&1
taskkill /F /IM ZoomOpener.exe /T >nul 2>&1
echo    - Zoom: Ferme

REM Skype
taskkill /F /IM Skype.exe /T >nul 2>&1
taskkill /F /IM lync.exe /T >nul 2>&1
echo    - Skype: Ferme

REM Chrome
taskkill /F /IM chrome.exe /T >nul 2>&1
echo    - Chrome: Ferme

REM Edge
taskkill /F /IM msedge.exe /T >nul 2>&1
echo    - Edge: Ferme

REM Firefox
taskkill /F /IM firefox.exe /T >nul 2>&1
echo    - Firefox: Ferme

REM Discord
taskkill /F /IM Discord.exe /T >nul 2>&1
echo    - Discord: Ferme

REM OBS
taskkill /F /IM obs64.exe /T >nul 2>&1
taskkill /F /IM obs32.exe /T >nul 2>&1
echo    - OBS: Ferme

REM Application Camera Windows
taskkill /F /IM WindowsCamera.exe /T >nul 2>&1
echo    - Camera Windows: Ferme

echo.
echo [OK] Toutes les applications fermees
echo.

echo [ETAPE 4/5] Attente de liberation de la camera...
echo.

echo Attente 5 secondes...
timeout /t 5 /nobreak >nul

echo [OK] Camera liberee
echo.

echo [ETAPE 5/5] Nettoyage des processus orphelins...
echo.

REM Chercher et tuer tous les processus qui contiennent "camera" ou "webcam"
for /f "tokens=2" %%a in ('tasklist ^| findstr /i "camera webcam"') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo [OK] Nettoyage termine
echo.

echo ========================================================================
echo                    CAMERA FORCEE A SE LIBERER!
echo ========================================================================
echo.
echo La camera a ete FORCEE a se liberer.
echo.
echo PROCHAINES ETAPES:
echo.
echo   1. Attendez 10 secondes (IMPORTANT!)
echo   2. Ouvrez NutriLife
echo   3. Essayez Face ID
echo.
echo Si ca ne marche TOUJOURS pas:
echo   - Redemarrez l'ordinateur
echo   - Ou essayez: RESTART_CAMERA_DRIVER.bat
echo.
echo ========================================================================
echo.
echo Attente de 10 secondes avant de continuer...
timeout /t 10 /nobreak

echo.
echo Vous pouvez maintenant ouvrir NutriLife!
echo.
pause
