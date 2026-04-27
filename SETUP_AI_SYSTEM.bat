@echo off
echo ========================================
echo   AI ANOMALY DETECTION - SETUP
echo ========================================
echo.

echo [1/2] Creating database tables...
mysql -u root nutrilife_db < CREATE_ANOMALY_DETECTION_TABLES.sql
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create tables!
    echo Make sure MySQL is running and nutrilife_db exists.
    pause
    exit /b 1
)
echo Tables created successfully!
echo.

echo [2/2] Generating sample data...
mysql -u root nutrilife_db < GENERATE_SAMPLE_DATA.sql
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to generate data!
    pause
    exit /b 1
)
echo Sample data generated!
echo.

echo ========================================
echo   SETUP COMPLETE!
echo ========================================
echo.
echo Now:
echo 1. Login as admin: kiro.admin@nutrilife.com / kiro2026
echo 2. Click on "AI Anomaly Detection"
echo 3. Click "Actualiser" to see the data
echo.
pause
