@echo off
echo ========================================
echo AI Anomaly Detection System Setup
echo ========================================
echo.

REM Find MySQL executable
set MYSQL_PATH=""
if exist "C:\xampp\mysql\bin\mysql.exe" set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if exist "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" set MYSQL_PATH="C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

if %MYSQL_PATH%=="" (
    echo ERROR: MySQL not found!
    pause
    exit /b 1
)

echo Using MySQL: %MYSQL_PATH%
echo.
echo Creating tables and inserting sample data...
%MYSQL_PATH% -u root nutrilife_db < SETUP_COMPLETE_SYSTEM.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo SUCCESS! AI System is ready!
    echo ========================================
    echo.
    echo - 15 sample anomalies created
    echo - 7 alerts generated
    echo - 10 user metrics calculated
    echo.
    echo You can now run the application!
) else (
    echo.
    echo ERROR: Setup failed!
    echo Check if MySQL is running.
)

echo.
pause
