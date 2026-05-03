@echo off
cls
echo.
echo ========================================
echo   LANCEMENT DU TEST FACE ID
echo ========================================
echo.
echo Compilation et lancement en cours...
echo.

cd /d "%~dp0"
call mvn clean compile
if %errorlevel% neq 0 (
    echo.
    echo ERREUR DE COMPILATION!
    pause
    exit /b 1
)

echo.
echo Lancement du test...
call mvn exec:java -Dexec.mainClass="tn.esprit.projet.TestFaceID"

pause
