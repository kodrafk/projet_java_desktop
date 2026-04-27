@echo off
echo ============================================================
echo  Fix Face ID Tables - MySQL Tablespace Repair
echo ============================================================
echo.
set /p MYSQL_USER=MySQL username (default: root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root
set /p MYSQL_PASS=MySQL password: 
set /p MYSQL_PORT=MySQL port (default: 3306): 
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

echo.
echo Step 1: Discard corrupted tablespaces and drop tables...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db -e "SET FOREIGN_KEY_CHECKS=0; SET GLOBAL innodb_force_recovery=0; ALTER TABLE face_embeddings DISCARD TABLESPACE;" 2>nul
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db -e "SET FOREIGN_KEY_CHECKS=0; ALTER TABLE face_verification_attempts DISCARD TABLESPACE;" 2>nul
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db -e "SET FOREIGN_KEY_CHECKS=0; DROP TABLE IF EXISTS face_verification_attempts, face_embeddings; SET FOREIGN_KEY_CHECKS=1;"

echo.
echo Step 2: Creating fresh tables...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db -e "CREATE TABLE IF NOT EXISTS face_embeddings (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL UNIQUE, embedding_encrypted MEDIUMTEXT NOT NULL, encryption_iv VARCHAR(255) NOT NULL, encryption_tag VARCHAR(255) NOT NULL, is_active TINYINT(1) NOT NULL DEFAULT 1, consent_given_at DATETIME DEFAULT NULL, consent_ip VARCHAR(45) DEFAULT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, last_used_at DATETIME DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db -e "CREATE TABLE IF NOT EXISTS face_verification_attempts (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT DEFAULT NULL, email VARCHAR(255) DEFAULT NULL, ip_address VARCHAR(45) NOT NULL DEFAULT '127.0.0.1', success TINYINT(1) NOT NULL DEFAULT 0, similarity_score DOUBLE DEFAULT NULL, attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo.
echo Step 3: Verifying...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -P %MYSQL_PORT% nutrilife_db -e "SHOW TABLES LIKE 'face%%';"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo  SUCCESS! Face ID tables are ready.
    echo  Restart the app and try Face ID enrollment again.
    echo ============================================================
) else (
    echo.
    echo ERROR. Try running MySQL Workbench and execute:
    echo   SET FOREIGN_KEY_CHECKS=0;
    echo   DROP TABLE IF EXISTS face_verification_attempts;
    echo   DROP TABLE IF EXISTS face_embeddings;
    echo   SET FOREIGN_KEY_CHECKS=1;
    echo Then run CREATE_FACE_TABLES.sql
)
echo.
pause
