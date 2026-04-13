package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
    @FXML private Button btnObjectives;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblTotalComplaints;

    @FXML private Label lblDate;
    @FXML private Label lblClock;

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
        showPlaceholder("Users Management");
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
    private void handleComplaints(ActionEvent event) {
        resetSidebarStyles();
        btnComplaints.setStyle(ACTIVE_BUTTON_STYLE);
        showPlaceholder("Complaints Management");
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
        btnObjectives.setStyle(ACTIVE_BUTTON_STYLE);
        loadPage("/fxml/admin_objectives.fxml");
    }

    private void resetSidebarStyles() {
        btnDashboard.setStyle(DEFAULT_BUTTON_STYLE);
        btnUsers.setStyle(DEFAULT_BUTTON_STYLE);
        btnIngredients.setStyle(DEFAULT_BUTTON_STYLE);
        btnRecipes.setStyle(DEFAULT_BUTTON_STYLE);
        btnComplaints.setStyle(DEFAULT_BUTTON_STYLE);
        btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
        btnBlogs.setStyle(DEFAULT_BUTTON_STYLE);
        if (btnObjectives != null) btnObjectives.setStyle(DEFAULT_BUTTON_STYLE);
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
        lblTotalUsers.setText("128");
        lblTotalIngredients.setText("54");
        lblTotalRecipes.setText("31");
        lblTotalComplaints.setText("12");

        if (lblDate != null) {
            lblDate.setText("April 5, 2026");
        }

        if (lblClock != null) {
            lblClock.setText("15:23:02");
        }
    }
}