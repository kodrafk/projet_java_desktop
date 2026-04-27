@echo off
echo ========================================
echo   NUTRILIFE - AI ANOMALY DETECTION
echo   Launch and Test Script
echo ========================================
echo.

echo [1/3] Cleaning and rebuilding project...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo.

echo [2/3] Build successful!
echo.
echo [3/3] Launching application...
echo.
echo ========================================
echo   LOGIN CREDENTIALS
echo ========================================
echo   Email: kiro.admin@nutrilife.com
echo   Password: kiro2026
echo ========================================
echo.
echo After login, look for the AI button in the MANAGEMENT section:
echo   - Dashboard
echo   - Users
echo   - User Profiles
echo   - Statistics
echo   - AI Anomaly Detection  ^<-- CLICK HERE!
echo.
echo ========================================
echo.

call mvn javafx:run

pause
