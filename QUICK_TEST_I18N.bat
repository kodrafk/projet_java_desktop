@echo off
echo ========================================
echo  TEST RAPIDE I18N - NUTRILIFE
echo ========================================
echo.
echo Lancement rapide pour tester l'internationalisation...
echo.

cd /d "%~dp0"

echo [INFO] Lancement de l'application...
echo.
echo TESTS A EFFECTUER :
echo 1. Cliquer sur le bouton de langue (FR/EN) en haut a droite
echo 2. Se connecter avec : admin@nutrilife.com / admin123
echo 3. Verifier le message de bienvenue traduit
echo 4. Tester le changement de langue dans le menu compte
echo.

start /B mvn javafx:run

echo.
echo Application lancee en arriere-plan.
echo Fermez cette fenetre quand vous avez termine les tests.
pause