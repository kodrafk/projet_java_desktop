@echo off
echo ========================================
echo   CREATION COMPTE ADMIN SQLITE
echo ========================================
echo.

cd /d "%~dp0"

echo Insertion du compte admin dans nutrilife.db...
echo.

sqlite3 nutrilife.db "DELETE FROM user WHERE email = 'admin@nutrilife.com';"
sqlite3 nutrilife.db "INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height, created_at) VALUES ('admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', 1, 'Admin', 'NutriLife', '1990-01-01', 75.0, 175.0, datetime('now'));"

echo.
echo Verification...
sqlite3 nutrilife.db "SELECT id, email, roles, first_name, last_name FROM user WHERE email = 'admin@nutrilife.com';"

echo.
echo ========================================
echo   COMPTE ADMIN CREE !
echo ========================================
echo.
echo Email: admin@nutrilife.com
echo Password: Admin@1234
echo Role: ROLE_ADMIN
echo.
pause
