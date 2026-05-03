@echo off
cls
color 0B
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🚀 LANCEMENT SIMPLE - SANS PYTHON 🚀                  ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Cette version fonctionne SANS Python!
echo Elle utilise uniquement Java.
echo.
echo ════════════════════════════════════════════════════════════════
echo.

echo 📦 Compilation du projet...
call mvn clean compile -q
if errorlevel 1 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)
echo ✅ Compilation reussie
echo.

echo 🚀 Lancement de l'application...
echo.
echo INSTRUCTIONS:
echo   1. Cliquez sur "Face ID Login" (bouton bleu)
echo   2. La camera va s'ouvrir avec interface professionnelle
echo   3. Vous verrez l'interface moderne!
echo.
echo ════════════════════════════════════════════════════════════════
echo.

call mvn javafx:run

pause
