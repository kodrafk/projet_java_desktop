@echo off
echo ============================================================================
echo CORRECTION DE LA STRUCTURE DE LA BASE DE DONNEES
echo ============================================================================
echo.
echo ATTENTION: Ce script va supprimer les donnees existantes dans:
echo   - weight_log
echo   - weight_objective
echo.
echo Appuyez sur une touche pour continuer ou fermez cette fenetre pour annuler...
pause
echo.

REM Chemin vers MySQL (ajustez si necessaire)
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH=mysql

echo Execution du script SQL...
echo.

%MYSQL_PATH% -u root nutrilife_db < FIX_DATABASE_STRUCTURE.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================================
    echo STRUCTURE DE LA BASE DE DONNEES CORRIGEE AVEC SUCCES !
    echo ============================================================================
    echo.
    echo Les tables suivantes ont ete corrigees:
    echo   - weight_log (avec photo, note, logged_at)
    echo   - weight_objective (avec start_weight, start_photo, is_active)
    echo   - progress_photo (nouvelle table creee)
    echo.
    echo ============================================================================
    echo IMPORTANT: Relancez l'application pour que les changements prennent effet
    echo ============================================================================
) else (
    echo.
    echo ERREUR: Impossible de corriger la structure.
    echo Verifiez que MySQL est installe et que la base de donnees existe.
)

echo.
pause
