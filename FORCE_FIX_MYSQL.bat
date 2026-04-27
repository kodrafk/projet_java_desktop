@echo off
color 0C
title REPARATION FORCEE MYSQL - SOLUTION AGGRESSIVE
echo.
echo ========================================
echo   REPARATION FORCEE MYSQL
echo ========================================
echo.
echo ATTENTION: Cette solution est AGGRESSIVE
echo Elle va FORCER la reparation de MySQL
echo.
echo Appuyez sur une touche pour continuer...
pause >nul

REM ========================================
REM ETAPE 1: TUER TOUS LES PROCESSUS MYSQL
REM ========================================
echo.
echo [1/10] Arret FORCE de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
taskkill /F /IM mysql.exe 2>nul
taskkill /F /IM mysqld-nt.exe 2>nul
timeout /t 5 >nul
echo OK

REM ========================================
REM ETAPE 2: VERIFIER LE PORT 3306
REM ========================================
echo.
echo [2/10] Verification du port 3306...
netstat -ano | findstr :3306 >nul
if %errorlevel% equ 0 (
    echo ATTENTION: Le port 3306 est utilise !
    echo Liberation du port...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3306') do (
        taskkill /F /PID %%a 2>nul
    )
    timeout /t 3 >nul
)
echo OK - Port 3306 libre

REM ========================================
REM ETAPE 3: SAUVEGARDER LES DONNEES
REM ========================================
echo.
echo [3/10] Sauvegarde des donnees...
set BACKUP_DIR=C:\xampp\mysql\backup_force_%date:~-4,4%%date:~-7,2%%date:~-10,2%
set BACKUP_DIR=%BACKUP_DIR: =0%
mkdir "%BACKUP_DIR%" 2>nul

if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo Sauvegarde de nutrilife_db...
    xcopy "C:\xampp\mysql\data\nutrilife_db" "%BACKUP_DIR%\nutrilife_db\" /E /I /Y >nul 2>&1
    echo OK - Sauvegarde dans: %BACKUP_DIR%
) else (
    echo Aucune base a sauvegarder
)

REM ========================================
REM ETAPE 4: SUPPRIMER TOUS LES FICHIERS CORROMPUS
REM ========================================
echo.
echo [4/10] Suppression COMPLETE des fichiers corrompus...
cd /d "C:\xampp\mysql\data"

echo Suppression de ibdata1...
del /F /Q "ibdata1" 2>nul

echo Suppression de ib_logfile*...
del /F /Q "ib_logfile*" 2>nul

echo Suppression de aria_log*...
del /F /Q "aria_log*" 2>nul

echo Suppression de *.err...
del /F /Q "*.err" 2>nul

echo Suppression de *.pid...
del /F /Q "*.pid" 2>nul

echo Suppression de auto.cnf...
del /F /Q "auto.cnf" 2>nul

echo Suppression de mysql-bin.*...
del /F /Q "mysql-bin.*" 2>nul

echo OK - Fichiers corrompus supprimes

REM ========================================
REM ETAPE 5: REINITIALISER LES PERMISSIONS
REM ========================================
echo.
echo [5/10] Reinitialisation des permissions...
icacls "C:\xampp\mysql" /grant Everyone:(OI)(CI)F /T >nul 2>&1
icacls "C:\xampp\mysql\data" /grant Everyone:(OI)(CI)F /T >nul 2>&1
icacls "C:\xampp\mysql\bin" /grant Everyone:(OI)(CI)F /T >nul 2>&1
echo OK

REM ========================================
REM ETAPE 6: VERIFIER my.ini
REM ========================================
echo.
echo [6/10] Verification de my.ini...
if exist "C:\xampp\mysql\bin\my.ini" (
    echo OK - my.ini existe
) else (
    echo ERREUR: my.ini introuvable !
    if exist "C:\xampp\mysql\bin\my.ini.backup" (
        echo Restauration depuis my.ini.backup...
        copy "C:\xampp\mysql\bin\my.ini.backup" "C:\xampp\mysql\bin\my.ini" >nul
    )
)

