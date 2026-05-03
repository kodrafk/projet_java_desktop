package tn.esprit.projet;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.projet.gui.faceid.FaceIDLauncher;
import tn.esprit.projet.utils.MyBDConnexion;

/**
 * Test Simple - Juste ouvrir la caméra
 */
public class TestCameraSimple extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize database
        MyBDConnexion.getInstance();
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              TEST SIMPLE - OUVERTURE CAMÉRA                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("La fenêtre Face ID va s'ouvrir...");
        System.out.println("La caméra devrait s'allumer automatiquement.");
        System.out.println();
        System.out.println("Si vous voyez votre visage dans le cercle = ✅ SUCCÈS !");
        System.out.println();
        
        // Launch Face ID - it will stay open until you click Cancel
        FaceIDLauncher.launchAuthentication(
            () -> {
                System.out.println("✅ Authentification réussie !");
                System.exit(0);
            },
            () -> {
                System.out.println("❌ Authentification échouée ou annulée");
                System.out.println();
                System.out.println("C'est NORMAL si vous n'avez pas encore enregistré votre visage.");
                System.out.println("La caméra s'est-elle ouverte ? Si OUI = le système fonctionne !");
                System.exit(0);
            }
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
