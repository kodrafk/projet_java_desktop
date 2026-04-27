@echo off
cd /d "%~dp0"
color 0B
title Restart Camera Driver

echo.
echo ========================================================================
echo                    RESTART CAMERA DRIVER
echo ========================================================================
echo.
echo Cette solution va redemarrer le driver de la camera Windows.
echo.
echo ATTENTION: Necessite les droits Administrateur!
echo.
echo ========================================================================
echo.

REM Verifier si execute en tant qu'administrateur
net session >nul 2>&1
if %errorlevel% NEQ 0 (
    echo [!] ERREUR: Ce script necessite les droits Administrateur!
    echo.
    echo SOLUTION:
    echo   1. Clic droit sur ce fichier
    echo   2. "Executer en tant qu'administrateur"
    echo.
    pause
    exit /b 1
)

echo [OK] Droits Administrateur detectes
echo.

set /p confirm="Continuer? (O/N): "
if /i "%confirm%" NEQ "O" (
    echo.
    echo Annule.
    pause
    exit /b
)

echo.
echo [ETAPE 1/4] Arret du service Windows Camera...
echo.

net stop "Windows Camera Frame Server" >nul 2>&1
if %errorlevel% EQU 0 (
    echo [OK] Service arrete
) else (
    echo [!] Service deja arrete ou non trouve
)

timeout /t 2 /nobreak >nul

echo.
echo [ETAPE 2/4] Attente de liberation...
echo.

timeout /t 3 /nobreak >nul

echo [OK] Liberation terminee
echo.

echo [ETAPE 3/4] Redemarrage du service Windows Camera...
echo.

net start "Windows Camera Frame Server" >nul 2>&1
if %errorlevel% EQU 0 (
    echo [OK] Service redemarre
) else (
    echo [!] Impossible de redemarrer le service
    echo     Ce n'est pas grave, continuez quand meme
)

timeout /t 2 /nobreak >nul

echo.
echo [ETAPE 4/4] Verification...
echo.

timeout /t 2 /nobreak >nul

echo [OK] Driver camera redemarre!
echo.

echo ========================================================================
echo                    DRIVER CAMERA REDEMARRE!
echo ========================================================================
echo.
echo Le driver de la camera a ete redemarre.
echo.
echo PROCHAINES ETAPES:
echo.
echo   1. Attendez 5 secondes
echo   2. Ouvrez NutriLife
echo   3. Essayez Face ID
echo.
echo Si ca ne marche TOUJOURS pas:
echo   - Redemarrez l'ordinateur (solution garantie)
echo.
echo ========================================================================
echo.
echo Attente de 5 secondes...
timeout /t 5 /nobreak

echo.
echo Vous pouvez maintenant ouvrir NutriLife!
echo.
pause
