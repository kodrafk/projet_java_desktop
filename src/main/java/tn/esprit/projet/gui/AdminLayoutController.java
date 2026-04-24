package tn.esprit.projet.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.RecetteService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminLayoutController {

    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;
    @FXML private ScrollPane dashboardScrollPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnStatistics;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnEvents;
    @FXML private Button btnSponsors;
    @FXML private Button btnSMS;
    @FXML private Button btnNutrition;
    @FXML private Button btnWellness;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblTotalComplaints;

    @FXML private Label lblDate;
    @FXML private Label lblClock;

    @FXML private TextField searchField;

    // Services
    private IngredientService ingredientService;
    private RecetteService recetteService;

    private static final String DEFAULT_BUTTON_STYLE =
            "-fx-background-color: transparent; " +
                    "-fx-text-fill: #A8C4B8; " +
                    "-fx-font-size: 12.5px; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 0 0 0 14;";

    private static final String ACTIVE_BUTTON_STYLE =
            "-fx-background-color: #2E7D5A; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12.5px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 0 0 0 14;";

    @FXML
    public void initialize() {
        // Initialiser les services
        ingredientService = new IngredientService();
        recetteService = new RecetteService();

        // Charger les statistiques depuis la base
        loadDashboardStats();

        // Démarrer l'horloge en temps réel
        startClock();

        // Configurer la recherche (placeholder)
        setupSearch();

        System.out.println("AdminLayoutController initialized.");
    }

    private void startClock() {
        // Mettre à jour la date
        updateDate();

        // Mettre à jour l'heure chaque seconde
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateDate() {
        if (lblDate != null) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
            lblDate.setText(today.format(formatter));
        }
    }

    private void updateTime() {
        if (lblClock != null) {
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            lblClock.setText(now.format(formatter));
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.setPromptText("Search...");
            // TODO: Implémenter la recherche globale plus tard
            searchField.textProperty().addListener((obs, old, newVal) -> {
                System.out.println("🔍 Recherche: " + newVal);
                // Placeholder pour recherche future
            });
        }
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleDashboard(ActionEvent event) {
        resetSidebarStyles();
        btnDashboard.setStyle(ACTIVE_BUTTON_STYLE);
        showHomePage();
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        resetSidebarStyles();
        if (btnUsers != null) btnUsers.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Users Management");
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        resetSidebarStyles();
        if (btnStatistics != null) btnStatistics.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Statistics");
    }

    @FXML
    private void handleIngredients(ActionEvent event) {
        resetSidebarStyles();
        if (btnIngredients != null) btnIngredients.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/ingredient_management.fxml");
    }

    @FXML
    private void handleRecipes(ActionEvent event) {
        resetSidebarStyles();
        if (btnRecipes != null) btnRecipes.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/recipe_management.fxml");
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        resetSidebarStyles();
        if (btnEvents != null) btnEvents.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Events Management");
    }

    @FXML
    private void handleSponsors(ActionEvent event) {
        resetSidebarStyles();
        if (btnSponsors != null) btnSponsors.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Sponsors Management");
    }

    @FXML
    private void handleSMS(ActionEvent event) {
        resetSidebarStyles();
        if (btnSMS != null) btnSMS.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("SMS Module");
    }

    @FXML
    private void handleNutrition(ActionEvent event) {
        resetSidebarStyles();
        if (btnNutrition != null) btnNutrition.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Nutrition Module");
    }

    @FXML
    private void handleWellness(ActionEvent event) {
        resetSidebarStyles();
        if (btnWellness != null) btnWellness.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Wellness Module");
    }

    @FXML
    private void handleBackToSite(ActionEvent event) {
        System.out.println("Back to Site clicked");
        // TODO: navigate back to main_layout
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout clicked");
        // TODO: Implémenter la déconnexion
    }

    // ==================== UTILITAIRES ====================

    private void resetSidebarStyles() {
        if (btnDashboard != null) btnDashboard.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnUsers != null) btnUsers.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnStatistics != null) btnStatistics.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnIngredients != null) btnIngredients.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnRecipes != null) btnRecipes.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnEvents != null) btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnSponsors != null) btnSponsors.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnSMS != null) btnSMS.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnNutrition != null) btnNutrition.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnWellness != null) btnWellness.setStyle(DEFAULT_BUTTON_STYLE);
    }

    private void showHomePage() {
        if (contentArea != null && dashboardScrollPane != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardScrollPane);
            loadDashboardStats(); // Rafraîchir les stats quand on revient au dashboard
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            System.out.println("📂 Chargement: " + fxmlPath);

            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));

            if (page != null && contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(page);
                System.out.println("✅ Page chargée avec succès!");
            }

        } catch (IOException e) {
            System.err.println("Error loading page: " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Page not found: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showPlaceholder("Error: " + e.getMessage());
        }
    }

    private void showPlaceholder(String pageName) {
        VBox placeholderBox = new VBox(20);
        placeholderBox.setStyle(
                "-fx-alignment: center; " +
                        "-fx-background-color: #F0F4F8; " +
                        "-fx-padding: 100;"
        );

        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label(pageName);
        title.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #1E293B;"
        );

        Label subtitle = new Label("This module is ready for integration.");
        subtitle.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #64748B;"
        );

        Button backBtn = new Button("← Retour au Dashboard");
        backBtn.setStyle(
                "-fx-background-color: #2E7D5A; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10 20; " +
                        "-fx-cursor: hand;"
        );
        backBtn.setOnAction(e -> handleDashboard(null));

        placeholderBox.getChildren().addAll(icon, title, subtitle, backBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholderBox);
    }

    private void loadDashboardStats() {
        // Récupérer les vraies données depuis les services
        int totalIngredients = 0;
        int totalRecipes = 0;

        try {
            totalIngredients = ingredientService.getAll().size();
            totalRecipes = recetteService.countTotal();
        } catch (Exception e) {
            System.err.println("Error loading stats: " + e.getMessage());
            // En cas d'erreur, on garde les valeurs par défaut
        }

        // Mettre à jour les labels
        if (lblTotalUsers != null) {
            // TODO: Remplacer par UserService quand disponible
            lblTotalUsers.setText("128");
        }

        if (lblTotalIngredients != null) {
            lblTotalIngredients.setText(String.valueOf(totalIngredients));
        }

        if (lblTotalRecipes != null) {
            lblTotalRecipes.setText(String.valueOf(totalRecipes));
        }

        if (lblTotalComplaints != null) {
            // TODO: Remplacer par ComplaintService quand disponible
            lblTotalComplaints.setText("12");
        }

        System.out.println("Stats loaded - Ingredients: " + totalIngredients + ", Recipes: " + totalRecipes);
    }
}