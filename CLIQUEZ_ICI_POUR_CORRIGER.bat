@echo off
color 0C
cls
echo.
echo ╔══════════════════════════════════════════════════════════════════════╗
echo ║                                                                      ║
echo ║                    CORRECTION DE LA BASE DE DONNEES                  ║
echo ║                                                                      ║
echo ╚══════════════════════════════════════════════════════════════════════╝
echo.
echo.
color 0E
echo PROBLEME:
echo ─────────────────────────────────────────────────────────────────────
echo.
echo   Vous avez ajoute des donnees (photo, poids, objectif)
echo   Vous avez ferme la fenetre
echo   Vous avez rouvert la fenetre
echo   TOUT A DISPARU!
echo.
echo.
echo CAUSE:
echo ─────────────────────────────────────────────────────────────────────
echo.
echo   La structure de la base de donnees est INCORRECTE.
echo   Les donnees ne peuvent pas etre sauvegardees correctement.
echo.
echo.
color 0A
echo SOLUTION:
echo ─────────────────────────────────────────────────────────────────────
echo.
echo   Ce script va corriger la structure de la base de donnees.
echo   Apres correction, les donnees seront sauvegardees correctement.
echo.
echo.
color 0C
echo ATTENTION:
echo ─────────────────────────────────────────────────────────────────────
echo.
echo   - Assurez-vous que MySQL est demarre
echo   - Les anciennes donnees seront supprimees
echo   - Fermez l'application avant de continuer
echo.
echo.
color 0F
echo Appuyez sur une touche pour CORRIGER la base de donnees...
echo Ou fermez cette fenetre pour annuler.
echo.
pause >nul

cls
echo.
echo Correction en cours...
echo.

REM Chemin vers MySQL
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH="C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
if not exist %MYSQL_PATH% set MYSQL_PATH=mysql

%MYSQL_PATH% -u root nutrilife_db < FORCE_FIX_DATABASE.sql

if %ERRORLEVEL% EQU 0 (
    color 0A
    cls
    echo.
    echo ╔══════════════════════════════════════════════════════════════════════╗
    echo ║                                                                      ║
    echo ║                          SUCCES !                                    ║
    echo ║                                                                      ║
    echo ╚══════════════════════════════════════════════════════════════════════╝
    echo.
    echo.
    echo La base de donnees a ete corrigee avec succes!
    echo.
    echo.
    echo PROCHAINES ETAPES:
    echo ─────────────────────────────────────────────────────────────────────
    echo.
    echo   1. Ouvrez un terminal dans le dossier projetJAV
    echo   2. Executez: mvn javafx:run
    echo   3. Connectez-vous: test@user.com / user123
    echo   4. Ajoutez un objectif avec une photo
    echo   5. Fermez et rouvrez la fenetre
    echo   6. L'objectif et la photo sont TOUJOURS LA!
    echo.
    echo.
    echo Appuyez sur une touche pour fermer...
    pause >nul
) else (
    color 0C
    cls
    echo.
    echo ╔══════════════════════════════════════════════════════════════════════╗
    echo ║                                                                      ║
    echo ║                          ERREUR !                                    ║
    echo ║                                                                      ║
    echo ╚══════════════════════════════════════════════════════════════════════╝
    echo.
    echo.
    echo Impossible de corriger la base de donnees.
    echo.
    echo.
    echo SOLUTIONS:
    echo ─────────────────────────────────────────────────────────────────────
    echo.
    echo   1. Verifiez que MySQL est demarre
    echo   2. Ouvrez MySQL Workbench ou phpMyAdmin
    echo   3. Connectez-vous a MySQL
    echo   4. Selectionnez la base de donnees "nutrilife_db"
    echo   5. Ouvrez le fichier FORCE_FIX_DATABASE.sql
    echo   6. Copiez tout le contenu
    echo   7. Collez-le dans MySQL Workbench/phpMyAdmin
    echo   8. Executez le script
    echo.
    echo.
    echo Appuyez sur une touche pour fermer...
    pause >nul
)
