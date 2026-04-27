@echo off
color 0B
title GESTIONNAIRE MYSQL NUTRILIFE
:MENU
cls
echo.
echo ========================================
echo   GESTIONNAIRE MYSQL NUTRILIFE
echo ========================================
echo.
echo Choisissez une option:
echo.
echo [1] Reparer MySQL + Creer la base (RECOMMANDE)
echo [2] REPARATION FORCEE (si option 1 ne marche pas)
echo [3] Diagnostic complet MySQL
echo [4] Changer le port MySQL (3306 -^> 3307)
echo [5] Sauvegarder la base de donnees
echo [6] Restaurer la base de donnees
echo [7] Verifier l'etat de MySQL
echo [8] Voir les logs d'erreur MySQL
echo [9] Arreter MySQL
echo [A] Demarrer MySQL
echo [B] Ouvrir phpMyAdmin
echo [0] Quitter
echo.
echo ========================================
echo.

set /p CHOICE="Votre choix: "

if "%CHOICE%"=="1" goto REPAIR
if "%CHOICE%"=="2" goto FORCE_REPAIR
if "%CHOICE%"=="3" goto DIAGNOSTIC
if "%CHOICE%"=="4" goto CHANGE_PORT
if "%CHOICE%"=="5" goto BACKUP
if "%CHOICE%"=="6" goto RESTORE
if "%CHOICE%"=="7" goto STATUS
if "%CHOICE%"=="8" goto LOGS
if "%CHOICE%"=="9" goto STOP
if /I "%CHOICE%"=="A" goto START
if /I "%CHOICE%"=="B" goto PHPMYADMIN
if "%CHOICE%"=="0" goto EXIT

echo.
echo Choix invalide !
timeout /t 2 >nul
goto MENU

REM ========================================
REM OPTION 1: REPARER MYSQL
REM ========================================
:REPAIR
cls
echo.
echo ========================================
echo   REPARATION MYSQL + BASE DE DONNEES
echo ========================================
echo.
call FIX_MYSQL_COMPLETE.bat
pause
goto MENU

REM ========================================
REM OPTION 2: REPARATION FORCEE
REM ========================================
:FORCE_REPAIR
cls
echo.
echo ========================================
echo   REPARATION FORCEE MYSQL
echo ========================================
echo.
call FORCE_FIX_MYSQL.bat
pause
goto MENU

REM ========================================
REM OPTION 3: DIAGNOSTIC
REM ========================================
:DIAGNOSTIC
cls
echo.
echo ========================================
echo   DIAGNOSTIC COMPLET MYSQL
echo ========================================
echo.
call DIAGNOSTIC_MYSQL.bat
pause
goto MENU

REM ========================================
REM OPTION 4: CHANGER LE PORT
REM ========================================
:CHANGE_PORT
cls
echo.
echo ========================================
echo   CHANGER LE PORT MYSQL
echo ========================================
echo.
call CHANGE_MYSQL_PORT.bat
pause
goto MENU

REM ========================================
REM OPTION 5: SAUVEGARDER
REM ========================================
:BACKUP
cls
echo.
echo ========================================
echo   SAUVEGARDE BASE DE DONNEES
echo ========================================
echo.
call BACKUP_DATABASE.bat
pause
goto MENU

REM ========================================
REM OPTION 6: RESTAURER
REM ========================================
:RESTORE
cls
echo.
echo ========================================
echo   RESTAURATION BASE DE DONNEES
echo ========================================
echo.
call RESTORE_DATABASE.bat
pause
goto MENU

REM ========================================
REM OPTION 7: VERIFIER L'ETAT
REM ========================================
:STATUS
cls
echo.
echo ========================================
echo   ETAT DE MYSQL
echo ========================================
echo.

echo [1/3] Verification du processus MySQL...
tasklist | findstr mysqld.exe >nul
if %errorlevel% equ 0 (
    echo OK - MySQL est actif
    tasklist | findstr mysqld.exe
) else (
    echo ERREUR - MySQL n'est pas actif
)

echo.
echo [2/3] Verification du port 3306...
netstat -ano | findstr :3306 >nul
if %errorlevel% equ 0 (
    echo OK - Port 3306 est utilise
    netstat -ano | findstr :3306
) else (
    echo Port 3306 est libre
)

echo.
echo [3/3] Verification de la base de donnees...
if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo OK - Base nutrilife_db existe
    dir "C:\xampp\mysql\data\nutrilife_db" | findstr "frm"
) else (
    echo ERREUR - Base nutrilife_db introuvable
)

echo.
pause
goto MENU

REM ========================================
REM OPTION 8: VOIR LES LOGS
REM ========================================
:LOGS
cls
echo.
echo ========================================
echo   LOGS D'ERREUR MYSQL
echo ========================================
echo.

if exist "C:\xampp\mysql\data\*.err" (
    echo Derniers logs d'erreur:
    echo.
    for %%f in (C:\xampp\mysql\data\*.err) do (
        echo Fichier: %%f
        echo.
        type "%%f" | more
    )
) else (
    echo Aucun fichier d'erreur trouve
)

echo.
pause
goto MENU

REM ========================================
REM OPTION 9: ARRETER MYSQL
REM ========================================
:STOP
cls
echo.
echo ========================================
echo   ARRET DE MYSQL
echo ========================================
echo.

echo Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
if %errorlevel% equ 0 (
    echo OK - MySQL arrete
) else (
    echo MySQL n'etait pas actif
)

echo.
timeout /t 3
goto MENU

REM ========================================
REM OPTION A: DEMARRER MYSQL
REM ========================================
:START
cls
echo.
echo ========================================
echo   DEMARRAGE DE MYSQL
echo ========================================
echo.

REM Verifier si MySQL tourne deja
tasklist | findstr mysqld.exe >nul
if %errorlevel% equ 0 (
    echo MySQL est deja actif !
    echo.
    pause
    goto MENU
)

echo Demarrage de MySQL...
cd /d "C:\xampp\mysql\bin"
start "MySQL Server" /MIN mysqld.exe --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console

echo Attente du demarrage (10 secondes)...
timeout /t 10 >nul

REM Verifier si MySQL a demarre
tasklist | findstr mysqld.exe >nul
if %errorlevel% equ 0 (
    echo.
    echo OK - MySQL est demarre !
) else (
    echo.
    echo ERREUR - MySQL n'a pas demarre
    echo Utilisez l'option [1] pour reparer MySQL
)

echo.
pause
goto MENU

REM ========================================
REM OPTION B: OUVRIR PHPMYADMIN
REM ========================================
:PHPMYADMIN
cls
echo.
echo ========================================
echo   OUVERTURE DE PHPMYADMIN
echo ========================================
echo.

REM Verifier si MySQL tourne
tasklist | findstr mysqld.exe >nul
if %errorlevel% neq 0 (
    echo ATTENTION: MySQL n'est pas actif !
    echo Demarrez MySQL d'abord (option 8)
    echo.
    pause
    goto MENU
)

echo Ouverture de phpMyAdmin dans le navigateur...
start http://localhost/phpmyadmin

echo.
echo Si phpMyAdmin ne s'ouvre pas:
echo 1. Verifiez qu'Apache est demarre dans XAMPP
echo 2. Essayez: http://127.0.0.1/phpmyadmin
echo.
pause
goto MENU

REM ========================================
REM OPTION 0: QUITTER
REM ========================================
:EXIT
cls
echo.
echo Au revoir !
echo.
timeout /t 2 >nul
exit
