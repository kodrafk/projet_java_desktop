@echo off
color 0B
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🚀 INSTALLATION PROFESSIONAL FACE ID SYSTEM
echo     Enterprise-Grade Face Recognition with Eye Tracking
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo [1/5] Verification de Python...
echo.
where python >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Python n'est pas installe
    echo.
    echo 💡 Installez Python 3.8+ : https://www.python.org/downloads/
    echo    Cochez "Add Python to PATH" pendant l'installation
    echo.
    pause
    exit /b 1
)

python --version
echo ✓ Python detecte
echo.

echo [2/5] Installation des dependances principales...
echo.
python -m pip install --upgrade pip
python -m pip install opencv-python numpy pillow
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Erreur d'installation
    pause
    exit /b 1
)
echo ✓ OpenCV et NumPy installes
echo.

echo [3/5] Installation de DeepFace (ArcFace model)...
echo.
echo ⏳ Cela peut prendre 2-3 minutes (telechargement des modeles AI)...
echo.
python -m pip install deepface tf-keras
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Erreur d'installation DeepFace
    pause
    exit /b 1
)
echo ✓ DeepFace installe
echo.

echo [4/5] Telechargement des modeles AI...
echo.
python -c "from deepface import DeepFace; DeepFace.build_model('ArcFace'); DeepFace.build_model('RetinaFace'); print('✓ Models downloaded')"
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️ Les modeles seront telecharges au premier usage
)
echo.

echo [5/5] Test du systeme...
echo.
python -c "import cv2; import numpy; from deepface import DeepFace; print('✓ All dependencies OK')"
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Erreur de test
    pause
    exit /b 1
)

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     ✅ INSTALLATION COMPLETE
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo 📋 Systeme installe :
echo    • OpenCV - Capture camera haute qualite
echo    • DeepFace - Reconnaissance faciale IA
echo    • ArcFace - Modele state-of-the-art
echo    • RetinaFace - Detection precise des traits
echo.
echo 🎯 Fonctionnalites :
echo    ✓ Detection des yeux en temps reel
echo    ✓ Analyse de qualite d'image
echo    ✓ Alignement automatique du visage
echo    ✓ Focus sur les traits discriminants
echo    ✓ Feedback visuel professionnel
echo.
echo 🚀 Prochaine etape :
echo    Lancez l'application avec LAUNCH_APP.bat
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.
pause
