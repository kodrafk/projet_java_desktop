@echo off
echo ========================================
echo Creating user_alerts table...
echo ========================================

mysql -u root -p nutrilife < CREATE_USER_ALERTS_TABLE.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Table created successfully!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ERROR! Failed to create table.
    echo ========================================
)

pause
