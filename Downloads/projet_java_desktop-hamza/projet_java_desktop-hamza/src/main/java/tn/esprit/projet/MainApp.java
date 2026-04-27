package tn.esprit.projet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main_layout.fxml"));
        primaryStage.setTitle("Nutri Coach Pro - Événements");
        primaryStage.setScene(new Scene(root, 1250, 750));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}