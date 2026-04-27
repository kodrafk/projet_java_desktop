@echo off
echo ========================================
echo Testing User Alerts System
echo ========================================
echo.

echo Step 1: Checking if table exists...
mysql -u root -p -e "USE nutrilife; DESCRIBE user_alerts;" 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Table does not exist. Creating it now...
    mysql -u root -p nutrilife < CREATE_USER_ALERTS_TABLE.sql
    echo.
    echo Table created successfully!
) else (
    echo Table already exists!
)

echo.
echo Step 2: Checking data...
mysql -u root -p -e "USE nutrilife; SELECT COUNT(*) AS alert_count FROM user_alerts;"

echo.
echo Step 3: Checking users...
mysql -u root -p -e "USE nutrilife; SELECT id, email, roles FROM users LIMIT 5;"

echo.
echo ========================================
echo Test Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Launch the application: mvn javafx:run
echo 2. Login as admin
echo 3. Go to Menu: User Alerts
echo 4. Send your first alert!
echo.

pause
