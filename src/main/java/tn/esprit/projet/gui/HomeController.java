package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.SessionManager;

/**
 * HomeController — immediately redirects to main_layout.fxml.
 * home.fxml is kept as an alias so that any code navigating to "home.fxml"
 * still works correctly.
 */
public class HomeController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // Redirect to main_layout.fxml as soon as this scene loads
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/main_layout.fxml"));
                Scene scene = new Scene(root, 1280, 760);
                stage.setScene(scene);
                stage.setTitle("NutriLife - Dashboard");
                stage.setMaximized(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
