-- Professional Face ID System - Database Schema
-- AES-256-GCM encrypted embeddings with audit trail

-- Table: face_embeddings
-- Stores encrypted 512D ArcFace embeddings
CREATE TABLE IF NOT EXISTS face_embeddings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    embedding_encrypted MEDIUMTEXT NOT NULL,
    encryption_iv VARCHAR(255) NOT NULL,
    encryption_tag VARCHAR(255) NOT NULL,
    liveness_verified BOOLEAN DEFAULT FALSE,
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_verified_at DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_user_id (user_id),
    INDEX idx_active (is_active)
);

-- Table: face_auth_attempts
-- Audit log for all Face ID authentication attempts
CREATE TABLE IF NOT EXISTS face_auth_attempts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    attempt_type ENUM('enroll', 'verify') NOT NULL,
    success BOOLEAN NOT NULL,
    similarity_score DOUBLE,
    liveness_passed BOOLEAN,
    failure_reason VARCHAR(255),
    ip_address VARCHAR(45),
    attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_attempts (user_id, attempted_at),
    INDEX idx_attempt_type (attempt_type),
    INDEX idx_success (success)
);

-- Insert test data (optional - for development)
-- User ID 1 will have Face ID enabled after enrollment
