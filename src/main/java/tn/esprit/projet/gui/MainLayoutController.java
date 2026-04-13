package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.utils.SessionManager;

import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane  contentArea;
    @FXML private VBox       homeContent;
    @FXML private MenuButton menuAccount;

    // Stats labels
    @FXML private Label lblStatUsers;
    @FXML private Label lblStatIngredients;
    @FXML private Label lblStatBmi;
    @FXML private Label lblStatName;

    private final UserDAO         userDAO           = new UserDAO();
    private final IngredientService ingredientService = new IngredientService();

    @FXML
    public void initialize() {
        // No stats bar — nothing to load on home
    }

    // ── Load personalized stats ────────────────────────────────────────────────
    private void loadStats() {
        User u = SessionManager.getCurrentUser();

        if (lblStatName != null && u != null)
            lblStatName.setText(u.getFirstName() != null ? u.getFirstName() : "");

        if (lblStatIngredients != null)
            lblStatIngredients.setText(String.valueOf(ingredientService.getAll().size()));

        if (lblStatUsers != null)
            lblStatUsers.setText(String.valueOf(userDAO.countActive()));

        if (lblStatBmi != null && u != null && u.getWeight() > 0 && u.getHeight() > 0)
            lblStatBmi.setText(String.format("%.1f", u.getBmi()));
        else if (lblStatBmi != null)
            lblStatBmi.setText("—");
    }

    // ── Navigation handlers ────────────────────────────────────────────────────
    @FXML private void handleHome(javafx.scene.input.MouseEvent e)       { showHome(); }
    @FXML private void handleKitchen(javafx.scene.input.MouseEvent e)    { showPlaceholder("My Kitchen"); }
    @FXML private void handleObjectives(javafx.scene.input.MouseEvent e) { showPlaceholder("Nutrition Objectives"); }
    @FXML private void handleEvents(javafx.scene.input.MouseEvent e)     { showPlaceholder("Events"); }
    @FXML private void handleBlog(javafx.scene.input.MouseEvent e)       { showPlaceholder("Blog"); }
    @FXML private void handleAssistant(ActionEvent e)                    { showPlaceholder("AI Assistant"); }
    @FXML private void handleBookAppointment(ActionEvent e)              { showPlaceholder("Book Appointment"); }

    // ── Account menu ──────────────────────────────────────────────────────────
    @FXML
    private void handleMyProfile(ActionEvent e) {
        openModal("/fxml/user_profile.fxml", "My Profile", 620, 700);
    }

    @FXML
    private void handleChangePassword(ActionEvent e) {
        openModal("/fxml/change_password.fxml", "Security Settings", 420, 340);
    }

    @FXML
    private void handleLogout(ActionEvent e) {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("NutriLife - Login");
            stage.setMaximized(false);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void showHome() {
        contentArea.getChildren().setAll(homeContent);
    }

    private void showPlaceholder(String name) {
        Label lbl = new Label(name + " — Coming Soon");
        lbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-font-family: Georgia;");
        contentArea.getChildren().setAll(lbl);
    }

    private void openModal(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.setResizable(false);
            stage.showAndWait();
            loadStats(); // refresh stats after profile edit
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}
