package tn.esprit.projet.services;

import tn.esprit.projet.models.*;
import tn.esprit.projet.repository.AnomalyRepository;
import tn.esprit.projet.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service de planification automatique pour la détection d'anomalies
 * Exécute la détection périodiquement en arrière-plan
 */
public class AnomalySchedulerService {
    
    private static AnomalySchedulerService instance;
    private final ScheduledExecutorService scheduler;
    private final AnomalyDetectionService detectionService;
    private final AnomalyRepository anomalyRepository;
    private final UserRepository userRepository;
    
    private boolean isRunning = false;
    private LocalDateTime lastRun;
    private int lastAnomaliesFound = 0;
    
    private AnomalySchedulerService() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.detectionService = new AnomalyDetectionService();
        this.anomalyRepository = new AnomalyRepository();
        this.userRepository = new UserRepository();
    }
    
    public static synchronized AnomalySchedulerService getInstance() {
        if (instance == null) {
            instance = new AnomalySchedulerService();
        }
        return instance;
    }
    
    /**
     * Démarre la détection automatique
     * @param intervalHours Intervalle en heures entre chaque détection
     */
    public void start(int intervalHours) {
        if (isRunning) {
            System.out.println("⚠️ Le scheduler est déjà en cours d'exécution");
            return;
        }
        
        System.out.println("🚀 Démarrage du scheduler de détection d'anomalies");
        System.out.println("📅 Intervalle: " + intervalHours + " heures");
        
        // Exécution immédiate puis périodique
        scheduler.scheduleAtFixedRate(
            this::runDetection,
            0,                      // Délai initial
            intervalHours,          // Période
            TimeUnit.HOURS
        );
        
        isRunning = true;
    }
    
    /**
     * Démarre avec l'intervalle par défaut (6 heures)
     */
    public void start() {
        start(6);
    }
    
    /**
     * Arrête le scheduler
     */
    public void stop() {
        if (!isRunning) {
            System.out.println("⚠️ Le scheduler n'est pas en cours d'exécution");
            return;
        }
        
        System.out.println("🛑 Arrêt du scheduler de détection d'anomalies");
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
    }
    
    /**
     * Exécute la détection pour tous les utilisateurs actifs
     */
    private void runDetection() {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("🔍 DÉTECTION AUTOMATIQUE D'ANOMALIES - " + LocalDateTime.now());
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        long startTime = System.currentTimeMillis();
        int usersProcessed = 0;
        int anomaliesFound = 0;
        int alertsGenerated = 0;
        
        try {
            // Récupérer tous les utilisateurs actifs
            List<User> users = userRepository.findAll();
            
            for (User user : users) {
                if (!user.isActive()) {
                    continue;
                }
                
                try {
                    // 1. Calculer les métriques de santé
                    UserHealthMetrics metrics = detectionService.calculateHealthMetrics(user.getId());
                    anomalyRepository.saveMetrics(metrics);
                    
                    // 2. Détecter les anomalies
                    List<HealthAnomaly> anomalies = detectionService.detectAnomalies(user.getId());
                    
                    // 3. Sauvegarder les nouvelles anomalies
                    for (HealthAnomaly anomaly : anomalies) {
                        // Vérifier si l'anomalie existe déjà (éviter les doublons)
                        if (!isDuplicateAnomaly(anomaly)) {
                            detectionService.saveAnomaly(anomaly);
                            anomaliesFound++;
                            
                            // 4. Générer des alertes
                            List<HealthAlert> alerts = detectionService.generateAlerts(List.of(anomaly));
                            for (HealthAlert alert : alerts) {
                                detectionService.saveAlert(alert);
                                alertsGenerated++;
                            }
                        }
                    }
                    
                    usersProcessed++;
                    
                    // Log pour les utilisateurs à risque élevé
                    if (metrics.isAtRiskOfAbandonment()) {
                        System.out.println("⚠️  Utilisateur à risque: " + user.getEmail() + 
                                         " (Risque: " + String.format("%.0f%%", metrics.getAbandonmentRisk()) + ")");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Erreur pour l'utilisateur " + user.getEmail() + ": " + e.getMessage());
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Résumé
            System.out.println("\n📊 RÉSUMÉ DE LA DÉTECTION:");
            System.out.println("   ✓ Utilisateurs analysés: " + usersProcessed);
            System.out.println("   ✓ Anomalies détectées: " + anomaliesFound);
            System.out.println("   ✓ Alertes générées: " + alertsGenerated);
            System.out.println("   ✓ Durée: " + duration + " ms");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            lastRun = LocalDateTime.now();
            lastAnomaliesFound = anomaliesFound;
            
            // Envoyer des notifications si anomalies critiques
            if (anomaliesFound > 0) {
                notifyCriticalAnomalies();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la détection automatique: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si une anomalie similaire existe déjà (dernières 24h)
     */
    private boolean isDuplicateAnomaly(HealthAnomaly newAnomaly) {
        List<HealthAnomaly> existing = anomalyRepository.findAnomaliesByUserId(newAnomaly.getUserId());
        
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        return existing.stream()
            .filter(a -> a.getDetectedAt().isAfter(cutoff))
            .anyMatch(a -> a.getType() == newAnomaly.getType() && !a.isResolved());
    }
    
    /**
     * Notifie les admins des anomalies critiques
     */
    private void notifyCriticalAnomalies() {
        List<HealthAnomaly> critical = anomalyRepository.findCriticalAnomalies();
        
        if (!critical.isEmpty()) {
            System.out.println("\n🚨 ALERTES CRITIQUES:");
            for (HealthAnomaly anomaly : critical) {
                User user = userRepository.findById(anomaly.getUserId());
                if (user != null) {
                    System.out.println("   ⚠️  " + user.getEmail() + " - " + 
                                     anomaly.getType().getLabel() + 
                                     " (Sévérité: " + String.format("%.0f%%", anomaly.getSeverity()) + ")");
                }
            }
            System.out.println();
            
            // TODO: Envoyer des emails aux admins
            // EmailService.sendCriticalAnomalyAlert(critical);
        }
    }
    
    /**
     * Exécute une détection manuelle immédiate
     */
    public void runNow() {
        System.out.println("🔄 Exécution manuelle de la détection...");
        new Thread(this::runDetection).start();
    }
    
    // Getters pour le monitoring
    public boolean isRunning() {
        return isRunning;
    }
    
    public LocalDateTime getLastRun() {
        return lastRun;
    }
    
    public int getLastAnomaliesFound() {
        return lastAnomaliesFound;
    }
    
    /**
     * Statistiques du scheduler
     */
    public String getStatus() {
        if (!isRunning) {
            return "Arrêté";
        }
        
        if (lastRun == null) {
            return "En attente de première exécution";
        }
        
        return String.format("Actif - Dernière exécution: %s (%d anomalies trouvées)",
            lastRun.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            lastAnomaliesFound);
    }
}
