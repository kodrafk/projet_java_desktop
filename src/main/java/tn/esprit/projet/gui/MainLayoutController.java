package tn.esprit.projet.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.projet.services.IngredientService;

import java.io.IOException;

public class MainLayoutController {

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

    @FXML private Label lblTotalIngredients;
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblExpiredItems;

    @FXML private Label lblTotalIngredients2;
    @FXML private Label lblTotalRecipes2;
    @FXML private Label lblExpiredItems2;

    private IngredientService ingredientService;

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
    @FXML
    public void initialize() {
        ingredientService = new IngredientService();
        loadHomeStats();
    }

    // ========== NAVIGATION ==========

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

        // TODO: remplacer par le vrai FXML plus tard
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

    // ========== UTILITAIRES ==========

    private void resetButtonStyles() {
        btnHome.setStyle(DEFAULT_BUTTON_STYLE);
        btnDailyFood.setStyle(DEFAULT_BUTTON_STYLE);
        btnMyKitchen.setStyle(DEFAULT_BUTTON_STYLE);
        btnBlog.setStyle(DEFAULT_BUTTON_STYLE);
        btnComplaints.setStyle(DEFAULT_BUTTON_STYLE);
        btnEvents.setStyle(DEFAULT_BUTTON_STYLE);
    }

    private void showHomePage() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeContent);
        loadHomeStats();
    }

    private void loadPage(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Resource not found: " + fxmlPath);
                showPlaceholder("Page not found (404)");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la page : " + fxmlPath);
            e.printStackTrace();
            showPlaceholder("Error loading page");
        }
    }

    private void showPlaceholder(String pageName) {
        Label placeholder = new Label(pageName + " - Coming Soon");
        placeholder.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-font-weight: bold;"
        );
        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholder);
    }

    private void loadHomeStats() {
        int totalIngredients = ingredientService.getAll().size();

        lblTotalIngredients.setText(String.valueOf(totalIngredients));
        if (lblTotalIngredients2 != null) {
            lblTotalIngredients2.setText(String.valueOf(totalIngredients));
        }

        // TODO: remplacer par les vraies données quand les services seront prêts
        int totalRecipes = 0;
        int expiredItems = 0;

        lblTotalRecipes.setText(String.valueOf(totalRecipes));
        lblExpiredItems.setText(String.valueOf(expiredItems));

        if (lblTotalRecipes2 != null) {
            lblTotalRecipes2.setText(String.valueOf(totalRecipes));
        }

        if (lblExpiredItems2 != null) {
            lblExpiredItems2.setText(String.valueOf(expiredItems));
        }
    }
}