@echo off
color 0A
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🧪 TEST PROFESSIONAL FACE ID SYSTEM
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo [TEST 1] Verification des dependances...
echo.
python -c "import cv2; print('✓ OpenCV:', cv2.__version__)" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ OpenCV manquant
    echo 💡 Lancez INSTALL_PROFESSIONAL_FACE_ID.bat
    pause
    exit /b 1
)

python -c "import numpy; print('✓ NumPy:', numpy.__version__)" 2>nul
python -c "from deepface import DeepFace; print('✓ DeepFace OK')" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ DeepFace manquant
    echo 💡 Lancez INSTALL_PROFESSIONAL_FACE_ID.bat
    pause
    exit /b 1
)

echo.
echo [TEST 2] Test de la camera...
echo.
python -c "import cv2; cap = cv2.VideoCapture(0, cv2.CAP_DSHOW); print('✓ Camera:', 'OK' if cap.isOpened() else 'UNAVAILABLE'); cap.release()" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️ Camera non disponible
    echo 💡 Fermez Skype, Teams, Zoom, etc.
)

echo.
echo [TEST 3] Test du serveur camera professionnel...
echo.
echo Demarrage du serveur (appuyez sur Ctrl+C pour arreter)...
echo.
echo ┌───────────────────────────────────────────────────────────────────────┐
echo │ 📹 SERVEUR CAMERA PROFESSIONNEL                                       │
echo ├───────────────────────────────────────────────────────────────────────┤
echo │                                                                       │
echo │ Fonctionnalites actives :                                            │
echo │  ✓ Detection de visage en temps reel                                 │
echo │  ✓ Detection des yeux (focus principal)                              │
echo │  ✓ Analyse de qualite d'image                                        │
echo │  ✓ Feedback visuel (couleurs + messages)                             │
echo │  ✓ Alignement automatique                                            │
echo │  ✓ Resolution HD (1280x720)                                          │
echo │                                                                       │
echo │ Indicateurs visuels :                                                │
echo │  🟢 Vert   = Qualite excellente (85-100%%)                            │
echo │  🟡 Jaune  = Qualite bonne (70-84%%)                                  │
echo │  🟠 Orange = Qualite moyenne (50-69%%)                                │
echo │  🔴 Rouge  = Qualite faible (<50%%)                                   │
echo │                                                                       │
echo └───────────────────────────────────────────────────────────────────────┘
echo.

python camera_server_pro.py 7654

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     Test termine
echo ═══════════════════════════════════════════════════════════════════════════
echo.
pause
