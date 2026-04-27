@echo off
color 0E
title DIAGNOSTIC COMPLET MYSQL
echo.
echo ========================================
echo   DIAGNOSTIC COMPLET MYSQL
echo ========================================
echo.

REM ========================================
REM TEST 1: PROCESSUS MYSQL
REM ========================================
echo [TEST 1] Verification des processus MySQL...
tasklist | findstr mysqld >nul
if %errorlevel% equ 0 (
    echo [OK] MySQL est actif
    tasklist | findstr mysqld
) else (
    echo [ERREUR] MySQL n'est pas actif
)
echo.

REM ========================================
REM TEST 2: PORT 3306
REM ========================================
echo [TEST 2] Verification du port 3306...
netstat -ano | findstr :3306 >nul
if %errorlevel% equ 0 (
    echo [ATTENTION] Port 3306 est utilise
    netstat -ano | findstr :3306
    echo.
    echo Processus utilisant le port:
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3306') do (
        echo PID: %%a
        tasklist /FI "PID eq %%a"
    )
) else (
    echo [OK] Port 3306 est libre
)
echo.

REM ========================================
REM TEST 3: FICHIERS MYSQL
REM ========================================
echo [TEST 3] Verification des fichiers MySQL...

if exist "C:\xampp\mysql\bin\mysqld.exe" (
    echo [OK] mysqld.exe existe
) else (
    echo [ERREUR] mysqld.exe introuvable !
)

if exist "C:\xampp\mysql\bin\my.ini" (
    echo [OK] my.ini existe
) else (
    echo [ERREUR] my.ini introuvable !
)

if exist "C:\xampp\mysql\data" (
    echo [OK] Dossier data existe
) else (
    echo [ERREUR] Dossier data introuvable !
)
echo.

REM ========================================
REM TEST 4: FICHIERS CORROMPUS
REM ========================================
echo [TEST 4] Verification des fichiers corrompus...

if exist "C:\xampp\mysql\data\ibdata1" (
    echo [ATTENTION] ibdata1 existe (peut etre corrompu)
) else (
    echo [INFO] ibdata1 n'existe pas (sera recree)
)

if exist "C:\xampp\mysql\data\ib_logfile0" (
    echo [ATTENTION] ib_logfile0 existe (peut etre corrompu)
) else (
    echo [INFO] ib_logfile0 n'existe pas (sera recree)
)

if exist "C:\xampp\mysql\data\*.err" (
    echo [ATTENTION] Fichiers d'erreur trouves
    dir /B "C:\xampp\mysql\data\*.err"
) else (
    echo [OK] Aucun fichier d'erreur
)
echo.

REM ========================================
REM TEST 5: BASE DE DONNEES
REM ========================================
echo [TEST 5] Verification de la base de donnees...

if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo [OK] Base nutrilife_db existe
    dir "C:\xampp\mysql\data\nutrilife_db" | findstr "frm"
) else (
    echo [INFO] Base nutrilife_db n'existe pas (sera creee)
)
echo.

REM ========================================
REM TEST 6: LOGS D'ERREUR
REM ========================================
echo [TEST 6] Lecture des logs d'erreur...

if exist "C:\xampp\mysql\data\*.err" (
    echo Derniers logs d'erreur:
    echo ----------------------------------------
    for %%f in (C:\xampp\mysql\data\*.err) do (
        echo Fichier: %%f
        type "%%f" | findstr /I "ERROR FATAL" | more
    )
    echo ----------------------------------------
) else (
    echo [OK] Aucun log d'erreur
)
echo.

REM ========================================
REM TEST 7: PERMISSIONS
REM ========================================
echo [TEST 7] Verification des permissions...

icacls "C:\xampp\mysql\data" | findstr "Everyone" >nul
if %errorlevel% equ 0 (
    echo [OK] Permissions correctes
) else (
    echo [ATTENTION] Permissions manquantes
)
echo.

REM ========================================
REM TEST 8: CONFIGURATION my.ini
REM ========================================
echo [TEST 8] Verification de my.ini...

if exist "C:\xampp\mysql\bin\my.ini" (
    echo Port configure:
    findstr /C:"port=" "C:\xampp\mysql\bin\my.ini" | findstr /V "#"
    echo.
    echo Dossier data:
    findstr /C:"datadir=" "C:\xampp\mysql\bin\my.ini" | findstr /V "#"
) else (
    echo [ERREUR] my.ini introuvable
)
echo.

REM ========================================
REM TEST 9: ESPACE DISQUE
REM ========================================
echo [TEST 9] Verification de l'espace disque...
wmic logicaldisk where "DeviceID='C:'" get FreeSpace,Size /format:list | findstr "="
echo.

REM ========================================
REM TEST 10: SERVICES WINDOWS
REM ========================================
echo [TEST 10] Verification des services MySQL...
sc query | findstr /I "mysql" >nul
if %errorlevel% equ 0 (
    echo Services MySQL trouves:
    sc query | findstr /I "mysql"
) else (
    echo [INFO] Aucun service MySQL installe (normal pour XAMPP)
)
echo.

REM ========================================
REM RESUME
REM ========================================
echo ========================================
echo   RESUME DU DIAGNOSTIC
echo ========================================
echo.

tasklist | findstr mysqld >nul
if %errorlevel% equ 0 (
    echo [OK] MySQL est actif
) else (
    echo [ERREUR] MySQL n'est pas actif
)

netstat -ano | findstr :3306 >nul
if %errorlevel% equ 0 (
    echo [ATTENTION] Port 3306 est utilise
) else (
    echo [OK] Port 3306 est libre
)

if exist "C:\xampp\mysql\bin\mysqld.exe" (
    echo [OK] Fichiers MySQL presents
) else (
    echo [ERREUR] Fichiers MySQL manquants
)

if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo [OK] Base nutrilife_db existe
) else (
    echo [INFO] Base nutrilife_db n'existe pas
)

echo.
echo ========================================
echo   RECOMMANDATIONS
echo ========================================
echo.

tasklist | findstr mysqld >nul
if %errorlevel% neq 0 (
    echo MySQL n'est pas actif. Solutions:
    echo.
    echo 1. REPARATION FORCEE (RECOMMANDE)
    echo    Executez: FORCE_FIX_MYSQL.bat
    echo.
    echo 2. CHANGER LE PORT
    echo    Executez: CHANGE_MYSQL_PORT.bat
    echo.
    echo 3. UTILISER SQLITE
    echo    Lancez directement votre application
    echo    SQLite est deja configure
    echo.
) else (
    echo MySQL est actif !
    echo.
    if not exist "C:\xampp\mysql\data\nutrilife_db" (
        echo La base nutrilife_db n'existe pas.
        echo.
        echo Executez: FORCE_FIX_MYSQL.bat
        echo Pour creer la base de donnees
    ) else (
        echo Tout semble OK !
        echo Ouvrez phpMyAdmin: http://localhost/phpmyadmin
    )
)

echo.
pause
