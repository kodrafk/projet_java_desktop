package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.models.Recette;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.RecetteService;
import tn.esprit.projet.services.WeatherMealSuggestionService;
import tn.esprit.projet.services.WeatherService;
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
    @FXML private ScrollPane mainScrollPane;

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
    @FXML private Button     btnMessages;
    @FXML private Label      lblUnreadBadge;
    @FXML private Button     btnAssistantIA;

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
    // WEATHER RECOMMENDATION
    // ═══════════════════════════════════
    @FXML private VBox     weatherCard;
    @FXML private Label    lblWeatherIcon;
    @FXML private Label    lblWeatherCity;
    @FXML private Label    lblWeatherDesc;
    @FXML private Label    lblWeatherTemp;
    @FXML private Label    lblWeatherRecipeName;
    @FXML private Label    lblWeatherRecipeInfo;
    @FXML private Label    lblWeatherReason;
    @FXML private Button   btnWeatherRecipe;

    // ═══════════════════════════════════
    // SERVICES
    // ═══════════════════════════════════
    private IngredientService ingredientService;
    private BoycottService boycottService;
    private RecetteService recetteService;
    private WeatherMealSuggestionService weatherMealSuggestionService;
    private Recette currentWeatherRecipe;

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
        ingredientService            = new IngredientService();
        boycottService               = new BoycottService();
        recetteService               = new RecetteService();
        weatherMealSuggestionService = new WeatherMealSuggestionService();

        loadHomeStats();
        loadEthicalWidget();
        loadBadgesWidget();
        loadBoycottWidget();
        loadWeatherRecommendation();

        if (txtBoycottSearch != null) {
            txtBoycottSearch.textProperty().addListener((obs, o, n) -> handleBoycottSearch());
        }

        // Expiry notification supprimée ici — elle se déclenche dans la page Ingredients
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
        loadPage("/fxml/blog_front.fxml");
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
        loadPage("/fxml/Front/FrontEvenement.fxml");
    }

    @FXML
    public void handleAssistantIA(ActionEvent event) {
        tn.esprit.projet.utils.IADialogUtil.ouvrirAssistantIA();
    }

    @FXML
    private void handleOpenChat(ActionEvent event) {
        openModal("/fxml/weight_objective.fxml", "My Goal & Progress", 700, 750);
    }

    @FXML
    private void handleWeeklyChallenges(ActionEvent event) {
        openModal("/fxml/weekly_challenges.fxml", "Weekly Challenges", 560, 680);
    }

    @FXML
    private void handleMyBadges(ActionEvent event) {
        openModal("/fxml/badges.fxml", "My Badges 🏆", 860, 820);
    }

    @FXML
    private void handleMyProfile(ActionEvent event) {
        openModal("/fxml/profile.fxml", "My Profile", 640, 720);
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        openModal("/fxml/change_password.fxml", "Security Settings", 460, 420);
    }

    @FXML
    private void handleKitchenRank(ActionEvent event) {
        openModal("/fxml/kitchen_rank.fxml", "Kitchen Rank", 700, 600);
    }

    private void openModal(String fxmlPath, String title, int w, int h) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("[MainLayout] Modal not found: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("[MainLayout] openModal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setTitle("NutriLife - Login");
            stage.setMaximized(false);
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
    // BADGES WIDGET
    // ═══════════════════════════════════
    private void loadBadgesWidget() {
        if (badgesContainer == null) return;
        badgesContainer.getChildren().clear();

        int recipeCount     = recetteService != null ? recetteService.countTotal() : 0;
        int scanCount       = EthicalPointsManager.getScanCount();
        int boycottCount    = EthicalPointsManager.getBoycottRejectCount();
        int completedPlans  = EthicalPointsManager.getCompletedMealPlanCount();
        int ingredientCount = ingredientService != null ? ingredientService.getAll().size() : 0;
        int totalPoints     = EthicalPointsManager.getTotalPoints();

        // [name, img, unlocked, cur, target]
        Object[][] badges = {
            {"First Recipe",     "first_recipe.png",     recipeCount >= 1,     recipeCount,     1},
            {"First Scan",       "first_scan.png",       scanCount >= 1,       scanCount,       1},
            {"Boycott Hero",     "boycott_hero.png",     boycottCount >= 3,    boycottCount,    3},
            {"Meal Plan Champ",  "meal_champ.png",       completedPlans >= 1,  completedPlans,  1},
            {"Master Chef",      "master_chef.png",      recipeCount >= 20,    recipeCount,     20},
            {"Weekly Warrior",   "Weekly_Warrior.png",   completedPlans >= 1,  completedPlans,  1},
            {"Ingr. Master",     "ingredient_master.png",ingredientCount >= 50,ingredientCount, 50},
            {"Eth. Guardian",    "ethical_guardian.png", boycottCount >= 10,   boycottCount,    10},
            {"Legend Crown",     "ethical_legend.png",   totalPoints >= 500,   totalPoints,     500}
        };

        long unlockedCount = 0;
        for (Object[] b : badges) if ((boolean) b[2]) unlockedCount++;

        if (lblProgressText != null)
            lblProgressText.setText(unlockedCount + " / " + badges.length + " badges unlocked");
        if (lblEthicalPoints2 != null)
            lblEthicalPoints2.setText(totalPoints + " pts total");
        if (progressEthical != null)
            progressEthical.setProgress((double) unlockedCount / badges.length);

        // Show 6 badges in 2 rows of 3
        HBox row1 = new HBox(10);
        HBox row2 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        row2.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < Math.min(badges.length, 6); i++) {
            Object[] b = badges[i];
            VBox card = buildMiniCard(
                (String)  b[0],
                (String)  b[1],
                (boolean) b[2],
                (int)     b[3],
                (int)     b[4]
            );
            if (i < 3) row1.getChildren().add(card);
            else       row2.getChildren().add(card);
        }
        badgesContainer.getChildren().addAll(row1, row2);
    }

    private VBox buildMiniCard(String name, String imgName, boolean unlocked, int cur, int target) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(120);
        card.setPadding(new Insets(10));
        card.setStyle(unlocked
            ? "-fx-background-color:#F0FFF4;-fx-background-radius:10;-fx-border-color:#2ECC71;-fx-border-radius:10;-fx-border-width:1.5;"
            : "-fx-background-color:#F8FAFC;-fx-background-radius:10;-fx-border-color:#E2E8F0;-fx-border-radius:10;");

        // Image or emoji
        StackPane imgPane = new StackPane();
        imgPane.setPrefSize(40, 40);
        try {
            var url = getClass().getResource("/images/" + imgName);
            if (url != null) {
                ImageView iv = new ImageView(new Image(url.toExternalForm()));
                iv.setFitWidth(36); iv.setFitHeight(36); iv.setPreserveRatio(true);
                if (!unlocked) iv.setOpacity(0.25);
                imgPane.getChildren().add(iv);
            } else {
                Label fb = new Label(unlocked ? "🏆" : "🔒");
                fb.setStyle("-fx-font-size:20px;");
                imgPane.getChildren().add(fb);
            }
        } catch (Exception e) {
            Label fb = new Label(unlocked ? "🏆" : "🔒");
            fb.setStyle("-fx-font-size:20px;");
            imgPane.getChildren().add(fb);
        }

        Label lbl = new Label(name);
        lbl.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:" + (unlocked ? "#1E293B" : "#94A3B8") + ";");
        lbl.setWrapText(true); lbl.setAlignment(Pos.CENTER);

        Label status = new Label(unlocked ? "✅" : cur + "/" + target);
        status.setStyle("-fx-font-size:9px;-fx-text-fill:" + (unlocked ? "#27AE60" : "#94A3B8") + ";");

        card.getChildren().addAll(imgPane, lbl, status);
        return card;
    }

    // ═══════════════════════════════════
    // WEATHER RECOMMENDATION
    // ═══════════════════════════════════
    private void loadWeatherRecommendation() {
        if (lblWeatherRecipeName != null) lblWeatherRecipeName.setText("Loading...");
        if (lblWeatherDesc != null)       lblWeatherDesc.setText("Fetching weather...");
        if (lblWeatherTemp != null)       lblWeatherTemp.setText("--°C");
        if (lblWeatherIcon != null)       lblWeatherIcon.setText("🌤️");

        Task<WeatherMealSuggestionService.Suggestion> task = new Task<>() {
            @Override
            protected WeatherMealSuggestionService.Suggestion call() {
                return weatherMealSuggestionService.getWeatherBasedSuggestion();
            }
        };

        task.setOnSucceeded(e -> updateWeatherCard(task.getValue()));
        task.setOnFailed(e -> {
            System.err.println("[MainLayout] Weather task failed: " + task.getException());
            if (lblWeatherRecipeName != null) lblWeatherRecipeName.setText("Unavailable");
            if (lblWeatherDesc != null)       lblWeatherDesc.setText("Could not load weather");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void updateWeatherCard(WeatherMealSuggestionService.Suggestion suggestion) {
        if (suggestion == null) return;

        WeatherService.WeatherInfo weather = suggestion.getWeather();
        Recette recipe = suggestion.getRecipe();

        if (lblWeatherIcon != null) lblWeatherIcon.setText(weather.getWeatherEmoji());
        if (lblWeatherCity != null) lblWeatherCity.setText("📍 Tunis");
        if (lblWeatherTemp != null) lblWeatherTemp.setText(weather.getFormattedTemp());
        if (lblWeatherDesc != null) lblWeatherDesc.setText(capitalize(weather.getDescription()));

        if (suggestion.hasRecipe() && recipe != null) {
            currentWeatherRecipe = recipe;
            if (lblWeatherRecipeName != null) lblWeatherRecipeName.setText(recipe.getNom());
            if (lblWeatherRecipeInfo != null)
                lblWeatherRecipeInfo.setText(recipe.getDifficulte() + " • " + recipe.getTempsPreparation() + " min");
            if (lblWeatherReason != null)
                lblWeatherReason.setText("🌡 Suggested based on today's weather");
            if (btnWeatherRecipe != null) btnWeatherRecipe.setDisable(false);
        } else {
            if (lblWeatherRecipeName != null) lblWeatherRecipeName.setText("No recipe found");
            if (lblWeatherRecipeInfo != null) lblWeatherRecipeInfo.setText("Add recipes to get suggestions");
            if (btnWeatherRecipe != null)     btnWeatherRecipe.setDisable(true);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @FXML
    private void handleWeatherRecipe(ActionEvent event) {
        if (currentWeatherRecipe != null) {
            loadPage("/fxml/recipes.fxml");
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
            if (mainScrollPane != null) {
                Platform.runLater(() -> mainScrollPane.setVvalue(0));
            }
            loadHomeStats();
            loadEthicalWidget();
            loadBadgesWidget();
            loadBoycottWidget();
            loadWeatherRecommendation();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Resource not found: " + fxmlPath);
                showPlaceholder("Page not found: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();

            if (contentArea != null) {
                contentArea.getChildren().setAll(page);
                // Make the loaded page fill the available width
                if (page instanceof javafx.scene.layout.Region r) {
                    r.setMaxWidth(Double.MAX_VALUE);
                    javafx.scene.layout.StackPane.setAlignment(r, javafx.geometry.Pos.TOP_LEFT);
                }
            }
            // Scroll back to top
            if (mainScrollPane != null) {
                javafx.application.Platform.runLater(() -> mainScrollPane.setVvalue(0));
            }
        } catch (IOException e) {
            System.err.println("Error loading page: " + fxmlPath + " — " + e.getMessage());
            e.printStackTrace();
            showPlaceholder("Error loading: " + fxmlPath);
        }
    }

    private void showPlaceholder(String text) {
        VBox container = new VBox(16);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #F6FBF7; -fx-padding: 80;");
        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 48px;");
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: #64748B; -fx-font-weight: bold;");
        Label sub = new Label("This page is coming soon.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");
        container.getChildren().addAll(icon, label, sub);
        if (contentArea != null) {
            contentArea.getChildren().setAll(container);
        }
    }

    private void resetButtonStyles() {
        Button[] buttons = {btnHome, btnAbout, btnIngredients, btnRecipes, btnDailyFood, btnBlog, btnComplaints, btnEvents, btnNutrition, btnAssistantIA };
        for (Button b : buttons) {
            if (b != null) b.setStyle(DEFAULT_BUTTON_STYLE);
        }
        if (btnMyKitchen != null) btnMyKitchen.setStyle(DEFAULT_BUTTON_STYLE);
    }
}
