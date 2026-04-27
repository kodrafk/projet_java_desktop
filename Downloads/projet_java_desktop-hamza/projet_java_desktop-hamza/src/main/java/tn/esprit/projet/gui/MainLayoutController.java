package tn.esprit.projet.gui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private VBox      homeContent;
    @FXML private VBox      submenuKitchen;

    // Boutons navbar
    @FXML private Button btnHome;
    @FXML private Button btnEvents;
    @FXML private Button btnCalendrier;
    @FXML private Button btnMyKitchen;
    @FXML private Button btnIngredients;
    @FXML private Button btnRecipes;
    @FXML private Button btnBlog;
    @FXML private Button btnComplaints;
    @FXML private Button btnDailyFood;

    // Styles navbar
    private static final String NAV_DEFAULT =
            "-fx-background-color: transparent; -fx-text-fill: #4a7a5a;" +
            "-fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 14;";
    private static final String NAV_ACTIVE =
            "-fx-background-color: #e8f5e9; -fx-text-fill: #1a3a2a;" +
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;" +
            "-fx-background-radius: 6; -fx-padding: 6 14;";

    @FXML
    public void initialize() {
        // Afficher le hero par défaut
        showHomePage();
    }

    // ── Navigation ──────────────────────────────────────

    @FXML private void handleHome(ActionEvent e) {
        resetNav(); if (btnHome != null) btnHome.setStyle(NAV_ACTIVE);
        showHomePage();
    }

    @FXML private void handleEvents(ActionEvent e) {
        resetNav(); if (btnEvents != null) btnEvents.setStyle(NAV_ACTIVE);
        loadPage("/fxml/Front/FrontEvenement.fxml");
    }

    @FXML private void handleCalendrier(ActionEvent e) {
        resetNav(); if (btnCalendrier != null) btnCalendrier.setStyle(NAV_ACTIVE);
        loadPage("/fxml/FrontCalendrier.fxml");
    }

    @FXML private void handleIngredients(ActionEvent e) {
        resetNav(); if (btnMyKitchen != null) btnMyKitchen.setStyle(NAV_ACTIVE);
        showPlaceholder("My Kitchen — Ingrédients");
    }

    @FXML private void handleRecipes(ActionEvent e) {
        resetNav(); if (btnMyKitchen != null) btnMyKitchen.setStyle(NAV_ACTIVE);
        showPlaceholder("My Kitchen — Recettes");
    }

    @FXML private void handleBlog(ActionEvent e) {
        resetNav(); if (btnBlog != null) btnBlog.setStyle(NAV_ACTIVE);
        showPlaceholder("Blog");
    }

    @FXML private void handleComplaints(ActionEvent e) {
        resetNav(); if (btnComplaints != null) btnComplaints.setStyle(NAV_ACTIVE);
        showPlaceholder("Signalements");
    }

    @FXML private void handleDailyFood(ActionEvent e) {
        resetNav(); if (btnDailyFood != null) btnDailyFood.setStyle(NAV_ACTIVE);
        showPlaceholder("Daily Food");
    }

    @FXML private void toggleKitchenMenu(ActionEvent e) {
        if (submenuKitchen != null) {
            boolean v = submenuKitchen.isVisible();
            submenuKitchen.setVisible(!v);
            submenuKitchen.setManaged(!v);
        }
    }

    // ── Basculer vers Admin ──────────────────────────────
    @FXML
    private void allerVersAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin_layout.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1320, 780));
            stage.setTitle("Nutri Coach Pro — Admin Panel");
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Utilitaires avec animations ──────────────────────

    private void showHomePage() {
        animatePageTransition(homeContent);
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            animatePageTransition(page);
        } catch (Exception e) {
            // Construire le message d'erreur complet
            StringBuilder sb = new StringBuilder();
            Throwable t = e;
            while (t != null) {
                sb.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
                t = t.getCause();
            }
            String errorMsg = sb.toString();
            System.err.println("❌ Erreur chargement FXML : " + fxmlPath + "\n" + errorMsg);
            e.printStackTrace();
            showError(errorMsg);
        }
    }

    private void showPlaceholder(String nom) {
        VBox box = new VBox(16);
        box.setStyle("-fx-alignment: center; -fx-background-color: #f0fdf4;");
        
        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 64px;");
        
        Label title = new Label(nom);
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a3a2a;");
        
        Label sub = new Label("Cette section sera bientôt disponible.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #5a8a6a;");
        
        box.getChildren().addAll(icon, title, sub);
        animatePageTransition(box);
    }

    private void showError(String message) {
        VBox box = new VBox(16);
        box.setStyle("-fx-alignment: center; -fx-background-color: #fff5f5; -fx-padding: 40;");
        box.setAlignment(javafx.geometry.Pos.CENTER);

        Label icon = new Label("❌");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Erreur de chargement");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");

        Label detail = new Label(message);
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f1d1d; -fx-wrap-text: true;");
        detail.setMaxWidth(600);
        detail.setWrapText(true);

        Label hint = new Label("💡 Vérifiez que MySQL est démarré et que la base 'nutrilife_db' existe.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #92400e; -fx-font-style: italic;");
        hint.setWrapText(true);
        hint.setMaxWidth(600);

        box.getChildren().addAll(icon, title, detail, hint);
        animatePageTransition(box);
    }

    /**
     * Animation de transition fluide entre les pages
     */
    private void animatePageTransition(Node newContent) {
        // Si le contentArea a déjà du contenu, on fait un fade out
        if (!contentArea.getChildren().isEmpty()) {
            Node oldContent = contentArea.getChildren().get(0);
            
            // Fade out de l'ancien contenu
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(newContent);
                
                // Fade in du nouveau contenu
                newContent.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                
                // Slide in depuis le bas
                TranslateTransition slide = new TranslateTransition(Duration.millis(300), newContent);
                slide.setFromY(20);
                slide.setToY(0);
                
                fadeIn.play();
                slide.play();
            });
            
            fadeOut.play();
        } else {
            // Premier chargement, juste fade in
            contentArea.getChildren().add(newContent);
            newContent.setOpacity(0.0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newContent);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void resetNav() {
        if (btnHome != null)       btnHome.setStyle(NAV_DEFAULT.replace("#4a7a5a", "#2d5a3d").replace("transparent", "transparent") + "-fx-font-weight: bold;");
        if (btnEvents != null)     btnEvents.setStyle(NAV_DEFAULT);
        if (btnCalendrier != null) btnCalendrier.setStyle(NAV_DEFAULT);
        if (btnMyKitchen != null)  btnMyKitchen.setStyle(NAV_DEFAULT);
        if (btnBlog != null)       btnBlog.setStyle(NAV_DEFAULT);
        if (btnComplaints != null) btnComplaints.setStyle(NAV_DEFAULT);
        if (btnDailyFood != null)  btnDailyFood.setStyle(NAV_DEFAULT);
    }
}
