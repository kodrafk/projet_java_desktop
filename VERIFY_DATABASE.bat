@echo off
cls
echo ============================================================================
echo VERIFICATION DE LA STRUCTURE DE LA BASE DE DONNEES
echo ============================================================================
echo.
echo Ce script va verifier que la base de donnees a la bonne structure.
echo.
pause
echo.

REM Chemin vers MySQL
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH=mysql

echo Execution de la verification...
echo.

%MYSQL_PATH% -u root nutrilife_db < VERIFY_DATABASE.sql

echo.
echo ============================================================================
echo VERIFICATION TERMINEE
echo ============================================================================
echo.
echo Si vous voyez des croix rouges (X), executez FORCE_FIX_DATABASE.bat
echo Si vous voyez des coches vertes (✓), la base de donnees est correcte!
echo.
pause
