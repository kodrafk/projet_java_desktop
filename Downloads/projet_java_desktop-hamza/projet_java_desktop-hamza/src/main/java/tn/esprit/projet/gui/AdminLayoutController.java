package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class AdminLayoutController {

    @FXML private StackPane contentArea;
    @FXML private VBox      homeContent;
    @FXML private ScrollPane dashboardScrollPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnComplaints;
    @FXML private Button btnEvents;
    @FXML private Button btnSponsors;
    @FXML private Button btnBlogs;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblTotalComplaints;
    @FXML private Label lblDate;
    @FXML private Label lblClock;

    private static final String DEFAULT_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #A8C4B8; -fx-font-size: 12.5px;" +
            "-fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";

    private static final String ACTIVE_STYLE =
            "-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 12.5px;" +
            "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;" +
            "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";

    @FXML
    public void initialize() {
        loadDashboardStats();
    }

    // ════════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR
    // ════════════════════════════════════════════════════════

    @FXML private void handleDashboard(ActionEvent e) {
        resetStyles(); btnDashboard.setStyle(ACTIVE_STYLE);
        showHomePage();
    }

    @FXML private void handleUsers(ActionEvent e) {
        resetStyles(); btnUsers.setStyle(ACTIVE_STYLE);
        showPlaceholder("Users Management");
    }

    @FXML private void handleIngredients(ActionEvent e) {
        resetStyles(); btnIngredients.setStyle(ACTIVE_STYLE);
        showPlaceholder("Ingredients Management");
    }

    @FXML private void handleRecipes(ActionEvent e) {
        resetStyles(); btnRecipes.setStyle(ACTIVE_STYLE);
        showPlaceholder("Recipes Management");
    }

    @FXML private void handleComplaints(ActionEvent e) {
        resetStyles(); btnComplaints.setStyle(ACTIVE_STYLE);
        showPlaceholder("Complaints Management");
    }

    @FXML private void handleEvents(ActionEvent e) {
        resetStyles(); btnEvents.setStyle(ACTIVE_STYLE);
        loadPageInContent("/fxml/AdminEvenement.fxml", null);
    }

    @FXML private void handleSponsors(ActionEvent e) {
        resetStyles();
        if (btnSponsors != null) btnSponsors.setStyle(ACTIVE_STYLE);
        loadPageInContent("/fxml/AdminSponsorModerne.fxml", null);
    }

    @FXML private void handleBlogs(ActionEvent e) {
        resetStyles(); btnBlogs.setStyle(ACTIVE_STYLE);
        showPlaceholder("Blogs Management");
    }

    @FXML
    private void allerVersClient() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/main_layout.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 1250, 750));
            stage.setTitle("Nutri Coach Pro — Espace Client");
            stage.centerOnScreen();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════
    //  CHARGEMENT DE PAGE DANS LE CONTENT AREA
    // ════════════════════════════════════════════════════════

    /**
     * Charge un FXML dans le contentArea et injecte ce AdminLayoutController
     * dans le contrôleur enfant (si c'est un AdminEvenementController).
     *
     * @param fxmlPath  chemin du FXML (ex: "/fxml/AdminEvenement.fxml")
     * @param caller    le contrôleur appelant (peut être null)
     */
    public void loadPageInContent(String fxmlPath, AdminEvenementController caller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Injecter la référence au layout parent dans le contrôleur enfant
            Object controller = loader.getController();
            if (controller instanceof AdminEvenementController) {
                ((AdminEvenementController) controller).setParentController(this);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException ex) {
            System.err.println("Erreur chargement : " + fxmlPath);
            ex.printStackTrace();
            showPlaceholder("Erreur : page introuvable — " + fxmlPath);
        }
    }

    // ════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════

    private void showHomePage() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardScrollPane);
        loadDashboardStats();
    }

    private void showPlaceholder(String pageName) {
        VBox box = new VBox(12);
        box.setStyle("-fx-alignment: center; -fx-background-color: #F0F4F8;");
        Label title = new Label(pageName);
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("Ce module sera bientôt disponible.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        box.getChildren().addAll(title, sub);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(box);
    }

    private void resetStyles() {
        btnDashboard.setStyle(DEFAULT_STYLE);
        btnUsers.setStyle(DEFAULT_STYLE);
        btnIngredients.setStyle(DEFAULT_STYLE);
        btnRecipes.setStyle(DEFAULT_STYLE);
        btnComplaints.setStyle(DEFAULT_STYLE);
        btnEvents.setStyle(DEFAULT_STYLE);
        btnBlogs.setStyle(DEFAULT_STYLE);
        if (btnSponsors != null) btnSponsors.setStyle(DEFAULT_STYLE);
    }

    private void loadDashboardStats() {
        if (lblTotalUsers != null)       lblTotalUsers.setText("128");
        if (lblTotalIngredients != null) lblTotalIngredients.setText("54");
        if (lblTotalRecipes != null)     lblTotalRecipes.setText("31");
        if (lblTotalComplaints != null)  lblTotalComplaints.setText("12");
        if (lblDate != null)             lblDate.setText("April 16, 2026");
        if (lblClock != null)            lblClock.setText("10:00:00");
    }
}
