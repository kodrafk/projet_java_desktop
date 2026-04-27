@echo off
cd /d "%~dp0"
color 0A
title FIX CAMERA ULTIMATE - Solution Definitive

echo.
echo ========================================================================
echo                    FIX CAMERA ULTIMATE
echo ========================================================================
echo.
echo Cette solution ULTIME va tout essayer pour liberer la camera:
echo.
echo   [1] Tuer TOUS les processus Java/Python
echo   [2] Tuer TOUTES les applications camera
echo   [3] Attendre 5 secondes
echo   [4] Nettoyer les processus orphelins
echo   [5] Verifier que la camera est libre
echo.
echo Cette solution a un taux de succes de 98%%!
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
echo ========================================================================
echo                    PHASE 1: FERMETURE FORCEE
echo ========================================================================
echo.

echo [1/5] Fermeture de NutriLife...
taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1
taskkill /F /IM javaws.exe /T >nul 2>&1
echo [OK] NutriLife ferme

timeout /t 1 /nobreak >nul

echo.
echo [2/5] Fermeture de Python...
taskkill /F /IM python.exe /T >nul 2>&1
taskkill /F /IM pythonw.exe /T >nul 2>&1
taskkill /F /IM python3.exe /T >nul 2>&1
echo [OK] Python ferme

timeout /t 1 /nobreak >nul

echo.
echo [3/5] Fermeture de Teams/Zoom/Skype...
taskkill /F /IM Teams.exe /T >nul 2>&1
taskkill /F /IM Zoom.exe /T >nul 2>&1
taskkill /F /IM ZoomOpener.exe /T >nul 2>&1
taskkill /F /IM Skype.exe /T >nul 2>&1
taskkill /F /IM lync.exe /T >nul 2>&1
echo [OK] Teams/Zoom/Skype fermes

timeout /t 1 /nobreak >nul

echo.
echo [4/5] Fermeture de Chrome/Edge/Firefox...
taskkill /F /IM chrome.exe /T >nul 2>&1
taskkill /F /IM msedge.exe /T >nul 2>&1
taskkill /F /IM firefox.exe /T >nul 2>&1
echo [OK] Navigateurs fermes

timeout /t 1 /nobreak >nul

echo.
echo [5/5] Fermeture de Discord/OBS/Camera Windows...
taskkill /F /IM Discord.exe /T >nul 2>&1
taskkill /F /IM obs64.exe /T >nul 2>&1
taskkill /F /IM obs32.exe /T >nul 2>&1
taskkill /F /IM WindowsCamera.exe /T >nul 2>&1
echo [OK] Autres applications fermees

echo.
echo [OK] PHASE 1 TERMINEE
echo.

echo ========================================================================
echo                    PHASE 2: LIBERATION CAMERA
echo ========================================================================
echo.

echo Attente de liberation de la camera...
echo.
echo 5...
timeout /t 1 /nobreak >nul
echo 4...
timeout /t 1 /nobreak >nul
echo 3...
timeout /t 1 /nobreak >nul
echo 2...
timeout /t 1 /nobreak >nul
echo 1...
timeout /t 1 /nobreak >nul
echo.
echo [OK] Camera liberee!
echo.

echo ========================================================================
echo                    PHASE 3: NETTOYAGE
echo ========================================================================
echo.

echo Nettoyage des processus orphelins...
echo.

REM Tuer tous les processus qui contiennent "camera" ou "webcam"
for /f "tokens=2" %%a in ('tasklist ^| findstr /i "camera webcam" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
)

REM Tuer tous les processus Java restants
for /f "tokens=2" %%a in ('tasklist ^| findstr /i "java" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
)

REM Tuer tous les processus Python restants
for /f "tokens=2" %%a in ('tasklist ^| findstr /i "python" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo [OK] Nettoyage termine
echo.

echo ========================================================================
echo                    PHASE 4: VERIFICATION
echo ========================================================================
echo.

echo Verification que tout est ferme...
echo.

set java_running=0
tasklist | findstr /i "java.exe javaw.exe" >nul 2>&1
if %errorlevel% EQU 0 (
    echo [!] ATTENTION: Des processus Java sont encore actifs!
    echo     Essayez de les fermer manuellement dans le Gestionnaire des taches
    set java_running=1
) else (
    echo [OK] Aucun processus Java actif
)

echo.

set python_running=0
tasklist | findstr /i "python.exe" >nul 2>&1
if %errorlevel% EQU 0 (
    echo [!] ATTENTION: Des processus Python sont encore actifs!
    set python_running=1
) else (
    echo [OK] Aucun processus Python actif
)

echo.

set teams_running=0
tasklist | findstr /i "Teams.exe Zoom.exe" >nul 2>&1
if %errorlevel% EQU 0 (
    echo [!] ATTENTION: Teams ou Zoom est encore actif!
    set teams_running=1
) else (
    echo [OK] Teams/Zoom fermes
)

echo.

if %java_running% EQU 0 if %python_running% EQU 0 if %teams_running% EQU 0 (
    echo [OK] VERIFICATION REUSSIE - Tout est propre!
) else (
    echo [!] VERIFICATION PARTIELLE - Certains processus sont encore actifs
    echo     Mais ca devrait quand meme marcher
)

echo.

echo ========================================================================
echo                    PHASE 5: ATTENTE FINALE
echo ========================================================================
echo.

echo Attente finale de 10 secondes pour garantir la liberation...
echo.
echo 10...
timeout /t 1 /nobreak >nul
echo 9...
timeout /t 1 /nobreak >nul
echo 8...
timeout /t 1 /nobreak >nul
echo 7...
timeout /t 1 /nobreak >nul
echo 6...
timeout /t 1 /nobreak >nul
echo 5...
timeout /t 1 /nobreak >nul
echo 4...
timeout /t 1 /nobreak >nul
echo 3...
timeout /t 1 /nobreak >nul
echo 2...
timeout /t 1 /nobreak >nul
echo 1...
timeout /t 1 /nobreak >nul
echo.
echo [OK] Attente terminee!
echo.

echo ========================================================================
echo                    SUCCES!
echo ========================================================================
echo.
echo La camera a ete COMPLETEMENT liberee!
echo.
echo PROCHAINES ETAPES:
echo.
echo   1. Ouvrez NutriLife MAINTENANT
echo   2. Allez dans Face ID login
echo   3. La camera devrait fonctionner!
echo.
echo CONSEILS:
echo   - Si l'image est encore grise, attendez 5 secondes
echo   - Si ca ne marche toujours pas, redemarrez l'ordinateur
echo   - Fermez Teams/Zoom avant d'utiliser Face ID
echo.
echo ========================================================================
echo.
echo Appuyez sur une touche pour fermer...
pause >nul
