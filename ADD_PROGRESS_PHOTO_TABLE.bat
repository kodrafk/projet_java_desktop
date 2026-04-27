@echo off
echo ============================================================================
echo Ajout de la table progress_photo
echo ============================================================================
echo.

REM Chemin vers MySQL (ajustez si necessaire)
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH=mysql

echo Execution du script SQL...
echo.

%MYSQL_PATH% -u root nutrilife_db < ADD_PROGRESS_PHOTO_TABLE.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================================
    echo TABLE progress_photo CREEE AVEC SUCCES !
    echo ============================================================================
    echo.
    echo La table progress_photo a ete ajoutee a la base de donnees.
    echo Les photos de progression du front office seront maintenant visibles
    echo dans le back office admin.
    echo.
    echo ============================================================================
) else (
    echo.
    echo ERREUR: Impossible de creer la table.
    echo Verifiez que MySQL est installe et que la base de donnees existe.
)

echo.
pause
