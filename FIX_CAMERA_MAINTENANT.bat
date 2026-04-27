@echo off
cd /d "%~dp0"
color 0C
title FIX CAMERA - Solution Immediate

echo.
echo ========================================================================
echo                    FIX CAMERA - SOLUTION IMMEDIATE
echo ========================================================================
echo.
echo PROBLEME: Camera bloquee / image grise / figee
echo.
echo SOLUTION: Fermer tous les processus qui utilisent la camera
echo.
echo ========================================================================
echo.

echo [ETAPE 1/3] Fermeture de l'application NutriLife...
echo.

taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1

timeout /t 2 /nobreak >nul

echo [OK] Application fermee
echo.

echo [ETAPE 2/3] Fermeture des applications qui bloquent la camera...
echo.

REM Fermer Teams
taskkill /F /IM Teams.exe /T >nul 2>&1
echo    - Microsoft Teams: Ferme

REM Fermer Zoom
taskkill /F /IM Zoom.exe /T >nul 2>&1
echo    - Zoom: Ferme

REM Fermer Skype
taskkill /F /IM Skype.exe /T >nul 2>&1
echo    - Skype: Ferme

REM Fermer Chrome (si utilise la camera)
taskkill /F /IM chrome.exe /T >nul 2>&1
echo    - Chrome: Ferme

REM Fermer Edge
taskkill /F /IM msedge.exe /T >nul 2>&1
echo    - Edge: Ferme

REM Fermer Firefox
taskkill /F /IM firefox.exe /T >nul 2>&1
echo    - Firefox: Ferme

REM Fermer OBS
taskkill /F /IM obs64.exe /T >nul 2>&1
taskkill /F /IM obs32.exe /T >nul 2>&1
echo    - OBS Studio: Ferme

echo.
echo [OK] Applications fermees
echo.

timeout /t 2 /nobreak >nul

echo [ETAPE 3/3] Liberation de la camera...
echo.

REM Attendre que la camera soit liberee
timeout /t 3 /nobreak >nul

echo [OK] Camera liberee!
echo.
echo ========================================================================
echo                    CAMERA FIXEE!
echo ========================================================================
echo.
echo La camera est maintenant disponible.
echo.
echo PROCHAINES ETAPES:
echo   1. Relancez l'application NutriLife
echo   2. Essayez Face ID login
echo   3. La camera devrait fonctionner maintenant!
echo.
echo CONSEILS:
echo   - Fermez Teams/Zoom avant d'utiliser Face ID
echo   - Ne laissez pas Chrome/Edge ouverts avec des onglets camera
echo   - Si le probleme persiste, redemarrez l'ordinateur
echo.
echo ========================================================================
echo.
echo Appuyez sur une touche pour fermer...
pause >nul