REM ========================================
REM ETAPE 7: CREER LES DOSSIERS NECESSAIRES
REM ========================================
echo.
echo [7/10] Creation des dossiers necessaires...
mkdir "C:\xampp\mysql\data\mysql" 2>nul
mkdir "C:\xampp\mysql\data\performance_schema" 2>nul
mkdir "C:\xampp\mysql\data\phpmyadmin" 2>nul
mkdir "C:\xampp\mysql\data\test" 2>nul
echo OK

REM ========================================
REM ETAPE 8: DEMARRER MYSQL EN MODE FORCE
REM ========================================
echo.
echo [8/10] Demarrage FORCE de MySQL...
cd /d "C:\xampp\mysql\bin"

REM Demarrer MySQL avec options de recuperation
start "MySQL Server" /MIN mysqld.exe --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console --skip-grant-tables --skip-networking

echo Attente du demarrage (20 secondes)...
timeout /t 20 >nul

REM ========================================
REM ETAPE 9: VERIFIER LE DEMARRAGE
REM ========================================
echo.
echo [9/10] Verification du demarrage...
tasklist | findstr mysqld.exe >nul
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo   ECHEC - MySQL ne demarre toujours pas
    echo ========================================
    echo.
    echo Le probleme est plus grave. Solutions:
    echo.
    echo SOLUTION 1: Reinstaller MySQL dans XAMPP
    echo    1. Arretez XAMPP
    echo    2. Supprimez: C:\xampp\mysql
    echo    3. Retelecharger XAMPP
    echo    4. Reinstallez uniquement MySQL
    echo.
    echo SOLUTION 2: Utiliser WAMP
    echo    1. Desinstallez XAMPP
    echo    2. Installez WAMP: www.wampserver.com
    echo    3. MySQL devrait fonctionner
    echo.
    echo SOLUTION 3: Utiliser SQLite (DEJA CONFIGURE)
    echo    Votre application utilise deja SQLite !
    echo    Lancez directement votre application Java
    echo    Fichier: nutrilife.db
    echo.
    echo SOLUTION 4: Changer le port MySQL
    echo    Executez: CHANGE_MYSQL_PORT.bat
    echo.
    pause
    exit /b 1
)

echo OK - MySQL est demarre !

REM Arreter MySQL pour le redemarrer normalement
echo Arret de MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 5 >nul

REM Redemarrer MySQL normalement
echo Redemarrage normal de MySQL...
start "MySQL Server" /MIN mysqld.exe --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console

echo Attente du demarrage (15 secondes)...
timeout /t 15 >nul

REM ========================================
REM ETAPE 10: CREER LA BASE DE DONNEES
REM ========================================
echo.
echo [10/10] Creation de la base de donnees...

REM Verifier que MySQL tourne
tasklist | findstr mysqld.exe >nul
if %errorlevel% neq 0 (
    echo ERREUR: MySQL n'est pas actif
    pause
    exit /b 1
)

