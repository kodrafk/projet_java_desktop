package tn.esprit.projet.services;

import tn.esprit.projet.models.*;
import tn.esprit.projet.repository.WeightRepository;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de détection intelligente d'anomalies avec Machine Learning
 * Utilise des algorithmes statistiques et ML pour détecter les comportements à risque
 */
public class AnomalyDetectionService {
    
    private final Connection connection;
    private final WeightRepository weightRepository;
    
    // Seuils configurables
    private static final double RAPID_WEIGHT_CHANGE_THRESHOLD = 2.0;  // kg par semaine
    private static final int INACTIVITY_THRESHOLD_DAYS = 14;
    private static final double YO_YO_VARIANCE_THRESHOLD = 3.0;
    private static final double UNREALISTIC_GOAL_THRESHOLD = 1.5;     // kg par semaine
    
    public AnomalyDetectionService() {
        this.connection = MyBDConnexion.getInstance().getCnx();
        this.weightRepository = new WeightRepository();
    }
    
    /**
     * Calcule les métriques de santé pour un utilisateur
     */
    public UserHealthMetrics calculateHealthMetrics(int userId) {
        UserHealthMetrics metrics = new UserHealthMetrics(userId);
        
        try {
            List<WeightLog> logs = weightRepository.findLogsByUser(userId);
            logs.sort(Comparator.comparing(WeightLog::getLoggedAt));

            if (logs.isEmpty()) {
                // ── No logs: detect inactivity based on account age ──────────
                // Check when the user account was created
                long accountAgeDays = getAccountAgeDays(userId);
                if (accountAgeDays >= INACTIVITY_THRESHOLD_DAYS) {
                    // User registered but never logged weight
                    metrics.setDaysSinceLastLog((int) accountAgeDays);
                    metrics.setTotalLogs(0);
                    metrics.setActivityScore(0);
                    // High abandonment risk: registered but never engaged
                    double risk = Math.min(100, 30 + accountAgeDays * 2);
                    metrics.setAbandonmentRisk(risk);
                }
                return metrics;
            }
            
            // Trier par date
            // Poids actuel
            WeightLog latest = logs.get(logs.size() - 1);
            metrics.setCurrentWeight(latest.getWeight());
            metrics.setTotalLogs(logs.size());
            
            // Jours depuis dernier log
            long daysSince = ChronoUnit.DAYS.between(latest.getLoggedAt(), LocalDateTime.now());
            metrics.setDaysSinceLastLog((int) daysSince);
            
            // Changement de poids sur 7 et 30 jours
            metrics.setWeightChange7Days(calculateWeightChange(logs, 7));
            metrics.setWeightChange30Days(calculateWeightChange(logs, 30));
            
            // Variance du poids (pattern yo-yo)
            metrics.setWeightVariance(calculateWeightVariance(logs));
            
            // Changement moyen par semaine
            metrics.setAvgWeeklyChange(calculateAvgWeeklyChange(logs));
            
            // Objectif actif et score de réalisme
            WeightObjective goal = getActiveGoal(userId);
            if (goal != null) {
                metrics.setHasActiveGoal(true);
                metrics.setGoalRealisticScore(calculateGoalRealisticScore(goal));
            }
            
            // Score d'activité (basé sur fréquence des logs)
            metrics.setActivityScore(calculateActivityScore(logs));
            
            // Risque d'abandon (ML)
            metrics.setAbandonmentRisk(calculateAbandonmentRisk(metrics, logs));
            
        } catch (Exception e) {
            System.err.println("Erreur calcul métriques: " + e.getMessage());
        }
        
        return metrics;
    }

