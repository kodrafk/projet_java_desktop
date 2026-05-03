@echo off
color 0C
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🔧 RÉPARATION CAMÉRA - DIAGNOSTIC ET FIX AUTOMATIQUE
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo [ÉTAPE 1] Arrêt de tous les processus...
echo.
taskkill /F /IM python.exe 2>nul
taskkill /F /IM pythonw.exe 2>nul
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul
timeout /t 2 >nul
echo ✓ Processus arrêtés
echo.

echo [ÉTAPE 2] Vérification de Python...
echo.
where python >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Python n'est pas installé
    echo.
    echo 💡 SOLUTION :
    echo    1. Téléchargez Python : https://www.python.org/downloads/
    echo    2. Cochez "Add Python to PATH" pendant l'installation
    echo    3. Relancez ce script
    echo.
    pause
    exit /b 1
)
python --version
echo ✓ Python OK
echo.

echo [ÉTAPE 3] Vérification d'OpenCV...
echo.
python -c "import cv2; print('✓ OpenCV version:', cv2.__version__)" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ OpenCV manquant
    echo.
    echo Installation d'OpenCV...
    python -m pip install --upgrade opencv-python
    if %ERRORLEVEL% NEQ 0 (
        echo ✗ Erreur d'installation
        pause
        exit /b 1
    )
    echo ✓ OpenCV installé
) else (
    echo ✓ OpenCV OK
)
echo.

echo [ÉTAPE 4] Test de la caméra...
echo.
python -c "import cv2; cap = cv2.VideoCapture(0, cv2.CAP_DSHOW if hasattr(cv2, 'CAP_DSHOW') else cv2.CAP_ANY); print('✓ Caméra:', 'OK' if cap.isOpened() else 'BLOQUÉE'); cap.release()" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Caméra bloquée
    echo.
    echo 💡 CAUSES POSSIBLES :
    echo    • Skype, Teams, Zoom, Discord utilisent la caméra
    echo    • Permissions Windows bloquées
    echo    • Pilote caméra défectueux
    echo.
    echo 🔧 SOLUTIONS :
    echo    1. Fermez Skype, Teams, Zoom, Discord
    echo    2. Paramètres Windows → Confidentialité → Caméra
    echo       → Activez "Autoriser les applications de bureau"
    echo    3. Redémarrez votre ordinateur
    echo.
    pause
    exit /b 1
)
echo ✓ Caméra accessible
echo.

echo [ÉTAPE 5] Test du serveur caméra...
echo.
echo Démarrage du serveur (appuyez sur Ctrl+C pour arrêter)...
echo.
echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ Si vous voyez votre visage, la caméra fonctionne !                   │
echo │                                                                       │
echo │ Vous devriez voir :                                                  │
echo │  • Votre visage en temps réel                                        │
echo │  • Cadre coloré autour du visage                                     │
echo │  • Message de qualité                                                │
echo │                                                                       │
echo │ Si l'écran reste noir :                                              │
echo │  • Vérifiez que votre caméra n'est pas couverte                      │
echo │  • Essayez de bouger devant la caméra                                │
echo │  • Vérifiez l'éclairage (pas trop sombre)                            │
echo └───────────────────────────────────────────────────────────────────────┘
echo.

REM Try professional server first, fallback to standard
if exist camera_server_pro.py (
    echo Lancement du serveur professionnel...
    python camera_server_pro.py 7654
) else if exist camera_server.py (
    echo Lancement du serveur standard...
    python camera_server.py 7654
) else (
    echo ✗ Fichier camera_server.py introuvable
    echo.
    echo 💡 Assurez-vous d'être dans le dossier projetJAV
    echo.
    pause
    exit /b 1
)

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     Test terminé
echo ═══════════════════════════════════════════════════════════════════════════
echo.
pause
