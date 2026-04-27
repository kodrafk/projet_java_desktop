package tn.esprit.projet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.projet.utils.DatabaseConnection;
import tn.esprit.projet.utils.DatabaseMigration;
import tn.esprit.projet.utils.DataSeeder;
import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.repository.WeightRepository;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Run schema migrations via MyBDConnexion, then use DatabaseConnection for queries
        MyBDConnexion.getInstance();

        // Ensure weight-related tables exist before any repository call
        WeightRepository.initSchema();
        
        // Migration automatique désactivée - Utilisez FORCE_FIX_DATABASE.bat à la place
        // DatabaseMigration.migrate();
        
        DataSeeder.seed();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 1100, 720);
        primaryStage.setTitle("NutriLife");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
