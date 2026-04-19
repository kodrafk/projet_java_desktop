package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.utils.SessionManager;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;

    private IngredientService ingredientService;

    @FXML
    public void initialize() {
        ingredientService = new IngredientService();
    }

    // ── Nav handlers (onMouseClicked → MouseEvent) ──────────────────────────

    @FXML
    private void handleHome(MouseEvent event) {
        showHomePage();
    }

    @FXML
    private void handleKitchen(MouseEvent event) {
        showPlaceholder("My Kitchen");
    }

    @FXML
    private void handleObjectives(MouseEvent event) {
        loadPage("/fxml/objectives.fxml");
    }

    @FXML
    private void handleEvents(MouseEvent event) {
        showPlaceholder("Events");
    }

    @FXML
    private void handleBlog(MouseEvent event) {
        showPlaceholder("Blog");
    }

    // ── Button / MenuItem handlers (onAction → ActionEvent) ─────────────────

    @FXML
    private void handleAssistant(ActionEvent event) {
        showPlaceholder("AI Assistant");
    }

    @FXML
    private void handleBookAppointment(ActionEvent event) {
        showPlaceholder("Book Appointment");
    }

    @FXML
    private void handleMyProfile(ActionEvent event) {
        loadPage("/fxml/user_profile.fxml");
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        loadPage("/fxml/change_password.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(login));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showHomePage() {
        if (homeContent != null) {
            contentArea.getChildren().setAll(homeContent);
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            System.err.println("Failed to load: " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Page not found");
        }
    }

    private void showPlaceholder(String pageName) {
        Label lbl = new Label(pageName + " — Coming Soon");
        lbl.setStyle("-fx-font-size: 28px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        contentArea.getChildren().setAll(lbl);
    }
}
