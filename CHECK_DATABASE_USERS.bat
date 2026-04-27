@echo off
echo ========================================
echo CHECKING DATABASE USERS
echo ========================================
echo.

cd /d "%~dp0"

echo Running diagnostic query...
echo.

mysql -u root -e "USE nutrilife; SELECT id, email, roles, first_name, last_name, is_active FROM user ORDER BY id;" 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo MySQL connection failed. Trying with password prompt...
    echo.
    mysql -u root -p -e "USE nutrilife; SELECT id, email, roles, first_name, last_name, is_active FROM user ORDER BY id;"
)

echo.
echo ========================================
echo DONE
echo ========================================
pause
