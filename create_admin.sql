-- Creates a new admin account
-- Email:    superadmin@nutrilife.com
-- Password: admin123
-- BCrypt hash of "admin123" (cost 10)

INSERT INTO `user` (email, password, roles, first_name, last_name, birthday, is_active)
VALUES (
  'superadmin@nutrilife.com',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'ROLE_ADMIN',
  'Super',
  'Admin',
  '1990-01-01',
  1
);
