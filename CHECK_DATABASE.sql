-- Check if data exists in database
USE nutrilife_db;

SELECT 'health_anomalies' as table_name, COUNT(*) as count FROM health_anomalies
UNION ALL
SELECT 'health_alerts', COUNT(*) FROM health_alerts
UNION ALL
SELECT 'user_health_metrics', COUNT(*) FROM user_health_metrics
UNION ALL
SELECT 'anomaly_detection_history', COUNT(*) FROM anomaly_detection_history;

-- Show sample anomalies
SELECT * FROM health_anomalies LIMIT 5;

-- Show sample metrics
SELECT * FROM user_health_metrics LIMIT 5;
