package tn.esprit.projet.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Navigation helper — replaces the current scene on a stage.
 */
public class Nav {

    public static void go(Stage stage, String fxml, String title) {
        go(stage, fxml, title, 1100, 720, false);
    }

    public static void go(Stage stage, String fxml, String title, int w, int h, boolean maximize) {
        try {
            Parent root = FXMLLoader.load(Nav.class.getResource("/fxml/" + fxml));
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.setMaximized(maximize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Load FXML and return the loader so the controller can be retrieved. */
    public static FXMLLoader loader(String fxml) {
        return new FXMLLoader(Nav.class.getResource("/fxml/" + fxml));
    }
}
