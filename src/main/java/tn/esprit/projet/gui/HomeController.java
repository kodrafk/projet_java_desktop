package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

public class HomeController {

    @FXML private StackPane  contentArea;
    @FXML private Label      lblWelcome;
    @FXML private MenuButton menuAccount;

    @FXML
    public void initialize() {
        if (!Session.isLoggedIn()) {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
            return;
        }
        User u = Session.getCurrentUser();
        if (lblWelcome != null)
            lblWelcome.setText("Welcome back, " + u.getFirstName() + "! 👋");
    }

    @FXML private void handleMyProfile() {
        openModal("profile.fxml", "My Profile", 640, 720);
    }

    @FXML private void handleChangePassword() {
        openModal("change_password.fxml", "Change Password", 460, 360);
    }

    @FXML private void handleMyBadges() {
        openModal("badges.fxml", "My Badges", 700, 620);
    }

    @FXML private void handleLogout() {
        Session.logout();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login", 1100, 720, false);
    }

    private void openModal(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
