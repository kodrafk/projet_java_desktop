@echo off
cls
echo.
echo ========================================
echo   TEST CAMERA - SIMPLE
echo ========================================
echo.
echo Ce test va:
echo 1. Compiler le projet
echo 2. Ouvrir la fenetre Face ID
echo 3. Allumer votre camera
echo.
echo Si vous voyez votre visage = CA MARCHE!
echo.
pause

cd /d "%~dp0"

echo Compilation...
call mvn clean compile -q

echo.
echo Lancement...
call mvn exec:java "-Dexec.mainClass=tn.esprit.projet.TestCameraSimple"

pause
