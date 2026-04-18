package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.IngredientService;

import java.io.IOException;
import java.util.List;

public class MainLayoutController {

    // ═══════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════
    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;
    @FXML private VBox submenuKitchen;

    @FXML private Button btnHome;
    @FXML private Button btnDailyFood;
    @FXML private MenuButton btnMyKitchen;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnBlog;
    @FXML private Button btnComplaints;
    @FXML private Button btnEvents;

    // ═══════════════════════════════════
    // STATS CARDS
    // ═══════════════════════════════════
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblExpiredItems;
    @FXML private Label lblEthicalPoints;
    @FXML private Label lblEthicalLevel;

    // ═══════════════════════════════════
    // PROGRESS SUMMARY
    // ═══════════════════════════════════
    @FXML private Label lblTotalIngredients2;
    @FXML private Label lblTotalRecipes2;
    @FXML private Label lblExpiredItems2;
    @FXML private Label lblEthicalPoints2;

    // ═══════════════════════════════════
    // ETHICAL POINTS WIDGET
    // ═══════════════════════════════════
    @FXML private Label lblWidgetLevel;
    @FXML private Label lblWidgetPoints;
    @FXML private ProgressBar progressEthical;
    @FXML private Label lblProgressText;
    @FXML private Label badge1;
    @FXML private Label badge2;
    @FXML private Label badge3;
    @FXML private Label badge4;


    // ═══════════════════════════════════
    // BOYCOTT CHECKER WIDGET
    // ═══════════════════════════════════
    @FXML private TextField txtBoycottSearch;
    @FXML private VBox boycottResultsBox;
    @FXML private Label lblBoycottTotal;

    @FXML private VBox badgeBox1;
    @FXML private VBox badgeBox2;
    @FXML private VBox badgeBox3;
    @FXML private VBox badgeBox4;

    // ═══════════════════════════════════
    // SERVICES
    // ═══════════════════════════════════
    private IngredientService ingredientService;
    private BoycottService boycottService;

    // ═══════════════════════════════════
    // STYLES
    // ═══════════════════════════════════
    private static final String DEFAULT_BUTTON_STYLE =
            "-fx-background-color: transparent; " +
                    "-fx-text-fill: #D7E6DF; " +
                    "-fx-font-size: 12px; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand;";

    private static final String ACTIVE_BUTTON_STYLE =
            "-fx-background-color: #2E7D5A; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12px; " +
                    "-fx-background-radius: 8; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;";

    // ═══════════════════════════════════
    // INITIALIZE
    // ═══════════════════════════════════
    @FXML
    public void initialize() {
        ingredientService = new IngredientService();
        boycottService    = new BoycottService();

        loadHomeStats();
        loadEthicalWidget();
        loadBoycottWidget();
    }

    // ═══════════════════════════════════
    // NAVIGATION HANDLERS
    // ═══════════════════════════════════
    @FXML
    private void handleHome(ActionEvent event) {
        resetButtonStyles();
        btnHome.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        showHomePage();
    }

    @FXML
    private void handleDailyFood(ActionEvent event) {
        resetButtonStyles();
        btnDailyFood.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        showPlaceholder("Daily Food Page");
    }

    @FXML
    private void toggleKitchenMenu(ActionEvent event) {
        boolean isVisible = submenuKitchen.isVisible();
        submenuKitchen.setVisible(!isVisible);
        submenuKitchen.setManaged(!isVisible);
    }

    @FXML
    private void handleIngredients(ActionEvent event) {
        resetButtonStyles();
        btnMyKitchen.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        loadPage("/fxml/ingredients.fxml");
    }

    @FXML
    private void handleRecipes(ActionEvent event) {
        resetButtonStyles();
        btnMyKitchen.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        loadPage("/fxml/recipes.fxml");
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        resetButtonStyles();
        btnBlog.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        showPlaceholder("Blog Page");
    }

    @FXML
    private void handleComplaints(ActionEvent event) {
        resetButtonStyles();
        btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        showPlaceholder("Complaints Page");
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        resetButtonStyles();
        btnEvents.setStyle(ACTIVE_BUTTON_STYLE);
        submenuKitchen.setVisible(false);
        submenuKitchen.setManaged(false);
        showPlaceholder("Events Page");
    }

