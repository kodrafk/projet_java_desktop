package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class AccueilController {

    /** Navigue vers le layout ADMIN (admin_layout.fxml) */
    @FXML
    public void allerAdminLayout() {
        naviguerVers("/fxml/admin_layout.fxml", 1320, 780, "Nutri Coach Pro — Admin Panel");
    }

    /** Navigue vers le layout FRONT utilisateur (main_layout.fxml) */
    @FXML
    public void allerFrontLayout() {
        naviguerVers("/fxml/main_layout.fxml", 1250, 750, "Nutri Coach Pro — Événements");
    }

    private void naviguerVers(String fxmlPath, int width, int height, String titre) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) Window.getWindows().stream()
                    .filter(Window::isShowing).findFirst().orElse(null);
            if (stage != null) {
                stage.setScene(new Scene(root, width, height));
                stage.setTitle(titre);
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
