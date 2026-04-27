@echo off
cd /d "%~dp0"
echo ========================================
echo   VERIFICATION COMPTE ADMIN
echo ========================================
echo.
mvn compile exec:java "-Dexec.mainClass=tn.esprit.projet.utils.CheckAdminAccount"
echo.
pause
