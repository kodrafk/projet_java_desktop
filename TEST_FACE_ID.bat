@echo off
cls
color 0B
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         🎯 TEST FACE ID - PROFESSIONAL SYSTEM 🎯              ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo INSTRUCTIONS:
echo.
echo 1. L'application va s'ouvrir
echo 2. Cliquez sur "Face ID Login" (bouton bleu)
echo 3. La camera va s'ouvrir avec interface professionnelle
echo 4. Positionnez votre visage dans le cercle
echo 5. Le systeme va automatiquement vous authentifier
echo.
echo IMPORTANT:
echo - Si c'est votre premiere fois, vous devez d'abord vous inscrire
echo - Ensuite, allez dans votre profil pour enroller votre Face ID
echo - Apres enrollment, vous pouvez utiliser Face ID Login
echo.
echo Demarrage dans 3 secondes...
timeout /t 3 >nul

cd /d "%~dp0"
call mvn javafx:run

pause
