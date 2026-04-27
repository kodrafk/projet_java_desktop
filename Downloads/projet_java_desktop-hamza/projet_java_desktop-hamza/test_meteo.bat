@echo off
echo ========================================
echo TEST SYSTEME INTELLIGENT METEO
echo ========================================
echo.

REM Compiler le projet
echo [1/2] Compilation...
javac -encoding UTF-8 -d bin -cp "src/main/java" src/main/java/tn/esprit/projet/test/TestMeteoIntelligent.java src/main/java/tn/esprit/projet/services/MeteoService.java src/main/java/tn/esprit/projet/models/Evenement.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: La compilation a echoue
    pause
    exit /b 1
)

echo [2/2] Execution du test...
echo.
java -cp bin tn.esprit.projet.test.TestMeteoIntelligent

echo.
echo ========================================
echo TEST TERMINE
echo ========================================
pause
