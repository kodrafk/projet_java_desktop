-- ═══════════════════════════════════════════════════════════════════════════
-- SYSTÈME DE DÉTECTION INTELLIGENTE D'ANOMALIES + ALERTES PRÉDICTIVES
-- Machine Learning pour la santé des utilisateurs
-- ═══════════════════════════════════════════════════════════════════════════

-- Table des anomalies détectées
CREATE TABLE IF NOT EXISTS health_anomalies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    severity DOUBLE NOT NULL,              -- Score 0-100
    confidence DOUBLE NOT NULL,            -- Confiance ML 0-1
    details TEXT,                          -- JSON avec détails techniques
    resolved BOOLEAN DEFAULT FALSE,
    detected_at DATETIME NOT NULL,
    resolved_at DATETIME,
    resolved_by VARCHAR(255),
    resolution TEXT,
    
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_severity (severity),
    INDEX idx_resolved (resolved),
    INDEX idx_detected_at (detected_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des alertes prédictives
CREATE TABLE IF NOT EXISTS health_alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    anomaly_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL,        -- LOW, MEDIUM, HIGH, CRITICAL
    risk_score DOUBLE NOT NULL,           -- Score de risque ML 0-100
    recommendation TEXT,                  -- Recommandation automatique
    sent BOOLEAN DEFAULT FALSE,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    sent_at DATETIME,
    acknowledged_at DATETIME,
    acknowledged_by VARCHAR(255),
    
    INDEX idx_user_id (user_id),
    INDEX idx_anomaly_id (anomaly_id),
    INDEX idx_priority (priority),
    INDEX idx_risk_score (risk_score),
    INDEX idx_acknowledged (acknowledged),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (anomaly_id) REFERENCES health_anomalies(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des métriques de santé calculées (cache pour ML)
CREATE TABLE IF NOT EXISTS user_health_metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    current_weight DOUBLE,
    weight_change_7days DOUBLE,
    weight_change_30days DOUBLE,
    days_since_last_log INT,
    total_logs INT,
    weight_variance DOUBLE,
    avg_weekly_change DOUBLE,
    has_active_goal BOOLEAN,
    goal_realistic_score DOUBLE,
    abandonment_risk DOUBLE,              -- Score ML 0-100
    activity_score DOUBLE,                -- Score 0-100
    calculated_at DATETIME NOT NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_abandonment_risk (abandonment_risk),
    INDEX idx_activity_score (activity_score),
    INDEX idx_calculated_at (calculated_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table d'historique des détections (pour améliorer le ML)
CREATE TABLE IF NOT EXISTS anomaly_detection_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    detection_date DATETIME NOT NULL,
    anomalies_found INT DEFAULT 0,
    alerts_generated INT DEFAULT 0,
    avg_severity DOUBLE,
    max_risk_score DOUBLE,
    processing_time_ms INT,
    
    INDEX idx_user_id (user_id),
    INDEX idx_detection_date (detection_date),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Vue pour le dashboard admin
CREATE OR REPLACE VIEW v_anomaly_dashboard AS
SELECT 
    ha.id,
    ha.user_id,
    u.email,
    u.first_name,
    u.last_name,
    ha.type,
    ha.description,
    ha.severity,
    ha.confidence,
    ha.resolved,
    ha.detected_at,
    hm.abandonment_risk,
    hm.activity_score,
    hm.current_weight,
    (SELECT COUNT(*) FROM health_alerts WHERE anomaly_id = ha.id AND acknowledged = FALSE) as pending_alerts
FROM health_anomalies ha
JOIN users u ON ha.user_id = u.id
LEFT JOIN user_health_metrics hm ON ha.user_id = hm.user_id
ORDER BY ha.severity DESC, ha.detected_at DESC;

-- Vue pour les alertes en attente
CREATE OR REPLACE VIEW v_pending_alerts AS
SELECT 
    ha.id,
    ha.user_id,
    u.email,
    u.first_name,
    u.last_name,
    ha.title,
    ha.message,
    ha.priority,
    ha.risk_score,
    ha.recommendation,
    ha.created_at,
    TIMESTAMPDIFF(HOUR, ha.created_at, NOW()) as hours_pending,
    an.type as anomaly_type,
    an.severity as anomaly_severity
FROM health_alerts ha
JOIN users u ON ha.user_id = u.id
JOIN health_anomalies an ON ha.anomaly_id = an.id
WHERE ha.acknowledged = FALSE
ORDER BY ha.risk_score DESC, ha.created_at ASC;

-- Vue statistiques globales
CREATE OR REPLACE VIEW v_anomaly_statistics AS
SELECT 
    COUNT(DISTINCT user_id) as users_with_anomalies,
    COUNT(*) as total_anomalies,
    SUM(CASE WHEN resolved = FALSE THEN 1 ELSE 0 END) as unresolved_anomalies,
    AVG(severity) as avg_severity,
    SUM(CASE WHEN severity >= 80 THEN 1 ELSE 0 END) as critical_anomalies,
    SUM(CASE WHEN severity >= 60 AND severity < 80 THEN 1 ELSE 0 END) as high_anomalies,
    SUM(CASE WHEN severity >= 40 AND severity < 60 THEN 1 ELSE 0 END) as medium_anomalies,
    SUM(CASE WHEN severity < 40 THEN 1 ELSE 0 END) as low_anomalies,
    (SELECT COUNT(*) FROM health_alerts WHERE acknowledged = FALSE) as pending_alerts,
    (SELECT AVG(abandonment_risk) FROM user_health_metrics WHERE abandonment_risk > 60) as avg_high_risk
FROM health_anomalies
WHERE detected_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Procédure stockée pour exécuter la détection sur tous les utilisateurs
DELIMITER //

CREATE PROCEDURE sp_detect_all_anomalies()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_user_id INT;
    DECLARE cur CURSOR FOR SELECT id FROM users WHERE is_active = TRUE;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_user_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- La détection sera appelée depuis Java
        -- Cette procédure sert de référence
        
    END LOOP;
    
    CLOSE cur;
END //

DELIMITER ;

-- Trigger pour mettre à jour les statistiques
DELIMITER //

CREATE TRIGGER tr_anomaly_after_insert
AFTER INSERT ON health_anomalies
FOR EACH ROW
BEGIN
    -- Incrémenter le compteur d'historique
    INSERT INTO anomaly_detection_history (user_id, detection_date, anomalies_found, avg_severity)
    VALUES (NEW.user_id, NEW.detected_at, 1, NEW.severity)
    ON DUPLICATE KEY UPDATE 
        anomalies_found = anomalies_found + 1,
        avg_severity = (avg_severity * anomalies_found + NEW.severity) / (anomalies_found + 1);
END //

DELIMITER ;

-- Index pour optimiser les requêtes ML
CREATE INDEX idx_weight_logs_user_date ON weight_logs(user_id, logged_at DESC);
CREATE INDEX idx_weight_objectives_active ON weight_objectives(user_id, active);

-- Données de test (optionnel)
-- INSERT INTO health_anomalies (user_id, type, description, severity, confidence, detected_at)
-- VALUES (1, 'RAPID_WEIGHT_LOSS', 'Test anomaly', 75.0, 0.95, NOW());

-- Afficher les statistiques
SELECT 'Tables créées avec succès!' as status;
SELECT * FROM v_anomaly_statistics;
