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
import javafx.stage.Stage;
import tn.esprit.projet.utils.SessionManager;

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
    @FXML private Button btnBlogs;
    @FXML private Button btnObjectives;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblInactiveUsers;
    @FXML private Label lblAdmins;
    @FXML private Label lblNewThisMonth;
    @FXML private Label lblPageTitle;
    @FXML private Label lblAdminEmail;
    @FXML private Label lblAdminAvatar;

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
        loadDashboardStats();
    }

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
        showPlaceholder("Ingredients Management");
    }

    @FXML
    private void handleRecipes(ActionEvent event) {
        resetSidebarStyles();
        btnRecipes.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Recipes Management");
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
    private void handleObjectives(ActionEvent event) {
        resetSidebarStyles();
        if (btnObjectives != null) btnObjectives.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/admin_objectives.fxml");
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        resetSidebarStyles();
        if (btnStatistics != null) btnStatistics.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/statistics.fxml");
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        loadPage("/fxml/user_form.fxml");
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

    private void resetSidebarStyles() {
        if (btnDashboard   != null) btnDashboard.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnUsers       != null) btnUsers.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnStatistics  != null) btnStatistics.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnIngredients != null) btnIngredients.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnRecipes     != null) btnRecipes.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnEvents      != null) btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnBlogs       != null) btnBlogs.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnObjectives  != null) btnObjectives.setStyle(DEFAULT_BUTTON_STYLE);
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (Exception e) {
            System.err.println("Error loading: " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Page not found");
        }
    }

    private void showHomePage() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardScrollPane);
        loadDashboardStats();
    }

    private void showPlaceholder(String pageName) {
        VBox placeholderBox = new VBox(12);
        placeholderBox.setStyle(
                "-fx-alignment: center; " +
                        "-fx-background-color: #F0F4F8;"
        );

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

        placeholderBox.getChildren().addAll(title, subtitle);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholderBox);
    }

    private void loadDashboardStats() {
        if (lblTotalUsers    != null) lblTotalUsers.setText("128");
        if (lblActiveUsers   != null) lblActiveUsers.setText("104");
        if (lblInactiveUsers != null) lblInactiveUsers.setText("24");
        if (lblAdmins        != null) lblAdmins.setText("3");
        if (lblNewThisMonth  != null) lblNewThisMonth.setText("12");

        if (lblAdminEmail != null) {
            var user = SessionManager.getCurrentUser();
            lblAdminEmail.setText(user != null ? user.getEmail() : "admin");
        }
        if (lblAdminAvatar != null) {
            var user = SessionManager.getCurrentUser();
            String name = user != null ? user.getEmail() : "A";
            lblAdminAvatar.setText(name.substring(0, 1).toUpperCase());
        }
    }
}
