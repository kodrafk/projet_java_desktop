@echo off
echo ========================================
echo Rebuilding NutriCoach Pro Application
echo ========================================
echo.

echo Step 1: Cleaning old build files...
call mvn clean

echo.
echo Step 2: Compiling source code...
call mvn compile

echo.
echo Step 3: Running the application...
call mvn javafx:run

echo.
echo Done!
pause
