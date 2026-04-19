package tn.esprit.projet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import tn.esprit.projet.utils.DataSeeder;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DataSeeder.seed(); // create default admin if none exists
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 1100, 720);
        primaryStage.setTitle("NutriLife - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
