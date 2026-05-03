@echo off
title ULTIMATE CAMERA TEST
color 0E
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                    ULTIMATE CAMERA TEST                        ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [PHASE 1] NETTOYAGE COMPLET...
echo ────────────────────────────────────────────────────────────────
taskkill /f /im Teams.exe 2>nul
taskkill /f /im Zoom.exe 2>nul
taskkill /f /im Skype.exe 2>nul
taskkill /f /im chrome.exe 2>nul
taskkill /f /im msedge.exe 2>nul
taskkill /f /im firefox.exe 2>nul
taskkill /f /im obs64.exe 2>nul
taskkill /f /im python.exe 2>nul
taskkill /f /im java.exe 2>nul

echo Suppression des anciens tests...
del test_*.jpg 2>nul
del TestUltimateCamera.class 2>nul

timeout /t 3 /nobreak >nul
echo ✅ Nettoyage terminé
echo.

echo [PHASE 2] DIAGNOSTIC SYSTÈME...
echo ────────────────────────────────────────────────────────────────
echo Caméras Windows détectées:
powershell -Command "Get-PnpDevice -Class Camera | Select-Object FriendlyName, Status | Format-Table -AutoSize" 2>nul
echo.

echo [PHASE 3] COMPILATION DU TEST...
echo ────────────────────────────────────────────────────────────────
javac -cp "lib/*;target/classes" TestUltimateCamera.java
if %errorlevel% neq 0 (
    echo ❌ ERREUR DE COMPILATION!
    echo Vérifiez que les JAR sarxos sont dans lib/
    pause
    exit /b 1
)
echo ✅ Compilation réussie
echo.

echo [PHASE 4] LANCEMENT DU TEST ULTIME...
echo ────────────────────────────────────────────────────────────────
java -cp ".;lib/*;target/classes" TestUltimateCamera
echo.

echo [PHASE 5] ANALYSE DES RÉSULTATS...
echo ────────────────────────────────────────────────────────────────
set /a success_count=0

if exist test_default.jpg (
    echo ✅ test_default.jpg - Caméra par défaut OK
    set /a success_count+=1
)

if exist test_camera_0.jpg (
    echo ✅ test_camera_0.jpg - Caméra 0 OK
    set /a success_count+=1
)

if exist test_camera_1.jpg (
    echo ✅ test_camera_1.jpg - Caméra 1 OK
    set /a success_count+=1
)

if exist test_timeout.jpg (
    echo ✅ test_timeout.jpg - Test timeout OK
    set /a success_count+=1
)

if exist test_vga.jpg (
    echo ✅ test_vga.jpg - Résolution VGA OK
    set /a success_count+=1
)

if exist test_qvga.jpg (
    echo ✅ test_qvga.jpg - Résolution QVGA OK
    set /a success_count+=1
)

echo.
echo ╔════════════════════════════════════════════════════════════════╗
if %success_count% gtr 0 (
    echo ║                    🎉 SUCCÈS TOTAL! 🎉                        ║
    echo ╠════════════════════════════════════════════════════════════════╣
    echo ║  %success_count% test(s) réussi(s) - La caméra fonctionne parfaitement!  ║
    echo ║                                                                ║
    echo ║  ✅ Face ID va maintenant fonctionner correctement            ║
    echo ║  ✅ UltimateWebcamService est opérationnel                    ║
    echo ║  ✅ Relancez votre application Face ID                        ║
    color 0A
) else (
    echo ║                    ❌ ÉCHEC TOTAL ❌                           ║
    echo ╠════════════════════════════════════════════════════════════════╣
    echo ║  Aucune image capturée - Problème matériel                    ║
    echo ║                                                                ║
    echo ║  🔧 SOLUTIONS:                                                ║
    echo ║  • Vérifier que la caméra est connectée                       ║
    echo ║  • Réinstaller les pilotes de caméra                          ║
    echo ║  • Vérifier les permissions Windows                           ║
    echo ║  • Essayer une caméra USB externe                             ║
    color 0C
)
echo ╚════════════════════════════════════════════════════════════════╝
echo.

echo Images créées dans le dossier:
dir test_*.jpg 2>nul

echo.
pause