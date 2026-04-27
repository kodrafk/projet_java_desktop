@echo off
cd /d "%~dp0"
echo ========================================
echo   CREATION COMPTE ADMIN
echo ========================================
echo.
mvn compile exec:java "-Dexec.mainClass=tn.esprit.projet.utils.CreateAdminAccount"
echo.
pause
