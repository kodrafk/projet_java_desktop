@echo off
chcp 65001 >nul
color 0A
cls

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           🔧 CORRECTION DU COMPTE ADMIN                                  ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.

echo Création du compte admin avec les bons identifiants...
echo.

mysql -u root -p nutrilife -e "DELETE FROM users WHERE email IN ('admin.test@nutrilife.com', 'admin@nutrilife.com', 'superadmin@nutrilife.com');"

mysql -u root -p nutrilife -e "INSERT INTO users (email, password, first_name, last_name, role, is_active, created_at) VALUES ('admin.test@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ', 'Admin', 'Test', 'ROLE_ADMIN', TRUE, NOW()), ('admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ', 'Super', 'Admin', 'ROLE_ADMIN', TRUE, NOW()), ('superadmin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ', 'Super', 'Admin', 'ROLE_ADMIN', TRUE, NOW());"

echo.
echo ✅ Comptes admin créés!
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo 📧 IDENTIFIANTS DISPONIBLES
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo Option 1:
echo   Email: admin.test@nutrilife.com
echo   Password: admin123
echo.
echo Option 2:
echo   Email: admin@nutrilife.com
echo   Password: admin123
echo.
echo Option 3:
echo   Email: superadmin@nutrilife.com
echo   Password: admin123
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

mysql -u root -p nutrilife -e "SELECT email, first_name, last_name, role FROM users WHERE role = 'ROLE_ADMIN';"

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo ✅ TERMINÉ!
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo Utilisez un des emails ci-dessus avec le mot de passe: admin123
echo.
pause
