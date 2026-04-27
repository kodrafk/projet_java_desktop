@echo off
cd /d "%~dp0"
echo ========================================
echo   FIX FACE ID - IMMEDIATE SOLUTION
echo ========================================
echo.
echo This will:
echo   1. Delete the Face ID for rihab.belhassen@gmail.com
echo   2. Allow you to re-enroll immediately
echo   3. Fix the "already exists" error
echo.
set /p confirm="Continue? (Y/N): "

if /i "%confirm%" NEQ "Y" (
    echo.
    echo Operation cancelled.
    echo.
    pause
    exit /b
)

echo.
echo Deleting Face ID for rihab.belhassen...
echo.

mvn compile exec:java "-Dexec.mainClass=tn.esprit.projet.utils.DeleteSpecificFaceId" "-Dexec.args=rihab.belhassen@gmail.com"

echo.
echo ========================================
echo   DONE!
echo ========================================
echo.
echo You can now:
echo   1. Login with password
echo   2. Go to Profile
echo   3. Click "Enroll Face ID"
echo   4. Use GOOD LIGHTING
echo   5. Test Face ID login
echo.
pause
