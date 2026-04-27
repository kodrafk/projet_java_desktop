@echo off
echo ========================================
echo   CREATION DEUXIEME COMPTE ADMIN
echo ========================================
echo.

cd /d "%~dp0"

echo Insertion du compte admin dans nutrilife.db...
echo.

sqlite3 nutrilife.db "DELETE FROM user WHERE email = 'superadmin@nutrilife.com';"
sqlite3 nutrilife.db "INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height, created_at) VALUES ('superadmin@nutrilife.com', '$2a$10$rZ5FQjhmQzGLYh5nY5vXXeKXPvqP5PqP5PqP5PqP5PqP5PqP5PqPO', 'ROLE_ADMIN', 1, 'Super', 'Admin', '1985-05-15', 80.0, 180.0, datetime('now'));"

echo.
echo Verification...
sqlite3 nutrilife.db "SELECT id, email, roles, first_name, last_name FROM user WHERE email = 'superadmin@nutrilife.com';"

echo.
echo ========================================
echo   COMPTE ADMIN CREE !
echo ========================================
echo.
echo Email: superadmin@nutrilife.com
echo Password: SuperAdmin@2024
echo Role: ROLE_ADMIN
echo.
pause
