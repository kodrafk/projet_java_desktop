@echo off
echo ========================================
echo Pushing to Git - Branch: integration_java
echo ========================================
echo.

echo Step 1: Checking current branch...
git branch
echo.

echo Step 2: Switching to integration_java branch...
git checkout integration_java
if %errorlevel% neq 0 (
    echo Branch doesn't exist locally, creating it...
    git checkout -b integration_java
)
echo.

echo Step 3: Checking status...
git status
echo.

echo Step 4: Adding all files...
git add .
echo.

echo Step 5: Committing changes...
git commit -m "Add complaint system with AI emotion analysis features"
echo.

echo Step 6: Pushing to remote...
git push origin integration_java
if %errorlevel% neq 0 (
    echo First push, setting upstream...
    git push -u origin integration_java
)
echo.

echo ========================================
echo Push completed!
echo ========================================
pause