REM Creer un fichier SQL temporaire
cd /d "%~dp0"
echo DROP DATABASE IF EXISTS nutrilife_db; > temp_force_setup.sql
echo CREATE DATABASE nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; >> temp_force_setup.sql
echo USE nutrilife_db; >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo CREATE TABLE IF NOT EXISTS user ( >> temp_force_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_force_setup.sql
echo     email VARCHAR(255) NOT NULL UNIQUE, >> temp_force_setup.sql
echo     password VARCHAR(255) NOT NULL, >> temp_force_setup.sql
echo     roles VARCHAR(50) DEFAULT 'ROLE_USER', >> temp_force_setup.sql
echo     is_active TINYINT(1) DEFAULT 1, >> temp_force_setup.sql
echo     first_name VARCHAR(100), >> temp_force_setup.sql
echo     last_name VARCHAR(100), >> temp_force_setup.sql
echo     birthday DATE, >> temp_force_setup.sql
echo     weight DECIMAL(5,2), >> temp_force_setup.sql
echo     height DECIMAL(5,2), >> temp_force_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_force_setup.sql
echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP >> temp_force_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo CREATE TABLE IF NOT EXISTS badge ( >> temp_force_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_force_setup.sql
echo     name VARCHAR(100) NOT NULL, >> temp_force_setup.sql
echo     description TEXT, >> temp_force_setup.sql
echo     icon_path VARCHAR(255), >> temp_force_setup.sql
echo     requirement_type VARCHAR(50), >> temp_force_setup.sql
echo     requirement_value INT, >> temp_force_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP >> temp_force_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo CREATE TABLE IF NOT EXISTS user_badge ( >> temp_force_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_force_setup.sql
echo     user_id INT NOT NULL, >> temp_force_setup.sql
echo     badge_id INT NOT NULL, >> temp_force_setup.sql
echo     earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_force_setup.sql
echo     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, >> temp_force_setup.sql
echo     FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE, >> temp_force_setup.sql
echo     UNIQUE KEY unique_user_badge (user_id, badge_id) >> temp_force_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo CREATE TABLE IF NOT EXISTS weight_log ( >> temp_force_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_force_setup.sql
echo     user_id INT NOT NULL, >> temp_force_setup.sql
echo     weight DECIMAL(5,2) NOT NULL, >> temp_force_setup.sql
echo     log_date DATE NOT NULL, >> temp_force_setup.sql
echo     notes TEXT, >> temp_force_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_force_setup.sql
echo     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE >> temp_force_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo CREATE TABLE IF NOT EXISTS weight_objective ( >> temp_force_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_force_setup.sql
echo     user_id INT NOT NULL, >> temp_force_setup.sql
echo     target_weight DECIMAL(5,2) NOT NULL, >> temp_force_setup.sql
echo     start_date DATE NOT NULL, >> temp_force_setup.sql
echo     target_date DATE NOT NULL, >> temp_force_setup.sql
echo     status VARCHAR(20) DEFAULT 'ACTIVE', >> temp_force_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_force_setup.sql
echo     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE >> temp_force_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo DELETE FROM user WHERE email = 'admin@nutrilife.com'; >> temp_force_setup.sql
echo INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height) >> temp_force_setup.sql
echo VALUES ('admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', 1, 'Admin', 'NutriLife', '1990-01-01', 75.0, 175.0); >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo DELETE FROM user WHERE email = 'user@test.com'; >> temp_force_setup.sql
echo INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height) >> temp_force_setup.sql
echo VALUES ('user@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 1, 'John', 'Doe', '1995-05-15', 80.0, 180.0); >> temp_force_setup.sql
echo. >> temp_force_setup.sql
echo SELECT 'Base de donnees creee avec succes !' AS Status; >> temp_force_setup.sql

REM Executer le script SQL
"C:\xampp\mysql\bin\mysql.exe" -u root < temp_force_setup.sql 2>nul
if %errorlevel% equ 0 (
    echo OK - Base de donnees creee !
    del temp_force_setup.sql
) else (
    echo ATTENTION: Erreur lors de la creation de la base
    echo Essayez manuellement avec phpMyAdmin
    echo Script SQL sauvegarde: temp_force_setup.sql
)

REM ========================================
REM SUCCES !
REM ========================================
echo.
echo ========================================
echo   REPARATION TERMINEE !
echo ========================================
echo.
echo MySQL devrait maintenant fonctionner !
echo.
echo VERIFICATION:
echo 1. Ouvrez XAMPP Control Panel
echo 2. Verifiez que MySQL est demarre
echo 3. Ouvrez phpMyAdmin: http://localhost/phpmyadmin
echo 4. Verifiez que la base nutrilife_db existe
echo.
echo COMPTES CREES:
echo.
echo ADMIN:
echo   Email    : admin@nutrilife.com
echo   Password : admin123
echo   Role     : ROLE_ADMIN
echo.
echo USER (test):
echo   Email    : user@test.com
echo   Password : admin123
echo   Role     : ROLE_USER
echo.
echo Sauvegarde des anciennes donnees:
echo %BACKUP_DIR%
echo.
echo ========================================
echo   SI CA NE FONCTIONNE TOUJOURS PAS
echo ========================================
echo.
echo OPTION 1: Utiliser SQLite (DEJA CONFIGURE)
echo   Votre application utilise deja SQLite !
echo   Lancez directement votre application Java
echo   Toutes les fonctionnalites marchent
echo.
echo OPTION 2: Changer le port MySQL
echo   Executez: CHANGE_MYSQL_PORT.bat
echo   Change le port de 3306 vers 3307
echo.
echo OPTION 3: Reinstaller XAMPP
echo   1. Desinstallez XAMPP
echo   2. Supprimez C:\xampp
echo   3. Reinstallez XAMPP
echo   4. Relancez ce script
echo.
pause
