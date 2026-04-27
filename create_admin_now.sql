USE nutrilife;

-- Créer plusieurs comptes admin pour être sûr
INSERT IGNORE INTO users (email, password, first_name, last_name, role, is_active, created_at) VALUES
('admin123@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ', 'Admin', 'User', 'ROLE_ADMIN', 1, NOW()),
('admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ', 'Super', 'Admin', 'ROLE_ADMIN', 1, NOW()),
('admin.test@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JZqmqQhJ5YzVKLNPCZQqJZQKZQKZ', 'Admin', 'Test', 'ROLE_ADMIN', 1, NOW());

SELECT 'Comptes admin créés!' as Status;
SELECT email, first_name, last_name FROM users WHERE role = 'ROLE_ADMIN';
