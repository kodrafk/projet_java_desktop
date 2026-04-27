@echo off
echo ========================================
echo  TEST NOTIFICATION PROFESSIONNELLE
echo ========================================
echo.
echo CORRECTIONS APPORTEES :
echo [✓] Nom d'utilisateur affiche correctement (plus de {0})
echo [✓] Positionnement intelligent (adapte a l'ecran)
echo [✓] Design moderne avec glassmorphism
echo [✓] Animations fluides et professionnelles
echo [✓] Traductions parfaites FR/EN
echo.
echo FONCTIONNALITES A TESTER :
echo 1. Message avec le VRAI nom d'utilisateur
echo 2. Position en haut a droite (intelligente)
echo 3. Duree : 10s pour user, 6.5s pour admin
echo 4. Changement de langue dynamique
echo 5. Animations d'entree/sortie fluides
echo 6. Hover effects et interactions
echo.

cd /d "%~dp0"

echo Lancement de l'application...
echo.
start mvn javafx:run

echo.
echo IDENTIFIANTS DE TEST :
echo Admin : admin@nutrilife.com / admin123
echo User  : user@test.com / password123
echo.
echo L'application se lance...
pause