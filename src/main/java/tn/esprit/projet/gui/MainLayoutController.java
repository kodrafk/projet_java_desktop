package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.RecetteService;
import tn.esprit.projet.utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainLayoutController {

    // ═══════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════
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

    // ═══════════════════════════════════
    // STATS CARDS
    // ═══════════════════════════════════
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblIngredientsSaved;
    @FXML private Label lblEthicalPoints;
    @FXML private Label lblEthicalLevel;

    // ═══════════════════════════════════
    // PROGRESS SUMMARY
    // ═══════════════════════════════════
    @FXML private Label lblTotalIngredients2;
    @FXML private Label lblTotalRecipes2;
    @FXML private Label lblExpiredItems2;
    @FXML private Label lblEthicalPoints2;

    @FXML private Label lblWidgetPoints;
    @FXML private ProgressBar progressEthical;
    @FXML private Label lblProgressText;

    @FXML private StackPane badgeOverlay;
    @FXML private StackPane badgeModalImageContainer;
    @FXML private ImageView badgeModalImage;
    @FXML private Label badgeModalTitle;
    @FXML private Label badgeModalDescription;
    @FXML private Label badgeModalStatus;
    @FXML private VBox badgeModalProgressBox;
    @FXML private ProgressBar badgeModalProgressBar;
    @FXML private Label badgeModalProgressText;

    // ═══════════════════════════════════
    // BOYCOTT CHECKER WIDGET
    // ═══════════════════════════════════
    @FXML private TextField txtBoycottSearch;
    @FXML private VBox boycottResultsBox;
    @FXML private Label lblBoycottTotal;
    @FXML private VBox badgesContainer;

    // ═══════════════════════════════════
    // SERVICES
    // ═══════════════════════════════════
    private IngredientService ingredientService;
    private BoycottService boycottService;
    private RecetteService recetteService;

    // ═══════════════════════════════════
    // STYLES
    // ═══════════════════════════════════
    private static final String DEFAULT_BUTTON_STYLE =
            "-fx-background-color: transparent; " +
                    "-fx-text-fill: #3D5A3D; " +
                    "-fx-font-size: 13px; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 0 12;";

    private static final String ACTIVE_BUTTON_STYLE =
            "-fx-background-color: #2E7D5A; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 13px; " +
                    "-fx-background-radius: 6; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 0 12;";

    // ═══════════════════════════════════
    // INITIALIZE
    // ═══════════════════════════════════
    @FXML
    public void initialize() {
        ingredientService = new IngredientService();
        boycottService    = new BoycottService();
        recetteService    = new RecetteService();

        loadHomeStats();
        loadEthicalWidget();
        loadBoycottWidget();
        if (txtBoycottSearch != null) {
            txtBoycottSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                handleBoycottSearch();
            });
        }
    }

    // ═══════════════════════════════════
    // NAVIGATION HANDLERS
    // ═══════════════════════════════════
    @FXML
    private void handleHome(ActionEvent event) {
        resetButtonStyles();
        if (btnHome != null) btnHome.setStyle(ACTIVE_BUTTON_STYLE);
        showHomePage();
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        resetButtonStyles();
        if (btnAbout != null) btnAbout.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("About Page");
    }

    @FXML
    private void handleDailyFood(ActionEvent event) {
        resetButtonStyles();
        if (btnDailyFood != null) btnDailyFood.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Daily Food Page");
    }

    @FXML
    private void toggleKitchenMenu(ActionEvent event) {
        if (submenuKitchen != null) {
            boolean isVisible = submenuKitchen.isVisible();
            submenuKitchen.setVisible(!isVisible);
            submenuKitchen.setManaged(!isVisible);
        }
    }

    @FXML
    private void handleIngredients(ActionEvent event) {
        resetButtonStyles();
        if (btnMyKitchen != null) btnMyKitchen.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/ingredients.fxml");
    }

    @FXML
    private void handleRecipes(ActionEvent event) {
        resetButtonStyles();
        if (btnMyKitchen != null) btnMyKitchen.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/recipes.fxml");
    }

    @FXML
    private void handleNutrition(ActionEvent event) {
        resetButtonStyles();
        if (btnNutrition != null) btnNutrition.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/objectives.fxml");
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        resetButtonStyles();
        if (btnBlog != null) btnBlog.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Blog Page");
    }

    @FXML
    private void handleComplaints(ActionEvent event) {
        resetButtonStyles();
        if (btnComplaints != null) btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/complaints_user.fxml");
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        resetButtonStyles();
        if (btnEvents != null) btnEvents.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Events Page");
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
    private void handleKitchenRank(ActionEvent event) {
        loadPage("/fxml/kitchen_rank.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(login));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════
    // STATS
    // ═══════════════════════════════════
    private void loadHomeStats() {
        try {
            int totalIngredients = ingredientService.getAll().size();
            int totalRecipes     = recetteService != null ? recetteService.countTotal() : 0;

            if (lblTotalIngredients != null)
                lblTotalIngredients.setText(String.valueOf(totalIngredients));
            if (lblTotalRecipes != null)
                lblTotalRecipes.setText(String.valueOf(totalRecipes));

            if (lblIngredientsSaved != null) {
                lblIngredientsSaved.setText(String.valueOf(totalIngredients));
            }
        } catch (Exception e) {
            System.err.println("loadHomeStats error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════
    // ETHICAL POINTS WIDGET
    // ═══════════════════════════════════
    private void loadEthicalWidget() {
        try {
            int totalPoints = EthicalPointsManager.getTotalPoints();
            int scanCount = EthicalPointsManager.getScanCount();
            int boycottCount = EthicalPointsManager.getBoycottRejectCount();
            int recipeCount = recetteService != null ? recetteService.countTotal() : 0;
            int completedPlans = EthicalPointsManager.getCompletedMealPlanCount();
            int ingredientCount = ingredientService != null ? ingredientService.getAll().size() : 0;

            int currentLevel = EthicalPointsManager.getCurrentLevel();

            // Level info
            String levelName;
            String levelIcon;
            int nextTarget;

            if (currentLevel == 1) {
                levelName = "Beginner";
                levelIcon = "🌱";
                nextTarget = 100;
            } else if (currentLevel == 2) {
                levelName = "Ethical Cook";
                levelIcon = "🌿";
                nextTarget = 500;
            } else {
                levelName = "Ethical Legend";
                levelIcon = "👑";
                nextTarget = totalPoints;
            }

            // Progress bar
            double progressValue = currentLevel == 3 ? 1.0 : Math.min((double) totalPoints / nextTarget, 1.0);

            if (progressEthical != null) progressEthical.setProgress(progressValue);
            if (lblProgressText != null) lblProgressText.setText(
                    levelIcon + " Level " + currentLevel + " — " + levelName + " (" + totalPoints + " / " + nextTarget + " pts)");
            if (lblWidgetPoints != null) lblWidgetPoints.setText(totalPoints + " pts");
            if (lblEthicalPoints != null) lblEthicalPoints.setText(String.valueOf(totalPoints));
            if (lblEthicalLevel != null) lblEthicalLevel.setText(levelIcon + " " + levelName);

        } catch (Exception e) {
            System.err.println("loadEthicalWidget error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════
    // BOYCOTT CHECKER WIDGET
    // ═══════════════════════════════════
    private void loadBoycottWidget() {
        try {
            int total = boycottService != null ? boycottService.getTotalBoycottedBrands() : 0;
            if (lblBoycottTotal != null) lblBoycottTotal.setText(total + " brands in database");
        } catch (Exception e) {
            System.err.println("loadBoycottWidget error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBoycottSearch() {
        if (txtBoycottSearch == null || boycottResultsBox == null || boycottService == null) return;

        String keyword = txtBoycottSearch.getText().trim();
        boycottResultsBox.getChildren().clear();

        if (keyword.isEmpty()) {
            VBox initialBox = new VBox(8);
            initialBox.setAlignment(Pos.CENTER);
            initialBox.setPrefHeight(160);
            Label icon = new Label("🔍");
            icon.setStyle("-fx-font-size: 30px;");
            Label msg = new Label("Search a brand to check");
            msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
            Label sub = new Label("Type a brand name to check if it's boycotted");
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #CBD5E1;");
            initialBox.getChildren().addAll(icon, msg, sub);
            boycottResultsBox.getChildren().add(initialBox);
            return;
        }

        if (keyword.length() < 2) return;

        List<BoycottBrand> results = boycottService.searchByName(keyword);

        if (results.isEmpty()) {
            VBox cleanBox = new VBox(8);
            cleanBox.setAlignment(Pos.CENTER);
            cleanBox.setStyle("-fx-padding: 20;");
            Label icon = new Label("🟢");
            icon.setStyle("-fx-font-size: 30px;");
            Label msg = new Label("\"" + keyword + "\" — Not boycotted");
            msg.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");
            Label sub = new Label("Not found in our boycott database");
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
            sub.setWrapText(true);
            cleanBox.getChildren().addAll(icon, msg, sub);
            boycottResultsBox.getChildren().add(cleanBox);
        } else {
            for (BoycottBrand brand : results) {
                VBox card = new VBox(6);
                card.setStyle("-fx-background-color: #FFF0F0; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #FECACA; -fx-border-radius: 8;");

                Label nameLabel = new Label("🔴 " + brand.getBrandName());
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #E74C3C;");

                Label parentLabel = new Label("🏢 " + brand.getParentCompany());
                parentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

                Label reasonLabel = new Label("⚠️ " + brand.getReason());
                reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
                reasonLabel.setWrapText(true);

                Label catLabel = new Label("📂 " + brand.getCategory());
                catLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

                Label altLabel = new Label("💡 Alternative: " + brand.getAlternatives());
                altLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27AE60;");
                altLabel.setWrapText(true);

                card.getChildren().addAll(nameLabel, parentLabel, reasonLabel, catLabel, altLabel);
                boycottResultsBox.getChildren().add(card);
            }
        }
    }

    // ═══════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════
    @FXML
    private void closeBadgeOverlay() {
        if (badgeOverlay != null) {
            badgeOverlay.setVisible(false);
            badgeOverlay.setManaged(false);
        }
    }

    private void showHomePage() {
        if (contentArea != null && homeContent != null) {
            contentArea.getChildren().setAll(homeContent);
            loadHomeStats();
            loadEthicalWidget();
            loadBoycottWidget();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Resource not found: " + fxmlPath);
                showPlaceholder("Page not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();
            if(contentArea != null) {
                contentArea.getChildren().setAll(page);
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la page : " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Error loading page: " + e.getMessage());
        }
    }

    private void showPlaceholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #D7E6DF;");
        VBox container = new VBox(label);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #1A3E2F;");
        if(contentArea != null) {
            contentArea.getChildren().setAll(container);
        }
    }

    private void resetButtonStyles() {
        Button[] buttons = {btnHome, btnAbout, btnIngredients, btnRecipes, btnDailyFood, btnBlog, btnComplaints, btnEvents, btnNutrition, };
        for (Button b : buttons) {
            if (b != null) b.setStyle(DEFAULT_BUTTON_STYLE);
        }
        if (btnMyKitchen != null) btnMyKitchen.setStyle(DEFAULT_BUTTON_STYLE);
    }
}
