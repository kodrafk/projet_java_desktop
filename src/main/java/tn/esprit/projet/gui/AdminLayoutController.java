package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminLayoutController {

    @FXML private StackPane  contentArea;
    @FXML private VBox       homeContent;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private Label      lblPageTitle;
    @FXML private Label      lblPageSub;
    @FXML private Label      lblAdminName;
    @FXML private Label      lblAdminEmail;
    @FXML private Label      lblAdminAvatar;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnStatistics;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnComplaints;
    @FXML private Button btnEvents;
    @FXML private Button btnBlogs;

    // Dashboard stat labels
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblInactiveUsers;
    @FXML private Label lblAdmins;
    @FXML private Label lblNewThisMonth;

    private final UserDAO dao = new UserDAO();

    private static final String DEFAULT_BTN =
            "-fx-background-color: transparent; -fx-text-fill: #A8C4B8; -fx-font-size: 12.5px;" +
            "-fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";
    private static final String ACTIVE_BTN =
            "-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 12.5px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";

    @FXML
    public void initialize() {
        User admin = SessionManager.getCurrentUser();
        if (admin != null) {
            if (lblAdminName   != null) lblAdminName.setText("Hello, " + admin.getFirstName() + " 👋");
            if (lblAdminEmail  != null) lblAdminEmail.setText(admin.getEmail());
            if (lblAdminAvatar != null && admin.getFirstName() != null)
                lblAdminAvatar.setText(String.valueOf(admin.getFirstName().charAt(0)).toUpperCase());
        }
        loadDashboardStats();
    }

    // ── Sidebar navigation ─────────────────────────────────────────────────────
    @FXML private void handleDashboard(ActionEvent e) {
        activate(btnDashboard, "Admin Dashboard", "Monitor and manage your platform");
        contentArea.getChildren().setAll(dashboardScrollPane);
        loadDashboardStats();
    }

    @FXML private void handleUsers(ActionEvent e) {
        activate(btnUsers, "User Management", "View, add, edit and delete users");
        loadPage("/fxml/user_list.fxml");
    }

    @FXML private void handleStatistics(ActionEvent e) {
        activate(btnStatistics, "Statistics", "Platform statistics overview");
        loadPage("/fxml/statistics.fxml");
    }

    @FXML private void handleAddUser(ActionEvent e) {
        activate(btnUsers, "User Management", "View, add, edit and delete users");
        loadPage("/fxml/user_list.fxml");
        // The user_list page has an Add User button
    }

    @FXML private void handleIngredients(ActionEvent e) {
        activate(btnIngredients, "Ingredients", "Manage ingredients");
        showPlaceholder("Ingredients Management");
    }

    @FXML private void handleRecipes(ActionEvent e) {
        activate(btnRecipes, "Recipes", "Manage recipes");
        showPlaceholder("Recipes Management");
    }

    @FXML private void handleComplaints(ActionEvent e) {
        activate(btnComplaints, "Complaints", "Manage complaints");
        showPlaceholder("Complaints Management");
    }

    @FXML private void handleEvents(ActionEvent e) {
        activate(btnEvents, "Events", "Manage events");
        showPlaceholder("Events Management");
    }

    @FXML private void handleBlogs(ActionEvent e) {
        activate(btnBlogs, "Blogs", "Manage blog posts");
        showPlaceholder("Blogs Management");
    }

    @FXML private void handleLogout(ActionEvent e) {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("NutriLife - Login");
            stage.setMaximized(false);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void activate(Button active, String title, String sub) {
        for (Button b : new Button[]{btnDashboard, btnUsers, btnStatistics, btnIngredients,
                btnRecipes, btnComplaints, btnEvents, btnBlogs})
            if (b != null) b.setStyle(DEFAULT_BTN);
        if (active != null) active.setStyle(ACTIVE_BTN);
        if (lblPageTitle != null) lblPageTitle.setText(title);
        if (lblPageSub   != null) lblPageSub.setText(sub);
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            showPlaceholder("Page not found");
        }
    }

    private void showPlaceholder(String name) {
        VBox box = new VBox(10);
        box.setStyle("-fx-alignment: center; -fx-background-color: #F0F4F8;");
        Label t = new Label(name);
        t.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label s = new Label("This module is ready for integration.");
        s.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        box.getChildren().addAll(t, s);
        contentArea.getChildren().setAll(box);
    }

    private void loadDashboardStats() {
        int total    = dao.countAll();
        int active   = dao.countActive();
        int inactive = total - active;
        int admins   = dao.countByRole("ROLE_ADMIN");
        int newMonth = dao.countRegisteredThisMonth();

        set(lblTotalUsers,    String.valueOf(total));
        set(lblActiveUsers,   String.valueOf(active));
        set(lblInactiveUsers, String.valueOf(inactive));
        set(lblAdmins,        String.valueOf(admins));
        set(lblNewThisMonth,  String.valueOf(newMonth));
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val); }
}
