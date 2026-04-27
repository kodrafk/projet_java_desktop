-- ============================================================================
-- ADD TEST USERS TO YOUR EXISTING DATABASE
-- ============================================================================
-- This script adds 5 test users with ROLE_USER to your existing "user" table
-- It does NOT create a new database or table
-- It does NOT modify your existing data
-- Password for all test users: "password123"
-- ============================================================================

USE nutrilife;

-- Check if users already exist before inserting
INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at, birthday, weight, height)
SELECT * FROM (
    SELECT 
        'john.doe@nutrilife.com' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as password,
        'ROLE_USER' as roles,
        'John' as first_name,
        'Doe' as last_name,
        1 as is_active,
        NOW() as created_at,
        '1990-05-15' as birthday,
        75.5 as weight,
        175.0 as height
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE email = 'john.doe@nutrilife.com'
);

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at, birthday, weight, height)
SELECT * FROM (
    SELECT 
        'jane.smith@nutrilife.com' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as password,
        'ROLE_USER' as roles,
        'Jane' as first_name,
        'Smith' as last_name,
        1 as is_active,
        NOW() as created_at,
        '1988-08-22' as birthday,
        62.0 as weight,
        165.0 as height
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE email = 'jane.smith@nutrilife.com'
);

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at, birthday, weight, height)
SELECT * FROM (
    SELECT 
        'bob.johnson@nutrilife.com' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as password,
        'ROLE_USER' as roles,
        'Bob' as first_name,
        'Johnson' as last_name,
        1 as is_active,
        NOW() as created_at,
        '1992-03-10' as birthday,
        82.0 as weight,
        180.0 as height
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE email = 'bob.johnson@nutrilife.com'
);

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at, birthday, weight, height)
SELECT * FROM (
    SELECT 
        'alice.williams@nutrilife.com' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as password,
        'ROLE_USER' as roles,
        'Alice' as first_name,
        'Williams' as last_name,
        1 as is_active,
        NOW() as created_at,
        '1995-11-30' as birthday,
        58.5 as weight,
        160.0 as height
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE email = 'alice.williams@nutrilife.com'
);

INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at, birthday, weight, height)
SELECT * FROM (
    SELECT 
        'charlie.brown@nutrilife.com' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as password,
        'ROLE_USER' as roles,
        'Charlie' as first_name,
        'Brown' as last_name,
        1 as is_active,
        NOW() as created_at,
        '1987-07-18' as birthday,
        78.0 as weight,
        178.0 as height
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE email = 'charlie.brown@nutrilife.com'
);

-- Verify the users were added
SELECT 'Users added successfully!' as Status;
SELECT id, email, roles, first_name, last_name, is_active FROM user WHERE roles = 'ROLE_USER';
