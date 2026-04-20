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

import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.utils.SessionManager;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminLayoutController {

    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;
    @FXML private ScrollPane dashboardScrollPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnComplaints;
    @FXML private Button btnEvents;
    @FXML private Button btnBlogs;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblTotalComplaints;

    @FXML private Label lblAdminName;
    @FXML private Label lblAdminAvatar;

    @FXML private Label lblDate;
    @FXML private Label lblClock;

    @FXML private TextField searchField;

    // Services
    private IngredientService ingredientService;
    private RecetteService recetteService;
    private UserDAO userDAO;

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

    public void initialize() {
        // Initialiser les services
        ingredientService = new IngredientService();
        recetteService = new RecetteService();
        userDAO = new UserDAO();

        // Charger les statistiques depuis la base
        loadDashboardStats();

        // Démarrer l'horloge en temps réel
        startClock();

        // Configurer la recherche (placeholder)
        setupSearch();

        // Afficher l'utilisateur connecté
        tn.esprit.projet.models.User admin = SessionManager.getCurrentUser();
        if (admin != null) {
            String firstName = admin.getFirstName() != null ? admin.getFirstName() : "Admin";
            String lastName = admin.getLastName() != null ? admin.getLastName() : "";
            if (lblAdminName != null) lblAdminName.setText(firstName + " " + lastName + " 👋");
            if (lblAdminAvatar != null && !firstName.isEmpty()) {
                lblAdminAvatar.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
            }
        }

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
        btnUsers.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/user_list.fxml");
    }

    @FXML
    private void handleIngredients(ActionEvent event) {
        resetSidebarStyles();
        btnIngredients.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/ingredient_management.fxml");
    }

    @FXML
    private void handleRecipes(ActionEvent event) {
        resetSidebarStyles();
        btnRecipes.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/recipe_management.fxml");
    }

    @FXML
    private void handleComplaints(ActionEvent event) {
        resetSidebarStyles();
        btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/complaints_admin.fxml");
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        resetSidebarStyles();
        btnEvents.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Events Management");
    }

    @FXML
    private void handleBlogs(ActionEvent event) {
        resetSidebarStyles();
        btnBlogs.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Blogs Management");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout clicked");
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ==================== UTILITAIRES ====================

    private void resetSidebarStyles() {
        if (btnDashboard != null) btnDashboard.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnUsers != null) btnUsers.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnIngredients != null) btnIngredients.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnRecipes != null) btnRecipes.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnComplaints != null) btnComplaints.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnEvents != null) btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnBlogs != null) btnBlogs.setStyle(DEFAULT_BUTTON_STYLE);
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
            int totalUsers = userDAO.countAll();
            lblTotalUsers.setText(String.valueOf(totalUsers));
        }

        if (lblTotalIngredients != null) {
            lblTotalIngredients.setText(String.valueOf(totalIngredients));
        }

        if (lblTotalRecipes != null) {
            lblTotalRecipes.setText(String.valueOf(totalRecipes));
        }

        if (lblTotalComplaints != null) {
            tn.esprit.projet.services.ComplaintService cs = new tn.esprit.projet.services.ComplaintService();
            lblTotalComplaints.setText(String.valueOf(cs.getAll().size()));
        }

        System.out.println("Stats loaded - Ingredients: " + totalIngredients + ", Recipes: " + totalRecipes);
    }
}