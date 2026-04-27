@echo off
chcp 65001 >nul
color 0A
cls

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           🚀 LANCEMENT DU SYSTÈME DE DÉTECTION D'ANOMALIES              ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.
echo.

echo ═══════════════════════════════════════════════════════════════════════════
echo ÉTAPE 1/5 : Vérification de MySQL
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo Vérification de la connexion MySQL...
mysql -u root -p -e "SELECT 'MySQL OK' as Status;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ MySQL n'est pas accessible
    echo.
    echo 💡 Solutions:
    echo    1. Démarrez WAMP/XAMPP
    echo    2. Vérifiez que MySQL est en cours d'exécution
    echo    3. Vérifiez vos identifiants MySQL
    echo.
    pause
    exit /b 1
)

echo ✅ MySQL est accessible
echo.
timeout /t 2 >nul

echo ═══════════════════════════════════════════════════════════════════════════
echo ÉTAPE 2/5 : Vérification de la base de données
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo Vérification de la base 'nutrilife'...
mysql -u root -p -e "USE nutrilife; SELECT 'Base OK' as Status;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ Base de données 'nutrilife' introuvable
    echo.
    echo 💡 Créez la base avec:
    echo    mysql -u root -p -e "CREATE DATABASE nutrilife;"
    echo.
    pause
    exit /b 1
)

echo ✅ Base de données 'nutrilife' existe
echo.
timeout /t 2 >nul

echo ═══════════════════════════════════════════════════════════════════════════
echo ÉTAPE 3/5 : Installation des tables d'anomalies
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo Installation des tables...
if exist "CREATE_ANOMALY_DETECTION_TABLES.sql" (
    mysql -u root -p nutrilife < CREATE_ANOMALY_DETECTION_TABLES.sql 2>nul
    if %errorlevel% equ 0 (
        echo ✅ Tables d'anomalies installées
    ) else (
        echo ⚠️  Tables peut-être déjà installées
    )
) else (
    echo ⚠️  Fichier SQL introuvable, tables peut-être déjà installées
)
echo.
timeout /t 2 >nul

echo ═══════════════════════════════════════════════════════════════════════════
echo ÉTAPE 4/5 : Création du compte admin
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo Création du compte admin de test...
if exist "CREATE_ADMIN_FOR_TESTING.sql" (
    mysql -u root -p nutrilife < CREATE_ADMIN_FOR_TESTING.sql 2>nul
    if %errorlevel% equ 0 (
        echo ✅ Compte admin créé
    ) else (
        echo ⚠️  Compte admin peut-être déjà existant
    )
) else (
    echo ⚠️  Fichier SQL introuvable, compte peut-être déjà créé
)
echo.
echo 📧 Email: admin.test@nutrilife.com
echo 🔐 Password: admin123
echo.
timeout /t 2 >nul

echo ═══════════════════════════════════════════════════════════════════════════
echo ÉTAPE 5/5 : Compilation et lancement de l'application
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo Compilation du projet Maven...
echo.
call mvn clean compile

if %errorlevel% neq 0 (
    echo.
    echo ❌ Erreur de compilation
    echo.
    echo 💡 Solutions:
    echo    1. Vérifiez que Maven est installé: mvn --version
    echo    2. Vérifiez que JAVA_HOME est configuré
    echo    3. Exécutez: mvn clean install
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Compilation réussie
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo 🚀 LANCEMENT DE L'APPLICATION
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo L'application va se lancer dans quelques secondes...
echo.
echo 📝 INSTRUCTIONS:
echo    1. Connectez-vous avec: admin.test@nutrilife.com / admin123
echo    2. Dans le menu latéral, cherchez "HEALTH AI"
echo    3. Cliquez sur "🔍 Anomaly Detection"
echo    4. Cliquez sur "🚀 Lancer Détection"
echo    5. Observez les résultats!
echo.
timeout /t 3 >nul

echo Lancement...
call mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ❌ Erreur au lancement
    echo.
    echo 💡 Essayez de lancer depuis votre IDE:
    echo    - IntelliJ: Clic droit sur MainApp.java → Run
    echo    - Eclipse: Clic droit sur MainApp.java → Run As → Java Application
    echo.
    pause
    exit /b 1
)

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo ✅ APPLICATION FERMÉE
echo ═══════════════════════════════════════════════════════════════════════════
echo.
pause
