@echo off
cd /d "%~dp0"
echo ========================================
echo   CAMERA DIAGNOSTIC TOOL
echo ========================================
echo.
echo Checking camera status...
echo.

mvn compile exec:java "-Dexec.mainClass=tn.esprit.projet.utils.CameraDiagnostic"

echo.
echo ========================================
echo   DIAGNOSTIC COMPLETE
echo ========================================
echo.
pause
