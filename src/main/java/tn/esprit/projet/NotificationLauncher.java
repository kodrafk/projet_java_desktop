package tn.esprit.projet;

import tn.esprit.projet.services.ExpiryNotificationService;

public class NotificationLauncher {

    public static void main(String[] args) {
        System.out.println("[NotificationLauncher] 🚀 Démarrage vérification expiration...");

        try {
            // Vérifier et envoyer notification si nécessaire
            ExpiryNotificationService.checkAndNotify();

            // Attendre 3 secondes pour que la notification s'affiche
            Thread.sleep(3000);

        } catch (Exception e) {
            System.err.println("[NotificationLauncher] ❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[NotificationLauncher] ✅ Vérification terminée.");
        System.exit(0);
    }
}
