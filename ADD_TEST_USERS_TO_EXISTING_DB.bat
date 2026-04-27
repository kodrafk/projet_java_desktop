@echo off
echo ========================================
echo ADDING TEST USERS TO EXISTING DATABASE
echo ========================================
echo.
echo This will add 5 test users to your existing "nutrilife" database
echo It will NOT create a new database or modify existing data
echo.

cd /d "%~dp0"

echo Executing SQL script...
echo.

mysql -u root nutrilife < ADD_TEST_USERS_TO_EXISTING_DB.sql 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo MySQL connection failed. Trying with password prompt...
    echo.
    mysql -u root -p nutrilife < ADD_TEST_USERS_TO_EXISTING_DB.sql
)

echo.
echo ========================================
echo DONE! Test users added.
echo ========================================
echo.
echo Test users added:
echo - john.doe@nutrilife.com
echo - jane.smith@nutrilife.com
echo - bob.johnson@nutrilife.com
echo - alice.williams@nutrilife.com
echo - charlie.brown@nutrilife.com
echo.
echo Password for all: password123
echo.
echo Now run: mvn javafx:run
echo Then go to: Menu -^> Personalized Messages
echo.
pause
