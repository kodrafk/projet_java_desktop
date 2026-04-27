package tn.esprit.projet.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.RecetteService;
import tn.esprit.projet.utils.SessionManager;

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
    @FXML private Button btnBlogs;
    @FXML private Button btnObjectives;
    @FXML private Button btnComplaints;
    @FXML private Button btnEmotionAnalysis;
    @FXML private Button btnAssistantIA;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblInactiveUsers;
    @FXML private Label lblAdmins;
    @FXML private Label lblNewThisMonth;
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblTotalComplaints;
    @FXML private Label lblPageTitle;
    @FXML private Label lblAdminEmail;
    @FXML private Label lblAdminAvatar;

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
            searchField.textProperty().addListener((obs, old, newVal) -> {
                System.out.println("🔍 Recherche: " + newVal);
            });
        }
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleDashboard(ActionEvent event) {
        resetSidebarStyles();
        if (btnDashboard != null) btnDashboard.setStyle(ACTIVE_BUTTON_STYLE);
        showHomePage();
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        resetSidebarStyles();
        if (btnUsers != null) btnUsers.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/user_list.fxml");
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        resetSidebarStyles();
        if (btnStatistics != null) btnStatistics.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/statistics.fxml");
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
        loadPage("/fxml/AdminEvenement.fxml");
    }

    @FXML
    private void handleSponsors(ActionEvent event) {
        resetSidebarStyles();
        if (btnSponsors != null) btnSponsors.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/AdminSponsor.fxml");
    }

    @FXML
    public void handleAssistantIA(ActionEvent event) {
        tn.esprit.projet.utils.IADialogUtil.ouvrirAssistantIA();
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
    private void handleBlogs(ActionEvent event) {
        resetSidebarStyles();
        if (btnBlogs != null) btnBlogs.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Blogs Management");
    }

    @FXML
    private void handleObjectives(ActionEvent event) {
        resetSidebarStyles();
        if (btnObjectives != null) btnObjectives.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/admin_objectives.fxml");
    }

    @FXML
    private void handleComplaints(ActionEvent event) {
        resetSidebarStyles();
        if (btnComplaints != null) btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/complaints_admin.fxml");
    }

    @FXML
    private void handleEmotionAnalysis(ActionEvent event) {
        resetSidebarStyles();
        if (btnEmotionAnalysis != null) btnEmotionAnalysis.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/complaints_emotion_analysis.fxml");
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        loadPage("/fxml/user_form.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout clicked");
        SessionManager.getInstance().logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(login));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToSite(ActionEvent event) {
        try {
            Parent main = FXMLLoader.load(getClass().getResource("/fxml/main_layout.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(main, 1280, 760));
            stage.setTitle("NutriLife");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("My Profile");
            stage.setScene(new Scene(root, 620, 700));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (btnBlogs != null) btnBlogs.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnObjectives != null) btnObjectives.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnComplaints != null) btnComplaints.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnEmotionAnalysis != null) btnEmotionAnalysis.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnAssistantIA != null) btnAssistantIA.setStyle(DEFAULT_BUTTON_STYLE);
    }

    private void showHomePage() {
        if (contentArea != null && dashboardScrollPane != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardScrollPane);
            loadDashboardStats(); // Rafraîchir les stats quand on revient au dashboard
        }
    }

    public void loadPageInContent(String fxmlPath, Object controllerReference) {
        try {
            System.out.println("📂 Chargement enfant: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            
            // Si le contrôleur a besoin d'une référence au parent
            Object controller = loader.getController();
            if (controller instanceof AdminEvenementController) {
                ((AdminEvenementController) controller).setParentController(this);
            }
            // Ajoutez d'autres vérifications si nécessaire

            if (page != null && contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(page);
                System.out.println("✅ Page enfant chargée!");
            }
        } catch (Exception e) {
            System.err.println("Error loadPageInContent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            System.out.println("📂 Chargement: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Injecter le parent controller si possible
            Object controller = loader.getController();
            if (controller instanceof AdminEvenementController) {
                ((AdminEvenementController) controller).setParentController(this);
            }

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
        int totalIngredients = 0;
        int totalRecipes = 0;

        try {
            totalIngredients = ingredientService.getAll().size();
            totalRecipes = recetteService != null ? recetteService.countTotal() : 0;
        } catch (Exception e) {
            System.err.println("Error loading stats: " + e.getMessage());
        }

        if (lblTotalUsers != null) lblTotalUsers.setText("128");
        if (lblActiveUsers != null) lblActiveUsers.setText("104");
        if (lblInactiveUsers != null) lblInactiveUsers.setText("24");
        if (lblAdmins != null) lblAdmins.setText("3");
        if (lblNewThisMonth != null) lblNewThisMonth.setText("12");

        if (lblTotalIngredients != null) {
            lblTotalIngredients.setText(String.valueOf(totalIngredients));
        }
        if (lblTotalRecipes != null) {
            lblTotalRecipes.setText(String.valueOf(totalRecipes));
        }
        if (lblTotalComplaints != null) {
            lblTotalComplaints.setText("12");
        }

        if (lblAdminEmail != null) {
            var user = SessionManager.getInstance().getCurrentUser();
            lblAdminEmail.setText(user != null ? user.getEmail() : "admin");
        }
        if (lblAdminAvatar != null) {
            var user = SessionManager.getInstance().getCurrentUser();
            String name = user != null ? user.getEmail() : "A";
            lblAdminAvatar.setText(name.substring(0, 1).toUpperCase());
        }

        System.out.println("Stats loaded - Ingredients: " + totalIngredients + ", Recipes: " + totalRecipes);
    }
}
