@echo off
echo ========================================
echo   TEST DE CONNEXION MYSQL
echo ========================================
echo.

echo Test 1: Verification du processus MySQL...
tasklist | findstr mysqld.exe
if %errorlevel% equ 0 (
    echo [OK] MySQL est en cours d'execution
) else (
    echo [ERREUR] MySQL n'est pas lance
    echo.
    echo SOLUTION: Ouvrez XAMPP Control Panel et cliquez sur "Start" a cote de MySQL
    pause
    exit /b 1
)

echo.
echo Test 2: Verification du port 3306...
netstat -ano | findstr :3306
if %errorlevel% equ 0 (
    echo [OK] MySQL ecoute sur le port 3306
) else (
    echo [ERREUR] MySQL n'ecoute pas sur le port 3306
    echo.
    echo SOLUTION: Verifiez le fichier my.ini ou changez le port
    pause
    exit /b 1
)

echo.
echo Test 3: Test de connexion avec mysql.exe...
if exist "C:\xampp\mysql\bin\mysql.exe" (
    echo Tentative de connexion...
    "C:\xampp\mysql\bin\mysql.exe" -u root -e "SELECT 'Connexion reussie!' AS Status;"
    if %errorlevel% equ 0 (
        echo [OK] Connexion MySQL reussie !
    ) else (
        echo [ERREUR] Impossible de se connecter
    )
) else (
    echo [ERREUR] mysql.exe introuvable
)

echo.
echo ========================================
echo   RESULTAT DU TEST
echo ========================================
echo.
echo Si tous les tests sont OK, votre MySQL fonctionne !
echo Vous pouvez maintenant lancer votre application Java.
echo.
pause
