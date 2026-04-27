@echo off
echo 🚀 Lancement de Nutri Coach Pro...
echo.
echo 📦 Compilation du projet...
call mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation réussie !
    echo.
    echo 🎯 Démarrage de l'application JavaFX...
    call mvn javafx:run
) else (
    echo.
    echo ❌ Erreur de compilation. Vérifiez les logs ci-dessus.
    pause
    exit /b 1
)
