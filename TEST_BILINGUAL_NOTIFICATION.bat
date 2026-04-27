@echo off
echo ========================================
echo  TEST NOTIFICATION BILINGUE
echo ========================================
echo.
echo NOUVELLE FONCTIONNALITE AJOUTEE :
echo [✓] Traduction en dessous du message principal
echo [✓] Message motivationnel avec traduction
echo [✓] Experience d'apprentissage enrichie
echo [✓] Design adapte pour plus de contenu
echo.
echo EXEMPLE DE CE QUE VOUS VERREZ :
echo.
echo Si interface en FRANCAIS :
echo   "Bon retour, yasmine !"
echo   "Welcome back, yasmine!"  (en italique)
echo.
echo Si interface en ANGLAIS :
echo   "Welcome back, yasmine!"
echo   "Bon retour, yasmine !"  (en italique)
echo.
echo PLUS : Message motivationnel bilingue
echo - Message principal dans la langue choisie
echo - Traduction en dessous en italique
echo.

cd /d "%~dp0"

echo Lancement de l'application...
echo.
start mvn javafx:run

echo.
echo TESTS A EFFECTUER :
echo 1. Connectez-vous (yasmine ou admin)
echo 2. Observez la notification avec traductions
echo 3. Changez de langue et reconnectez-vous
echo 4. Comparez les deux versions
echo.
echo IDENTIFIANTS :
echo Admin : admin@nutrilife.com / admin123
echo User  : user@test.com / password123
echo.
pause