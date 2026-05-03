@echo off
title TEST REAL CAMERA
color 0A
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                  📷 TEST REAL CAMERA 📷                       ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [1/3] FERMETURE DES APPS CONFLICTUELLES...
echo ────────────────────────────────────────────────────────────────
taskkill /f /im Teams.exe /im Zoom.exe /im Skype.exe /im chrome.exe /im msedge.exe 2>nul
timeout /t 2 /nobreak >nul
echo ✅ Apps fermées

echo.
echo [2/3] COMPILATION DU SERVICE...
echo ────────────────────────────────────────────────────────────────
javac -cp "lib/*;src/main/java" src/main/java/tn/esprit/projet/services/RealCameraService.java
if %errorlevel% equ 0 (
    echo ✅ RealCameraService compilé
) else (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)

javac -cp "lib/*;src/main/java;target/classes" src/main/java/tn/esprit/projet/gui/FaceIdVerifyController.java
if %errorlevel% equ 0 (
    echo ✅ FaceIdVerifyController compilé
) else (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)

echo.
echo [3/3] CRÉATION DU TEST...
echo ────────────────────────────────────────────────────────────────

echo import tn.esprit.projet.services.RealCameraService; > TestRealCamera.java
echo import javafx.embed.swing.SwingFXUtils; >> TestRealCamera.java
echo import javafx.scene.image.WritableImage; >> TestRealCamera.java
echo import javax.imageio.ImageIO; >> TestRealCamera.java
echo import java.io.File; >> TestRealCamera.java
echo. >> TestRealCamera.java
echo public class TestRealCamera { >> TestRealCamera.java
echo     public static void main(String[] args) { >> TestRealCamera.java
echo         System.out.println("=== TEST REAL CAMERA ==="); >> TestRealCamera.java
echo         RealCameraService cam = new RealCameraService(); >> TestRealCamera.java
echo         if (cam.open()) { >> TestRealCamera.java
echo             System.out.println("✅ Camera opened!"); >> TestRealCamera.java
echo             WritableImage img = cam.grabFrame(); >> TestRealCamera.java
echo             if (img != null) { >> TestRealCamera.java
echo                 try { >> TestRealCamera.java
echo                     ImageIO.write(SwingFXUtils.fromFXImage(img, null), "jpg", new File("real_camera_test.jpg")); >> TestRealCamera.java
echo                     System.out.println("✅ Image saved: real_camera_test.jpg"); >> TestRealCamera.java
echo                 } catch (Exception e) { e.printStackTrace(); } >> TestRealCamera.java
echo             } >> TestRealCamera.java
echo             cam.close(); >> TestRealCamera.java
echo         } else { >> TestRealCamera.java
echo             System.out.println("❌ Cannot open camera"); >> TestRealCamera.java
echo         } >> TestRealCamera.java
echo     } >> TestRealCamera.java
echo } >> TestRealCamera.java

javac -cp "lib/*;src/main/java;target/classes" TestRealCamera.java
if %errorlevel% neq 0 (
    echo ❌ Erreur compilation test
    pause
    exit /b 1
)

echo ✅ Test compilé
echo.
echo LANCEMENT DU TEST...
echo ────────────────────────────────────────────────────────────────
java -cp ".;lib/*;src/main/java;target/classes" TestRealCamera

echo.
echo ╔════════════════════════════════════════════════════════════════╗
if exist real_camera_test.jpg (
    echo ║                    🎉 SUCCÈS! 🎉                              ║
    echo ╠════════════════════════════════════════════════════════════════╣
    echo ║  ✅ La VRAIE caméra s'est ouverte!                            ║
    echo ║  ✅ Image capturée: real_camera_test.jpg                      ║
    echo ║  ✅ Face ID va maintenant afficher votre VRAI visage          ║
    echo ║                                                                ║
    echo ║  🚀 RELANCEZ VOTRE APPLICATION MAINTENANT!                    ║
    color 0A
) else (
    echo ║                    ❌ ÉCHEC ❌                                 ║
    echo ╠════════════════════════════════════════════════════════════════╣
    echo ║  ❌ La caméra ne s'est pas ouverte                            ║
    echo ║                                                                ║
    echo ║  🔧 SOLUTIONS:                                                ║
    echo ║  • Fermez TOUTES les apps (Teams, Zoom, Chrome)              ║
    echo ║  • Redémarrez l'ordinateur                                    ║
    echo ║  • Vérifiez les permissions Windows                           ║
    color 0C
)
echo ╚════════════════════════════════════════════════════════════════╝

echo.
if exist real_camera_test.jpg (
    echo Ouvrir l'image capturée?
    set /p open="(O/N): "
    if /i "%open%"=="O" start real_camera_test.jpg
)

pause