@echo off
cd /d "%~dp0"
echo ========================================
echo   FACE ID SYSTEM RESET
echo ========================================
echo.
echo WARNING: This will DELETE ALL Face ID enrollments!
echo All users will need to re-enroll their Face ID.
echo.
set /p confirm="Are you sure? Type YES to confirm: "

if /i "%confirm%" NEQ "YES" (
    echo.
    echo Operation cancelled.
    echo.
    pause
    exit /b
)

echo.
echo Resetting Face ID system...
echo.

mvn compile exec:java "-Dexec.mainClass=tn.esprit.projet.utils.FaceIdReset"

echo.
echo ========================================
echo   RESET COMPLETE
echo ========================================
echo.
pause
