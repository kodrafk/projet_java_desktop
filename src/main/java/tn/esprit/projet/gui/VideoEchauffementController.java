package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.VideoEchauffement;
import tn.esprit.projet.services.VideoLocaleService;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Contrôleur pour l'interface des vidéos d'échauffement
 */
public class VideoEchauffementController {
    
    private final VideoLocaleService videoService = new VideoLocaleService();
    private Stage stage;
    private Evenement evenement;
    private List<VideoEchauffement> videos;
    
    /**
     * Ouvre la fenêtre des vidéos d'échauffement pour un événement
     */
    public static void ouvrirVideosEchauffement(Evenement evenement) {
        VideoEchauffementController controller = new VideoEchauffementController();
        controller.afficherVideos(evenement);
    }
    
    private void afficherVideos(Evenement evenement) {
        this.evenement = evenement;
        
        System.out.println("🎬 Ouverture des vidéos pour l'événement : " + evenement.getNom());
        
        try {
            this.videos = videoService.getVideosParEvenement(evenement);
            System.out.println("📹 Nombre de vidéos récupérées : " + videos.size());
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération vidéos : " + e.getMessage());
            // Créer des vidéos de secours si le service échoue
            this.videos = creerVideosSecours(evenement);
        }
        
        stage = new Stage();
        stage.setTitle("🎥 Vidéos d'Échauffement - " + evenement.getNom());
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setResizable(true);
        
        // Container principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8fafc, #e2e8f0);");
        
        // Header
        VBox header = creerHeader();
        root.setTop(header);
        
        // Contenu principal avec les vidéos
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        VBox contenuVideos = creerContenuVideos();
        scrollPane.setContent(contenuVideos);
        
        root.setCenter(scrollPane);
        
        // Footer avec conseils
        VBox footer = creerFooter();
        root.setBottom(footer);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // Animation d'entrée
        animerEntree(contenuVideos);
    }
    
