@echo off
color 0A
title SAUVEGARDE BASE DE DONNEES
echo.
echo ========================================
echo   SAUVEGARDE BASE DE DONNEES
echo ========================================
echo.

REM Creer le dossier de sauvegarde
set BACKUP_DIR=backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_DIR=%BACKUP_DIR: =0%
mkdir "%BACKUP_DIR%" 2>nul

echo Dossier de sauvegarde: %BACKUP_DIR%
echo.

REM ========================================
REM SAUVEGARDER MYSQL
REM ========================================
echo [1/3] Sauvegarde de MySQL...

REM Verifier si MySQL tourne
tasklist | findstr mysqld.exe >nul
if %errorlevel% equ 0 (
    echo MySQL est actif - Export de la base...
    
    REM Exporter la base de donnees
    "C:\xampp\mysql\bin\mysqldump.exe" -u root nutrilife_db > "%BACKUP_DIR%\nutrilife_db_mysql.sql" 2>nul
    
    if %errorlevel% equ 0 (
        echo OK - Base MySQL exportee: %BACKUP_DIR%\nutrilife_db_mysql.sql
    ) else (
        echo ERREUR - Impossible d'exporter MySQL
        echo Verifiez que la base nutrilife_db existe
    )
) else (
    echo MySQL n'est pas actif - Sauvegarde ignoree
)

REM ========================================
REM SAUVEGARDER SQLITE
REM ========================================
echo.
echo [2/3] Sauvegarde de SQLite...

if exist "nutrilife.db" (
    copy "nutrilife.db" "%BACKUP_DIR%\nutrilife.db" >nul
    echo OK - Base SQLite sauvegardee: %BACKUP_DIR%\nutrilife.db
) else (
    echo Aucune base SQLite trouvee
)

REM ========================================
REM SAUVEGARDER LES FICHIERS MYSQL
REM ========================================
echo.
echo [3/3] Sauvegarde des fichiers MySQL...

if exist "C:\xampp\mysql\data\nutrilife_db" (
    xcopy "C:\xampp\mysql\data\nutrilife_db" "%BACKUP_DIR%\mysql_data\" /E /I /Y >nul 2>&1
    echo OK - Fichiers MySQL sauvegardes: %BACKUP_DIR%\mysql_data\
) else (
    echo Aucun fichier MySQL a sauvegarder
)

REM ========================================
REM CREER UN FICHIER README
REM ========================================
echo.
echo Creation du fichier README...

echo ======================================== > "%BACKUP_DIR%\README.txt"
echo   SAUVEGARDE NUTRILIFE >> "%BACKUP_DIR%\README.txt"
echo ======================================== >> "%BACKUP_DIR%\README.txt"
echo. >> "%BACKUP_DIR%\README.txt"
echo Date: %date% %time% >> "%BACKUP_DIR%\README.txt"
echo. >> "%BACKUP_DIR%\README.txt"
echo CONTENU: >> "%BACKUP_DIR%\README.txt"
echo. >> "%BACKUP_DIR%\README.txt"
if exist "%BACKUP_DIR%\nutrilife_db_mysql.sql" (
    echo - nutrilife_db_mysql.sql : Export SQL de la base MySQL >> "%BACKUP_DIR%\README.txt"
)
if exist "%BACKUP_DIR%\nutrilife.db" (
    echo - nutrilife.db : Base de donnees SQLite >> "%BACKUP_DIR%\README.txt"
)
if exist "%BACKUP_DIR%\mysql_data" (
    echo - mysql_data\ : Fichiers bruts MySQL >> "%BACKUP_DIR%\README.txt"
)
echo. >> "%BACKUP_DIR%\README.txt"
echo RESTAURATION: >> "%BACKUP_DIR%\README.txt"
echo. >> "%BACKUP_DIR%\README.txt"
echo Pour restaurer MySQL: >> "%BACKUP_DIR%\README.txt"
echo   mysql -u root nutrilife_db ^< nutrilife_db_mysql.sql >> "%BACKUP_DIR%\README.txt"
echo. >> "%BACKUP_DIR%\README.txt"
echo Pour restaurer SQLite: >> "%BACKUP_DIR%\README.txt"
echo   Copiez nutrilife.db dans le dossier du projet >> "%BACKUP_DIR%\README.txt"
echo. >> "%BACKUP_DIR%\README.txt"

REM ========================================
REM SUCCES
REM ========================================
echo.
echo ========================================
echo   SAUVEGARDE TERMINEE !
echo ========================================
echo.
echo Dossier: %BACKUP_DIR%
echo.
echo Fichiers sauvegardes:
dir /B "%BACKUP_DIR%"
echo.
echo Pour restaurer:
echo   - MySQL: mysql -u root nutrilife_db ^< nutrilife_db_mysql.sql
echo   - SQLite: Copiez nutrilife.db dans le projet
echo.
pause
