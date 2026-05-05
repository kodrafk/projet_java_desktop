INSERT INTO `user` (email, password, roles, first_name, last_name, birthday, is_active)
VALUES (
  'admin@nutrilife.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'ROLE_ADMIN',
  'Admin',
  'User',
  '1990-01-01',
  1
);

INSERT INTO `user` (email, password, roles, first_name, last_name, birthday, is_active)
VALUES (
  'user@nutrilife.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'ROLE_USER',
  'Test',
  'User',
  '1995-06-15',
  1
);

SELECT id, email, roles FROM `user`;