    private VBox creerHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(30, 30, 20, 30));
        header.setStyle("-fx-background-color: linear-gradient(135deg, #2E7D5A 0%, #1F4D3A 100%);");
        
        // Titre principal
        HBox titreBox = new HBox(15);
        titreBox.setAlignment(Pos.CENTER_LEFT);
        
        Label icone = new Label("🎥");
        icone.setFont(Font.font(40));
        
        VBox titreInfo = new VBox(5);
        Label titre = new Label("Vidéos d'Échauffement");
        titre.setFont(Font.font("System", FontWeight.BOLD, 28));
        titre.setTextFill(Color.WHITE);
        
        Label sousTitre = new Label("Préparez-vous pour : " + evenement.getNom());
        sousTitre.setFont(Font.font("System", FontWeight.NORMAL, 16));
        sousTitre.setTextFill(Color.web("#D7E6DF"));
        
        titreInfo.getChildren().addAll(titre, sousTitre);
        titreBox.getChildren().addAll(icone, titreInfo);
        
        // Informations événement
        HBox infoEvent = new HBox(30);
        infoEvent.setAlignment(Pos.CENTER_LEFT);
        infoEvent.setPadding(new Insets(15, 0, 0, 0));
        
        Label lieuInfo = new Label("📍 " + evenement.getLieu());
        lieuInfo.setTextFill(Color.web("#A8C5B8"));
        lieuInfo.setFont(Font.font(14));
        
        Label coachInfo = new Label("👤 Coach: " + evenement.getCoach_name());
        coachInfo.setTextFill(Color.web("#A8C5B8"));
        coachInfo.setFont(Font.font(14));
        
        Label nbVideos = new Label("🚀 " + videos.size() + " vidéo" + (videos.size() > 1 ? "s" : "") + " locale" + (videos.size() > 1 ? "s" : "") + " - ULTRA RAPIDE");
        nbVideos.setTextFill(Color.web("#A8C5B8"));
        nbVideos.setFont(Font.font(14));
        
        infoEvent.getChildren().addAll(lieuInfo, coachInfo, nbVideos);
        
        header.getChildren().addAll(titreBox, infoEvent);
        return header;
    }
    
    /**
     * Crée des vidéos de secours si le service principal échoue
     */
    private List<VideoEchauffement> creerVideosSecours(Evenement evenement) {
        System.out.println("🆘 Création de vidéos de secours pour : " + evenement.getNom());
        
        List<VideoEchauffement> videosSecours = new ArrayList<>();
        
        // Créer 3 vidéos de démonstration basiques
        videosSecours.add(new VideoEchauffement(
            1, "Échauffement Général", 
            "Échauffement complet pour préparer votre corps à l'activité", 
            "DEMO_VIDEO_GENERAL", 
            createThumbnailSvg("🏃‍♂️", "Général"), 
            480, "général", "débutant", evenement.getId()
        ));
        
        videosSecours.add(new VideoEchauffement(
            2, "Mobilité Articulaire", 
            "Préparation des articulations avant l'effort physique", 
            "DEMO_VIDEO_MOBILITE", 
            createThumbnailSvg("🤸‍♂️", "Mobilité"), 
            360, "mobilité", "débutant", evenement.getId()
        ));
        
        videosSecours.add(new VideoEchauffement(
            3, "Activation Musculaire", 
            "Réveil des muscles pour une performance optimale", 
            "DEMO_VIDEO_ACTIVATION", 
            createThumbnailSvg("💪", "Activation"), 
            420, "activation", "intermédiaire", evenement.getId()
        ));
        
        System.out.println("✅ " + videosSecours.size() + " vidéos de secours créées");
        return videosSecours;
    }
    
    /**
     * Crée une thumbnail SVG de secours
     */
    private String createThumbnailSvg(String emoji, String title) {
        String svg = "<svg width='350' height='200' xmlns='http://www.w3.org/2000/svg'>" +
               "<defs>" +
               "<linearGradient id='grad' x1='0%' y1='0%' x2='100%' y2='100%'>" +
               "<stop offset='0%' style='stop-color:#2E7D5A;stop-opacity:1' />" +
               "<stop offset='100%' style='stop-color:#1F4D3A;stop-opacity:1' />" +
               "</linearGradient>" +
               "</defs>" +
               "<rect width='350' height='200' fill='url(#grad)'/>" +
               "<text x='175' y='80' font-family='Arial' font-size='60' text-anchor='middle' fill='white'>" + emoji + "</text>" +
               "<text x='175' y='130' font-family='Arial' font-size='14' font-weight='bold' text-anchor='middle' fill='white'>" + title.toUpperCase() + "</text>" +
               "<text x='175' y='150' font-family='Arial' font-size='12' text-anchor='middle' fill='#D7E6DF'>VIDÉO D'ÉCHAUFFEMENT</text>" +
               "<text x='175' y='170' font-family='Arial' font-size='10' text-anchor='middle' fill='#9CA3AF'>Mode Secours</text>" +
               "</svg>";
        
        return "data:image/svg+xml;base64," + 
               java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }
    
    private VBox creerContenuVideos() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        
        System.out.println("📋 Création du contenu vidéos - Nombre de vidéos : " + (videos != null ? videos.size() : 0));
        
        // TOUJOURS afficher des vidéos, même si la liste est vide
        if (videos == null || videos.isEmpty()) {
            System.out.println("⚠️ Aucune vidéo du service, création de vidéos par défaut");
            // Créer des vidéos par défaut immédiatement
            videos = creerVideosParDefaut();
        }
        
        // Titre section
        Label titreSection = new Label("🏃‍♂️ Choisissez votre échauffement (" + videos.size() + " vidéos disponibles)");
        titreSection.setFont(Font.font("System", FontWeight.BOLD, 20));
        titreSection.setTextFill(Color.web("#1E293B"));
        container.getChildren().add(titreSection);
        
        // Grille de vidéos
        FlowPane gridVideos = new FlowPane();
        gridVideos.setHgap(20);
        gridVideos.setVgap(20);
        gridVideos.setAlignment(Pos.CENTER_LEFT);
        
        for (VideoEchauffement video : videos) {
            try {
                VBox carteVideo = creerCarteVideo(video);
                gridVideos.getChildren().add(carteVideo);
                System.out.println("📹 Carte créée pour : " + video.getTitre());
            } catch (Exception e) {
                System.err.println("❌ Erreur création carte pour " + video.getTitre() + " : " + e.getMessage());
            }
        }
        
        container.getChildren().add(gridVideos);
        
        return container;
    }
    
    /**
     * Crée des vidéos par défaut qui fonctionnent toujours
     */
    private List<VideoEchauffement> creerVideosParDefaut() {
        System.out.println("🎬 Création de vidéos par défaut garanties");
        
        List<VideoEchauffement> videosDefaut = new ArrayList<>();
        
        // Créer 6 vidéos par défaut avec différents types
        videosDefaut.add(new VideoEchauffement(
            1, "Échauffement Général", 
            "Échauffement complet pour tous types d'activités sportives", 
            "DEMO_GENERAL", 
            createThumbnailSvg("🏃‍♂️", "Général"), 
            480, "général", "débutant", evenement != null ? evenement.getId() : 0
        ));
        
        videosDefaut.add(new VideoEchauffement(
            2, "Échauffement Cardio", 
            "Préparation spéciale pour les activités cardiovasculaires", 
            "DEMO_CARDIO", 
            createThumbnailSvg("❤️", "Cardio"), 
            360, "cardio", "débutant", evenement != null ? evenement.getId() : 0
        ));
        
        videosDefaut.add(new VideoEchauffement(
            3, "Échauffement Musculation", 
            "Préparation articulaire pour la musculation", 
            "DEMO_MUSCULATION", 
            createThumbnailSvg("💪", "Musculation"), 
            600, "musculation", "intermédiaire", evenement != null ? evenement.getId() : 0
        ));
        
        videosDefaut.add(new VideoEchauffement(
            4, "Échauffement Yoga", 
            "Préparation douce pour les séances de yoga", 
            "DEMO_YOGA", 
            createThumbnailSvg("🧘‍♀️", "Yoga"), 
            420, "yoga", "débutant", evenement != null ? evenement.getId() : 0
        ));
        
        videosDefaut.add(new VideoEchauffement(
            5, "Échauffement CrossFit", 
            "Préparation intensive pour le CrossFit", 
            "DEMO_CROSSFIT", 
            createThumbnailSvg("🏋️‍♀️", "CrossFit"), 
            540, "crossfit", "avancé", evenement != null ? evenement.getId() : 0
        ));
        
        videosDefaut.add(new VideoEchauffement(
            6, "Mobilité Articulaire", 
            "Échauffement axé sur la mobilité des articulations", 
            "DEMO_MOBILITE", 
            createThumbnailSvg("🤸‍♂️", "Mobilité"), 
            300, "mobilité", "débutant", evenement != null ? evenement.getId() : 0
        ));
        
        System.out.println("✅ " + videosDefaut.size() + " vidéos par défaut créées");
        return videosDefaut;
    }
    
    /**
     * Crée le message d'installation si aucune vidéo n'est disponible
     */
    private VBox creerMessageInstallation() {
        VBox messageVide = new VBox(20);
        messageVide.setAlignment(Pos.CENTER);
        messageVide.setPadding(new Insets(50));
        
        Label iconeVide = new Label("🎥");
        iconeVide.setFont(Font.font(60));
        
        Label messageTexte = new Label("Aucune vidéo d'échauffement disponible");
        messageTexte.setFont(Font.font("System", FontWeight.BOLD, 18));
        messageTexte.setTextFill(Color.web("#64748B"));
        
        Label conseil = new Label("Installez des vidéos pour profiter de l'échauffement guidé");
        conseil.setFont(Font.font(14));
        conseil.setTextFill(Color.web("#94A3B8"));
        
        // Bouton d'installation
        Button btnInstaller = new Button("🚀 Installer les Vidéos");
        btnInstaller.setPrefWidth(250);
        btnInstaller.setPrefHeight(50);
        btnInstaller.setFont(Font.font("System", FontWeight.BOLD, 16));
        btnInstaller.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 12; " +
                             "-fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.4), 10, 0, 0, 3);");
        
        btnInstaller.setOnAction(e -> {
            try {
                Class<?> dialogClass = Class.forName("tn.esprit.projet.gui.InstallationVideosDialog");
                java.lang.reflect.Method afficher = dialogClass.getMethod("afficher");
                afficher.invoke(null);
            } catch (Exception ex) {
                System.err.println("❌ Erreur ouverture dialogue installation : " + ex.getMessage());
            }
        });
        
        // Effet hover
        btnInstaller.setOnMouseEntered(e -> {
            btnInstaller.setStyle("-fx-background-color: linear-gradient(to right, #1F4D3A, #163828); " +
                                 "-fx-text-fill: white; " +
                                 "-fx-background-radius: 12; " +
                                 "-fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.6), 15, 0, 0, 5);");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnInstaller);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        btnInstaller.setOnMouseExited(e -> {
            btnInstaller.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                                 "-fx-text-fill: white; " +
                                 "-fx-background-radius: 12; " +
                                 "-fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.4), 10, 0, 0, 3);");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnInstaller);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        messageVide.getChildren().addAll(iconeVide, messageTexte, conseil, btnInstaller);
        return messageVide;
    }
    
    private VBox creerCarteVideo(VideoEchauffement video) {
        VBox carte = new VBox(0);
        carte.setPrefWidth(350);
        carte.setStyle("-fx-background-color: white; " +
                      "-fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4); " +
                      "-fx-cursor: hand;");
        
        // Thumbnail avec overlay play
        StackPane thumbnailContainer = new StackPane();
        thumbnailContainer.setPrefHeight(200);
        
        // Image de fond
        ImageView thumbnail = new ImageView();
        try {
            thumbnail.setImage(new Image(video.getThumbnail(), true));
        } catch (Exception e) {
            // Image par défaut si erreur
            thumbnail.setImage(new Image("https://via.placeholder.com/350x200/2E7D5A/FFFFFF?text=Video"));
        }
        thumbnail.setFitWidth(350);
        thumbnail.setFitHeight(200);
        thumbnail.setPreserveRatio(false);
        thumbnail.setStyle("-fx-background-radius: 16 16 0 0;");
        
        // Overlay avec bouton play
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 16 16 0 0;");
        overlay.setPrefSize(350, 200);
        
        Label playButton = new Label("▶");
        playButton.setFont(Font.font(40));
        playButton.setTextFill(Color.WHITE);
        playButton.setStyle("-fx-background-color: rgba(46,125,90,0.9); " +
                           "-fx-background-radius: 50; " +
                           "-fx-padding: 15 20; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);");
        
        // Durée en overlay
        Label dureeLabel = new Label(video.getDureeFormatee());
        dureeLabel.setTextFill(Color.WHITE);
        dureeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        dureeLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7); " +
                           "-fx-background-radius: 4; " +
                           "-fx-padding: 4 8;");
        StackPane.setAlignment(dureeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(dureeLabel, new Insets(10));
        
        overlay.getChildren().add(playButton);
        thumbnailContainer.getChildren().addAll(thumbnail, overlay, dureeLabel);
        
        // Contenu de la carte
        VBox contenu = new VBox(12);
        contenu.setPadding(new Insets(20));
        
        // Titre et badges
        HBox titreBox = new HBox(10);
        titreBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titre = new Label(video.getTitre());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web("#1E293B"));
        titre.setWrapText(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Badge niveau
        Label badgeNiveau = new Label(video.getEmojiNiveau() + " " + video.getNiveau());
        badgeNiveau.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; " +
                            "-fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        titreBox.getChildren().addAll(titre, spacer, badgeNiveau);
        
        // Description
        Label description = new Label(video.getDescription());
        description.setFont(Font.font(13));
        description.setTextFill(Color.web("#64748B"));
        description.setWrapText(true);
        description.setMaxHeight(40);
        
        // Informations supplémentaires
        HBox infos = new HBox(15);
        infos.setAlignment(Pos.CENTER_LEFT);
        
        Label typeInfo = new Label(video.getEmojiType() + " " + video.getTypeEvenement());
        typeInfo.setFont(Font.font(12));
        typeInfo.setTextFill(Color.web("#94A3B8"));
        
        Label dureeInfo = new Label("⏱️ " + video.getDureeFormatee());
        dureeInfo.setFont(Font.font(12));
        dureeInfo.setTextFill(Color.web("#94A3B8"));
        
        infos.getChildren().addAll(typeInfo, dureeInfo);
        
        // Bouton regarder
        Button btnRegarder = new Button("🎬 Regarder la vidéo");
        btnRegarder.setMaxWidth(Double.MAX_VALUE);
        btnRegarder.setPrefHeight(40);
        btnRegarder.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 8; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;");
        
        // Clic sur la carte pour ouvrir la vidéo
        carte.setOnMouseClicked(e -> {
            System.out.println("🎬 Clic sur vidéo : " + video.getTitre());
            LecteurVideoLocal.ouvrirVideo(video);
        });
        
        // Clic sur le bouton pour ouvrir la vidéo
        btnRegarder.setOnAction(e -> {
            System.out.println("🎬 Bouton regarder cliqué : " + video.getTitre());
            LecteurVideoLocal.ouvrirVideo(video);
        });
        
        
        // Effets hover
        carte.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), carte);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        
        carte.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), carte);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        contenu.getChildren().addAll(titreBox, description, infos, btnRegarder);
        carte.getChildren().addAll(thumbnailContainer, contenu);
        
        return carte;
    }
    
    private VBox creerFooter() {
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(20, 30, 30, 30));
        footer.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");
        
        Label titreConseils = new Label("💡 Conseils d'Échauffement");
        titreConseils.setFont(Font.font("System", FontWeight.BOLD, 16));
        titreConseils.setTextFill(Color.web("#1E293B"));
        
        VBox conseils = new VBox(8);
        String[] listeConseils = {
            "• Commencez toujours par un échauffement de 5-10 minutes minimum",
            "• Adaptez l'intensité de l'échauffement à votre niveau",
            "• Hydratez-vous avant, pendant et après l'échauffement",
            "• Écoutez votre corps et arrêtez en cas de douleur",
            "• N'hésitez pas à demander conseil à votre coach"
        };
        
        for (String conseil : listeConseils) {
            Label lblConseil = new Label(conseil);
            lblConseil.setFont(Font.font(13));
            lblConseil.setTextFill(Color.web("#64748B"));
            conseils.getChildren().add(lblConseil);
        }
        
        footer.getChildren().addAll(titreConseils, conseils);
        return footer;
    }
    
    private void ouvrirVideo(VideoEchauffement video) {
        Stage videoStage = new Stage();
        videoStage.setTitle("🎥 " + video.getTitre());
        videoStage.setWidth(900);
        videoStage.setHeight(600);
        
        BorderPane root = new BorderPane();
        
        // Header avec infos vidéo
        VBox headerVideo = new VBox(10);
        headerVideo.setPadding(new Insets(15));
        headerVideo.setStyle("-fx-background-color: #1E293B;");
        
        Label titreVideo = new Label(video.getTitre());
        titreVideo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titreVideo.setTextFill(Color.WHITE);
        
        Label infoVideo = new Label(video.getEmojiType() + " " + video.getTypeEvenement() + 
                                   " • " + video.getEmojiNiveau() + " " + video.getNiveau() + 
                                   " • ⏱️ " + video.getDureeFormatee());
        infoVideo.setFont(Font.font(14));
        infoVideo.setTextFill(Color.web("#94A3B8"));
        
        headerVideo.getChildren().addAll(titreVideo, infoVideo);
        root.setTop(headerVideo);
        
        // Lecteur vidéo
        WebView webView = new WebView();
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(video.getUrlVideo());
        
        root.setCenter(webView);
        
        Scene scene = new Scene(root);
        videoStage.setScene(scene);
        videoStage.show();
    }
    
    private void animerEntree(VBox contenu) {
        contenu.setOpacity(0);
        contenu.setTranslateY(30);
        
        FadeTransition fade = new FadeTransition(Duration.millis(600), contenu);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), contenu);
        slide.setFromY(30);
        slide.setToY(0);
        
        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setInterpolator(Interpolator.EASE_OUT);
        animation.play();
    }
}