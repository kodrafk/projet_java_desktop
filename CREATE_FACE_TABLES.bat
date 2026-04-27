@echo off
echo ============================================================
echo  Creating Face ID tables in nutrilife_db
echo ============================================================
echo.
echo Enter your MySQL credentials:
set /p MYSQL_USER=MySQL username (default: root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root
set /p MYSQL_PASS=MySQL password: 
set /p MYSQL_PORT=MySQL port (default: 3306): 
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

echo.
echo Running SQL...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db < CREATE_FACE_TABLES.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! Face ID tables created.
    echo You can now use Face ID enrollment and login.
) else (
    echo.
    echo ERROR: Could not create tables.
    echo Make sure MySQL is running and credentials are correct.
    echo.
    echo Alternative: Open MySQL Workbench and run CREATE_FACE_TABLES.sql manually.
)
echo.
pause
