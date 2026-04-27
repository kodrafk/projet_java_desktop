package tn.esprit.projet.utils;

import tn.esprit.projet.services.AnomalySchedulerService;

/**
 * Utility to start the anomaly detection system
 * Can be executed at application startup
 */
public class StartAnomalyDetection {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🚀 STARTING ANOMALY DETECTION SYSTEM");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println();
        
        try {
            // Start scheduler with detection every 6 hours
            AnomalySchedulerService scheduler = AnomalySchedulerService.getInstance();
            scheduler.start(6);
            
            System.out.println("✅ System started successfully!");
            System.out.println("📅 Automatic detection every 6 hours");
            System.out.println("🔍 First detection in progress...");
            System.out.println();
            System.out.println("💡 System running in background");
            System.out.println("💡 Check admin dashboard to see results");
            System.out.println();
            System.out.println("Press Enter to stop...");
            
            // Wait for user input
            System.in.read();
            
            System.out.println("\n🛑 Stopping system...");
            scheduler.stop();
            System.out.println("✅ System stopped");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Method to integrate at application startup
     */
    public static void startInBackground() {
        AnomalySchedulerService scheduler = AnomalySchedulerService.getInstance();
        scheduler.start(6);  // Detection every 6 hours
        
        System.out.println("✅ Anomaly detection system started in background");
    }
    
    /**
     * Method to shutdown cleanly
     */
    public static void shutdown() {
        AnomalySchedulerService scheduler = AnomalySchedulerService.getInstance();
        if (scheduler.isRunning()) {
            scheduler.stop();
            System.out.println("✅ Anomaly detection system stopped");
        }
    }
}
