@echo off
color 0B
echo ========================================
echo   DEMARRAGE FORCE DE MYSQL
echo ========================================
echo.

echo [1] Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 2 >nul

echo [2] Nettoyage du port 3306...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3306') do (
    echo Processus trouve sur port 3306: %%a
    taskkill /F /PID %%a 2>nul
)

echo [3] Demarrage de MySQL directement...
echo.
echo Tentative 1: Via mysqld.exe...
start "MySQL" "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone --console

timeout /t 5 >nul

echo.
echo [4] Verification...
tasklist | findstr mysqld.exe
if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCES ! MySQL est demarre
    echo ========================================
    echo.
    echo Vous pouvez maintenant:
    echo 1. Ouvrir phpMyAdmin: http://localhost/phpmyadmin
    echo 2. Lancer votre application Java
    echo.
) else (
    echo.
    echo ========================================
    echo   ECHEC - MySQL ne demarre pas
    echo ========================================
    echo.
    echo SOLUTIONS:
    echo 1. Verifiez les logs: C:\xampp\mysql\data\*.err
    echo 2. Essayez de changer le port (3306 -^> 3307)
    echo 3. Reinstallez XAMPP
    echo.
)

pause
