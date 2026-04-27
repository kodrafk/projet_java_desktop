-- ============================================================
-- NutriLife AI Anomaly Detection - FINAL SETUP
-- Drops and recreates all tables with correct schema
-- ============================================================
USE nutrilife_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS anomaly_detection_history;
DROP TABLE IF EXISTS health_alerts;
DROP TABLE IF EXISTS user_health_metrics;
DROP TABLE IF EXISTS health_anomalies;
SET FOREIGN_KEY_CHECKS = 1;

-- Table: health_anomalies (column = 'type' to match Java code)
CREATE TABLE health_anomalies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    severity DOUBLE NOT NULL DEFAULT 0,
    confidence DOUBLE DEFAULT 0,
    details TEXT,
    resolved BOOLEAN DEFAULT FALSE,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolved_by VARCHAR(100),
    resolution TEXT,
    INDEX idx_user_id (user_id),
    INDEX idx_severity (severity),
    INDEX idx_resolved (resolved),
    INDEX idx_detected_at (detected_at)
);

-- Table: health_alerts (columns match Java HealthAlert model)
CREATE TABLE health_alerts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    anomaly_id INT DEFAULT 0,
    title VARCHAR(200),
    message TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    risk_score DOUBLE DEFAULT 0,
    recommendation TEXT,
    sent BOOLEAN DEFAULT FALSE,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    acknowledged_at TIMESTAMP NULL,
    acknowledged_by VARCHAR(100),
    INDEX idx_user_id (user_id),
    INDEX idx_acknowledged (acknowledged),
    INDEX idx_priority (priority)
);

-- Table: user_health_metrics (columns match Java UserHealthMetrics model)
CREATE TABLE user_health_metrics (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    current_weight DOUBLE DEFAULT 0,
    weight_change_7days DOUBLE DEFAULT 0,
    weight_change_30days DOUBLE DEFAULT 0,
    days_since_last_log INT DEFAULT 0,
    total_logs INT DEFAULT 0,
    weight_variance DOUBLE DEFAULT 0,
    avg_weekly_change DOUBLE DEFAULT 0,
    has_active_goal BOOLEAN DEFAULT FALSE,
    goal_realistic_score DOUBLE DEFAULT 100,
    abandonment_risk DOUBLE DEFAULT 0,
    activity_score DOUBLE DEFAULT 0,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_abandonment_risk (abandonment_risk)
);

-- Table: anomaly_detection_history
CREATE TABLE anomaly_detection_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    detection_type VARCHAR(20) NOT NULL,
    anomalies_found INT DEFAULT 0,
    users_scanned INT DEFAULT 0,
    execution_time_ms BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INSERT REALISTIC SAMPLE DATA
-- ============================================================

-- 15 anomalies with correct 'type' column
INSERT INTO health_anomalies (user_id, type, description, severity, confidence, resolved, detected_at) VALUES
(1,  'RAPID_WEIGHT_LOSS',    'Rapid weight loss: -3.5 kg in 7 days (limit: 1kg/week)',       88.0, 0.95, FALSE, NOW() - INTERVAL 1 DAY),
(2,  'PROLONGED_INACTIVITY', 'No activity recorded for 15 days',                              72.0, 0.98, FALSE, NOW() - INTERVAL 1 DAY),
(3,  'YO_YO_PATTERN',        'Yo-yo pattern detected: weight variance 4.2 kg over 30 days',  66.0, 0.87, FALSE, NOW() - INTERVAL 2 DAY),
(4,  'RAPID_WEIGHT_GAIN',    'Rapid weight gain: +4.2 kg in 10 days',                         82.0, 0.91, FALSE, NOW() - INTERVAL 1 DAY),
(5,  'UNREALISTIC_GOAL',     'Unrealistic goal: -15 kg in 30 days (max safe: 4 kg)',          58.0, 0.80, FALSE, NOW() - INTERVAL 3 DAY),
(1,  'ABANDONMENT_RISK',     'High abandonment risk predicted by ML: 87.5%',                  90.0, 0.95, FALSE, NOW() - INTERVAL 1 DAY),
(6,  'ABNORMAL_BEHAVIOR',    'Abnormal behavior: high variance + low activity score',          62.0, 0.78, FALSE, NOW() - INTERVAL 2 DAY),
(7,  'RAPID_WEIGHT_LOSS',    'Rapid weight loss: -2.8 kg in 5 days',                          76.0, 0.89, FALSE, NOW() - INTERVAL 4 DAY),
(8,  'PROLONGED_INACTIVITY', 'Critical inactivity: no activity for 20 days',                  86.0, 0.99, FALSE, NOW() - INTERVAL 2 DAY),
(2,  'ABANDONMENT_RISK',     'Abandonment risk predicted by ML: 72.3%',                       74.0, 0.88, FALSE, NOW() - INTERVAL 1 DAY),
(9,  'RAPID_WEIGHT_LOSS',    'Severe weight loss: -4.1 kg in 8 days — medical attention',     91.0, 0.96, FALSE, NOW() - INTERVAL 1 DAY),
(10, 'YO_YO_PATTERN',        'Yo-yo pattern: 3 cycles detected in 30 days',                   64.0, 0.83, FALSE, NOW() - INTERVAL 3 DAY),
(11, 'PROLONGED_INACTIVITY', 'Inactivity detected: 12 days without logging',                  68.0, 0.97, FALSE, NOW() - INTERVAL 2 DAY),
(12, 'RAPID_WEIGHT_GAIN',    'Rapid weight gain: +3.8 kg in 7 days',                          80.0, 0.90, FALSE, NOW() - INTERVAL 2 DAY),
(13, 'ABANDONMENT_RISK',     'High abandonment risk predicted by ML: 82.1%',                  84.0, 0.93, FALSE, NOW() - INTERVAL 1 DAY);

