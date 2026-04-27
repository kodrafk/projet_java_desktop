@echo off
cd /d "%~dp0"
echo ========================================
echo   FACE ID SYSTEM UPGRADE
echo ========================================
echo.
echo This will upgrade your Face ID system from:
echo   OLD: LBP+HOG (70%% accuracy, 128D)
echo   NEW: ArcFace (99.83%% accuracy, 512D)
echo.
echo This upgrade is:
echo   - FREE
echo   - Takes 5-10 minutes
echo   - Dramatically improves recognition
echo.
set /p confirm="Continue with upgrade? (Y/N): "

if /i "%confirm%" NEQ "Y" (
    echo.
    echo Upgrade cancelled.
    echo.
    pause
    exit /b
)

echo.
echo Starting upgrade...
echo.

python upgrade_face_recognition.py

echo.
echo ========================================
echo   UPGRADE PROCESS COMPLETE
echo ========================================
echo.
echo IMPORTANT: You must now:
echo   1. Run FACE_ID_RESET.bat to clear old enrollments
echo   2. Restart the application
echo   3. Re-enroll Face ID
echo.
pause
