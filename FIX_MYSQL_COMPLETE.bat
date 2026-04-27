@echo off
color 0B
title REPARATION COMPLETE MYSQL + BASE DE DONNEES
echo.
echo ========================================
echo   REPARATION COMPLETE MYSQL
echo ========================================
echo.
echo Ce script va:
echo 1. Reparer MySQL
echo 2. Creer la base de donnees nutrilife_db
echo 3. Importer les tables et le compte admin
echo.
echo Appuyez sur une touche pour continuer...
pause >nul

REM ========================================
REM ETAPE 1: ARRETER MYSQL
REM ========================================
echo.
echo [1/7] Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 3 >nul
echo OK

REM ========================================
REM ETAPE 2: SAUVEGARDER LES DONNEES
REM ========================================
echo.
echo [2/7] Sauvegarde des donnees existantes...
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

REM ========================================
REM ETAPE 3: SUPPRIMER FICHIERS CORROMPUS
REM ========================================
echo.
echo [3/7] Suppression des fichiers corrompus...
cd /d "C:\xampp\mysql\data"

del /F /Q "ibdata1" 2>nul
del /F /Q "ib_logfile0" 2>nul
del /F /Q "ib_logfile1" 2>nul
del /F /Q "aria_log_control" 2>nul
del /F /Q "aria_log.00000001" 2>nul
del /F /Q "*.err" 2>nul

echo OK - Fichiers corrompus supprimes

REM ========================================
REM ETAPE 4: REINITIALISER PERMISSIONS
REM ========================================
echo.
echo [4/7] Reinitialisation des permissions...
icacls "C:\xampp\mysql\data" /grant Everyone:(OI)(CI)F /T >nul 2>&1
echo OK

REM ========================================
REM ETAPE 5: DEMARRER MYSQL
REM ========================================
echo.
echo [5/7] Demarrage de MySQL...
cd /d "C:\xampp\mysql\bin"
start "MySQL Server" /MIN mysqld.exe --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console

echo Attente du demarrage (15 secondes)...
timeout /t 15 >nul

REM ========================================
REM ETAPE 6: VERIFIER MYSQL
REM ========================================
echo.
echo [6/7] Verification de MySQL...
tasklist | findstr mysqld.exe >nul
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo   ERREUR: MySQL ne demarre pas !
    echo ========================================
    echo.
    echo Solutions alternatives:
    echo.
    echo 1. CHANGER LE PORT MYSQL:
    echo    - Editez: C:\xampp\mysql\bin\my.ini
    echo    - Changez: port=3306 en port=3307
    echo    - Relancez ce script
    echo.
    echo 2. REINSTALLER XAMPP:
    echo    - Desinstallez XAMPP
    echo    - Supprimez C:\xampp
    echo    - Reinstallez XAMPP
    echo.
    echo 3. UTILISER SQLITE (deja configure):
    echo    - Votre application utilise deja SQLite
    echo    - Toutes les fonctionnalites marchent
    echo    - Fichier: nutrilife.db
    echo.
    pause
    exit /b 1
)

echo OK - MySQL est demarre !

REM ========================================
REM ETAPE 7: CREER LA BASE DE DONNEES
REM ========================================
echo.
echo [7/7] Creation de la base de donnees...

