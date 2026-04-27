@echo off
echo ============================================================================
echo Creation d'un compte USER de test
echo ============================================================================
echo.

REM Chemin vers MySQL (ajustez si necessaire)
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH=mysql

echo Execution du script SQL...
echo.

%MYSQL_PATH% -u root nutrilife_db < CREATE_TEST_USER.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================================
    echo COMPTE USER CREE AVEC SUCCES !
    echo ============================================================================
    echo.
    echo Email    : test@user.com
    echo Password : user123
    echo Role     : ROLE_USER
    echo Statut   : Actif
    echo.
    echo ============================================================================
    echo Vous pouvez maintenant vous connecter avec ce compte !
    echo ============================================================================
) else (
    echo.
    echo ERREUR: Impossible de creer le compte.
    echo Verifiez que MySQL est installe et que la base de donnees existe.
)

echo.
pause
