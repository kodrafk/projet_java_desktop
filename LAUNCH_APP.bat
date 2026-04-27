@echo off
color 0A
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🚀 LANCEMENT DE L'APPLICATION NUTRILIFE
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo     Système de Détection d'Anomalies avec Machine Learning
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo [1/3] Vérification de Maven...
echo.

where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Maven n'est pas installé ou pas dans le PATH
    echo.
    echo 💡 Installez Maven : https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo ✓ Maven détecté
echo.

echo [2/3] Compilation du projet...
echo.

call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ✗ Erreur de compilation
    echo.
    echo 💡 Vérifiez les erreurs ci-dessus
    echo.
    pause
    exit /b 1
)

echo.
echo ✓ Compilation réussie
echo.

echo [3/3] Lancement de l'application...
echo.
echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ 🎯 INSTRUCTIONS                                                       │
echo ├───────────────────────────────────────────────────────────────────────┤
echo │                                                                       │
echo │ 1. Connectez-vous avec un compte ADMIN                               │
echo │                                                                       │
echo │ 2. Dans le menu latéral, cherchez :                                  │
echo │    HEALTH AI → 🔍 Anomaly Detection                                  │
echo │                                                                       │
echo │ 3. Cliquez sur "🚀 Lancer Détection"                                 │
echo │                                                                       │
echo │ 4. Consultez les résultats dans le dashboard                         │
echo │                                                                       │
echo └───────────────────────────────────────────────────────────────────────┘
echo.
echo L'application va démarrer...
echo.

call mvn javafx:run

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     Application fermée
echo ═══════════════════════════════════════════════════════════════════════════
echo.
pause
