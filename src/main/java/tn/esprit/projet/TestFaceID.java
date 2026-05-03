package tn.esprit.projet;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.projet.gui.faceid.FaceIDLauncher;
import tn.esprit.projet.utils.MyBDConnexion;

/**
 * Test Face ID System
 * Quick test without modifying main application
 */
public class TestFaceID extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize database
        MyBDConnexion.getInstance();
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         PROFESSIONAL FACE ID - TEST D'INSCRIPTION             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Ce test va:");
        System.out.println("  1. Ouvrir votre caméra");
        System.out.println("  2. Détecter votre visage");
        System.out.println("  3. Enregistrer votre Face ID");
        System.out.println();
        
        // Test Enrollment (inscription)
        testEnrollment();
    }

    private void testAuthentication() {
        System.out.println("\n[TEST] Launching Face ID Authentication...");
        
        FaceIDLauncher.launchAuthentication(
            () -> {
                System.out.println("✓ SUCCESS - User authenticated!");
                System.out.println("In real app, you would now load the dashboard");
                System.exit(0);
            },
            () -> {
                System.out.println("✗ FAILURE - Authentication failed");
                System.out.println("In real app, you would show password login");
                System.exit(0);
            }
        );
    }

    private void testEnrollment() {
        System.out.println("[TEST] Lancement de l'inscription Face ID...");
        System.out.println();
        
        // Test with user ID 1
        int testUserId = 1;
        
        System.out.println("📸 Fenêtre Face ID en cours d'ouverture...");
        System.out.println("   Positionnez votre visage dans le cercle");
        System.out.println("   Cliquez sur 'Enroll Face' quand votre visage est détecté");
        System.out.println();
        
        FaceIDLauncher.launchEnrollment(
            testUserId,
            () -> {
                System.out.println();
                System.out.println("╔════════════════════════════════════════════════════════════════╗");
                System.out.println("║                    ✅ SUCCÈS!                                  ║");
                System.out.println("╚════════════════════════════════════════════════════════════════╝");
                System.out.println();
                System.out.println("Face ID enregistré avec succès pour l'utilisateur " + testUserId);
                System.out.println();
                System.out.println("Vous pouvez maintenant:");
                System.out.println("  1. Tester l'authentification");
                System.out.println("  2. Intégrer Face ID dans votre application");
                System.out.println();
                System.out.println("Pour tester l'authentification, modifiez TestFaceID.java");
                System.out.println("et appelez testAuthentication() au lieu de testEnrollment()");
                System.out.println();
                System.exit(0);
            },
            () -> {
                System.out.println();
                System.out.println("╔════════════════════════════════════════════════════════════════╗");
                System.out.println("║                    ❌ ANNULÉ                                   ║");
                System.out.println("╚════════════════════════════════════════════════════════════════╝");
                System.out.println();
                System.out.println("Inscription annulée par l'utilisateur");
                System.out.println();
                System.exit(0);
            }
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
