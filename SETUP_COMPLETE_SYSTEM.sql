-- Complete AI Anomaly Detection System Setup
-- Professional English Version
-- Run this file once to set up everything

USE nutrilife_db;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS anomaly_detection_history;
DROP TABLE IF EXISTS health_alerts;
DROP TABLE IF EXISTS user_health_metrics;
DROP TABLE IF EXISTS health_anomalies;

-- Create health_anomalies table
CREATE TABLE health_anomalies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    anomaly_type VARCHAR(50) NOT NULL,
    description TEXT,
    severity DOUBLE NOT NULL,
    confidence DOUBLE,
    details TEXT,
    resolved BOOLEAN DEFAULT FALSE,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolved_by VARCHAR(100),
    resolution TEXT,
    INDEX idx_user_id (user_id),
    INDEX idx_severity (severity),
    INDEX idx_detected_at (detected_at),
    INDEX idx_resolved (resolved)
);

-- Create health_alerts table
CREATE TABLE health_alerts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    risk_score DOUBLE,
    recommended_action TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP NULL,
    acknowledged_by VARCHAR(100),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity)
);

-- Create user_health_metrics table
CREATE TABLE user_health_metrics (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    abandonment_risk DOUBLE DEFAULT 0,
    inactivity_days INT DEFAULT 0,
    activity_decline_rate DOUBLE DEFAULT 0,
    weight_variance DOUBLE DEFAULT 0,
    unrealistic_goal_flag BOOLEAN DEFAULT FALSE,
    last_activity_date TIMESTAMP NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_risk (abandonment_risk),
    INDEX idx_user (user_id)
);

-- Create detection history table
CREATE TABLE anomaly_detection_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    detection_type VARCHAR(20) NOT NULL,
    anomalies_found INT DEFAULT 0,
    users_scanned INT DEFAULT 0,
    execution_time_ms BIGINT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert realistic sample data
INSERT INTO health_anomalies (user_id, anomaly_type, description, severity, confidence, detected_at, resolved) VALUES
(1, 'RAPID_WEIGHT_LOSS', 'Rapid weight loss detected: -3.5 kg in 7 days', 85.0, 0.92, NOW() - INTERVAL 2 DAY, 0),
(2, 'PROLONGED_INACTIVITY', 'No activity for 15 days', 70.0, 0.88, NOW() - INTERVAL 1 DAY, 0),
(3, 'YO_YO_PATTERN', 'Yo-yo pattern detected: 5 cycles in 30 days', 65.0, 0.85, NOW() - INTERVAL 3 DAY, 0),
(4, 'RAPID_WEIGHT_GAIN', 'Rapid weight gain: +4.2 kg in 10 days', 80.0, 0.90, NOW() - INTERVAL 1 DAY, 0),
(5, 'UNREALISTIC_GOAL', 'Unrealistic goal: -15 kg in 30 days', 55.0, 0.78, NOW() - INTERVAL 4 DAY, 0),
(1, 'ABANDONMENT_RISK', 'High abandonment risk: 87.5%', 90.0, 0.95, NOW() - INTERVAL 1 DAY, 0),
(6, 'ABNORMAL_BEHAVIOR', 'Abnormal behavior pattern detected', 60.0, 0.82, NOW() - INTERVAL 2 DAY, 0),
(7, 'RAPID_WEIGHT_LOSS', 'Rapid weight loss: -2.8 kg in 5 days', 75.0, 0.89, NOW() - INTERVAL 5 DAY, 0),
(8, 'PROLONGED_INACTIVITY', 'No activity for 20 days', 85.0, 0.91, NOW() - INTERVAL 3 DAY, 0),
(2, 'ABANDONMENT_RISK', 'Medium abandonment risk: 72.3%', 75.0, 0.87, NOW() - INTERVAL 2 DAY, 0),
(9, 'RAPID_WEIGHT_LOSS', 'Rapid weight loss: -4.1 kg in 8 days', 88.0, 0.93, NOW() - INTERVAL 1 DAY, 0),
(10, 'YO_YO_PATTERN', 'Yo-yo pattern: 3 cycles detected', 62.0, 0.80, NOW() - INTERVAL 4 DAY, 0),
(11, 'PROLONGED_INACTIVITY', 'Inactive for 12 days', 68.0, 0.85, NOW() - INTERVAL 2 DAY, 0),
(12, 'RAPID_WEIGHT_GAIN', 'Rapid gain: +3.8 kg', 78.0, 0.88, NOW() - INTERVAL 3 DAY, 0),
(13, 'ABANDONMENT_RISK', 'High risk: 82.1%', 85.0, 0.91, NOW() - INTERVAL 1 DAY, 0);

