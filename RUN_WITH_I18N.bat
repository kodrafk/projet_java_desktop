@echo off
echo ========================================
echo  LANCEMENT NUTRILIFE AVEC I18N
echo ========================================
echo.
echo Compilation et lancement de l'application avec support multilingue...
echo.

cd /d "%~dp0"

echo [1/3] Nettoyage du projet...
call mvn clean

echo.
echo [2/3] Compilation avec les nouvelles fonctionnalites i18n...
call mvn compile

echo.
echo [3/3] Lancement de l'application...
echo.
echo FONCTIONNALITES A TESTER :
echo - Bouton de langue (FR/EN) dans l'ecran de connexion
echo - Changement de langue en temps reel
echo - Messages de bienvenue traduits
echo - Interface admin multilingue
echo - Menu compte avec option langue
echo.

call mvn javafx:run

echo.
echo Application fermee.
pause