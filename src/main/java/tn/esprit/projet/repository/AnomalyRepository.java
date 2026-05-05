package tn.esprit.projet.repository;

import tn.esprit.projet.models.HealthAlert;
import tn.esprit.projet.models.HealthAnomaly;
import tn.esprit.projet.models.UserHealthMetrics;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository pour la gestion des anomalies et alertes
 */
public class AnomalyRepository {
    
    private final Connection connection;
    
    public AnomalyRepository() {
        this.connection = MyBDConnexion.getInstance().getCnx();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ANOMALIES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public List<HealthAnomaly> findAllAnomalies() {
        List<HealthAnomaly> anomalies = new ArrayList<>();
        String sql = "SELECT * FROM health_anomalies ORDER BY detected_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                anomalies.add(mapAnomaly(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAllAnomalies: " + e.getMessage());
        }
        
        return anomalies;
    }
    
    public List<HealthAnomaly> findAnomaliesByUserId(int userId) {
        List<HealthAnomaly> anomalies = new ArrayList<>();
        String sql = "SELECT * FROM health_anomalies WHERE user_id = ? ORDER BY detected_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                anomalies.add(mapAnomaly(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAnomaliesByUserId: " + e.getMessage());
        }
        
        return anomalies;
    }
    
    public List<HealthAnomaly> findUnresolvedAnomalies() {
        List<HealthAnomaly> anomalies = new ArrayList<>();
        String sql = "SELECT * FROM health_anomalies WHERE resolved = FALSE ORDER BY severity DESC, detected_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                anomalies.add(mapAnomaly(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findUnresolvedAnomalies: " + e.getMessage());
        }
        
        return anomalies;
    }
    
    public List<HealthAnomaly> findCriticalAnomalies() {
        List<HealthAnomaly> anomalies = new ArrayList<>();
        String sql = "SELECT * FROM health_anomalies WHERE severity >= 80 AND resolved = FALSE ORDER BY severity DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                anomalies.add(mapAnomaly(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findCriticalAnomalies: " + e.getMessage());
        }
        
        return anomalies;
    }
    
    public void resolveAnomaly(int anomalyId, String resolvedBy, String resolution) {
        String sql = "UPDATE health_anomalies SET resolved = TRUE, resolved_at = ?, " +
                    "resolved_by = ?, resolution = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, resolvedBy);
            stmt.setString(3, resolution);
            stmt.setInt(4, anomalyId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur resolveAnomaly: " + e.getMessage());
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ALERTES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public List<HealthAlert> findAllAlerts() {
        List<HealthAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM health_alerts ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                alerts.add(mapAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAllAlerts: " + e.getMessage());
        }
        
        return alerts;
    }
    
    public List<HealthAlert> findPendingAlerts() {
        List<HealthAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM health_alerts WHERE acknowledged = FALSE ORDER BY risk_score DESC, created_at ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                alerts.add(mapAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error findPendingAlerts: " + e.getMessage());
        }
        
        return alerts;
    }
    
    public List<HealthAlert> findAlertsByUserId(int userId) {
        List<HealthAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM health_alerts WHERE user_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                alerts.add(mapAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAlertsByUserId: " + e.getMessage());
        }
        
        return alerts;
    }
    
    public void acknowledgeAlert(int alertId, String acknowledgedBy) {
        String sql = "UPDATE health_alerts SET acknowledged = TRUE, acknowledged_at = ?, " +
                    "acknowledged_by = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, acknowledgedBy);
            stmt.setInt(3, alertId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur acknowledgeAlert: " + e.getMessage());
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Statement stmt = connection.createStatement()) {
            // Total anomalies
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM health_anomalies");
            if (rs.next()) stats.put("total_anomalies", rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM health_anomalies WHERE resolved = FALSE");
            if (rs.next()) stats.put("unresolved_anomalies", rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM health_anomalies WHERE severity >= 80 AND resolved = FALSE");
            if (rs.next()) stats.put("critical_anomalies", rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM health_alerts WHERE acknowledged = FALSE");
            if (rs.next()) stats.put("pending_alerts", rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(DISTINCT user_id) FROM health_anomalies");
            if (rs.next()) stats.put("users_with_anomalies", rs.getInt(1));
            
        } catch (SQLException e) {
            System.err.println("Error getStatistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    public Map<String, Integer> getAnomaliesByType() {
        Map<String, Integer> byType = new HashMap<>();
        String sql = "SELECT type, COUNT(*) as count FROM health_anomalies " +
                    "WHERE detected_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                    "GROUP BY type ORDER BY count DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                byType.put(rs.getString("type"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAnomaliesByType: " + e.getMessage());
        }
        
        return byType;
    }
    
    public List<Map<String, Object>> getTopRiskUsers(int limit) {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT user_id, abandonment_risk, days_since_last_log, activity_score " +
                    "FROM user_health_metrics " +
                    "WHERE abandonment_risk > 50 " +
                    "ORDER BY abandonment_risk DESC LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("user_id", rs.getInt("user_id"));
                user.put("abandonment_risk", rs.getDouble("abandonment_risk"));
                user.put("inactivity_days", rs.getInt("days_since_last_log"));
                int daysAgo = rs.getInt("days_since_last_log");
                user.put("last_activity_date", java.time.LocalDate.now().minusDays(daysAgo).toString());
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getTopRiskUsers: " + e.getMessage());
        }
        
        return users;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTRIQUES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public void saveMetrics(UserHealthMetrics metrics) {
        String sql = "INSERT INTO user_health_metrics (user_id, current_weight, weight_change_7days, " +
                    "weight_change_30days, days_since_last_log, total_logs, weight_variance, " +
                    "avg_weekly_change, has_active_goal, goal_realistic_score, abandonment_risk, " +
                    "activity_score, calculated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "current_weight = VALUES(current_weight), " +
                    "weight_change_7days = VALUES(weight_change_7days), " +
                    "weight_change_30days = VALUES(weight_change_30days), " +
                    "days_since_last_log = VALUES(days_since_last_log), " +
                    "total_logs = VALUES(total_logs), " +
                    "weight_variance = VALUES(weight_variance), " +
                    "avg_weekly_change = VALUES(avg_weekly_change), " +
                    "has_active_goal = VALUES(has_active_goal), " +
                    "goal_realistic_score = VALUES(goal_realistic_score), " +
                    "abandonment_risk = VALUES(abandonment_risk), " +
                    "activity_score = VALUES(activity_score), " +
                    "calculated_at = VALUES(calculated_at)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, metrics.getUserId());
            stmt.setDouble(2, metrics.getCurrentWeight());
            stmt.setDouble(3, metrics.getWeightChange7Days());
            stmt.setDouble(4, metrics.getWeightChange30Days());
            stmt.setInt(5, metrics.getDaysSinceLastLog());
            stmt.setInt(6, metrics.getTotalLogs());
            stmt.setDouble(7, metrics.getWeightVariance());
            stmt.setDouble(8, metrics.getAvgWeeklyChange());
            stmt.setBoolean(9, metrics.isHasActiveGoal());
            stmt.setDouble(10, metrics.getGoalRealisticScore());
            stmt.setDouble(11, metrics.getAbandonmentRisk());
            stmt.setDouble(12, metrics.getActivityScore());
            stmt.setTimestamp(13, Timestamp.valueOf(metrics.getCalculatedAt()));
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur saveMetrics: " + e.getMessage());
        }
    }
    
    public UserHealthMetrics findMetricsByUserId(int userId) {
        String sql = "SELECT * FROM user_health_metrics WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapMetrics(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur findMetricsByUserId: " + e.getMessage());
        }
        
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPING
    // ═══════════════════════════════════════════════════════════════════════════
    
    private HealthAnomaly mapAnomaly(ResultSet rs) throws SQLException {
        HealthAnomaly anomaly = new HealthAnomaly();
        anomaly.setId(rs.getInt("id"));
        anomaly.setUserId(rs.getInt("user_id"));
        
        // Map anomaly_type string to enum
        String typeStr = rs.getString("anomaly_type");
        try {
            anomaly.setType(HealthAnomaly.AnomalyType.valueOf(typeStr));
        } catch (Exception e) {
            System.err.println("Unknown anomaly type: " + typeStr);
            anomaly.setType(HealthAnomaly.AnomalyType.ABNORMAL_BEHAVIOR);
        }
        
        anomaly.setDescription(rs.getString("description"));
        anomaly.setSeverity(rs.getDouble("severity"));
        anomaly.setConfidence(rs.getDouble("confidence"));
        anomaly.setDetails(rs.getString("details"));
        anomaly.setResolved(rs.getBoolean("resolved"));
        anomaly.setDetectedAt(rs.getTimestamp("detected_at").toLocalDateTime());
        
        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) {
            anomaly.setResolvedAt(resolvedAt.toLocalDateTime());
        }
        anomaly.setResolvedBy(rs.getString("resolved_by"));
        anomaly.setResolution(rs.getString("resolution"));
        
        return anomaly;
    }
    
    private HealthAlert mapAlert(ResultSet rs) throws SQLException {
        HealthAlert alert = new HealthAlert();
        alert.setId(rs.getInt("id"));
        alert.setUserId(rs.getInt("user_id"));
        alert.setAnomalyId(rs.getInt("anomaly_id"));
        alert.setTitle(rs.getString("title"));
        alert.setMessage(rs.getString("message"));
        alert.setPriority(HealthAlert.AlertPriority.valueOf(rs.getString("priority")));
        alert.setRiskScore(rs.getDouble("risk_score"));
        alert.setRecommendation(rs.getString("recommendation"));
        alert.setSent(rs.getBoolean("sent"));
        alert.setAcknowledged(rs.getBoolean("acknowledged"));
        alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp sentAt = rs.getTimestamp("sent_at");
        if (sentAt != null) {
            alert.setSentAt(sentAt.toLocalDateTime());
        }
        
        Timestamp ackAt = rs.getTimestamp("acknowledged_at");
        if (ackAt != null) {
            alert.setAcknowledgedAt(ackAt.toLocalDateTime());
        }
        alert.setAcknowledgedBy(rs.getString("acknowledged_by"));
        
        return alert;
    }
    
    private UserHealthMetrics mapMetrics(ResultSet rs) throws SQLException {
        UserHealthMetrics metrics = new UserHealthMetrics();
        metrics.setUserId(rs.getInt("user_id"));
        metrics.setCurrentWeight(rs.getDouble("current_weight"));
        metrics.setWeightChange7Days(rs.getDouble("weight_change_7days"));
        metrics.setWeightChange30Days(rs.getDouble("weight_change_30days"));
        metrics.setDaysSinceLastLog(rs.getInt("days_since_last_log"));
        metrics.setTotalLogs(rs.getInt("total_logs"));
        metrics.setWeightVariance(rs.getDouble("weight_variance"));
        metrics.setAvgWeeklyChange(rs.getDouble("avg_weekly_change"));
        metrics.setHasActiveGoal(rs.getBoolean("has_active_goal"));
        metrics.setGoalRealisticScore(rs.getDouble("goal_realistic_score"));
        metrics.setAbandonmentRisk(rs.getDouble("abandonment_risk"));
        metrics.setActivityScore(rs.getDouble("activity_score"));
        metrics.setCalculatedAt(rs.getTimestamp("calculated_at").toLocalDateTime());
        
        return metrics;
    }
}
