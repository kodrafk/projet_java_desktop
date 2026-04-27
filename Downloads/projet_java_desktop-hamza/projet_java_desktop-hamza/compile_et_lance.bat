@echo off
echo ========================================
echo   COMPILATION ET LANCEMENT
echo   Projet JavaFX - Evenements
echo ========================================
echo.

echo [1/3] Nettoyage...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Echec du nettoyage
    pause
    exit /b 1
)

echo.
echo [2/3] Compilation...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Echec de la compilation
    pause
    exit /b 1
)

echo.
echo [3/3] Lancement de l'application...
call mvn javafx:run

pause
