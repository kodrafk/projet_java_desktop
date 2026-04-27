@echo off
color 0E
title REINITIALISATION COMPLETE MYSQL
cls

echo.
echo ========================================
echo   REINITIALISATION COMPLETE MYSQL
echo ========================================
echo.
echo ATTENTION: Cette operation va:
echo - Supprimer TOUTES vos bases de donnees
echo - Reinitialiser MySQL completement
echo - Recreer MySQL comme une installation neuve
echo.
echo Appuyez sur CTRL+C pour annuler
echo Appuyez sur une touche pour continuer...
pause >nul

echo.
echo [1/8] Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
taskkill /F /IM mysql.exe 2>nul
timeout /t 3 >nul
echo OK

echo.
echo [2/8] Sauvegarde complete du dossier data...
if exist "C:\xampp\mysql\data_backup" (
    echo Suppression de l'ancienne sauvegarde...
    rmdir /S /Q "C:\xampp\mysql\data_backup" 2>nul
)
echo Creation de la sauvegarde...
xcopy "C:\xampp\mysql\data" "C:\xampp\mysql\data_backup\" /E /I /Y /Q >nul 2>&1
echo OK - Sauvegarde dans: C:\xampp\mysql\data_backup\

echo.
echo [3/8] Suppression complete du dossier data...
cd /d "C:\xampp\mysql"
rmdir /S /Q "data" 2>nul
timeout /t 2 >nul
echo OK

echo.
echo [4/8] Recreation du dossier data...
mkdir "data" 2>nul
mkdir "data\mysql" 2>nul
mkdir "data\performance_schema" 2>nul
mkdir "data\phpmyadmin" 2>nul
echo OK

echo.
echo [5/8] Copie des bases systeme depuis backup...
if exist "C:\xampp\mysql\backup" (
    echo Copie de mysql...
    xcopy "C:\xampp\mysql\backup\mysql" "C:\xampp\mysql\data\mysql\" /E /I /Y /Q >nul 2>&1
    echo Copie de performance_schema...
    xcopy "C:\xampp\mysql\backup\performance_schema" "C:\xampp\mysql\data\performance_schema\" /E /I /Y /Q >nul 2>&1
    echo Copie de phpmyadmin...
    xcopy "C:\xampp\mysql\backup\phpmyadmin" "C:\xampp\mysql\data\phpmyadmin\" /E /I /Y /Q >nul 2>&1
    echo OK
) else (
    echo ATTENTION: Pas de backup trouve, MySQL va recreer les bases systeme
)

echo.
echo [6/8] Configuration des permissions...
icacls "C:\xampp\mysql\data" /grant Everyone:(OI)(CI)F /T >nul 2>&1
echo OK

echo.
echo [7/8] Demarrage de MySQL...
cd /d "C:\xampp\mysql\bin"

echo Initialisation de MySQL (peut prendre 30 secondes)...
mysqld.exe --initialize-insecure --datadir="C:\xampp\mysql\data" >nul 2>&1

echo Demarrage du serveur MySQL...
start "MySQL Server" /MIN mysqld.exe --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console

echo Attente du demarrage (15 secondes)...
timeout /t 15 >nul

echo.
echo [8/8] Verification...
tasklist | findstr mysqld.exe >nul
if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCES ! MySQL est demarre !
    echo ========================================
    echo.
    echo MySQL a ete reinitialise avec succes !
    echo.
    echo IMPORTANT:
    echo - Toutes vos anciennes bases ont ete supprimees
    echo - Votre ancienne data est sauvegardee dans: C:\xampp\mysql\data_backup\
    echo - MySQL utilise maintenant le mot de passe root vide
    echo.
    echo PROCHAINES ETAPES:
    echo 1. Ouvrez phpMyAdmin: http://localhost/phpmyadmin
    echo 2. Creez la base: nutrilife_db
    echo 3. Importez: CREATE_ADMIN_ACCOUNT.sql
    echo 4. Lancez votre application Java
    echo.
    
    echo Creation automatique de la base nutrilife_db...
    mysql.exe -u root -e "CREATE DATABASE IF NOT EXISTS nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
    if %errorlevel% equ 0 (
        echo OK - Base nutrilife_db creee !
    )
    
) else (
    echo.
    echo ========================================
    echo   ECHEC - MySQL ne demarre pas
    echo ========================================
    echo.
    echo Le probleme est critique. SOLUTIONS:
    echo.
    echo 1. REINSTALLER XAMPP COMPLETEMENT:
    echo    a. Panneau de configuration ^> Desinstaller XAMPP
    echo    b. Supprimer C:\xampp
    echo    c. Redemarrer l'ordinateur
    echo    d. Reinstaller XAMPP: https://www.apachefriends.org/
    echo.
    echo 2. UTILISER WAMP A LA PLACE:
    echo    a. Desinstaller XAMPP
    echo    b. Installer WAMP: https://www.wampserver.com/
    echo    c. WAMP est plus stable pour MySQL
    echo.
    echo 3. INSTALLER MYSQL SEUL:
    echo    a. Telecharger MySQL: https://dev.mysql.com/downloads/
    echo    b. Installer MySQL Workbench
    echo    c. Configurer MySQL sur le port 3306
    echo.
)

echo.
echo Appuyez sur une touche pour fermer...
pause >nul
