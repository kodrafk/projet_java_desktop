@echo off
cls
echo ============================================================================
echo CORRECTION FORCEE DE LA BASE DE DONNEES
echo ============================================================================
echo.
echo Ce script va:
echo   1. Supprimer completement les tables weight_log et weight_objective
echo   2. Recreer les tables avec la bonne structure
echo   3. Creer les tables progress_photo et message
echo.
echo ATTENTION: Toutes les donnees dans ces tables seront perdues!
echo.
echo Appuyez sur une touche pour continuer ou fermez cette fenetre pour annuler...
pause >nul
echo.

REM Chemin vers MySQL
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH=mysql

echo Execution du script SQL...
echo.

%MYSQL_PATH% -u root nutrilife_db < FORCE_FIX_DATABASE.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================================
    echo BASE DE DONNEES CORRIGEE AVEC SUCCES !
    echo ============================================================================
    echo.
    echo Les tables suivantes ont ete recreees:
    echo   - weight_log (avec photo, note, logged_at)
    echo   - weight_objective (avec start_weight, start_photo, is_active)
    echo   - progress_photo (nouvelle table)
    echo   - message (nouvelle table)
    echo.
    echo ============================================================================
    echo PROCHAINES ETAPES:
    echo ============================================================================
    echo.
    echo 1. Relancez l'application: mvn javafx:run
    echo 2. Front Office: Ajoutez des poids, objectifs, photos
    echo 3. Back Office: Verifiez que tout s'affiche correctement
    echo.
    echo ============================================================================
) else (
    echo.
    echo ============================================================================
    echo ERREUR !
    echo ============================================================================
    echo.
    echo Impossible de corriger la base de donnees.
    echo.
    echo Verifiez que:
    echo   - MySQL est demarre
    echo   - La base de donnees nutrilife_db existe
    echo   - Vous avez les droits d'acces
    echo.
    echo ============================================================================
)

echo.
pause
