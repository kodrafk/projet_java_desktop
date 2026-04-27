@echo off
color 0A
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     🔑 COMPTE ADMIN - VÉRIFICATION ET CRÉATION
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Configuration MySQL
set MYSQL_USER=root
set MYSQL_PASSWORD=
set MYSQL_DATABASE=nutrilife
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo [1/3] Vérification des comptes admin existants...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% -e "SELECT id, email, first_name, last_name, role FROM users WHERE role = 'ROLE_ADMIN';"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ✗ Erreur de connexion à MySQL
    echo.
    pause
    exit /b 1
)

echo.
echo [2/3] Création d'un compte admin de test (si nécessaire)...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% << EOF

-- Créer un compte admin de test
INSERT IGNORE INTO users (email, password, first_name, last_name, role, is_active, created_at)
VALUES (
    'admin@nutrilife.com',
    '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ',
    'Admin',
    'NutriLife',
    'ROLE_ADMIN',
    TRUE,
    NOW()
);

EOF

echo.
echo [3/3] Affichage des comptes admin disponibles...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% %MYSQL_DATABASE% << EOF

SELECT 
    '═══════════════════════════════════════════════════════════════' as '';
SELECT '🔑 COMPTES ADMIN DISPONIBLES' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';

SELECT 
    CONCAT('📧 Email: ', email) as 'Identifiants',
    CONCAT('👤 Nom: ', first_name, ' ', last_name) as '',
    CONCAT('🔐 Mot de passe: admin123 (par défaut)') as ''
FROM users 
WHERE role = 'ROLE_ADMIN'
LIMIT 5;

SELECT '' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '💡 INSTRUCTIONS' as '';
SELECT '═══════════════════════════════════════════════════════════════' as '';
SELECT '' as '';
SELECT '1. Lancez votre application NutriLife' as '';
SELECT '2. Utilisez un des emails ci-dessus' as '';
SELECT '3. Mot de passe par défaut: admin123' as '';
SELECT '4. Menu → HEALTH AI → Anomaly Detection' as '';
SELECT '' as '';

EOF

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo     ✅ VÉRIFICATION TERMINÉE
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo 💡 Si aucun compte n'apparaît ci-dessus, créez-en un manuellement :
echo.
echo    Email: admin@nutrilife.com
echo    Password: admin123
echo    Role: ROLE_ADMIN
echo.
pause