    // ═══════════════════════════════════
    // STATS
    // ═══════════════════════════════════
    private void loadHomeStats() {
        try {
            int totalIngredients = ingredientService.getAll().size();
            int totalRecipes     = 0; // TODO: connecter RecetteService
            int expiredItems     = 0; // TODO: connecter expiry logic

            // Stats cards
            if (lblTotalIngredients != null)
                lblTotalIngredients.setText(String.valueOf(totalIngredients));
            if (lblTotalRecipes != null)
                lblTotalRecipes.setText(String.valueOf(totalRecipes));
            if (lblExpiredItems != null)
                lblExpiredItems.setText(String.valueOf(expiredItems));



        } catch (Exception e) {
            System.err.println("loadHomeStats error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════
    // ETHICAL POINTS WIDGET
    // ═══════════════════════════════════
    private void loadEthicalWidget() {
        try {
            int total    = EthicalPointsManager.getTotalPoints();
            String level = EthicalPointsManager.getLevelName();
            int progress = EthicalPointsManager.getProgressToNextLevel();

            if (lblEthicalPoints != null)
                lblEthicalPoints.setText(String.valueOf(total));
            if (lblEthicalLevel != null)
                lblEthicalLevel.setText(level);
            if (lblWidgetLevel != null)
                lblWidgetLevel.setText(level);
            if (lblWidgetPoints != null)
                lblWidgetPoints.setText(total + " pts");
            if (progressEthical != null)
                progressEthical.setProgress(progress / 100.0);
            if (lblProgressText != null)
                lblProgressText.setText(progress + "% to next level");
            if (lblEthicalPoints2 != null)
                lblEthicalPoints2.setText(total + " pts total");

            // Badge 1 : First Scan (5+ pts)
            updateBadge(badgeBox1, badge1, total >= 5);
            // Badge 2 : Boycott Hero (15+ pts)
            updateBadge(badgeBox2, badge2, total >= 15);
            // Badge 3 : Eco Warrior (25+ pts)
            updateBadge(badgeBox3, badge3, total >= 25);
            // Badge 4 : Health Expert (50+ pts)
            updateBadge(badgeBox4, badge4, total >= 50);

        } catch (Exception e) {
            System.err.println("loadEthicalWidget error: " + e.getMessage());
        }
    }

    private void updateBadge(VBox badgeBox, Label badgeLabel, boolean unlocked) {
        if (badgeBox == null || badgeLabel == null) return;

        if (unlocked) {
            badgeBox.setStyle(
                    "-fx-background-color: #F0FFF4;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #2ECC71;" +
                            "-fx-border-radius: 14;" +
                            "-fx-border-width: 2;" +
                            "-fx-padding: 14;"
            );
            badgeLabel.setText("✅ Unlocked");
            badgeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #27AE60; -fx-font-weight: bold;");
        } else {
            badgeBox.setStyle(
                    "-fx-background-color: #F8FAFC;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-radius: 14;" +
                            "-fx-padding: 14;"
            );
            badgeLabel.setText("🔒 Locked");
            badgeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8;");
        }
    }

    // ═══════════════════════════════════
    // BOYCOTT CHECKER WIDGET
    // ═══════════════════════════════════
    private void loadBoycottWidget() {
        try {
            int total = boycottService.getTotalBoycottedBrands();

            if (lblBoycottTotal != null)
                lblBoycottTotal.setText(total + " brands in database");


        } catch (Exception e) {
            System.err.println("loadBoycottWidget error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBoycottSearch() {
        if (txtBoycottSearch == null || boycottResultsBox == null) return;

        String keyword = txtBoycottSearch.getText().trim();
        if (keyword.isEmpty()) return;

        List<BoycottBrand> results = boycottService.searchByName(keyword);
        boycottResultsBox.getChildren().clear();

        if (results.isEmpty()) {
            // CLEAN
            VBox cleanBox = new VBox(8);
            cleanBox.setAlignment(Pos.CENTER);
            cleanBox.setStyle("-fx-padding: 20;");

            Label icon = new Label("🟢");
            icon.setStyle("-fx-font-size: 30px;");

            Label msg = new Label("\"" + keyword + "\" is NOT boycotted");
            msg.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #27AE60;"
            );

            Label sub = new Label("This brand has no reported ties to occupation");
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
            sub.setWrapText(true);

            cleanBox.getChildren().addAll(icon, msg, sub);
            boycottResultsBox.getChildren().add(cleanBox);

        } else {
            // BOYCOTTED
            for (BoycottBrand brand : results) {
                VBox card = new VBox(6);
                card.setStyle(
                        "-fx-background-color: #FFF0F0;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 12;" +
                                "-fx-border-color: #FECACA;" +
                                "-fx-border-radius: 8;"
                );

                Label nameLabel = new Label("🔴 " + brand.getBrandName());
                nameLabel.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-text-fill: #E74C3C;"
                );

                Label reasonLabel = new Label("Reason: " + brand.getReason());
                reasonLabel.setStyle(
                        "-fx-font-size: 11px;" +
                                "-fx-text-fill: #64748B;"
                );
                reasonLabel.setWrapText(true);

                Label catLabel = new Label("Category: " + brand.getCategory());
                catLabel.setStyle(
                        "-fx-font-size: 11px;" +
                                "-fx-text-fill: #94A3B8;"
                );

                Label altLabel = new Label("💡 " + brand.getAlternatives());
                altLabel.setStyle(
                        "-fx-font-size: 11px;" +
                                "-fx-text-fill: #27AE60;"
                );
                altLabel.setWrapText(true);

                card.getChildren().addAll(nameLabel, reasonLabel, catLabel, altLabel);
                boycottResultsBox.getChildren().add(card);
            }
        }
    }

    // ═══════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════
    private void showHomePage() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeContent);
        loadHomeStats();
        loadEthicalWidget();
        loadBoycottWidget();
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
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException e) {
            System.err.println("Error loading: " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Error loading page");
        }
    }

    private void showPlaceholder(String pageName) {
        Label placeholder = new Label(pageName + " - Coming Soon");
        placeholder.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-text-fill: #475569;" +
                        "-fx-font-weight: bold;"
        );
        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholder);
    }

    private void resetButtonStyles() {
        btnHome.setStyle(DEFAULT_BUTTON_STYLE);
        btnDailyFood.setStyle(DEFAULT_BUTTON_STYLE);
        btnMyKitchen.setStyle(DEFAULT_BUTTON_STYLE);
        btnBlog.setStyle(DEFAULT_BUTTON_STYLE);
        btnComplaints.setStyle(DEFAULT_BUTTON_STYLE);
        btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
    }
}