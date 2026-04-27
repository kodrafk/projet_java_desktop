@echo off
echo ========================================
echo   COMPILATION DU PROJET
echo ========================================
echo.

cd /d "%~dp0"

echo [1/2] Nettoyage...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERREUR: Echec du nettoyage
    echo.
    pause
    exit /b 1
)

echo.
echo [2/2] Compilation...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERREUR: Echec de la compilation
    echo    Consultez les messages d'erreur ci-dessus
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ COMPILATION RÉUSSIE !
echo ========================================
echo.
echo Vous pouvez maintenant :
echo   1. Lancer l'application : mvn javafx:run
echo   2. Tester l'email : mvn exec:java -Dexec.mainClass="tn.esprit.projet.test.TestEmailServicePro"
echo.
pause
