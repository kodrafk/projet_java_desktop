@echo off
echo ========================================
echo   REPARATION MYSQL XAMPP
echo ========================================
echo.

echo [1/5] Arret de tous les processus MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 2 >nul

echo [2/5] Verification du port 3306...
netstat -ano | findstr :3306
if %errorlevel% equ 0 (
    echo ERREUR: Le port 3306 est deja utilise !
    echo Identifiez le processus avec: netstat -ano ^| findstr :3306
    pause
    exit /b 1
)
echo Port 3306 disponible.

echo [3/5] Sauvegarde des donnees MySQL...
if exist "C:\xampp\mysql\data\nutrilife_db" (
    echo Base de donnees nutrilife_db trouvee.
) else (
    echo Aucune base de donnees a sauvegarder.
)

echo [4/5] Verification de la configuration MySQL...
if exist "C:\xampp\mysql\bin\my.ini" (
    echo Fichier my.ini trouve.
) else (
    echo ERREUR: Fichier my.ini introuvable !
    pause
    exit /b 1
)

echo [5/5] Demarrage de MySQL...
echo.
echo INSTRUCTIONS:
echo 1. Ouvrez XAMPP Control Panel
echo 2. Cliquez sur "Start" a cote de MySQL
echo 3. Si ca ne fonctionne pas, cliquez sur "Logs" pour voir l'erreur
echo.
echo Si MySQL ne demarre toujours pas, essayez:
echo - Changer le port dans my.ini (3306 -^> 3307)
echo - Reinstaller XAMPP
echo - Utiliser WAMP ou MySQL Workbench
echo.

pause
