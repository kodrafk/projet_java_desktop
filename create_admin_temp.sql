USE nutrilife_db;

INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height)
VALUES ('superadmin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', 1, 'Super', 'Admin', '1990-01-01', 75.0, 175.0);
