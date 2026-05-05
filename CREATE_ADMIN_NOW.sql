-- ============================================================
-- NutriLife — Create/Fix admin accounts
-- Run this in phpMyAdmin on the "integration" database
-- ============================================================

USE `integration`;

-- Fix roles column (remove any CHECK constraint issues)
ALTER TABLE `user` MODIFY COLUMN `roles` VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER';

-- Fix any corrupted role values
UPDATE `user` SET `roles` = 'ROLE_ADMIN' WHERE `roles` LIKE '%ROLE_ADMIN%' AND `roles` != 'ROLE_ADMIN';
UPDATE `user` SET `roles` = 'ROLE_USER'  WHERE `roles` NOT IN ('ROLE_USER','ROLE_ADMIN');

-- ============================================================
-- Admin: admin@nutrilife.com / Admin@1234
-- ============================================================
INSERT INTO `user`
  (email, password, roles, is_active, first_name, last_name, birthday, weight, height)
VALUES
  ('admin@nutrilife.com',
   '$2a$10$DstY7zTbI5t3GiaFr3qIfu/ufaNxt7i2xxMUTjcM28YOdxuAXMqUq',
   'ROLE_ADMIN', 1, 'Admin', 'NutriLife', '1990-01-01', 70, 175)
ON DUPLICATE KEY UPDATE
  password  = '$2a$10$DstY7zTbI5t3GiaFr3qIfu/ufaNxt7i2xxMUTjcM28YOdxuAXMqUq',
  roles     = 'ROLE_ADMIN',
  is_active = 1;

-- ============================================================
-- Admin: salim@gmail.com / Salim@1234
-- ============================================================
INSERT INTO `user`
  (email, password, roles, is_active, first_name, last_name, birthday, weight, height)
VALUES
  ('salim@gmail.com',
   '$2a$10$H0Tm0fLk3JvnFA.lKnPB1uU4pmKkPH3SBrYxbqilfg/7T5EAnwFGy',
   'ROLE_ADMIN', 1, 'Salim', 'Admin', '1995-01-01', 70, 175)
ON DUPLICATE KEY UPDATE
  password  = '$2a$10$H0Tm0fLk3JvnFA.lKnPB1uU4pmKkPH3SBrYxbqilfg/7T5EAnwFGy',
  roles     = 'ROLE_ADMIN',
  is_active = 1;

-- ============================================================
-- User: user@nutrilife.com / User@1234
-- ============================================================
INSERT INTO `user`
  (email, password, roles, is_active, first_name, last_name, birthday, weight, height)
VALUES
  ('user@nutrilife.com',
   '$2a$10$.cdVvGjEvT3ixkfCfErmNO/xqlRHZpEe4TFORASKCL3koUB3VZfaO',
   'ROLE_USER', 1, 'Demo', 'User', '1995-06-15', 70, 175)
ON DUPLICATE KEY UPDATE
  password  = '$2a$10$.cdVvGjEvT3ixkfCfErmNO/xqlRHZpEe4TFORASKCL3koUB3VZfaO',
  roles     = 'ROLE_USER',
  is_active = 1;

-- ============================================================
-- Verify result
-- ============================================================
SELECT id, email, roles, is_active FROM `user`
WHERE email IN ('admin@nutrilife.com','salim@gmail.com','user@nutrilife.com');
