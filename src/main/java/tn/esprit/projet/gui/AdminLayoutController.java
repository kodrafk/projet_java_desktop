package tn.esprit.projet.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.services.ComplaintService;
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
    @FXML private Button btnAnomalyDetection;
    @FXML private Button btnGroups;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblTotalComplaints;
    @FXML private Label lblAdminEmail;
    @FXML private Label lblAdminAvatar;
    @FXML private Label lblDate;
    @FXML private Label lblClock;
    @FXML private TextField searchField;

    private IngredientService ingredientService;
    private RecetteService recetteService;
    private ComplaintService complaintService;

    private static final String DEFAULT_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #A8C4B8; -fx-font-size: 12.5px; " +
            "-fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";
    private static final String ACTIVE_STYLE =
            "-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 12.5px; -fx-font-weight: bold; " +
            "-fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";

    @FXML
    public void initialize() {
        ingredientService = new IngredientService();
        recetteService    = new RecetteService();
        complaintService  = new ComplaintService();

        loadDashboardStats();
        startClock();

        // Set admin avatar
        var user = SessionManager.getCurrentUser();
        if (lblAdminEmail != null && user != null) lblAdminEmail.setText(user.getEmail() != null ? user.getEmail() : "Admin");
        if (lblAdminAvatar != null && user != null) {
            String name = user.getEmail() != null ? user.getEmail() : "A";
            lblAdminAvatar.setText(name.substring(0, 1).toUpperCase());
        }
    }

    private void startClock() {
        updateDate();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateDate() {
        if (lblDate != null) lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)));
    }

    private void updateTime() {
        if (lblClock != null) lblClock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboard(ActionEvent e)  { reset(); set(btnDashboard); showHomePage(); }
    @FXML private void handleUsers(ActionEvent e)      { reset(); set(btnUsers);      loadPage("/fxml/admin_user_list.fxml"); }
    @FXML private void handleStatistics(ActionEvent e) { reset(); set(btnStatistics); loadPage("/fxml/statistics.fxml"); }
    @FXML private void handleIngredients(ActionEvent e){ reset(); set(btnIngredients);loadPage("/fxml/ingredient_management.fxml"); }
    @FXML private void handleRecipes(ActionEvent e)    { reset(); set(btnRecipes);    loadPage("/fxml/recipe_management.fxml"); }
    @FXML private void handleEvents(ActionEvent e)     { reset(); set(btnEvents);     showPlaceholder("Events Management"); }
    @FXML private void handleSponsors(ActionEvent e)   { reset(); set(btnSponsors);   showPlaceholder("Sponsors Management"); }
    @FXML private void handleSMS(ActionEvent e)        { reset(); set(btnSMS);        showPlaceholder("SMS Module"); }
    @FXML private void handleNutrition(ActionEvent e)  { reset(); set(btnNutrition);  showPlaceholder("Nutrition Module"); }
    @FXML private void handleBlogs(ActionEvent e)      { reset(); set(btnBlogs);      showPlaceholder("Blogs Management"); }
    @FXML private void handleObjectives(ActionEvent e) { reset(); set(btnObjectives); loadPage("/fxml/admin_objectives.fxml"); }
    @FXML private void handleComplaints(ActionEvent e) { reset(); set(btnComplaints); loadPage("/fxml/complaints_admin.fxml"); }
    @FXML private void handleAnomalyDetection(ActionEvent e) { reset(); set(btnAnomalyDetection); loadPage("/fxml/admin_anomaly_dashboard.fxml"); }
    @FXML private void handleGroups(ActionEvent e)     { reset(); set(btnGroups);     loadPage("/fxml/admin_groups.fxml"); }

    @FXML private void handleLogout(ActionEvent e) {
        SessionManager.logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(login));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML private void handleBackToSite(ActionEvent e) {
        try {
            Parent main = FXMLLoader.load(getClass().getResource("/fxml/main_layout.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(main, 1280, 760));
            stage.setTitle("NutriLife");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML private void handleAdminProfile(MouseEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/user_profile.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("My Profile");
            stage.setScene(new Scene(root, 620, 700));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void reset() {
        for (Button b : new Button[]{btnDashboard, btnUsers, btnStatistics, btnIngredients, btnRecipes,
                btnEvents, btnSponsors, btnSMS, btnNutrition, btnBlogs, btnObjectives, btnComplaints,
                btnAnomalyDetection, btnGroups})
            if (b != null) b.setStyle(DEFAULT_STYLE);
    }

    private void set(Button b) { if (b != null) b.setStyle(ACTIVE_STYLE); }

    private void showHomePage() {
        if (contentArea != null && dashboardScrollPane != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardScrollPane);
            loadDashboardStats();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) { showPlaceholder("Page not found: " + fxmlPath); return; }
            Parent page = FXMLLoader.load(resource);
            if (contentArea != null) { contentArea.getChildren().clear(); contentArea.getChildren().add(page); }
        } catch (IOException e) {
            System.err.println("Error loading: " + fxmlPath + " — " + e.getMessage());
            showPlaceholder("Error: " + e.getMessage());
        }
    }

    private void showPlaceholder(String pageName) {
        VBox box = new VBox(20);
        box.setStyle("-fx-alignment: center; -fx-background-color: #F0F4F8; -fx-padding: 100;");
        Label icon = new Label("🚧"); icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label(pageName); title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("This module is ready for integration."); sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        Button back = new Button("← Back to Dashboard");
        back.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        back.setOnAction(ev -> handleDashboard(null));
        box.getChildren().addAll(icon, title, sub, back);
        if (contentArea != null) { contentArea.getChildren().clear(); contentArea.getChildren().add(box); }
    }

    private void loadDashboardStats() {
        try {
            if (lblTotalIngredients != null) lblTotalIngredients.setText(String.valueOf(ingredientService.getAll().size()));
            if (lblTotalRecipes != null)     lblTotalRecipes.setText(String.valueOf(recetteService.countTotal()));
            if (lblTotalComplaints != null)  lblTotalComplaints.setText(String.valueOf(complaintService.getAll().size()));
        } catch (Exception e) { System.err.println("loadDashboardStats: " + e.getMessage()); }
    }
}
