package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.projet.utils.VideoDownloader;

/**
 * Dialogue d'installation des vidéos d'échauffement
 */
public class InstallationVideosDialog {
    
    private Stage dialog;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Button btnInstaller;
    private Button btnDemo;
    private Button btnAnnuler;
    private VBox progressBox;
    private VBox optionsBox;
    
    public static void afficher() {
        InstallationVideosDialog installDialog = new InstallationVideosDialog();
        installDialog.creerDialog();
    }
    
    private void creerDialog() {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Installation Vidéos");
        
        // Container principal avec effet de transparence
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        // Card principale
        VBox card = new VBox(0);
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);");
        
        // Header avec gradient
        VBox header = creerHeader();
        
        // Corps du dialogue
        VBox body = creerCorps();
        
        // Footer avec boutons
        HBox footer = creerFooter();
        
        card.getChildren().addAll(header, body, footer);
        root.getChildren().add(card);
        
        // Fermeture en cliquant sur le fond
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) {
                dialog.close();
            }
        });
        
        Scene scene = new Scene(root, 600, 500);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        
        // Animation d'ouverture
        playOpenAnimation(card);
        
        dialog.show();
    }
    
    private VBox creerHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 30, 25, 30));
        header.setStyle("-fx-background-color: linear-gradient(135deg, #2E7D5A 0%, #1F4D3A 100%); " +
                       "-fx-background-radius: 20 20 0 0;");
        
        // Icône
        Label icon = new Label("🎥");
        icon.setFont(Font.font(50));
        
        // Titre
        Label titre = new Label("Installation des Vidéos");
        titre.setFont(Font.font("System", FontWeight.BOLD, 24));
        titre.setTextFill(Color.WHITE);
        
        // Sous-titre
        Label sousTitre = new Label("Choisissez comment installer les vidéos d'échauffement");
        sousTitre.setFont(Font.font("System", FontWeight.NORMAL, 14));
        sousTitre.setTextFill(Color.web("#D7E6DF"));
        sousTitre.setWrapText(true);
        sousTitre.setAlignment(Pos.CENTER);
        sousTitre.setMaxWidth(400);
        
        header.getChildren().addAll(icon, titre, sousTitre);
        return header;
    }
    
    private VBox creerCorps() {
        VBox body = new VBox(20);
        body.setPadding(new Insets(30));
        
        // Statut actuel
        VBox statusBox = new VBox(10);
        statusBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label statusTitle = new Label("📊 Statut Actuel");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusTitle.setTextFill(Color.web("#1E293B"));
        
        int nbVideos = VideoDownloader.compterVideos();
        String statusText = nbVideos > 0 
            ? "✅ " + nbVideos + " vidéo" + (nbVideos > 1 ? "s" : "") + " installée" + (nbVideos > 1 ? "s" : "")
            : "❌ Aucune vidéo installée - Mode démonstration actif";
        
        Label statusInfo = new Label(statusText);
        statusInfo.setFont(Font.font(13));
        statusInfo.setTextFill(Color.web("#64748B"));
        
        statusBox.getChildren().addAll(statusTitle, statusInfo);
        
        // Options d'installation
        optionsBox = new VBox(15);
        
        Label optionsTitle = new Label("🚀 Options d'Installation");
        optionsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        optionsTitle.setTextFill(Color.web("#1E293B"));
        
        // Option 1 : Vidéos de démonstration
        VBox option1 = creerOption(
            "🎬", "Vidéos de Démonstration", 
            "Créer des vidéos d'exemple locales (instantané)",
            "#10B981", true
        );
        
        // Option 2 : Téléchargement (désactivé pour l'instant)
        VBox option2 = creerOption(
            "📥", "Télécharger des Vidéos", 
            "Télécharger de vraies vidéos depuis Internet (bientôt disponible)",
            "#94A3B8", false
        );
        
        optionsBox.getChildren().addAll(optionsTitle, option1, option2);
        
        // Barre de progression
        progressBox = new VBox(10);
        progressBox.setVisible(false);
        progressBox.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label progressTitle = new Label("⏳ Installation en cours...");
        progressTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        progressTitle.setTextFill(Color.web("#1E293B"));
        
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #2E7D5A;");
        
        statusLabel = new Label("Préparation...");
        statusLabel.setFont(Font.font(12));
        statusLabel.setTextFill(Color.web("#64748B"));
        
        progressBox.getChildren().addAll(progressTitle, progressBar, statusLabel);
        
        body.getChildren().addAll(statusBox, optionsBox, progressBox);
        return body;
    }
    
    private VBox creerOption(String emoji, String titre, String description, String couleur, boolean enabled) {
        VBox option = new VBox(8);
        option.setStyle("-fx-background-color: white; " +
                       "-fx-border-color: " + couleur + "; " +
                       "-fx-border-width: 2; " +
                       "-fx-border-radius: 12; " +
                       "-fx-background-radius: 12; " +
                       "-fx-padding: 15; " +
                       (enabled ? "-fx-cursor: hand;" : "-fx-opacity: 0.6;"));
        
        HBox headerOption = new HBox(12);
        headerOption.setAlignment(Pos.CENTER_LEFT);
        
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(24));
        
        VBox textBox = new VBox(3);
        
        Label titreLabel = new Label(titre);
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titreLabel.setTextFill(Color.web("#1E293B"));
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(12));
        descLabel.setTextFill(Color.web("#64748B"));
        descLabel.setWrapText(true);
        
        textBox.getChildren().addAll(titreLabel, descLabel);
        headerOption.getChildren().addAll(emojiLabel, textBox);
        
        option.getChildren().add(headerOption);
        
        if (enabled) {
            option.setOnMouseClicked(e -> {
                if (titre.contains("Démonstration")) {
                    installerVideosDemo();
                }
            });
            
            option.setOnMouseEntered(e -> {
                option.setStyle("-fx-background-color: #F8FAFC; " +
                               "-fx-border-color: " + couleur + "; " +
                               "-fx-border-width: 2; " +
                               "-fx-border-radius: 12; " +
                               "-fx-background-radius: 12; " +
                               "-fx-padding: 15; " +
                               "-fx-cursor: hand;");
            });
            
            option.setOnMouseExited(e -> {
                option.setStyle("-fx-background-color: white; " +
                               "-fx-border-color: " + couleur + "; " +
                               "-fx-border-width: 2; " +
                               "-fx-border-radius: 12; " +
                               "-fx-background-radius: 12; " +
                               "-fx-padding: 15; " +
                               "-fx-cursor: hand;");
            });
        }
        
        return option;
    }
    
    private HBox creerFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(0, 30, 30, 30));
        footer.setAlignment(Pos.CENTER);
        
        // Bouton Annuler
        btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefWidth(120);
        btnAnnuler.setPrefHeight(40);
        btnAnnuler.setFont(Font.font("System", FontWeight.BOLD, 13));
        btnAnnuler.setStyle("-fx-background-color: #F1F5F9; " +
                           "-fx-text-fill: #475569; " +
                           "-fx-background-radius: 10; " +
                           "-fx-cursor: hand;");
        
        btnAnnuler.setOnAction(e -> dialog.close());
        
        // Bouton Fermer (apparaît après installation)
        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefWidth(120);
        btnFermer.setPrefHeight(40);
        btnFermer.setFont(Font.font("System", FontWeight.BOLD, 13));
        btnFermer.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                          "-fx-text-fill: white; " +
                          "-fx-background-radius: 10; " +
                          "-fx-cursor: hand;");
        btnFermer.setVisible(false);
        btnFermer.setOnAction(e -> dialog.close());
        
        footer.getChildren().addAll(btnAnnuler, btnFermer);
        return footer;
    }
    
    private void installerVideosDemo() {
        // Afficher la barre de progression
        progressBox.setVisible(true);
        
        // Désactiver les options
        optionsBox.setDisable(true);
        
        // Animation de la barre de progression
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.millis(0), new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.millis(500), e -> statusLabel.setText("Création des dossiers...")),
            new KeyFrame(Duration.millis(1000), new KeyValue(progressBar.progressProperty(), 0.2)),
            new KeyFrame(Duration.millis(1500), e -> statusLabel.setText("Génération des vidéos de démonstration...")),
            new KeyFrame(Duration.millis(2500), new KeyValue(progressBar.progressProperty(), 0.6)),
            new KeyFrame(Duration.millis(3000), e -> statusLabel.setText("Création des thumbnails...")),
            new KeyFrame(Duration.millis(4000), new KeyValue(progressBar.progressProperty(), 0.9)),
            new KeyFrame(Duration.millis(4500), e -> statusLabel.setText("Finalisation...")),
            new KeyFrame(Duration.millis(5000), new KeyValue(progressBar.progressProperty(), 1.0))
        );
        
        timeline.setOnFinished(e -> {
            // Créer les vidéos de démonstration
            VideoDownloader.creerVideosDemo();
            
            // Afficher le succès
            statusLabel.setText("✅ Installation terminée ! " + VideoDownloader.compterVideos() + " vidéos créées.");
            statusLabel.setTextFill(Color.web("#059669"));
            
            // Changer les boutons
            btnAnnuler.setText("Redémarrer App");
            btnAnnuler.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 10; " +
                               "-fx-cursor: hand;");
            
            btnAnnuler.setOnAction(ev -> {
                // Fermer et suggérer de redémarrer
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Installation Terminée");
                alert.setHeaderText("🎉 Vidéos installées avec succès !");
                alert.setContentText("Les vidéos d'échauffement sont maintenant disponibles.\n\n" +
                                   "Conseil : Redémarrez l'application pour une meilleure performance.");
                alert.showAndWait();
                dialog.close();
            });
        });
        
        timeline.play();
    }
    
    private void playOpenAnimation(VBox card) {
        card.setScaleX(0.8);
        card.setScaleY(0.8);
        card.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), card);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setToValue(1.0);
        
        ParallelTransition pt = new ParallelTransition(scale, fade);
        pt.setInterpolator(Interpolator.EASE_OUT);
        pt.play();
    }
}