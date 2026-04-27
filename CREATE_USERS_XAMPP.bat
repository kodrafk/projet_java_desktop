@echo off
echo ========================================
echo Creating Test Users (XAMPP/WAMP)
echo ========================================
echo.

REM Essayer différents chemins MySQL
set MYSQL_PATH=""

if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
    echo Found MySQL in XAMPP
)

if exist "C:\wamp64\bin\mysql\mysql8.0.27\bin\mysql.exe" (
    set MYSQL_PATH="C:\wamp64\bin\mysql\mysql8.0.27\bin\mysql.exe"
    echo Found MySQL in WAMP
)

if exist "C:\wamp\bin\mysql\mysql8.0.27\bin\mysql.exe" (
    set MYSQL_PATH="C:\wamp\bin\mysql\mysql8.0.27\bin\mysql.exe"
    echo Found MySQL in WAMP
)

if %MYSQL_PATH%=="" (
    echo.
    echo ERROR: MySQL not found!
    echo.
    echo Please install XAMPP or WAMP, or add MySQL to your PATH.
    echo.
    echo Alternative: Open phpMyAdmin and execute CHECK_AND_CREATE_TEST_USERS.sql manually
    echo.
    pause
    exit /b 1
)

echo.
echo Using MySQL at: %MYSQL_PATH%
echo.
echo Please enter your MySQL root password:
echo.

%MYSQL_PATH% -u root -p nutrilife < CHECK_AND_CREATE_TEST_USERS.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Users created!
    echo ========================================
    echo.
    echo Test users:
    echo - john.doe@nutrilife.com
    echo - jane.smith@nutrilife.com
    echo - bob.johnson@nutrilife.com
    echo - alice.williams@nutrilife.com
    echo - charlie.brown@nutrilife.com
    echo.
    echo Password: password123
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR! Failed to create users.
    echo ========================================
    echo.
    echo Try opening phpMyAdmin and executing
    echo CHECK_AND_CREATE_TEST_USERS.sql manually
    echo.
)

pause
