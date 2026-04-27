@echo off
echo ========================================
echo   FIX CAMERA - IMMEDIATE SOLUTION
echo ========================================
echo.
echo This will:
echo   1. Close all Java processes
echo   2. Release camera
echo   3. Restart application
echo.
pause

echo.
echo Closing Java processes...
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM javaw.exe /T 2>nul

echo.
echo Waiting 3 seconds...
timeout /t 3 /nobreak >nul

echo.
echo Starting application...
cd /d "%~dp0"
start cmd /c "mvn javafx:run"

echo.
echo ========================================
echo   DONE!
echo ========================================
echo.
echo Application is starting...
echo Try Face ID now!
echo.
pause
