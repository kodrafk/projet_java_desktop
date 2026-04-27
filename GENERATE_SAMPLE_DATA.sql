-- Generate Sample Data for AI Anomaly Detection System
-- This creates realistic test data to demonstrate the ML system

USE nutrilife_db;

-- Insert sample anomalies
INSERT INTO health_anomalies (user_id, anomaly_type, description, severity, confidence, details, resolved, detected_at) VALUES
(1, 'RAPID_WEIGHT_LOSS', 'Perte de poids rapide: -3.5 kg en 7 jours', 85.0, 0.92, '{"weight_change": -3.5, "days": 7, "rate": -0.5}', 0, NOW() - INTERVAL 2 DAY),
(2, 'PROLONGED_INACTIVITY', 'Aucune activité depuis 15 jours', 70.0, 0.88, '{"days_inactive": 15, "last_activity": "2026-04-10"}', 0, NOW() - INTERVAL 1 DAY),
(3, 'YO_YO_PATTERN', 'Pattern yo-yo détecté: 5 cycles en 30 jours', 65.0, 0.85, '{"cycles": 5, "period_days": 30, "amplitude": 2.5}', 0, NOW() - INTERVAL 3 DAY),
(4, 'RAPID_WEIGHT_GAIN', 'Gain de poids rapide: +4.2 kg en 10 jours', 80.0, 0.90, '{"weight_change": 4.2, "days": 10, "rate": 0.42}', 0, NOW() - INTERVAL 1 DAY),
(5, 'UNREALISTIC_GOAL', 'Objectif irréaliste: -15 kg en 30 jours', 55.0, 0.78, '{"target_loss": 15, "timeframe_days": 30, "safe_rate": 4}', 0, NOW() - INTERVAL 4 DAY),
(1, 'ABANDONMENT_RISK', 'Risque d\'abandon élevé: 87.5%', 90.0, 0.95, '{"risk_score": 87.5, "factors": ["inactivity", "declining_engagement"]}', 0, NOW() - INTERVAL 1 DAY),
(6, 'ABNORMAL_BEHAVIOR', 'Comportement anormal: Connexions irrégulières', 60.0, 0.82, '{"pattern": "irregular", "deviation": 2.3}', 0, NOW() - INTERVAL 2 DAY),
(7, 'RAPID_WEIGHT_LOSS', 'Perte de poids rapide: -2.8 kg en 5 jours', 75.0, 0.89, '{"weight_change": -2.8, "days": 5, "rate": -0.56}', 0, NOW() - INTERVAL 5 DAY),
(8, 'PROLONGED_INACTIVITY', 'Aucune activité depuis 20 jours', 85.0, 0.91, '{"days_inactive": 20, "last_activity": "2026-04-05"}', 0, NOW() - INTERVAL 3 DAY),
(2, 'ABANDONMENT_RISK', 'Risque d\'abandon moyen: 72.3%', 75.0, 0.87, '{"risk_score": 72.3, "factors": ["reduced_activity"]}', 0, NOW() - INTERVAL 2 DAY);

-- Insert sample alerts
INSERT INTO health_alerts (user_id, alert_type, message, severity, risk_score, recommended_action, status, created_at) VALUES
(1, 'ABANDONMENT_RISK', 'Utilisateur à risque élevé d\'abandon (87.5%)', 'HIGH', 87.5, 'Contacter l\'utilisateur pour un suivi personnalisé', 'PENDING', NOW() - INTERVAL 1 DAY),
(2, 'PROLONGED_INACTIVITY', 'Inactivité prolongée détectée (15 jours)', 'MEDIUM', 70.0, 'Envoyer un email de motivation', 'PENDING', NOW() - INTERVAL 1 DAY),
(4, 'RAPID_WEIGHT_GAIN', 'Gain de poids rapide nécessitant attention', 'HIGH', 80.0, 'Consultation nutritionniste recommandée', 'PENDING', NOW() - INTERVAL 1 DAY),
(8, 'PROLONGED_INACTIVITY', 'Inactivité critique (20 jours)', 'CRITICAL', 85.0, 'Intervention urgente requise', 'PENDING', NOW() - INTERVAL 3 DAY),
(3, 'YO_YO_PATTERN', 'Pattern yo-yo détecté - Risque santé', 'MEDIUM', 65.0, 'Programme de stabilisation recommandé', 'PENDING', NOW() - INTERVAL 3 DAY);

-- Insert sample user health metrics
INSERT INTO user_health_metrics (user_id, abandonment_risk, inactivity_days, activity_decline_rate, weight_variance, unrealistic_goal_flag, last_activity_date, calculated_at) VALUES
(1, 87.5, 12, 0.45, 2.8, 0, NOW() - INTERVAL 12 DAY, NOW()),
(2, 72.3, 15, 0.38, 1.5, 0, NOW() - INTERVAL 15 DAY, NOW()),
(3, 58.2, 5, 0.22, 2.5, 0, NOW() - INTERVAL 5 DAY, NOW()),
(4, 45.8, 3, 0.15, 1.2, 0, NOW() - INTERVAL 3 DAY, NOW()),
(5, 62.5, 8, 0.28, 1.8, 1, NOW() - INTERVAL 8 DAY, NOW()),
(6, 55.3, 6, 0.20, 1.4, 0, NOW() - INTERVAL 6 DAY, NOW()),
(7, 68.9, 10, 0.35, 2.1, 0, NOW() - INTERVAL 10 DAY, NOW()),
(8, 82.1, 20, 0.52, 3.2, 0, NOW() - INTERVAL 20 DAY, NOW());

-- Insert detection history
INSERT INTO anomaly_detection_history (detection_type, anomalies_found, users_scanned, execution_time_ms, status, executed_at) VALUES
('MANUAL', 10, 25, 1250, 'SUCCESS', NOW() - INTERVAL 1 DAY),
('SCHEDULED', 8, 25, 1180, 'SUCCESS', NOW() - INTERVAL 2 DAY),
('MANUAL', 12, 25, 1320, 'SUCCESS', NOW() - INTERVAL 3 DAY),
('SCHEDULED', 9, 25, 1200, 'SUCCESS', NOW() - INTERVAL 4 DAY),
('MANUAL', 11, 25, 1280, 'SUCCESS', NOW() - INTERVAL 5 DAY);

-- Success message
SELECT 'Sample data generated successfully!' as Status,
       (SELECT COUNT(*) FROM health_anomalies) as Anomalies,
       (SELECT COUNT(*) FROM health_alerts) as Alerts,
       (SELECT COUNT(*) FROM user_health_metrics) as Metrics;
