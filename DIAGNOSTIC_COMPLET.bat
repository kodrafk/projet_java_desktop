@echo off
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                            ║
echo ║                    DIAGNOSTIC COMPLET DU SYSTÈME                          ║
echo ║                                                                            ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.

echo [1/5] Vérification de la base de données...
echo.

REM Chercher MySQL dans les emplacements courants
set MYSQL_FOUND=0

if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
    set MYSQL_FOUND=1
    echo ✅ MySQL trouvé dans XAMPP
)

if exist "C:\wamp64\bin\mysql\mysql8.0.27\bin\mysql.exe" (
    set MYSQL_PATH="C:\wamp64\bin\mysql\mysql8.0.27\bin\mysql.exe"
    set MYSQL_FOUND=1
    echo ✅ MySQL trouvé dans WAMP64
)

if exist "C:\wamp\bin\mysql\mysql8.0.27\bin\mysql.exe" (
    set MYSQL_PATH="C:\wamp\bin\mysql\mysql8.0.27\bin\mysql.exe"
    set MYSQL_FOUND=1
    echo ✅ MySQL trouvé dans WAMP
)

if %MYSQL_FOUND%==0 (
    echo ❌ MySQL non trouvé dans les emplacements standards
    echo.
    echo L'application utilise probablement SQLite (nutrilife.db)
    echo.
    goto CHECK_SQLITE
)

echo.
echo [2/5] Vérification de la base nutrilife...
echo.

%MYSQL_PATH% -u root -p -e "USE nutrilife; SELECT 'Base nutrilife OK' AS status;"

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Impossible de se connecter à la base nutrilife
    echo.
    goto CHECK_SQLITE
)

echo.
echo [3/5] Vérification de la table user...
echo.

%MYSQL_PATH% -u root -p -e "USE nutrilife; SELECT COUNT(*) as total_users FROM user;"

echo.
echo [4/5] Vérification des rôles...
echo.

%MYSQL_PATH% -u root -p -e "USE nutrilife; SELECT roles, COUNT(*) as count FROM user GROUP BY roles;"

echo.
echo [5/5] Affichage des utilisateurs...
echo.

%MYSQL_PATH% -u root -p -e "USE nutrilife; SELECT id, email, roles, first_name, last_name FROM user;"

goto END

:CHECK_SQLITE
echo.
echo [SQLite] Vérification du fichier nutrilife.db...
echo.

if exist "nutrilife.db" (
    echo ✅ Fichier nutrilife.db trouvé
    echo.
    echo Pour voir les données SQLite, utilisez DB Browser for SQLite
    echo Téléchargement : https://sqlitebrowser.org/
) else (
    echo ❌ Fichier nutrilife.db non trouvé
    echo.
    echo L'application va créer ce fichier au premier lancement
)

:END
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo RÉSUMÉ :
echo.
echo Si vous voyez des utilisateurs avec roles = 'ROLE_USER', ils devraient
echo s'afficher dans l'interface.
echo.
echo Si vous ne voyez que des admins (ROLE_ADMIN), exécutez :
echo   AJOUTER_USERS_A_MA_TABLE.sql dans phpMyAdmin
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

pause
