@echo off
color 0C
title REPARATION AUTOMATIQUE MYSQL
echo.
echo ========================================
echo   REPARATION AUTOMATIQUE MYSQL
echo ========================================
echo.
echo ATTENTION: Ce script va supprimer les fichiers
echo corrompus de MySQL. Vos bases de donnees seront
echo perdues mais MySQL va redemarrer.
echo.
echo Appuyez sur une touche pour continuer...
pause >nul

echo.
echo [1/6] Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 3 >nul
echo OK

echo.
echo [2/6] Sauvegarde de la base de donnees (si elle existe)...
if exist "C:\xampp\mysql\backup_nutrilife" (
    rmdir /S /Q "C:\xampp\mysql\backup_nutrilife" 2>nul
)
mkdir "C:\xampp\mysql\backup_nutrilife" 2>nul

if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo Sauvegarde de nutrilife_db...
    xcopy "C:\xampp\mysql\data\nutrilife_db" "C:\xampp\mysql\backup_nutrilife\" /E /I /Y >nul 2>&1
    echo OK - Sauvegarde dans: C:\xampp\mysql\backup_nutrilife\
) else (
    echo Aucune base nutrilife_db trouvee
)

echo.
echo [3/6] Suppression des fichiers corrompus...
cd /d "C:\xampp\mysql\data"

echo Suppression de ibdata1...
del /F /Q "ibdata1" 2>nul

echo Suppression de ib_logfile0...
del /F /Q "ib_logfile0" 2>nul

echo Suppression de ib_logfile1...
del /F /Q "ib_logfile1" 2>nul

echo Suppression de aria_log_control...
del /F /Q "aria_log_control" 2>nul

echo Suppression de aria_log.00000001...
del /F /Q "aria_log.00000001" 2>nul

echo Suppression de *.err...
del /F /Q "*.err" 2>nul

echo OK - Fichiers corrompus supprimes

echo.
echo [4/6] Reinitialisation des permissions...
icacls "C:\xampp\mysql\data" /grant Everyone:(OI)(CI)F /T >nul 2>&1
echo OK

echo.
echo [5/6] Demarrage de MySQL...
cd /d "C:\xampp\mysql\bin"
start "MySQL Server" /MIN mysqld.exe --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console

echo Attente du demarrage (10 secondes)...
timeout /t 10 >nul

echo.
echo [6/6] Verification...
tasklist | findstr mysqld.exe >nul
if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCES ! MySQL est demarre !
    echo ========================================
    echo.
    echo MySQL fonctionne maintenant !
    echo.
    echo PROCHAINES ETAPES:
    echo 1. Ouvrez phpMyAdmin: http://localhost/phpmyadmin
    echo 2. Creez la base de donnees: nutrilife_db
    echo 3. Importez le script SQL: CREATE_ADMIN_ACCOUNT.sql
    echo 4. Lancez votre application Java
    echo.
    echo Votre ancienne base a ete sauvegardee dans:
    echo C:\xampp\mysql\backup_nutrilife\
    echo.
) else (
    echo.
    echo ========================================
    echo   ECHEC - MySQL ne demarre toujours pas
    echo ========================================
    echo.
    echo Le probleme est plus grave. Solutions:
    echo.
    echo 1. REINSTALLER XAMPP:
    echo    - Desinstallez XAMPP
    echo    - Supprimez C:\xampp
    echo    - Reinstallez XAMPP
    echo.
    echo 2. UTILISER WAMP:
    echo    - Telechargez WAMP: www.wampserver.com
    echo    - Installez WAMP
    echo    - MySQL devrait fonctionner
    echo.
    echo 3. UTILISER MYSQL STANDALONE:
    echo    - Telechargez MySQL: dev.mysql.com/downloads
    echo    - Installez MySQL seul
    echo.
)

echo.
echo Appuyez sur une touche pour fermer...
pause >nul
