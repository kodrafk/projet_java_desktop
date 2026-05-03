@echo off
echo ========================================
echo Testing Google Authentication Setup
echo ========================================

echo.
echo 1. Testing database connectivity and Google ID operations...
cd /d "%~dp0"
mvn -q compile exec:java -Dexec.mainClass="tn.esprit.projet.test.GoogleAuthTest" -Dexec.args=""

echo.
echo 2. Checking Google OAuth configuration...
mvn -q compile exec:java -Dexec.mainClass="tn.esprit.projet.test.GoogleAuthConfigChecker" -Dexec.args=""

echo.
echo ========================================
echo Test completed!
echo ========================================
echo.
echo If tests pass, your Google Auth should work.
echo To test the full flow:
echo 1. Run your JavaFX application
echo 2. Navigate to the Google sign-in screen
echo 3. Complete the OAuth flow
echo.
pause