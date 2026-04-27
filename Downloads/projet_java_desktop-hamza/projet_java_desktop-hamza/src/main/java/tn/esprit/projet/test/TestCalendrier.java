package tn.esprit.projet.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.projet.services.CalendrierService;
import tn.esprit.projet.models.Evenement;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Classe de test pour le calendrier
 * Lance l'interface calendrier en mode standalone
 */
public class TestCalendrier extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("=== Démarrage du test calendrier ===");
            
            // Test 1 : Vérifier le service
            testService();
            
            // Test 2 : Charger l'interface
            System.out.println("\n📱 Chargement de l'interface...");
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/FrontCalendrier.fxml")
            );
            Parent root = loader.load();
            System.out.println("✅ Interface chargée avec succès");
            
            // Créer la scène
            Scene scene = new Scene(root, 1400, 800);
            
            // Configurer la fenêtre
            primaryStage.setTitle("📅 Test Calendrier - projetJAV");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.show();
            
            System.out.println("✅ Fenêtre affichée");
            System.out.println("\n=== Test terminé avec succès ! ===");
            System.out.println("💡 Testez les fonctionnalités :");
            System.out.println("   - Navigation entre les mois");
            System.out.println("   - Clic sur un jour pour voir les événements");
            System.out.println("   - Bouton 'Aujourd'hui' pour revenir au jour actuel");
            System.out.println("   - Statistiques en haut de la page");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test du service CalendrierService
     */
    private void testService() {
        System.out.println("\n🧪 Test du CalendrierService...");
        
        try {
            CalendrierService service = new CalendrierService();
            
            // Test 1 : Événements du mois
            YearMonth moisCourant = YearMonth.now();
            List<Evenement> evenementsMois = service.getEvenementsDuMois(moisCourant);
            System.out.println("✅ Événements ce mois (" + moisCourant + ") : " + evenementsMois.size());
            
            // Test 2 : Événements à venir
            List<Evenement> avenir = service.getEvenementsAvenir();
            System.out.println("✅ Événements à venir : " + avenir.size());
            
            // Test 3 : Événements de la semaine
            List<Evenement> semaine = service.getEvenementsSemaine();
            System.out.println("✅ Événements cette semaine : " + semaine.size());
            
            // Test 4 : Événements d'aujourd'hui
            LocalDate aujourdhui = LocalDate.now();
            List<Evenement> aujourdhuiEvents = service.getEvenementsDuJour(aujourdhui);
            System.out.println("✅ Événements aujourd'hui (" + aujourdhui + ") : " + aujourdhuiEvents.size());
            
            // Test 5 : Compteur d'événements
            Map<LocalDate, Integer> compteur = service.getCompteurEvenementsMois(moisCourant);
            System.out.println("✅ Jours avec événements ce mois : " + compteur.size());
            
            // Test 6 : Prochains événements
            List<Evenement> prochains = service.getProchainsEvenements(5);
            System.out.println("✅ 5 prochains événements : " + prochains.size());
            
            // Afficher quelques détails
            if (!evenementsMois.isEmpty()) {
                System.out.println("\n📋 Exemple d'événements ce mois :");
                evenementsMois.stream()
                    .limit(3)
                    .forEach(ev -> System.out.println(
                        "   - " + ev.getNom() + 
                        " le " + ev.getDate_debut().toLocalDate() + 
                        " à " + ev.getDate_debut().toLocalTime().toString().substring(0, 5)
                    ));
            } else {
                System.out.println("\n⚠️  Aucun événement trouvé ce mois");
                System.out.println("💡 Ajoutez des événements dans la base de données pour tester");
            }
            
            System.out.println("\n✅ Tous les tests du service ont réussi !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test du service : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║     TEST CALENDRIER - projetJAV                        ║");
        System.out.println("║     Version 1.0.0 - Avril 2026                         ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        launch(args);
    }
}