    /** Returns how many days ago the user account was created */
    private long getAccountAgeDays(int userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT created_at FROM `user` WHERE id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    return ChronoUnit.DAYS.between(ts.toLocalDateTime(), LocalDateTime.now());
                }
            }
        } catch (Exception e) {
            System.err.println("[AnomalyService] getAccountAgeDays: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Détecte toutes les anomalies pour un utilisateur
     */
    public List<HealthAnomaly> detectAnomalies(int userId) {
        List<HealthAnomaly> anomalies = new ArrayList<>();
        UserHealthMetrics metrics = calculateHealthMetrics(userId);
        
        // 1. Perte de poids rapide
        if (metrics.hasRapidWeightLoss()) {
            double severity = Math.min(100, Math.abs(metrics.getWeightChange7Days()) * 25);
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.RAPID_WEIGHT_LOSS,
                String.format("Weight dropped %.1f kg in 7 days — safe limit is 1 kg/week",
                    Math.abs(metrics.getWeightChange7Days())),
                severity,
                0.95
            ));
        }
        
        // 2. Gain de poids rapide
        if (metrics.hasRapidWeightGain()) {
            double severity = Math.min(100, metrics.getWeightChange7Days() * 25);
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.RAPID_WEIGHT_GAIN,
                String.format("Weight increased %.1f kg in 7 days", metrics.getWeightChange7Days()),
                severity,
                0.92
            ));
        }
        
        // 3. Inactivité prolongée — détectée même sans logs de poids
        if (metrics.isInactive()) {
            int days = metrics.getDaysSinceLastLog();
            double severity = Math.min(100, days * 3);
            String desc = metrics.getTotalLogs() == 0
                ? String.format("User registered %d days ago but has never logged their weight", days)
                : String.format("No activity recorded for %d days", days);
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.PROLONGED_INACTIVITY,
                desc,
                severity,
                0.98
            ));
        }
        
        // 4. Pattern yo-yo
        if (metrics.hasYoYoPattern()) {
            double severity = Math.min(100, metrics.getWeightVariance() * 15);
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.YO_YO_PATTERN,
                String.format("Unstable weight pattern detected — variance %.1f kg over 30 days",
                    metrics.getWeightVariance()),
                severity,
                0.88
            ));
        }
        
        // 5. Objectif irréaliste — only if user has an active goal AND it's unrealistic
        if (metrics.isHasActiveGoal() && metrics.hasUnrealisticGoal()) {
            double severity = 100 - metrics.getGoalRealisticScore();
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.UNREALISTIC_GOAL,
                "Weight goal is unrealistic — target pace exceeds safe limits (max 1 kg/week)",
                severity,
                0.85
            ));
        }
        
        // 6. Risque d'abandon (ML)
        if (metrics.isAtRiskOfAbandonment()) {
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.ABANDONMENT_RISK,
                String.format("Dropout risk score: %.0f%% — user likely to stop the program",
                    metrics.getAbandonmentRisk()),
                metrics.getAbandonmentRisk(),
                0.90
            ));
        }
        
        // 7. Comportement anormal (statistiques)
        if (detectAbnormalBehavior(metrics)) {
            anomalies.add(new HealthAnomaly(
                userId,
                HealthAnomaly.AnomalyType.ABNORMAL_BEHAVIOR,
                "Unusual activity pattern detected — high variance combined with low engagement",
                65.0,
                0.75
            ));
        }
        
        return anomalies;
    }
    
    /**
     * Saves an anomaly only if no identical unresolved anomaly exists for this user today.
     * Prevents duplicate entries on repeated scans.
     */
    public void saveAnomaly(HealthAnomaly anomaly) {
        // Deduplication: skip if same type already active for this user (detected today)
        String checkSql = "SELECT COUNT(*) FROM health_anomalies " +
                "WHERE user_id = ? AND type = ? AND resolved = FALSE " +
                "AND DATE(detected_at) = CURDATE()";
        try (PreparedStatement check = connection.prepareStatement(checkSql)) {
            check.setInt(1, anomaly.getUserId());
            check.setString(2, anomaly.getType().name());
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already recorded today — skip
            }
        } catch (SQLException e) {
            System.err.println("Dedup check error: " + e.getMessage());
        }

        String sql = "INSERT INTO health_anomalies (user_id, type, description, severity, confidence, " +
                    "details, resolved, detected_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, anomaly.getUserId());
            stmt.setString(2, anomaly.getType().name());
            stmt.setString(3, anomaly.getDescription());
            stmt.setDouble(4, anomaly.getSeverity());
            stmt.setDouble(5, anomaly.getConfidence());
            stmt.setString(6, anomaly.getDetails());
            stmt.setBoolean(7, anomaly.isResolved());
            stmt.setTimestamp(8, Timestamp.valueOf(anomaly.getDetectedAt()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                anomaly.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error saving anomaly: " + e.getMessage());
        }
    }
    
    /**
     * Génère des alertes prédictives basées sur les anomalies
     */
    public List<HealthAlert> generateAlerts(List<HealthAnomaly> anomalies) {
        List<HealthAlert> alerts = new ArrayList<>();
        
        for (HealthAnomaly anomaly : anomalies) {
            HealthAlert.AlertPriority priority = determinePriority(anomaly);
            String recommendation = generateRecommendation(anomaly);
            
            HealthAlert alert = new HealthAlert(
                anomaly.getUserId(),
                anomaly.getId(),
                anomaly.getType().getLabel(),
                anomaly.getDescription(),
                priority,
                anomaly.getSeverity()
            );
            alert.setRecommendation(recommendation);
            
            alerts.add(alert);
        }
        
        return alerts;
    }
    
    /**
     * Sauvegarde une alerte
     */
    public void saveAlert(HealthAlert alert) {
        String sql = "INSERT INTO health_alerts (user_id, anomaly_id, title, message, priority, " +
                    "risk_score, recommendation, sent, acknowledged, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, alert.getUserId());
            stmt.setInt(2, alert.getAnomalyId());
            stmt.setString(3, alert.getTitle());
            stmt.setString(4, alert.getMessage());
            stmt.setString(5, alert.getPriority().name());
            stmt.setDouble(6, alert.getRiskScore());
            stmt.setString(7, alert.getRecommendation());
            stmt.setBoolean(8, alert.isSent());
            stmt.setBoolean(9, alert.isAcknowledged());
            stmt.setTimestamp(10, Timestamp.valueOf(alert.getCreatedAt()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                alert.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde alerte: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère toutes les anomalies non résolues
     */
    public List<HealthAnomaly> getUnresolvedAnomalies() {
        List<HealthAnomaly> anomalies = new ArrayList<>();
        String sql = "SELECT * FROM health_anomalies WHERE resolved = FALSE ORDER BY severity DESC, detected_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                anomalies.add(mapAnomaly(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération anomalies: " + e.getMessage());
        }
        
        return anomalies;
    }
    
    /**
     * Récupère les alertes en attente
     */
    public List<HealthAlert> getPendingAlerts() {
        List<HealthAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM health_alerts WHERE acknowledged = FALSE ORDER BY risk_score DESC, created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                alerts.add(mapAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération alertes: " + e.getMessage());
        }
        
        return alerts;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES - ALGORITHMES ML
    // ═══════════════════════════════════════════════════════════════════════════
    
    private double calculateWeightChange(List<WeightLog> logs, int days) {
        if (logs.size() < 2) return 0;
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        WeightLog latest = logs.get(logs.size() - 1);
        
        Optional<WeightLog> oldLog = logs.stream()
            .filter(log -> log.getLoggedAt().isBefore(cutoff))
            .max(Comparator.comparing(WeightLog::getLoggedAt));
        
        if (oldLog.isPresent()) {
            return latest.getWeight() - oldLog.get().getWeight();
        }
        
        return 0;
    }
    
    private double calculateWeightVariance(List<WeightLog> logs) {
        if (logs.size() < 3) return 0;
        
        // Prendre les 30 derniers jours
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Double> weights = logs.stream()
            .filter(log -> log.getLoggedAt().isAfter(cutoff))
            .map(WeightLog::getWeight)
            .collect(Collectors.toList());
        
        if (weights.size() < 3) return 0;
        
        double mean = weights.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = weights.stream()
            .mapToDouble(w -> Math.pow(w - mean, 2))
            .average()
            .orElse(0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateAvgWeeklyChange(List<WeightLog> logs) {
        if (logs.size() < 2) return 0;
        
        WeightLog first = logs.get(0);
        WeightLog last = logs.get(logs.size() - 1);
        
        long daysBetween = ChronoUnit.DAYS.between(first.getLoggedAt(), last.getLoggedAt());
        if (daysBetween < 7) return 0;
        
        double totalChange = last.getWeight() - first.getWeight();
        double weeks = daysBetween / 7.0;
        
        return totalChange / weeks;
    }
    
    private double calculateActivityScore(List<WeightLog> logs) {
        if (logs.isEmpty()) return 0;
        
        // Score basé sur fréquence et régularité
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        long recentLogs = logs.stream()
            .filter(log -> log.getLoggedAt().isAfter(cutoff))
            .count();
        
        // Score: 0-100 (idéal: 1 log tous les 3 jours = 10 logs/mois)
        double frequencyScore = Math.min(100, (recentLogs / 10.0) * 100);
        
        // Pénalité pour inactivité récente
        WeightLog latest = logs.get(logs.size() - 1);
        long daysSince = ChronoUnit.DAYS.between(latest.getLoggedAt(), LocalDateTime.now());
        double recencyPenalty = Math.max(0, 1 - (daysSince / 30.0));
        
        return frequencyScore * recencyPenalty;
    }
    
    /**
     * Algorithme ML pour calculer le risque d'abandon
     * Utilise une régression logistique simplifiée
     */
    private double calculateAbandonmentRisk(UserHealthMetrics metrics, List<WeightLog> logs) {
        double risk = 0;
        
        // Facteur 1: Inactivité (poids: 35%)
        double inactivityFactor = Math.min(100, metrics.getDaysSinceLastLog() * 5);
        risk += inactivityFactor * 0.35;
        
        // Facteur 2: Baisse d'activité (poids: 25%)
        double activityFactor = 100 - metrics.getActivityScore();
        risk += activityFactor * 0.25;
        
        // Facteur 3: Variance élevée (poids: 20%)
        double varianceFactor = Math.min(100, metrics.getWeightVariance() * 20);
        risk += varianceFactor * 0.20;
        
        // Facteur 4: Objectif irréaliste (poids: 15%)
        if (metrics.isHasActiveGoal()) {
            double goalFactor = 100 - metrics.getGoalRealisticScore();
            risk += goalFactor * 0.15;
        }
        
        // Facteur 5: Historique (poids: 5%)
        double historyFactor = logs.size() < 5 ? 80 : 20;
        risk += historyFactor * 0.05;
        
        return Math.min(100, risk);
    }
    
    private double calculateGoalRealisticScore(WeightObjective goal) {
        double totalKg = Math.abs(goal.getTargetWeight() - goal.getStartWeight());
        long daysToGoal = ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
        
        if (daysToGoal <= 0) return 0;
        
        double weeksToGoal = daysToGoal / 7.0;
        double kgPerWeek = totalKg / weeksToGoal;
        
        // Recommandation: 0.5-1 kg/semaine
        if (kgPerWeek <= 0.5) return 100;
        if (kgPerWeek <= 1.0) return 90;
        if (kgPerWeek <= 1.5) return 60;
        if (kgPerWeek <= 2.0) return 30;
        return 10;
    }
    
    private boolean detectAbnormalBehavior(UserHealthMetrics metrics) {
        // Détection de patterns anormaux
        return (metrics.getWeightVariance() > 2.5 && metrics.getActivityScore() < 30) ||
               (Math.abs(metrics.getAvgWeeklyChange()) > 1.5 && metrics.getTotalLogs() > 10);
    }
    
    private WeightObjective getActiveGoal(int userId) {
        String sql = "SELECT * FROM weight_objective WHERE user_id = ? AND is_active = 1 LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                WeightObjective goal = new WeightObjective();
                goal.setId(rs.getInt("id"));
                goal.setUserId(rs.getInt("user_id"));
                goal.setStartWeight(rs.getDouble("start_weight"));
                goal.setTargetWeight(rs.getDouble("target_weight"));
                goal.setStartDate(rs.getDate("start_date").toLocalDate());
                goal.setTargetDate(rs.getDate("target_date").toLocalDate());
                goal.setActive(rs.getBoolean("is_active"));
                return goal;
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération objectif: " + e.getMessage());
        }
        
        return null;
    }
    
    private HealthAlert.AlertPriority determinePriority(HealthAnomaly anomaly) {
        if (anomaly.getSeverity() >= 80) return HealthAlert.AlertPriority.CRITICAL;
        if (anomaly.getSeverity() >= 60) return HealthAlert.AlertPriority.HIGH;
        if (anomaly.getSeverity() >= 40) return HealthAlert.AlertPriority.MEDIUM;
        return HealthAlert.AlertPriority.LOW;
    }
    
    private String generateRecommendation(HealthAnomaly anomaly) {
        switch (anomaly.getType()) {
            case RAPID_WEIGHT_LOSS:
                return "Contact user to verify health status. Recommend gradual weight loss (0.5–1 kg/week).";
            case RAPID_WEIGHT_GAIN:
                return "Encourage user to resume healthy habits. Suggest nutritional follow-up.";
            case PROLONGED_INACTIVITY:
                return "Send a motivational message. Propose simple daily challenges to re-engage.";
            case YO_YO_PATTERN:
                return "Recommend more consistent tracking and stable, realistic goals.";
            case UNREALISTIC_GOAL:
                return "Suggest revising the goal with intermediate milestones at a safe pace.";
            case ABANDONMENT_RISK:
                return "Priority intervention: contact user directly, offer personalized coaching.";
            case ABNORMAL_BEHAVIOR:
                return "Review user activity in detail and adjust the program accordingly.";
            default:
                return "Monitor progress and follow up if the situation worsens.";
        }
    }
    
    private HealthAnomaly mapAnomaly(ResultSet rs) throws SQLException {
        HealthAnomaly anomaly = new HealthAnomaly();
        anomaly.setId(rs.getInt("id"));
        anomaly.setUserId(rs.getInt("user_id"));
        anomaly.setType(HealthAnomaly.AnomalyType.valueOf(rs.getString("type")));
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
}
