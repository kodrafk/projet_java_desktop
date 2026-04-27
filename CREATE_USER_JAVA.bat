@echo off
echo ============================================================================
echo Creation d'un compte USER de test via Java
echo ============================================================================
echo.

echo Compilation et execution...
echo.

cd /d "%~dp0"
call mvn compile exec:java -Dexec.mainClass="tn.esprit.projet.utils.CreateTestUser"

echo.
echo ============================================================================
echo Termine !
echo ============================================================================
echo.
pause
