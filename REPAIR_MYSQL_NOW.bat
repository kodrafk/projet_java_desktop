@echo off
color 0A
echo ========================================
echo   REPARATION AUTOMATIQUE MYSQL
echo ========================================
echo.

echo [ETAPE 1] Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 3 >nul
echo OK

echo.
echo [ETAPE 2] Sauvegarde des bases de donnees...
if exist "C:\xampp\mysql\backup" (
    rmdir /S /Q "C:\xampp\mysql\backup"
)
mkdir "C:\xampp\mysql\backup" 2>nul

if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo Sauvegarde de nutrilife_db...
    xcopy "C:\xampp\mysql\data\nutrilife_db" "C:\xampp\mysql\backup\nutrilife_db\" /E /I /Y >nul
    echo OK - Base sauvegardee dans C:\xampp\mysql\backup\
) else (
    echo Aucune base de donnees a sauvegarder
)

echo.
echo [ETAPE 3] Suppression des fichiers corrompus...
cd /d "C:\xampp\mysql\data"

if exist "ibdata1" (
    echo Suppression de ibdata1...
    del /F /Q "ibdata1" 2>nul
)

if exist "ib_logfile0" (
    echo Suppression de ib_logfile0...
    del /F /Q "ib_logfile0" 2>nul
)

if exist "ib_logfile1" (
    echo Suppression de ib_logfile1...
    del /F /Q "ib_logfile1" 2>nul
)

if exist "aria_log_control" (
    echo Suppression de aria_log_control...
    del /F /Q "aria_log_control" 2>nul
)

if exist "aria_log.00000001" (
    echo Suppression de aria_log.00000001...
    del /F /Q "aria_log.00000001" 2>nul
)

echo OK - Fichiers corrompus supprimes

echo.
echo [ETAPE 4] Copie des fichiers de sauvegarde...
if exist "C:\xampp\mysql\backup\ibdata1" (
    copy "C:\xampp\mysql\backup\ibdata1" "C:\xampp\mysql\data\" >nul
    echo OK - Fichiers restaures
) else (
    echo Pas de sauvegarde - MySQL va recreer les fichiers
)

echo.
echo ========================================
echo   REPARATION TERMINEE !
echo ========================================
echo.
echo PROCHAINES ETAPES:
echo.
echo 1. Ouvrez XAMPP Control Panel
echo 2. Cliquez sur "Start" a cote de MySQL
echo 3. MySQL devrait demarrer normalement
echo.
echo Si MySQL demarre:
echo    - Votre base de donnees a ete sauvegardee dans C:\xampp\mysql\backup\
echo    - Vous devrez peut-etre recreer la base nutrilife_db
echo.
echo Si MySQL ne demarre toujours pas:
echo    - Cliquez sur "Logs" dans XAMPP pour voir l'erreur
echo    - Essayez la SOLUTION 2 (changer le port)
echo.
pause
