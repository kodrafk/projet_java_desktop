@echo off
cd /d "%~dp0"
echo ========================================
echo   FACE ID DIAGNOSTIC TOOL
echo ========================================
echo.
echo Running diagnostic...
echo.
mvn compile exec:java "-Dexec.mainClass=tn.esprit.projet.utils.FaceIdDiagnostic"
echo.
echo ========================================
echo   DIAGNOSTIC COMPLETE
echo ========================================
echo.
pause
