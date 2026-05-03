@echo off
title PROFESSIONAL FACE ID - TEST RAPIDE
color 0B
echo ╔════════════════════════════════════════════════════════════════╗
echo ║           🎉 PROFESSIONAL FACE ID - TEST RAPIDE 🎉            ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Ce script va:
echo   1. Compiler le projet
echo   2. Creer les tables automatiquement
echo   3. Lancer le test Face ID avec VRAIE CAMERA
echo.
echo Appuyez sur une touche pour continuer...
pause > nul

cd /d "%~dp0"

echo.
echo ════════════════════════════════════════════════════════════════
echo [1/2] Compilation du projet...
echo ════════════════════════════════════════════════════════════════
call mvn clean compile

if %errorlevel% neq 0 (
    color 0C
    echo.
    echo ❌ ERREUR: Compilation echouee
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Compilation reussie!
echo.

echo ════════════════════════════════════════════════════════════════
echo [2/2] Lancement du test Face ID...
echo ════════════════════════════════════════════════════════════════
echo.
echo 📋 INSTRUCTIONS:
echo.
echo   1. La fenetre Face ID va s'ouvrir
echo   2. Votre camera va s'allumer (LED verte)
echo   3. Positionnez votre visage dans le cercle
echo   4. Le systeme detectera votre visage automatiquement
echo   5. Cliquez sur "Enroll Face" pour enregistrer
echo.
echo ⚠️  IMPORTANT:
echo   - Fermez Zoom, Teams, Skype (ils bloquent la camera)
echo   - Assurez un bon eclairage
echo   - Regardez la camera directement
echo.
echo Appuyez sur une touche pour lancer le test...
pause > nul

echo.
echo 🚀 Lancement...
echo.

call mvn exec:java -Dexec.mainClass="tn.esprit.projet.TestFaceID"

echo.
echo ════════════════════════════════════════════════════════════════
echo   Test termine!
echo ════════════════════════════════════════════════════════════════
echo.
echo Si la camera n'a pas fonctionne:
echo   1. Verifiez les permissions Windows
echo   2. Fermez les autres apps utilisant la camera
echo   3. Redemarrez l'ordinateur
echo.
pause
