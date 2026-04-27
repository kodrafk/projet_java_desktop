@echo off
echo ========================================
echo Checking and Creating Test Users
echo ========================================
echo.

mysql -u root -p nutrilife < CHECK_AND_CREATE_TEST_USERS.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Users checked/created!
    echo ========================================
    echo.
    echo Test users credentials:
    echo Email: john.doe@nutrilife.com
    echo Email: jane.smith@nutrilife.com
    echo Email: bob.johnson@nutrilife.com
    echo Email: alice.williams@nutrilife.com
    echo Email: charlie.brown@nutrilife.com
    echo Password: password123
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR! Failed to check/create users.
    echo ========================================
)

pause
