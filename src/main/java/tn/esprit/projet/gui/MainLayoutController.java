package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.RecetteService;
import tn.esprit.projet.utils.SessionManager;

import java.io.IOException;
import java.util.List;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;
    @FXML private VBox submenuKitchen;

    @FXML private Button btnHome;
    @FXML private Button btnAbout;
    @FXML private MenuButton btnMyKitchen;
    @FXML private MenuButton btnAccount;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnDailyFood;
    @FXML private Button btnBlog;
    @FXML private Button btnComplaints;
    @FXML private Button btnEvents;
    @FXML private Button btnNutrition;

    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblIngredientsSaved;
    @FXML private Label lblEthicalPoints;
    @FXML private Label lblEthicalLevel;
    @FXML private Label lblWidgetPoints;
    @FXML private ProgressBar progressEthical;
    @FXML private Label lblProgressText;
    @FXML private Label lblEthicalPoints2;
    @FXML private TextField txtBoycottSearch;
    @FXML private VBox boycottResultsBox;
    @FXML private Label lblBoycottTotal;
    @FXML private VBox badgesContainer;
    @FXML private StackPane badgeOverlay;

    private IngredientService ingredientService;
    private BoycottService boycottService;
    private RecetteService recetteService;

    private static final String DEFAULT_BUTTON_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #3D5A3D; -fx-font-size: 13px; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0 12;";
    private static final String ACTIVE_BUTTON_STYLE =
            "-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 13px; " +
            "-fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 12;";

    @FXML
    public void initialize() {
        ingredientService = new IngredientService();
        boycottService    = new BoycottService();
        recetteService    = new RecetteService();

        loadHomeStats();
        loadEthicalWidget();
        loadBoycottWidget();

        if (txtBoycottSearch != null) {
            txtBoycottSearch.textProperty().addListener((obs, oldVal, newVal) -> handleBoycottSearch());
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleHome(ActionEvent e)        { resetButtonStyles(); if (btnHome != null) btnHome.setStyle(ACTIVE_BUTTON_STYLE); showHomePage(); }
    @FXML private void handleAbout(ActionEvent e)       { resetButtonStyles(); if (btnAbout != null) btnAbout.setStyle(ACTIVE_BUTTON_STYLE); showPlaceholder("About Page"); }
    @FXML private void handleDailyFood(ActionEvent e)   { resetButtonStyles(); showPlaceholder("Daily Food"); }
    @FXML private void handleBlog(ActionEvent e)        { resetButtonStyles(); if (btnBlog != null) btnBlog.setStyle(ACTIVE_BUTTON_STYLE); showPlaceholder("Blog Page"); }
    @FXML private void handleEvents(ActionEvent e)      { resetButtonStyles(); if (btnEvents != null) btnEvents.setStyle(ACTIVE_BUTTON_STYLE); showPlaceholder("Events Page"); }

    @FXML private void handleIngredients(ActionEvent e) {
        resetButtonStyles(); if (btnMyKitchen != null) btnMyKitchen.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/ingredients.fxml");
    }
    @FXML private void handleRecipes(ActionEvent e) {
        resetButtonStyles(); if (btnMyKitchen != null) btnMyKitchen.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/recipes.fxml");
    }
    @FXML private void handleNutrition(ActionEvent e) {
        resetButtonStyles(); if (btnNutrition != null) btnNutrition.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/objectives.fxml");
    }
    @FXML private void handleComplaints(ActionEvent e) {
        resetButtonStyles(); if (btnComplaints != null) btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/complaints_user.fxml");
    }

    // ── Account menu ──────────────────────────────────────────────────────────

    @FXML private void handleMyProfile(ActionEvent e)      { loadPage("/fxml/user_profile.fxml"); }
    @FXML private void handleChangePassword(ActionEvent e) { loadPage("/fxml/change_password.fxml"); }
    @FXML private void handleKitchenRank(ActionEvent e)    { loadPage("/fxml/kitchen_rank.fxml"); }

    @FXML private void handleLogout(ActionEvent e) {
        SessionManager.logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(login));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    private void loadHomeStats() {
        try {
            int totalIngredients = ingredientService.getAll().size();
            int totalRecipes     = recetteService != null ? recetteService.countTotal() : 0;
            if (lblTotalIngredients != null) lblTotalIngredients.setText(String.valueOf(totalIngredients));
            if (lblTotalRecipes != null)     lblTotalRecipes.setText(String.valueOf(totalRecipes));
            if (lblIngredientsSaved != null) lblIngredientsSaved.setText(String.valueOf(totalIngredients));
        } catch (Exception e) { System.err.println("loadHomeStats: " + e.getMessage()); }
    }

    private void loadEthicalWidget() {
        try {
            EthicalPointsManager.loadFromDatabase();
            int totalPoints  = EthicalPointsManager.getTotalPoints();
            int currentLevel = EthicalPointsManager.getCurrentLevel();
            String levelName; String levelIcon; int nextTarget;
            if (currentLevel == 1)      { levelName = "Beginner";       levelIcon = "🌱"; nextTarget = 100; }
            else if (currentLevel == 2) { levelName = "Ethical Cook";   levelIcon = "🌿"; nextTarget = 500; }
            else                        { levelName = "Ethical Legend"; levelIcon = "👑"; nextTarget = totalPoints; }
            double progressValue = currentLevel == 3 ? 1.0 : Math.min((double) totalPoints / nextTarget, 1.0);
            if (progressEthical != null) progressEthical.setProgress(progressValue);
            if (lblProgressText != null) lblProgressText.setText(levelIcon + " Level " + currentLevel + " — " + levelName + " (" + totalPoints + " / " + nextTarget + " pts)");
            if (lblWidgetPoints != null) lblWidgetPoints.setText(totalPoints + " pts");
            if (lblEthicalPoints != null) lblEthicalPoints.setText(String.valueOf(totalPoints));
            if (lblEthicalLevel != null)  lblEthicalLevel.setText(levelIcon + " " + levelName);
        } catch (Exception e) { System.err.println("loadEthicalWidget: " + e.getMessage()); }
    }

    private void loadBoycottWidget() {
        try {
            int total = boycottService != null ? boycottService.getTotalBoycottedBrands() : 0;
            if (lblBoycottTotal != null) lblBoycottTotal.setText(total + " brands in database");
        } catch (Exception e) { System.err.println("loadBoycottWidget: " + e.getMessage()); }
    }

    @FXML private void handleBoycottSearch() {
        if (txtBoycottSearch == null || boycottResultsBox == null || boycottService == null) return;
        String keyword = txtBoycottSearch.getText().trim();
        boycottResultsBox.getChildren().clear();
        if (keyword.isEmpty() || keyword.length() < 2) return;

        List<BoycottBrand> results = boycottService.searchByName(keyword);
        if (results.isEmpty()) {
            VBox cleanBox = new VBox(8);
            cleanBox.setAlignment(Pos.CENTER);
            cleanBox.setStyle("-fx-padding: 20;");
            Label icon = new Label("🟢"); icon.setStyle("-fx-font-size: 30px;");
            Label msg = new Label("\"" + keyword + "\" — Not boycotted");
            msg.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");
            cleanBox.getChildren().addAll(icon, msg);
            boycottResultsBox.getChildren().add(cleanBox);
        } else {
            for (BoycottBrand brand : results) {
                VBox card = new VBox(6);
                card.setStyle("-fx-background-color: #FFF0F0; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #FECACA; -fx-border-radius: 8;");
                Label nameLabel = new Label("🔴 " + brand.getBrandName());
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #E74C3C;");
                Label reasonLabel = new Label("⚠️ " + (brand.getReason() != null ? brand.getReason() : ""));
                reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;"); reasonLabel.setWrapText(true);
                Label altLabel = new Label("💡 Alternative: " + (brand.getAlternatives() != null ? brand.getAlternatives() : ""));
                altLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27AE60;"); altLabel.setWrapText(true);
                card.getChildren().addAll(nameLabel, reasonLabel, altLabel);
                boycottResultsBox.getChildren().add(card);
            }
        }
    }

    @FXML private void closeBadgeOverlay() {
        if (badgeOverlay != null) { badgeOverlay.setVisible(false); badgeOverlay.setManaged(false); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showHomePage() {
        if (contentArea != null && homeContent != null) {
            contentArea.getChildren().setAll(homeContent);
            loadHomeStats(); loadEthicalWidget(); loadBoycottWidget();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) { showPlaceholder("Page not found: " + fxmlPath); return; }
            Parent page = FXMLLoader.load(resource);
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("Error loading: " + fxmlPath + " — " + e.getMessage());
            showPlaceholder("Error: " + e.getMessage());
        }
    }

    private void showPlaceholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #64748B;");
        VBox container = new VBox(label);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #F6FBF7;");
        if (contentArea != null) contentArea.getChildren().setAll(container);
    }

    private void resetButtonStyles() {
        for (Button b : new Button[]{btnHome, btnAbout, btnIngredients, btnRecipes, btnDailyFood, btnBlog, btnComplaints, btnEvents, btnNutrition})
            if (b != null) b.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnMyKitchen != null) btnMyKitchen.setStyle(DEFAULT_BUTTON_STYLE);
    }
}
