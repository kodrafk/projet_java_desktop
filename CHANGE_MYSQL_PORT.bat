@echo off
color 0E
echo ========================================
echo   CHANGER LE PORT MYSQL (3306 -^> 3307)
echo ========================================
echo.

echo Cette solution change le port MySQL de 3306 vers 3307
echo pour eviter les conflits.
echo.
pause

echo.
echo [ETAPE 1] Arret de MySQL...
taskkill /F /IM mysqld.exe 2>nul
timeout /t 2 >nul
echo OK

echo.
echo [ETAPE 2] Sauvegarde de my.ini...
if exist "C:\xampp\mysql\bin\my.ini" (
    copy "C:\xampp\mysql\bin\my.ini" "C:\xampp\mysql\bin\my.ini.backup" >nul
    echo OK - Sauvegarde creee: my.ini.backup
) else (
    echo ERREUR: Fichier my.ini introuvable !
    pause
    exit /b 1
)

echo.
echo [ETAPE 3] Modification du port dans my.ini...
powershell -Command "(Get-Content 'C:\xampp\mysql\bin\my.ini') -replace '^port=3306', 'port=3307' | Set-Content 'C:\xampp\mysql\bin\my.ini'"
echo OK - Port change de 3306 vers 3307

echo.
echo ========================================
echo   MODIFICATION TERMINEE !
echo ========================================
echo.
echo PROCHAINES ETAPES:
echo.
echo 1. Ouvrez XAMPP Control Panel
echo 2. Cliquez sur "Start" a cote de MySQL
echo 3. MySQL devrait demarrer sur le port 3307
echo.
echo IMPORTANT: Vous devez aussi changer le port dans votre code Java:
echo    Fichier: DatabaseConnection.java
echo    Ligne 16: private static final int PORT = 3307;
echo.
echo Pour revenir au port 3306:
echo    Restaurez le fichier: C:\xampp\mysql\bin\my.ini.backup
echo.
pause
