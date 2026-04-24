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
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.RecetteService;
import tn.esprit.projet.utils.SessionManager;

import java.io.IOException;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;

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
    @FXML private Button btnWellness;

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

    @FXML private VBox badgeBox1;
    @FXML private VBox badgeBox2;
    @FXML private VBox badgeBox3;
    @FXML private VBox badgeBox4;

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
            "-fx-background-color: transparent; " +
                    "-fx-text-fill: #1A2E1A; " +
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
    private void handleHome(MouseEvent event) {
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
    private void handleKitchen(MouseEvent event) {
        showPlaceholder("My Kitchen");
    }

    @FXML
    private void handleNutrition(ActionEvent event) {
        resetButtonStyles();
        if (btnNutrition != null) btnNutrition.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/objectives.fxml");
    }

    @FXML
    private void handleObjectives(MouseEvent event) {
        loadPage("/fxml/objectives.fxml");
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        resetButtonStyles();
        if (btnBlog != null) btnBlog.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Blog Page");
    }

    @FXML
    private void handleBlog(MouseEvent event) {
        showPlaceholder("Blog");
    }

    @FXML
    private void handleComplaints(ActionEvent event) {
        resetButtonStyles();
        if (btnComplaints != null) btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Complaints Page");
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        resetButtonStyles();
        if (btnEvents != null) btnEvents.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Events Page");
    }

    @FXML
    private void handleEvents(MouseEvent event) {
        showPlaceholder("Events");
    }

    @FXML
    private void handleWellness(ActionEvent event) {
        resetButtonStyles();
        if (btnWellness != null) btnWellness.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Wellness Page");
    }

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
    private void handleKitchenRank(ActionEvent event) {
        loadPage("/fxml/kitchen_rank.fxml");
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

    // ═══════════════════════════════════
    // STATS
    // ═══════════════════════════════════
    private void loadHomeStats() {
        try {
            int totalIngredients = ingredientService.getAll().size();
            int totalRecipes     = recetteService != null ? recetteService.countTotal() : 0;
            int expiredItems     = 0;

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

            // ═══ Badges selon le niveau actuel ═══
            List<BadgeData> badges = new ArrayList<>();

            if (currentLevel == 1) {
                badges.add(new BadgeData("First Recipe", "Create your first recipe", "first_recipe.png",
                        recipeCount >= 1, recipeCount, 1));
                badges.add(new BadgeData("First Scan", "Scan your first product", "first_scan.png",
                        scanCount >= 1, scanCount, 1));
                badges.add(new BadgeData("Boycott Hero", "Reject 3 boycotted products", "boycott_hero.png",
                        boycottCount >= 3, boycottCount, 3));
            } else if (currentLevel == 2) {
                badges.add(new BadgeData("Meal Plan Champion", "Complete a full meal plan", "meal_champ.png",
                        completedPlans >= 1, completedPlans, 1));
                badges.add(new BadgeData("Master Chef", "Create 20 recipes", "master_chef.png",
                        recipeCount >= 20, recipeCount, 20));
                badges.add(new BadgeData("Weekly Warrior", "Complete all 21 meals in a week", "weekly_warrior.png",
                        completedPlans >= 1, completedPlans, 1));
            } else {
                badges.add(new BadgeData("Ingredient Master", "Add 50 ingredients", "ingredient_master.png",
                        ingredientCount >= 50, ingredientCount, 50));
                badges.add(new BadgeData("Ethical Guardian", "Reject 10 boycotted products", "ethical_guardian.png",
                        boycottCount >= 10, boycottCount, 10));
                badges.add(new BadgeData("Legend Crown", "Reach 500 ethical points", "ethical_legend.png",
                        totalPoints >= 500, totalPoints, 500));
            }

            // ═══ Level info ═══
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

            // ═══ Progress bar → points progression ═══
            double progressValue = currentLevel == 3 ? 1.0 : Math.min((double) totalPoints / nextTarget, 1.0);
            long unlockedCount = badges.stream().filter(b -> b.unlocked).count();

            if (progressEthical != null) progressEthical.setProgress(progressValue);
            if (lblProgressText != null) lblProgressText.setText(
                    levelIcon + " Level " + currentLevel + " — " + levelName + " (" + totalPoints + " / " + nextTarget + " pts)");
            if (lblWidgetPoints != null) lblWidgetPoints.setText(totalPoints + " pts");
            if (lblEthicalPoints != null) lblEthicalPoints.setText(String.valueOf(totalPoints));
            if (lblEthicalLevel != null) lblEthicalLevel.setText(levelIcon + " " + levelName);
            if (lblEthicalPoints2 != null) lblEthicalPoints2.setText(unlockedCount + " / 3 badges unlocked");

            // ═══ Badges grid ═══
            if (badgesContainer != null) {
                badgesContainer.getChildren().clear();
                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(12);
                grid.setVgap(12);
                javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
                col1.setPercentWidth(33.33);
                javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
                col2.setPercentWidth(33.33);
                javafx.scene.layout.ColumnConstraints col3 = new javafx.scene.layout.ColumnConstraints();
                col3.setPercentWidth(33.33);
                grid.getColumnConstraints().addAll(col1, col2, col3);

                for (int i = 0; i < badges.size(); i++) {
                    BadgeData badge = badges.get(i);
                    VBox card = buildBadgeCard(badge);
                    grid.add(card, i, 0);
                }
                badgesContainer.getChildren().add(grid);
            }
        } catch (Exception e) {
            System.err.println("loadEthicalWidget error: " + e.getMessage());
        }
    }

    private static class BadgeData {
        String name;
        String description;
        String imageName;
        boolean unlocked;
        int currentValue;
        int targetValue;

        BadgeData(String name, String description, String imageName,
                  boolean unlocked, int currentValue, int targetValue) {
            this.name = name;
            this.description = description;
            this.imageName = imageName;
            this.unlocked = unlocked;
            this.currentValue = currentValue;
            this.targetValue = targetValue;
        }
    }

    private VBox buildBadgeCard(BadgeData badge) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);

        if (badge.unlocked) {
            card.setStyle("-fx-background-color: #F0FFF4;-fx-background-radius: 14;-fx-border-color: #2ECC71;-fx-border-radius: 14;-fx-border-width: 2;-fx-padding: 16;");
        } else {
            card.setStyle("-fx-background-color: #F8FAFC;-fx-background-radius: 14;-fx-border-color: #E2E8F0;-fx-border-radius: 14;-fx-padding: 16;");
        }

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(64, 64);
        imageContainer.setMaxSize(64, 64);

        try {
            var imgUrl = getClass().getResource("/images/" + badge.imageName);
            if (imgUrl != null) {
                ImageView imageView = new ImageView(new Image(imgUrl.toExternalForm()));
                imageView.setFitWidth(56);
                imageView.setFitHeight(56);
                imageView.setPreserveRatio(true);
                if (!badge.unlocked) imageView.setStyle("-fx-opacity: 0.3;");
                imageContainer.getChildren().add(imageView);
            }
        } catch (Exception e) {
            Label fallback = new Label(badge.unlocked ? "🏆" : "🔒");
            fallback.setStyle("-fx-font-size: 28px;");
            imageContainer.getChildren().add(fallback);
        }

        Label nameLabel = new Label(badge.name);
        nameLabel.setStyle("-fx-font-size: 12px;-fx-font-weight: bold;-fx-text-fill: " + (badge.unlocked ? "#1E293B" : "#94A3B8") + ";-fx-wrap-text: true;");
        nameLabel.setAlignment(Pos.CENTER);

        Label descLabel = new Label(badge.description);
        descLabel.setStyle("-fx-font-size: 10px;-fx-text-fill: #94A3B8;-fx-wrap-text: true;");
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setWrapText(true);

        Label statusLabel = new Label(badge.unlocked ? "✅ Unlocked" : "🔒 " + badge.currentValue + " / " + badge.targetValue);
        statusLabel.setStyle("-fx-font-size: 10px;-fx-font-weight: bold;-fx-text-fill: " + (badge.unlocked ? "#27AE60" : "#64748B") + ";-fx-background-color: " + (badge.unlocked ? "#DCFCE7" : "#F1F5F9") + ";-fx-background-radius: 6;-fx-padding: 3 8;");

        card.getChildren().addAll(imageContainer, nameLabel, descLabel, statusLabel);
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");
        card.setOnMouseClicked(e -> showBadgeOverlay(badge));

        return card;
    }

    private void showBadgeOverlay(BadgeData badge) {
        if(badgeOverlay == null) return;
        try {
            var imgUrl = getClass().getResource("/images/" + badge.imageName);
            if (imgUrl != null) {
                badgeModalImage.setImage(new Image(imgUrl.toExternalForm()));
                badgeModalImage.setOpacity(badge.unlocked ? 1.0 : 0.3);
            }
        } catch (Exception e) {
            badgeModalImage.setImage(null);
        }

        badgeModalTitle.setText(badge.name);
        badgeModalDescription.setText(badge.description);

        if (badge.unlocked) {
            badgeModalStatus.setText("✅ Unlocked");
            badgeModalStatus.setStyle("-fx-font-size: 13px;-fx-font-weight: bold;-fx-text-fill: #27AE60;-fx-background-color: #DCFCE7;-fx-background-radius: 8;-fx-padding: 6 16;");
            badgeModalImageContainer.setStyle("-fx-background-color: #F0FFF4;-fx-background-radius: 70;-fx-border-color: #2ECC71;-fx-border-radius: 70;-fx-border-width: 3;");
            badgeModalProgressBox.setVisible(false);
            badgeModalProgressBox.setManaged(false);
        } else {
            badgeModalStatus.setText("🔒 Locked");
            badgeModalStatus.setStyle("-fx-font-size: 13px;-fx-font-weight: bold;-fx-text-fill: #64748B;-fx-background-color: #F1F5F9;-fx-background-radius: 8;-fx-padding: 6 16;");
            badgeModalImageContainer.setStyle("-fx-background-color: #F8FAFC;-fx-background-radius: 70;-fx-border-color: #E2E8F0;-fx-border-radius: 70;-fx-border-width: 3;");
            double progress = badge.targetValue > 0 ? (double) badge.currentValue / badge.targetValue : 0;
            badgeModalProgressBar.setProgress(Math.min(progress, 1.0));
            badgeModalProgressText.setText(badge.currentValue + " / " + badge.targetValue);
            badgeModalProgressBox.setVisible(true);
            badgeModalProgressBox.setManaged(true);
        }

        badgeOverlay.setVisible(true);
        badgeOverlay.setManaged(true);
    }

    @FXML
    private void closeBadgeOverlay() {
        if(badgeOverlay != null) {
            badgeOverlay.setVisible(false);
            badgeOverlay.setManaged(false);
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

        // Champ vide → message initial
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

        // Recherche trop courte
        if (keyword.length() < 2) return;

        List<BoycottBrand> results = boycottService.searchByName(keyword);

        if (results.isEmpty()) {
            // ✅ Non boycotté
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
            // 🔴 Boycotté
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
        } catch (Exception e) {
            System.err.println("Error loading: " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Error loading page");
        }
    }

    private void showPlaceholder(String pageName) {
        if(contentArea != null) {
            Label placeholder = new Label(pageName + " - Coming Soon");
            placeholder.setStyle("-fx-font-size: 28px;-fx-text-fill: #475569;-fx-font-weight: bold;");
            contentArea.getChildren().setAll(placeholder);
        }
    }

    private void resetButtonStyles() {
        if (btnHome != null) btnHome.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnAbout != null) btnAbout.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnMyKitchen != null) btnMyKitchen.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnNutrition != null) btnNutrition.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnBlog != null) btnBlog.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnComplaints != null) btnComplaints.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnEvents != null) btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnWellness != null) btnWellness.setStyle(DEFAULT_BUTTON_STYLE);
    }
}
