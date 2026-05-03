@echo off
title REPARATION CAMERA FINALE
color 0C
echo ========================================
echo    REPARATION CAMERA FACE ID FINALE
echo ========================================
echo.

cd /d "%~dp0"

echo [ETAPE 1] Fermeture FORCEE des applications...
taskkill /f /im Teams.exe 2>nul
taskkill /f /im Zoom.exe 2>nul
taskkill /f /im Skype.exe 2>nul
taskkill /f /im chrome.exe 2>nul
taskkill /f /im msedge.exe 2>nul
taskkill /f /im firefox.exe 2>nul
taskkill /f /im obs64.exe 2>nul
taskkill /f /im python.exe 2>nul
taskkill /f /im pythonw.exe 2>nul
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul
echo ✅ Applications fermees
timeout /t 3 /nobreak >nul

echo [ETAPE 2] Test camera Windows...
powershell -Command "Get-PnpDevice -Class Camera | Select-Object FriendlyName, Status"
echo.

echo [ETAPE 3] Test camera avec sarxos/webcam...
if exist TestDirectCamera.class del TestDirectCamera.class
javac -cp "lib/*;target/classes" TestDirectCamera.java 2>nul
if %errorlevel% equ 0 (
    java -cp ".;lib/*;target/classes" TestDirectCamera
) else (
    echo ❌ Impossible de compiler le test
)

echo.
echo [ETAPE 4] Verification des permissions...
echo Ouvrez Parametres Windows ^> Confidentialite ^> Camera
echo Verifiez que les applications peuvent acceder a la camera
echo.

echo [ETAPE 5] Instructions finales...
echo ========================================
echo    INSTRUCTIONS IMPORTANTES
echo ========================================
echo.
echo 1. Si le test sarxos a reussi:
echo    → Relancez votre application Face ID
echo    → La camera devrait maintenant fonctionner
echo.
echo 2. Si le test a echoue:
echo    → Redemarrez l'ordinateur
echo    → Verifiez les pilotes de camera
echo    → Contactez le support technique
echo.
echo 3. La nouvelle version utilise sarxos/webcam
echo    → Plus de serveur Python
echo    → Acces direct a la camera
echo    → Plus fiable et plus rapide
echo.

if exist test_camera_sarxos.jpg (
    echo ✅ SUCCES: Image de test capturee!
    echo La camera fonctionne parfaitement.
    color 0A
) else (
    echo ❌ ECHEC: Pas d'image capturee
    echo Probleme materiel ou pilotes
    color 0C
)

echo.
pause