@echo off
echo ========================================
echo   GENERATING AI SAMPLE DATA
echo ========================================
echo.
echo This will:
echo - Run anomaly detection on all users
echo - Generate health metrics
echo - Create alerts
echo.
echo Please wait...
echo.

cd /d "%~dp0"
mvn exec:java -Dexec.mainClass="tn.esprit.projet.utils.GenerateSampleData"

echo.
pause
