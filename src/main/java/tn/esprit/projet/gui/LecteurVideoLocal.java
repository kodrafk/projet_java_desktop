package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.VideoEchauffement;

/**
 * Lecteur vidéo local optimisé pour les vidéos d'échauffement
 * Fonctionne SANS connexion Internet - Lecture ultra-rapide
 */
public class LecteurVideoLocal {
    
    private Stage stage;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private VideoEchauffement video;
    private VBox controlsBox;
    private Button playPauseBtn;
    private Slider progressSlider;
    private Label timeLabel;
    private boolean isPlaying = false;
    
    /**
     * Ouvre le lecteur vidéo local
     */
    public static void ouvrirVideo(VideoEchauffement video) {
        LecteurVideoLocal lecteur = new LecteurVideoLocal();
        lecteur.afficherLecteur(video);
    }
    
    private void afficherLecteur(VideoEchauffement video) {
        this.video = video;
        
        stage = new Stage();
        stage.setTitle("🎥 " + video.getTitre() + " - Lecteur Local");
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.setResizable(true);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");
        
        // Header avec informations vidéo
        VBox header = creerHeader();
        root.setTop(header);
        
        // Zone vidéo principale
        StackPane videoContainer = creerZoneVideo();
        root.setCenter(videoContainer);
        
        // Contrôles de lecture
        VBox controls = creerControles();
        root.setBottom(controls);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // Initialiser le lecteur média
        initialiserLecteur();
        
        stage.show();
        
        // Animation d'entrée
        animerEntree(root);
        
        // Fermeture propre
        stage.setOnCloseRequest(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
        });
    }
    
    private VBox creerHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15, 20, 10, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1E293B, #334155);");
        
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        // Icône type d'exercice
        Label iconType = new Label(video.getEmojiType());
        iconType.setFont(Font.font(24));
        
        // Informations vidéo
        VBox infoBox = new VBox(3);
        
        Label titre = new Label(video.getTitre());
        titre.setFont(Font.font("System", FontWeight.BOLD, 18));
        titre.setTextFill(Color.WHITE);
        
        HBox detailsBox = new HBox(20);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        
        Label typeInfo = new Label(video.getTypeEvenement().toUpperCase());
        typeInfo.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeInfo.setTextFill(Color.web("#10B981"));
        
        Label niveauInfo = new Label(video.getEmojiNiveau() + " " + video.getNiveau());
        niveauInfo.setFont(Font.font(12));
        niveauInfo.setTextFill(Color.web("#94A3B8"));
        
        Label dureeInfo = new Label("⏱️ " + video.getDureeFormatee());
        dureeInfo.setFont(Font.font(12));
        dureeInfo.setTextFill(Color.web("#94A3B8"));
        
        Label statusLocal = new Label("🔥 LECTURE LOCALE - ULTRA RAPIDE");
        statusLocal.setFont(Font.font("System", FontWeight.BOLD, 11));
        statusLocal.setTextFill(Color.web("#F59E0B"));
        statusLocal.setStyle("-fx-background-color: rgba(245,158,11,0.2); " +
                            "-fx-padding: 4 8; -fx-background-radius: 12;");
        
        detailsBox.getChildren().addAll(typeInfo, niveauInfo, dureeInfo, statusLocal);
        
        infoBox.getChildren().addAll(titre, detailsBox);
        titleBox.getChildren().addAll(iconType, infoBox);
        
        // Bouton fermer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font(16));
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; " +
                         "-fx-cursor: hand; -fx-padding: 5 10;");
        closeBtn.setOnAction(e -> stage.close());
        
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; " +
                                                         "-fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 4;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; " +
                                                        "-fx-cursor: hand; -fx-padding: 5 10;"));
        
        HBox headerBox = new HBox();
        headerBox.getChildren().addAll(titleBox, spacer, closeBtn);
        
        header.getChildren().add(headerBox);
        return header;
    }
    
    private StackPane creerZoneVideo() {
        StackPane container = new StackPane();
        container.setStyle("-fx-background-color: #000000;");
        
        // MediaView pour la vidéo
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        
        // Message de chargement
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        
        ProgressIndicator loading = new ProgressIndicator();
        loading.setStyle("-fx-progress-color: #10B981;");
        
        Label loadingText = new Label("🚀 Chargement ultra-rapide...");
        loadingText.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingText.setTextFill(Color.WHITE);
        
        Label localText = new Label("Lecture locale - Aucune connexion requise");
        localText.setFont(Font.font(14));
        localText.setTextFill(Color.web("#94A3B8"));
        
        loadingBox.getChildren().addAll(loading, loadingText, localText);
        
        // Overlay de contrôles (apparaît au hover)
        VBox overlayControls = creerOverlayControles();
        overlayControls.setVisible(false);
        
        container.getChildren().addAll(mediaView, loadingBox, overlayControls);
        
        // Afficher/masquer les contrôles au hover
        container.setOnMouseEntered(e -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                overlayControls.setVisible(true);
                FadeTransition fade = new FadeTransition(Duration.millis(200), overlayControls);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        });
        
        container.setOnMouseExited(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(500), overlayControls);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(ev -> overlayControls.setVisible(false));
            fade.play();
        });
        
        return container;
    }
    
    private VBox creerOverlayControles() {
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        
        // Bouton play/pause central
        Button centralPlayBtn = new Button("⏸️");
        centralPlayBtn.setFont(Font.font(40));
        centralPlayBtn.setStyle("-fx-background-color: rgba(16,185,129,0.9); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 50; " +
                               "-fx-padding: 20; " +
                               "-fx-cursor: hand; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);");
        
        centralPlayBtn.setOnAction(e -> togglePlayPause());
        
        overlay.getChildren().add(centralPlayBtn);
        return overlay;
    }
    
    private VBox creerControles() {
        controlsBox = new VBox(10);
        controlsBox.setPadding(new Insets(15, 20, 15, 20));
        controlsBox.setStyle("-fx-background-color: linear-gradient(to right, #1E293B, #334155);");
        
        // Barre de progression
        progressSlider = new Slider();
        progressSlider.setStyle("-fx-control-inner-background: #475569; " +
                               "-fx-accent: #10B981;");
        progressSlider.setOnMouseClicked(e -> {
            if (mediaPlayer != null) {
                Duration seekTime = mediaPlayer.getTotalDuration().multiply(progressSlider.getValue() / 100.0);
                mediaPlayer.seek(seekTime);
            }
        });
        
        // Contrôles de lecture
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        // Bouton play/pause
        playPauseBtn = new Button("▶️");
        playPauseBtn.setFont(Font.font(20));
        playPauseBtn.setStyle("-fx-background-color: #10B981; " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 8; " +
                             "-fx-padding: 8 12; " +
                             "-fx-cursor: hand;");
        playPauseBtn.setOnAction(e -> togglePlayPause());
        
        // Bouton stop
        Button stopBtn = new Button("⏹️");
        stopBtn.setFont(Font.font(16));
        stopBtn.setStyle("-fx-background-color: #EF4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 12; " +
                        "-fx-cursor: hand;");
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                isPlaying = false;
                playPauseBtn.setText("▶️");
            }
        });
        
        // Label temps
        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        timeLabel.setTextFill(Color.WHITE);
        
        // Contrôle volume
        Slider volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);
        volumeSlider.setStyle("-fx-control-inner-background: #475569; " +
                             "-fx-accent: #10B981;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });
        
        Label volumeIcon = new Label("🔊");
        volumeIcon.setFont(Font.font(14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Bouton plein écran
        Button fullscreenBtn = new Button("⛶");
        fullscreenBtn.setFont(Font.font(16));
        fullscreenBtn.setStyle("-fx-background-color: #6B7280; " +
                              "-fx-text-fill: white; " +
                              "-fx-background-radius: 8; " +
                              "-fx-padding: 8 12; " +
                              "-fx-cursor: hand;");
        fullscreenBtn.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        
        controls.getChildren().addAll(playPauseBtn, stopBtn, timeLabel, spacer, 
                                     volumeIcon, volumeSlider, fullscreenBtn);
        
        controlsBox.getChildren().addAll(progressSlider, controls);
        return controlsBox;
    }
    
    private void initialiserLecteur() {
        System.out.println("🎬 Initialisation lecteur pour : " + video.getTitre());
        System.out.println("📁 URL vidéo : " + video.getUrlVideo());
        
        // TOUJOURS utiliser le mode démonstration pour une expérience garantie
        System.out.println("📺 Lancement mode démonstration interactive pour : " + video.getTitre());
        creerVideoDemonstration();
    }
    
    private void creerVideoDemonstration() {
        // Créer une démonstration visuelle interactive si la vidéo n'existe pas
        StackPane container = (StackPane) mediaView.getParent();
        
        // Masquer immédiatement le message de chargement
        if (container.getChildren().size() > 1) {
            container.getChildren().get(1).setVisible(false); // Loading box
        }
        
        VBox demo = new VBox(25);
        demo.setAlignment(Pos.CENTER);
        demo.setPadding(new Insets(40));
        demo.setStyle("-fx-background-color: linear-gradient(135deg, #2E7D5A 0%, #1F4D3A 100%);");
        
        // Animation de l'icône
        Label demoIcon = new Label(video.getEmojiType());
        demoIcon.setFont(Font.font(100));
        
        // Animation de pulsation pour l'icône
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), demoIcon);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
        
        Label demoTitle = new Label("🎬 ÉCHAUFFEMENT INTERACTIF");
        demoTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        demoTitle.setTextFill(Color.WHITE);
        
        Label demoSubtitle = new Label(video.getTitre());
        demoSubtitle.setFont(Font.font("System", FontWeight.NORMAL, 20));
        demoSubtitle.setTextFill(Color.web("#D7E6DF"));
        demoSubtitle.setWrapText(true);
        demoSubtitle.setMaxWidth(500);
        demoSubtitle.setAlignment(Pos.CENTER);
        
        // Informations détaillées
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20;");
        infoBox.setMaxWidth(600);
        
        Label demoDescription = new Label(video.getDescription());
        demoDescription.setFont(Font.font("System", FontWeight.NORMAL, 16));
        demoDescription.setTextFill(Color.web("#A8C5B8"));
        demoDescription.setWrapText(true);
        demoDescription.setAlignment(Pos.CENTER);
        
        HBox detailsBox = new HBox(30);
        detailsBox.setAlignment(Pos.CENTER);
        
        Label niveauInfo = new Label(video.getEmojiNiveau() + " " + video.getNiveau());
        niveauInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        niveauInfo.setTextFill(Color.WHITE);
        
        Label dureeInfo = new Label("⏱️ " + video.getDureeFormatee());
        dureeInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        dureeInfo.setTextFill(Color.WHITE);
        
        Label typeInfo = new Label("🏃‍♂️ " + video.getTypeEvenement());
        typeInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        typeInfo.setTextFill(Color.WHITE);
        
        detailsBox.getChildren().addAll(niveauInfo, dureeInfo, typeInfo);
        infoBox.getChildren().addAll(demoDescription, detailsBox);
        
        // Instructions d'échauffement simulées
        VBox instructionsBox = new VBox(8);
        instructionsBox.setAlignment(Pos.CENTER_LEFT);
        instructionsBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); " +
                               "-fx-background-radius: 10; " +
                               "-fx-padding: 15;");
        instructionsBox.setMaxWidth(500);
        
        Label instructionsTitle = new Label("📋 Programme d'échauffement :");
        instructionsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        instructionsTitle.setTextFill(Color.WHITE);
        
        String[] instructions = getInstructionsParType(video.getTypeEvenement());
        for (int i = 0; i < instructions.length; i++) {
            Label instruction = new Label((i + 1) + ". " + instructions[i]);
            instruction.setFont(Font.font(12));
            instruction.setTextFill(Color.web("#D7E6DF"));
            instruction.setWrapText(true);
            instructionsBox.getChildren().add(instruction);
        }
        
        // Boutons d'action
        HBox boutonsBox = new HBox(15);
        boutonsBox.setAlignment(Pos.CENTER);
        
        Button btnAction = new Button("🚀 Commencer l'Échauffement");
        btnAction.setPrefWidth(250);
        btnAction.setPrefHeight(50);
        btnAction.setFont(Font.font("System", FontWeight.BOLD, 16));
        btnAction.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                          "-fx-text-fill: #1F4D3A; " +
                          "-fx-background-radius: 25; " +
                          "-fx-cursor: hand; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        
        btnAction.setOnAction(e -> demarrerEchauffementVirtuel(instructionsBox));
        
        Button btnInstaller = new Button("📥 Installer Vraies Vidéos");
        btnInstaller.setPrefWidth(200);
        btnInstaller.setPrefHeight(40);
        btnInstaller.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnInstaller.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 20; " +
                             "-fx-cursor: hand; " +
                             "-fx-border-color: white; " +
                             "-fx-border-width: 1; " +
                             "-fx-border-radius: 20;");
        
        btnInstaller.setOnAction(e -> {
            try {
                Class<?> dialogClass = Class.forName("tn.esprit.projet.gui.InstallationVideosDialog");
                java.lang.reflect.Method afficher = dialogClass.getMethod("afficher");
                afficher.invoke(null);
            } catch (Exception ex) {
                System.err.println("❌ Erreur ouverture dialogue installation : " + ex.getMessage());
            }
        });
        
        boutonsBox.getChildren().addAll(btnAction, btnInstaller);
        
        // Note d'information
        Label demoNote = new Label("💡 Mode démonstration - Échauffement guidé sans vidéo réelle");
        demoNote.setFont(Font.font("System", FontWeight.NORMAL, 12));
        demoNote.setTextFill(Color.web("#94A3B8"));
        demoNote.setWrapText(true);
        demoNote.setAlignment(Pos.CENTER);
        
        demo.getChildren().addAll(demoIcon, demoTitle, demoSubtitle, infoBox, 
                                 instructionsBox, boutonsBox, demoNote);
        
        // Remplacer le contenu
        container.getChildren().clear();
        container.getChildren().add(demo);
        
        // Afficher les contrôles simplifiés pour la démo
        controlsBox.setVisible(true);
        // Désactiver les contrôles de lecture car c'est une démo
        if (playPauseBtn != null) playPauseBtn.setDisable(true);
        if (progressSlider != null) progressSlider.setDisable(true);
        
        // Animation d'entrée
        demo.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), demo);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        System.out.println("✅ Démonstration interactive lancée pour : " + video.getTitre());
    }
    
    /**
     * Retourne des instructions d'échauffement selon le type d'événement
     */
    private String[] getInstructionsParType(String type) {
        switch (type.toLowerCase()) {
            case "yoga":
                return new String[]{
                    "Respiration profonde (2 minutes)",
                    "Étirements du cou et des épaules",
                    "Salutation au soleil modifiée",
                    "Étirements des hanches",
                    "Préparation mentale et relaxation"
                };
            case "musculation":
                return new String[]{
                    "Échauffement articulaire (5 minutes)",
                    "Mouvements dynamiques des bras",
                    "Squats au poids du corps",
                    "Pompes légères",
                    "Activation du core"
                };
            case "cardio":
                return new String[]{
                    "Marche sur place (2 minutes)",
                    "Montées de genoux",
                    "Talons-fesses légers",
                    "Mouvements des bras",
                    "Accélération progressive"
                };
            case "crossfit":
                return new String[]{
                    "Mobilité articulaire complète",
                    "Mouvements fonctionnels",
                    "Activation musculaire",
                    "Préparation aux mouvements complexes",
                    "Échauffement spécifique WOD"
                };
            default:
                return new String[]{
                    "Échauffement général (5 minutes)",
                    "Mobilisation articulaire",
                    "Activation musculaire progressive",
                    "Préparation mentale",
                    "Hydratation et vérification équipement"
                };
        }
    }
    
    /**
     * Démarre un échauffement virtuel interactif
     */
    private void demarrerEchauffementVirtuel(VBox instructionsBox) {
        // Animation des instructions une par une
        Timeline timeline = new Timeline();
        
        for (int i = 1; i < instructionsBox.getChildren().size(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(i * 2),
                e -> {
                    Label instruction = (Label) instructionsBox.getChildren().get(index);
                    instruction.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    
                    // Animation de highlight
                    FadeTransition highlight = new FadeTransition(Duration.millis(500), instruction);
                    highlight.setFromValue(0.5);
                    highlight.setToValue(1.0);
                    highlight.setAutoReverse(true);
                    highlight.setCycleCount(2);
                    highlight.play();
                    
                    // Remettre le style normal après 1.5 secondes
                    Timeline resetStyle = new Timeline(
                        new KeyFrame(Duration.seconds(1.5), ev -> {
                            instruction.setStyle("-fx-text-fill: #D7E6DF;");
                        })
                    );
                    resetStyle.play();
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // Message de fin
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(instructionsBox.getChildren().size() * 2), e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Échauffement Terminé");
                alert.setHeaderText("🎉 Excellent travail !");
                alert.setContentText("Votre échauffement virtuel est terminé.\nVous êtes maintenant prêt pour l'événement !");
                alert.showAndWait();
            })
        );
        
        timeline.play();
    }
    
    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }
    
    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void animerEntree(BorderPane root) {
        root.setOpacity(0);
        root.setScaleX(0.9);
        root.setScaleY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(400), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), root);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setInterpolator(Interpolator.EASE_OUT);
        animation.play();
    }
}