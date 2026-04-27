@echo off
color 0C
title RESTAURATION BASE DE DONNEES
echo.
echo ========================================
echo   RESTAURATION BASE DE DONNEES
echo ========================================
echo.

REM Lister les sauvegardes disponibles
echo Sauvegardes disponibles:
echo.
dir /B /AD backup_* 2>nul
if %errorlevel% neq 0 (
    echo Aucune sauvegarde trouvee !
    echo.
    echo Executez d'abord: BACKUP_DATABASE.bat
    pause
    exit /b 1
)

echo.
set /p BACKUP_FOLDER="Entrez le nom du dossier de sauvegarde: "

if not exist "%BACKUP_FOLDER%" (
    echo.
    echo ERREUR: Dossier %BACKUP_FOLDER% introuvable !
    pause
    exit /b 1
)

echo.
echo ========================================
echo   RESTAURATION EN COURS...
echo ========================================
echo.

REM ========================================
REM RESTAURER MYSQL
REM ========================================
echo [1/2] Restauration de MySQL...

if exist "%BACKUP_FOLDER%\nutrilife_db_mysql.sql" (
    REM Verifier si MySQL tourne
    tasklist | findstr mysqld.exe >nul
    if %errorlevel% equ 0 (
        echo MySQL est actif - Import de la base...
        
        REM Creer la base si elle n'existe pas
        "C:\xampp\mysql\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
        
        REM Importer la sauvegarde
        "C:\xampp\mysql\bin\mysql.exe" -u root nutrilife_db < "%BACKUP_FOLDER%\nutrilife_db_mysql.sql" 2>nul
        
        if %errorlevel% equ 0 (
            echo OK - Base MySQL restauree !
        ) else (
            echo ERREUR - Impossible d'importer la base
        )
    ) else (
        echo ERREUR: MySQL n'est pas actif !
        echo Demarrez MySQL dans XAMPP Control Panel
        pause
        exit /b 1
    )
) else (
    echo Aucune sauvegarde MySQL trouvee dans %BACKUP_FOLDER%
)

REM ========================================
REM RESTAURER SQLITE
REM ========================================
echo.
echo [2/2] Restauration de SQLite...

if exist "%BACKUP_FOLDER%\nutrilife.db" (
    REM Sauvegarder l'ancienne base
    if exist "nutrilife.db" (
        copy "nutrilife.db" "nutrilife.db.old" >nul
        echo Ancienne base sauvegardee: nutrilife.db.old
    )
    
    REM Restaurer la sauvegarde
    copy "%BACKUP_FOLDER%\nutrilife.db" "nutrilife.db" >nul
    echo OK - Base SQLite restauree !
) else (
    echo Aucune sauvegarde SQLite trouvee dans %BACKUP_FOLDER%
)

REM ========================================
REM SUCCES
REM ========================================
echo.
echo ========================================
echo   RESTAURATION TERMINEE !
echo ========================================
echo.
echo Base de donnees restauree depuis: %BACKUP_FOLDER%
echo.
echo Vous pouvez maintenant:
echo 1. Ouvrir phpMyAdmin: http://localhost/phpmyadmin
echo 2. Verifier que la base nutrilife_db existe
echo 3. Lancer votre application Java
echo.
pause