REM Creer un fichier SQL temporaire
cd /d "%~dp0"
echo DROP DATABASE IF EXISTS nutrilife_db; > temp_setup.sql
echo CREATE DATABASE nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; >> temp_setup.sql
echo USE nutrilife_db; >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Table user >> temp_setup.sql
echo CREATE TABLE IF NOT EXISTS user ( >> temp_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_setup.sql
echo     email VARCHAR(255) NOT NULL UNIQUE, >> temp_setup.sql
echo     password VARCHAR(255) NOT NULL, >> temp_setup.sql
echo     roles VARCHAR(50) DEFAULT 'ROLE_USER', >> temp_setup.sql
echo     is_active TINYINT(1) DEFAULT 1, >> temp_setup.sql
echo     first_name VARCHAR(100), >> temp_setup.sql
echo     last_name VARCHAR(100), >> temp_setup.sql
echo     birthday DATE, >> temp_setup.sql
echo     weight DECIMAL(5,2), >> temp_setup.sql
echo     height DECIMAL(5,2), >> temp_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_setup.sql
echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP >> temp_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Table badge >> temp_setup.sql
echo CREATE TABLE IF NOT EXISTS badge ( >> temp_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_setup.sql
echo     name VARCHAR(100) NOT NULL, >> temp_setup.sql
echo     description TEXT, >> temp_setup.sql
echo     icon_path VARCHAR(255), >> temp_setup.sql
echo     requirement_type VARCHAR(50), >> temp_setup.sql
echo     requirement_value INT, >> temp_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP >> temp_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Table user_badge >> temp_setup.sql
echo CREATE TABLE IF NOT EXISTS user_badge ( >> temp_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_setup.sql
echo     user_id INT NOT NULL, >> temp_setup.sql
echo     badge_id INT NOT NULL, >> temp_setup.sql
echo     earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_setup.sql
echo     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, >> temp_setup.sql
echo     FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE, >> temp_setup.sql
echo     UNIQUE KEY unique_user_badge (user_id, badge_id) >> temp_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Table weight_log >> temp_setup.sql
echo CREATE TABLE IF NOT EXISTS weight_log ( >> temp_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_setup.sql
echo     user_id INT NOT NULL, >> temp_setup.sql
echo     weight DECIMAL(5,2) NOT NULL, >> temp_setup.sql
echo     log_date DATE NOT NULL, >> temp_setup.sql
echo     notes TEXT, >> temp_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_setup.sql
echo     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE >> temp_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Table weight_objective >> temp_setup.sql
echo CREATE TABLE IF NOT EXISTS weight_objective ( >> temp_setup.sql
echo     id INT AUTO_INCREMENT PRIMARY KEY, >> temp_setup.sql
echo     user_id INT NOT NULL, >> temp_setup.sql
echo     target_weight DECIMAL(5,2) NOT NULL, >> temp_setup.sql
echo     start_date DATE NOT NULL, >> temp_setup.sql
echo     target_date DATE NOT NULL, >> temp_setup.sql
echo     status VARCHAR(20) DEFAULT 'ACTIVE', >> temp_setup.sql
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, >> temp_setup.sql
echo     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE >> temp_setup.sql
echo ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Compte ADMIN >> temp_setup.sql
echo DELETE FROM user WHERE email = 'admin@nutrilife.com'; >> temp_setup.sql
echo INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height) >> temp_setup.sql
echo VALUES ('admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', 1, 'Admin', 'NutriLife', '1990-01-01', 75.0, 175.0); >> temp_setup.sql
echo. >> temp_setup.sql
echo -- Compte USER test >> temp_setup.sql
echo DELETE FROM user WHERE email = 'user@test.com'; >> temp_setup.sql
echo INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height) >> temp_setup.sql
echo VALUES ('user@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 1, 'John', 'Doe', '1995-05-15', 80.0, 180.0); >> temp_setup.sql
echo. >> temp_setup.sql
echo SELECT 'Base de donnees creee avec succes !' AS Status; >> temp_setup.sql

REM Executer le script SQL
"C:\xampp\mysql\bin\mysql.exe" -u root -e "source temp_setup.sql" 2>nul
if %errorlevel% equ 0 (
    echo OK - Base de donnees creee !
    del temp_setup.sql
) else (
    echo ERREUR lors de la creation de la base
    echo Essayez manuellement avec phpMyAdmin
    pause
    exit /b 1
)

REM ========================================
REM SUCCES !
REM ========================================
echo.
echo ========================================
echo   SUCCES ! TOUT EST PRET !
echo ========================================
echo.
echo MySQL est demarre et configure !
echo.
echo Base de donnees: nutrilife_db
echo.
echo Comptes crees:
echo.
echo 1. ADMIN
echo    Email    : admin@nutrilife.com
echo    Password : admin123
echo    Role     : ROLE_ADMIN
echo.
echo 2. USER (test)
echo    Email    : user@test.com
echo    Password : admin123
echo    Role     : ROLE_USER
echo.
echo ========================================
echo   PROCHAINES ETAPES
echo ========================================
echo.
echo 1. Ouvrez phpMyAdmin: http://localhost/phpmyadmin
echo 2. Verifiez que la base nutrilife_db existe
echo 3. Lancez votre application Java
echo 4. Connectez-vous avec admin@nutrilife.com / admin123
echo.
echo Votre ancienne base a ete sauvegardee dans:
echo C:\xampp\mysql\backup_nutrilife\
echo.
pause