-- 7 alerts with correct schema
INSERT INTO health_alerts (user_id, anomaly_id, title, message, priority, risk_score, recommendation, sent, acknowledged, created_at) VALUES
(1,  1,  'Critical Weight Loss',       'User #1 losing weight dangerously fast: -3.5kg/week',    'CRITICAL', 88.0, 'Contact user immediately. Recommend medical consultation.',          TRUE,  FALSE, NOW() - INTERVAL 1 DAY),
(1,  6,  'High Abandonment Risk',      'ML predicts 87.5% chance of abandonment for User #1',    'HIGH',     87.5, 'Send personalized re-engagement message. Offer 1-on-1 coaching.',    FALSE, FALSE, NOW() - INTERVAL 1 DAY),
(2,  2,  'Prolonged Inactivity',       'User #2 has not logged for 15 days',                     'HIGH',     72.0, 'Send motivational email. Suggest easy 5-minute daily challenge.',     FALSE, FALSE, NOW() - INTERVAL 1 DAY),
(4,  4,  'Rapid Weight Gain',          'User #4 gained 4.2kg in 10 days — nutritionist needed',  'HIGH',     82.0, 'Recommend nutritionist consultation. Review meal plan.',              FALSE, FALSE, NOW() - INTERVAL 1 DAY),
(8,  9,  'Critical Inactivity',        'User #8 inactive for 20 days — urgent intervention',     'CRITICAL', 86.0, 'Urgent: call user directly. Risk of complete dropout.',              FALSE, FALSE, NOW() - INTERVAL 2 DAY),
(9,  11, 'Severe Weight Loss',         'User #9 lost 4.1kg in 8 days — medical emergency risk',  'CRITICAL', 91.0, 'Immediate medical consultation required. Pause program.',             FALSE, FALSE, NOW() - INTERVAL 1 DAY),
(13, 15, 'High Abandonment Risk',      'ML predicts 82.1% abandonment risk for User #13',        'HIGH',     82.1, 'Immediate engagement: personalized plan + coach assignment.',         FALSE, FALSE, NOW() - INTERVAL 1 DAY);

-- 10 user health metrics with ML scores
INSERT INTO user_health_metrics (user_id, current_weight, weight_change_7days, weight_change_30days, days_since_last_log, total_logs, weight_variance, avg_weekly_change, has_active_goal, goal_realistic_score, abandonment_risk, activity_score, calculated_at) VALUES
(1,  72.5, -3.5, -5.2, 12, 28, 2.8, -0.87, TRUE,  45.0, 87.5, 18.0, NOW()),
(2,  85.0, -0.3, -1.1, 15, 12, 1.5, -0.28, TRUE,  70.0, 72.3, 22.0, NOW()),
(3,  68.2, -0.8, -2.1,  5, 35, 4.2,  0.12, TRUE,  80.0, 58.2, 55.0, NOW()),
(4,  91.4,  4.2,  5.8,  3, 22, 1.2,  1.05, FALSE, 100.0, 45.8, 62.0, NOW()),
(5,  78.9, -1.1, -3.5,  8, 18, 1.8, -0.55, TRUE,  25.0, 62.5, 40.0, NOW()),
(6,  65.3, -0.5, -1.8,  6, 30, 3.1, -0.25, TRUE,  75.0, 55.3, 48.0, NOW()),
(7,  88.7, -2.8, -4.1, 10, 15, 2.1, -0.70, TRUE,  60.0, 68.9, 30.0, NOW()),
(8,  95.2,  0.0,  0.0, 20,  8, 3.2,  0.00, FALSE, 100.0, 82.1, 10.0, NOW()),
(9,  70.1, -4.1, -6.3, 14, 20, 3.0, -1.02, TRUE,  40.0, 85.2, 15.0, NOW()),
(10, 80.5, -0.9, -2.8,  7, 25, 1.9, -0.45, TRUE,  85.0, 60.5, 45.0, NOW());

-- Detection history
INSERT INTO anomaly_detection_history (detection_type, anomalies_found, users_scanned, execution_time_ms, status, executed_at) VALUES
('MANUAL',    15, 25, 1247, 'SUCCESS', NOW() - INTERVAL 1 DAY),
('SCHEDULED', 12, 25, 1183, 'SUCCESS', NOW() - INTERVAL 2 DAY),
('MANUAL',    14, 25, 1312, 'SUCCESS', NOW() - INTERVAL 3 DAY);

-- ============================================================
-- VERIFY
-- ============================================================
SELECT 'health_anomalies' AS tbl, COUNT(*) AS total FROM health_anomalies
UNION ALL SELECT 'health_alerts',             COUNT(*) FROM health_alerts
UNION ALL SELECT 'user_health_metrics',       COUNT(*) FROM user_health_metrics
UNION ALL SELECT 'anomaly_detection_history', COUNT(*) FROM anomaly_detection_history;
