@echo off
cls
color 0A
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🧪 TEST RAPIDE - FACE ID SYSTEM 🧪                    ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

echo 1. Test de Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python n'est pas installe
    echo    Installez Python depuis: https://www.python.org/downloads/
    goto :end
) else (
    echo ✅ Python installe
)

echo.
echo 2. Test du serveur Python...
curl -s http://localhost:5000/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️ Serveur Python non demarre
    echo    Demarrez-le avec: cd python_face_server ^&^& START_SERVER.bat
) else (
    echo ✅ Serveur Python en cours d'execution
)

echo.
echo 3. Test de Maven...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven n'est pas installe
) else (
    echo ✅ Maven installe
)

echo.
echo 4. Test de compilation Java...
cd /d "%~dp0"
mvn compile -q >nul 2>&1
if errorlevel 1 (
    echo ❌ Erreur de compilation
) else (
    echo ✅ Compilation reussie
)

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo RÉSUMÉ:
echo.
echo Pour lancer l'application:
echo   1. Demarrez le serveur Python: cd python_face_server ^&^& START_SERVER.bat
echo   2. Lancez l'application: mvn javafx:run
echo.
echo OU utilisez le script automatique:
echo   LANCER_TOUT.bat
echo.

:end
pause