-- Insert sample alerts
INSERT INTO health_alerts (user_id, alert_type, message, severity, risk_score, recommended_action, created_at, status) VALUES
(1, 'ABANDONMENT_RISK', 'User at high abandonment risk (87.5%)', 'HIGH', 87.5, 'Contact user for personalized follow-up', NOW() - INTERVAL 1 DAY, 'PENDING'),
(2, 'PROLONGED_INACTIVITY', 'Prolonged inactivity detected (15 days)', 'MEDIUM', 70.0, 'Send motivation email', NOW() - INTERVAL 1 DAY, 'PENDING'),
(4, 'RAPID_WEIGHT_GAIN', 'Rapid weight gain requires attention', 'HIGH', 80.0, 'Nutritionist consultation recommended', NOW() - INTERVAL 1 DAY, 'PENDING'),
(8, 'PROLONGED_INACTIVITY', 'Critical inactivity (20 days)', 'CRITICAL', 85.0, 'Urgent intervention required', NOW() - INTERVAL 3 DAY, 'PENDING'),
(3, 'YO_YO_PATTERN', 'Yo-yo pattern detected - Health risk', 'MEDIUM', 65.0, 'Stabilization program recommended', NOW() - INTERVAL 3 DAY, 'PENDING'),
(9, 'RAPID_WEIGHT_LOSS', 'Severe weight loss detected', 'CRITICAL', 88.0, 'Medical consultation required', NOW() - INTERVAL 1 DAY, 'PENDING'),
(13, 'ABANDONMENT_RISK', 'High abandonment risk detected', 'HIGH', 82.1, 'Immediate engagement needed', NOW() - INTERVAL 1 DAY, 'PENDING');

-- Insert sample metrics
INSERT INTO user_health_metrics (user_id, abandonment_risk, inactivity_days, activity_decline_rate, weight_variance, last_activity_date, unrealistic_goal_flag) VALUES
(1, 87.5, 12, 0.45, 2.8, NOW() - INTERVAL 12 DAY, 0),
(2, 72.3, 15, 0.38, 1.5, NOW() - INTERVAL 15 DAY, 0),
(3, 58.2, 5, 0.22, 2.5, NOW() - INTERVAL 5 DAY, 0),
(4, 45.8, 3, 0.15, 1.2, NOW() - INTERVAL 3 DAY, 0),
(5, 62.5, 8, 0.28, 1.8, NOW() - INTERVAL 8 DAY, 1),
(6, 55.3, 6, 0.20, 1.4, NOW() - INTERVAL 6 DAY, 0),
(7, 68.9, 10, 0.35, 2.1, NOW() - INTERVAL 10 DAY, 0),
(8, 82.1, 20, 0.52, 3.2, NOW() - INTERVAL 20 DAY, 0),
(9, 85.2, 14, 0.48, 3.0, NOW() - INTERVAL 14 DAY, 0),
(10, 60.5, 7, 0.25, 1.9, NOW() - INTERVAL 7 DAY, 0);

-- Insert detection history
INSERT INTO anomaly_detection_history (detection_type, anomalies_found, users_scanned, execution_time_ms, status, executed_at) VALUES
('MANUAL', 15, 25, 1250, 'SUCCESS', NOW() - INTERVAL 1 DAY),
('SCHEDULED', 12, 25, 1180, 'SUCCESS', NOW() - INTERVAL 2 DAY),
('MANUAL', 14, 25, 1320, 'SUCCESS', NOW() - INTERVAL 3 DAY);

-- Verify data
SELECT 
    'Setup Complete!' as Status,
    (SELECT COUNT(*) FROM health_anomalies) as Anomalies,
    (SELECT COUNT(*) FROM health_alerts) as Alerts,
    (SELECT COUNT(*) FROM user_health_metrics) as Metrics,
    (SELECT COUNT(*) FROM anomaly_detection_history) as History;

SELECT '✓ AI System Ready!' as Message;
