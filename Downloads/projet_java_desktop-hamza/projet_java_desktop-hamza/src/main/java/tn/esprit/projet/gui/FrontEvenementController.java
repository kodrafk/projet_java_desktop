package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.models.Paiement;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.services.EmailServicePro;
import tn.esprit.projet.services.PaiementService;
import tn.esprit.projet.services.AssistantIAService;
import tn.esprit.projet.services.MeteoService;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur FRONT — intégré dans main_layout.fxml (contentArea).
 * Style cohérent avec le dashboard : fond clair, cartes blanches avec animations.
 */
public class FrontEvenementController {

    @FXML private FlowPane flowPane;
    @FXML private TextField tfRecherche;
    @FXML private Label     lblCompteur;

    private EvenementService eventService   = null;
    private SponsorService   sponsorService = null;
    private EmailServicePro  emailService   = null;
    private PaiementService  paiementService = null;
    private final MeteoService meteoService = new MeteoService();

    private EvenementService getEventService() {
        if (eventService == null) {
            try { eventService = new EvenementService(); }
            catch (Exception e) { System.err.println("⚠️ EvenementService : " + e.getMessage()); }
        }
        return eventService;
    }

    private SponsorService getSponsorService() {
        if (sponsorService == null) {
            try { sponsorService = new SponsorService(); }
            catch (Exception e) { System.err.println("⚠️ SponsorService : " + e.getMessage()); }
        }
        return sponsorService;
    }

    private EmailServicePro getEmailService() {
        if (emailService == null) {
            try { emailService = new EmailServicePro(); }
            catch (Exception e) { System.err.println("⚠️ EmailService : " + e.getMessage()); }
        }
        return emailService;
    }

    private PaiementService getPaiementService() {
        if (paiementService == null) {
            try { paiementService = new PaiementService(); }
            catch (Exception e) { System.err.println("⚠️ PaiementService : " + e.getMessage()); }
        }
        return paiementService;
    }

    @FXML
    public void initialize() {
        try {
            refreshCards(null);
        } catch (Exception e) {
            System.err.println("❌ ERREUR initialize() FrontEvenementController : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Recherche ──────────────────────────────────────────
    @FXML
    public void handleRecherche() {
        String f = tfRecherche != null ? tfRecherche.getText().trim().toLowerCase() : "";
        refreshCards(f.isEmpty() ? null : f);
    }

    // ── Rafraîchissement des cards avec animation ─────────
    public void refreshCards(String filtre) {
        if (flowPane == null) return;
        flowPane.getChildren().clear();

        List<Evenement> liste = getEventService() != null ? getEventService().getAll() : new java.util.ArrayList<>();
        int count = 0;
        int delay = 0;

        for (Evenement ev : liste) {
            if (filtre == null
                    || ev.getNom().toLowerCase().contains(filtre)
                    || ev.getLieu().toLowerCase().contains(filtre)
                    || ev.getCoach_name().toLowerCase().contains(filtre)) {
                
                VBox card = createCard(ev);
                flowPane.getChildren().add(card);
                
                // Animation d'apparition en cascade
                animateCardEntrance(card, delay);
                delay += 80; // Délai entre chaque carte
                
                count++;
            }
        }

        if (lblCompteur != null) {
            lblCompteur.setText(count + " événement" + (count > 1 ? "s" : ""));
            animateCounter();
        }
    }

    /**
     * Animation d'entrée pour les cartes
     */
    private void animateCardEntrance(VBox card, int delayMs) {
        card.setOpacity(0);
        card.setTranslateY(30);
        
        PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
        pause.setOnFinished(e -> {
            // Fade in
            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            
            // Slide up
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
            slide.setFromY(30);
            slide.setToY(0);
            
            ParallelTransition parallel = new ParallelTransition(fade, slide);
            parallel.play();
        });
        pause.play();
    }

    /**
     * Animation du compteur
     */
    private void animateCounter() {
        if (lblCompteur == null) return;
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), lblCompteur);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    // ── Création d'une card avec effets hover et image ──────
    private VBox createCard(Evenement ev) {
        VBox card = new VBox(0);
        card.setPrefWidth(360);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #D6A46D;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1.2;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);" +
            "-fx-cursor: hand;"
        );

        // Effet hover avec animation
        addHoverEffect(card);

        // ── Image de l'événement ──
        StackPane imageContainer = createImageContainer(ev);

        // ── Corps ──
        VBox body = new VBox(10);
        body.setPadding(new Insets(16, 18, 16, 18));

        // Badge statut avec animation
        HBox badgeRow = new HBox(8);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(ev.getStatut() != null ? ev.getStatut().toUpperCase() : "ACTIF");
        badge.setStyle(
            "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20; -fx-padding: 3 10;"
        );
        addPulseEffect(badge);

        // Badge prix
        Label badgePrix = new Label(ev.getPrix() > 0
            ? String.format("💰 %.2f TND", ev.getPrix()) : "🎁 GRATUIT");
        badgePrix.setStyle(
            "-fx-background-color: " + (ev.getPrix() > 0 ? "#FEF3C7" : "#DCFCE7") + ";" +
            "-fx-text-fill: " + (ev.getPrix() > 0 ? "#92400E" : "#166534") + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20; -fx-padding: 4 12;"
        );
        badgeRow.getChildren().addAll(badge, badgePrix);

        // Titre
        Label title = new Label(ev.getNom());
        title.setFont(Font.font("System", FontWeight.BOLD, 17));
        title.setTextFill(Color.web("#1E293B"));
        title.setWrapText(true);

        // Séparateur léger
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #F1F5F9;");

        // Infos — afficher le lieu propre (sans préfixe [OUTDOOR])
        Label coach = buildInfoLabel("👤", ev.getCoach_name());
        Label lieu  = buildInfoLabel("📍", MeteoService.getLieuPropre(ev.getLieu()));
        Label date  = buildInfoLabel("🗓",
                ev.getDate_debut().toLocalDate().toString()
                + "  ·  "
                + ev.getDate_debut().toLocalTime().toString().substring(0, 5));

        // Description
        String descText = ev.getDescription() != null && !ev.getDescription().isEmpty()
                ? ev.getDescription() : "Aucune description disponible.";
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.setMaxHeight(52);
        desc.setStyle("-fx-font-style: italic; -fx-text-fill: #64748B; -fx-font-size: 12px;");

        // ── Section Sponsors ──
        VBox sponsorBox = buildSponsorSection(ev.getId());

        // ── Barre de capacité ──
        VBox capaciteBox = buildCapaciteBar(ev);

        // ── Séparateur ──
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #E2E8F0;");

        // ── Boutons d'action ──
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER);
        
        // Bouton Ouvrir dans HERE Maps
        Button btnMap = new Button("🗺️ Voir sur la carte");
        btnMap.setPrefHeight(42);
        btnMap.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnMap, Priority.ALWAYS);
        btnMap.setFont(Font.font("System", FontWeight.BOLD, 12));
        btnMap.setStyle(
            "-fx-background-color: #00AFAA;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btnMap.setOnMouseEntered(e -> btnMap.setStyle("-fx-background-color: #008C88; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnMap.setOnMouseExited(e -> btnMap.setStyle("-fx-background-color: #00AFAA; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnMap.setOnAction(e -> ouvrirGoogleMapsDansNavigateur(ev));
        
        // Bouton Participer (désactivé si complet)
        Button btnParticiper = createAnimatedButton(ev);
        HBox.setHgrow(btnParticiper, Priority.ALWAYS);
        
        actionButtons.getChildren().addAll(btnMap, btnParticiper);

        body.getChildren().addAll(
            badgeRow, title, sep1,
            coach, lieu, date, desc,
            capaciteBox,
            sponsorBox, sep2,
            actionButtons
        );
        card.getChildren().addAll(imageContainer, body);
        return card;
    }

    /**
     * Crée la barre de capacité visuelle pour une carte événement
     */
    private VBox buildCapaciteBar(Evenement ev) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(4, 0, 4, 0));

        int capacite = ev.getCapacite();
        int inscrits = ev.getNbParticipants();

        // ── Ligne info participants ──
        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label lblInscrits = new Label("👥 " + inscrits + " inscrit" + (inscrits > 1 ? "s" : ""));
        lblInscrits.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        HBox.setHgrow(lblInscrits, Priority.ALWAYS);

        // Badge capacité
        Label lblBadge;
        if (capacite <= 0) {
            // Illimité
            lblBadge = new Label("♾️ Illimité");
            lblBadge.setStyle(
                "-fx-background-color: #EFF6FF; -fx-text-fill: #3B82F6;" +
                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-padding: 2 8;"
            );
        } else if (ev.estComplet()) {
            lblBadge = new Label("🔴 COMPLET");
            lblBadge.setStyle(
                "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;" +
                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-padding: 2 8;"
            );
        } else {
            int restantes = ev.getPlacesRestantes();
            if (restantes <= 5) {
                lblBadge = new Label("⚠️ " + restantes + " place" + (restantes > 1 ? "s" : "") + " restante" + (restantes > 1 ? "s" : ""));
                lblBadge.setStyle(
                    "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 2 8;"
                );
            } else {
                lblBadge = new Label("✅ " + restantes + " / " + capacite + " places");
                lblBadge.setStyle(
                    "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 2 8;"
                );
            }
        }

        infoRow.getChildren().addAll(lblInscrits, lblBadge);
        box.getChildren().add(infoRow);

        // ── Barre de progression ──
        StackPane barreContainer = new StackPane();
        barreContainer.setPrefHeight(10);
        barreContainer.setMaxWidth(Double.MAX_VALUE);
        barreContainer.setStyle(
            "-fx-background-color: #E2E8F0;" +
            "-fx-background-radius: 5;"
        );

        Pane barreFill = new Pane();
        barreFill.setPrefHeight(10);
        barreFill.setStyle("-fx-background-radius: 5;");

        // Calculer le taux et la couleur
        double taux;
        String couleur;

        if (capacite <= 0) {
            // Illimité : barre bleue proportionnelle aux inscrits (max visuel = 100)
            taux = inscrits > 0 ? Math.min((double) inscrits / 100.0, 1.0) : 0.0;
            couleur = "#3B82F6"; // bleu
        } else {
            taux = Math.min((double) inscrits / capacite, 1.0);
            if (taux >= 1.0)      couleur = "#DC2626"; // rouge — complet
            else if (taux >= 0.8) couleur = "#F59E0B"; // orange — presque plein
            else if (taux >= 0.5) couleur = "#3B82F6"; // bleu — à moitié
            else                  couleur = "#10B981"; // vert — beaucoup de places
        }

        barreFill.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 5;");
        barreContainer.getChildren().add(barreFill);

        // Animation de la barre au chargement
        final double tauxFinal = taux;
        barreContainer.widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 0 && barreFill.getPrefWidth() == 0) {
                double targetWidth = newW.doubleValue() * tauxFinal;
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                        new javafx.animation.KeyValue(barreFill.prefWidthProperty(), 0)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(900),
                        new javafx.animation.KeyValue(barreFill.prefWidthProperty(), targetWidth,
                            javafx.animation.Interpolator.EASE_OUT))
                );
                timeline.play();
            }
        });

        box.getChildren().add(barreContainer);
        return box;
    }

    /**
     * Crée le conteneur d'image pour la carte
     */
    private StackPane createImageContainer(Evenement ev) {
        StackPane container = new StackPane();
        container.setPrefHeight(180);
        container.setMaxHeight(180);
        container.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1F4D3A, #2E7D5A);" +
            "-fx-background-radius: 14 14 0 0;"
        );

        String imageUrl = ev.getImage();
        javafx.scene.image.Image image = null;
        
        // Essayer de charger l'image
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            image = AdminEvenementController.chargerImage(imageUrl);
            if (image == null || image.isError()) {
                System.out.println("⚠️ Image non trouvée pour : " + ev.getNom() + " (URL: " + imageUrl + ")");
            }
        }
        
        // Si l'image n'est pas trouvée, essayer une image par défaut
        if (image == null || image.isError()) {
            image = AdminEvenementController.chargerImage("default.jpg");
        }

        if (image != null && !image.isError()) {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setFitWidth(360);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);

            // Clip pour arrondir les coins supérieurs
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(360, 180);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            imageView.setClip(clip);

            // Overlay gradient
            Pane overlay = new Pane();
            overlay.setPrefSize(360, 180);
            overlay.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.05), rgba(0,0,0,0.25));");

            container.getChildren().addAll(imageView, overlay);
        } else {
            // Placeholder amélioré avec icône et texte
            VBox placeholder = new VBox(10);
            placeholder.setAlignment(javafx.geometry.Pos.CENTER);
            
            Label icon = new Label("📸");
            icon.setFont(Font.font(48));
            icon.setTextFill(Color.web("#FFFFFF", 0.8));
            
            Label text = new Label("Image non disponible");
            text.setFont(Font.font("System", 12));
            text.setTextFill(Color.web("#FFFFFF", 0.6));
            
            placeholder.getChildren().addAll(icon, text);
            container.getChildren().add(placeholder);
            
            System.out.println("⚠️ Affichage du placeholder pour : " + ev.getNom());
        }

        return container;
    }

    /**
     * Ajoute un effet hover à la carte
     */
    private void addHoverEffect(VBox card) {
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.rgb(0, 0, 0, 0.15));
        hoverShadow.setRadius(16);
        hoverShadow.setOffsetY(6);

        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();
            
            card.setEffect(hoverShadow);
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            
            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.rgb(0, 0, 0, 0.06));
            normalShadow.setRadius(8);
            normalShadow.setOffsetY(3);
            card.setEffect(normalShadow);
        });
    }

    /**
     * Ajoute un effet de pulsation au badge
     */
    private void addPulseEffect(Label badge) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), badge);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    /**
     * Crée un bouton avec animations — désactivé si événement complet
     */
    private Button createAnimatedButton(Evenement ev) {
        boolean complet = ev.estComplet();

        String label = complet ? "🔴 COMPLET"
            : ev.getPrix() > 0 ? String.format("💳 PAYER %.2f TND", ev.getPrix())
            : "🎁 PARTICIPER GRATUITEMENT";

        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setFont(Font.font("System", FontWeight.BOLD, 13));

        if (complet) {
            btn.setStyle(
                "-fx-background-color: #E2E8F0; -fx-text-fill: #94A3B8;" +
                "-fx-background-radius: 10; -fx-cursor: default;"
            );
            btn.setDisable(true);
        } else {
            String colorNormal = ev.getPrix() > 0 ? "#1F4D3A" : "#2E7D5A";
            String colorHover  = ev.getPrix() > 0 ? "#2E7D5A" : "#3A9D6F";
            btn.setStyle("-fx-background-color: " + colorNormal + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: " + colorHover + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
                ScaleTransition s = new ScaleTransition(Duration.millis(150), btn);
                s.setToX(1.02); s.setToY(1.02); s.play();
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: " + colorNormal + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
                ScaleTransition s = new ScaleTransition(Duration.millis(150), btn);
                s.setToX(1.0); s.setToY(1.0); s.play();
            });
            btn.setOnAction(e -> ouvrirFormulairePaiement(ev));
        }
        return btn;
    }

    // ── Label info avec icône ──────────────────────────────
    private Label buildInfoLabel(String icon, String text) {
        Label l = new Label(icon + "  " + text);
        l.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        return l;
    }

    // ── Section sponsors avec vidéos et logos ──────────────────────────────────
    private VBox buildSponsorSection(int evenementId) {
        VBox box = new VBox(12);
        List<Sponsor> sponsors = getSponsorService() != null ? getSponsorService().getSponsorsByEvenement(evenementId) : new java.util.ArrayList<>();
        if (sponsors.isEmpty()) return box;

        Label header = new Label("🤝  Sponsors & Partenaires");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F4D3A; -fx-font-size: 13px;");
        box.getChildren().add(header);

        // Grille de logos sponsors (design professionnel)
        FlowPane logosPane = new FlowPane(8, 8);
        logosPane.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-padding: 12;");
        
        for (Sponsor s : sponsors) {
            VBox sponsorCard = creerCarteSponsor(s);
            logosPane.getChildren().add(sponsorCard);
        }
        box.getChildren().add(logosPane);
        
        // Bouton pour voir toutes les vidéos
        if (sponsors.stream().anyMatch(s -> s.getVideo_url() != null && !s.getVideo_url().isEmpty())) {
            Button btnVideos = new Button("🎬 Voir les vidéos des sponsors");
            btnVideos.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;" +
                "-fx-cursor: hand;"
            );
            btnVideos.setOnMouseEntered(e -> btnVideos.setStyle(
                "-fx-background-color: #b91c1c;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;" +
                "-fx-cursor: hand;"
            ));
            btnVideos.setOnMouseExited(e -> btnVideos.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;" +
                "-fx-cursor: hand;"
            ));
            btnVideos.setOnAction(e -> ouvrirGalerieVideos(sponsors));
            box.getChildren().add(btnVideos);
        }
        
        return box;
    }
    
    /**
     * Crée une carte sponsor professionnelle avec logo
     */
    private VBox creerCarteSponsor(Sponsor sponsor) {
        VBox card = new VBox(6);
        card.setPrefWidth(80);
        card.setPrefHeight(90);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);"
        );
        
        // Logo du sponsor
        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(50, 50);
        logoContainer.setStyle(
            "-fx-background-color: #f8fafc;" +
            "-fx-background-radius: 6;"
        );
        
        try {
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView();
            String logoUrl = sponsor.getLogo();
            javafx.scene.image.Image logoImage = AdminEvenementController.chargerImage(logoUrl);
            
            if (logoImage != null && !logoImage.isError()) {
                logoView.setImage(logoImage);
                logoView.setFitWidth(45);
                logoView.setFitHeight(45);
                logoView.setPreserveRatio(true);
                logoView.setSmooth(true);
                logoContainer.getChildren().add(logoView);
            } else {
                // Icône par défaut si pas de logo
                Label defaultIcon = new Label("🏢");
                defaultIcon.setFont(Font.font(24));
                defaultIcon.setStyle("-fx-text-fill: #94a3b8;");
                logoContainer.getChildren().add(defaultIcon);
            }
            
        } catch (Exception e) {
            // Icône par défaut en cas d'erreur
            Label defaultIcon = new Label("🏢");
            defaultIcon.setFont(Font.font(24));
            defaultIcon.setStyle("-fx-text-fill: #94a3b8;");
            logoContainer.getChildren().add(defaultIcon);
        }
        
        // Nom du sponsor (tronqué si trop long)
        String nomAffiche = sponsor.getNom_partenaire();
        if (nomAffiche.length() > 10) {
            nomAffiche = nomAffiche.substring(0, 9) + "...";
        }
        Label nomLabel = new Label(nomAffiche);
        nomLabel.setStyle(
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-text-alignment: center;"
        );
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(70);
        
        card.getChildren().addAll(logoContainer, nomLabel);
        
        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #1F4D3A;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 2;" +
                "-fx-padding: 8;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(31,77,58,0.2), 8, 0, 0, 3);"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);"
            );
        });
        
        // Clic pour voir la vidéo ou les infos
        card.setOnMouseClicked(e -> {
            if (sponsor.getVideo_url() != null && !sponsor.getVideo_url().isEmpty()) {
                ouvrirVideoSponsor(sponsor);
            } else {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Sponsor");
                info.setHeaderText(sponsor.getNom_partenaire());
                info.setContentText(
                    (sponsor.getType() != null ? "Type : " + sponsor.getType() + "\n\n" : "") +
                    (sponsor.getDescription() != null ? sponsor.getDescription() : "Sponsor officiel de l'événement")
                );
                info.showAndWait();
            }
        });
        
        return card;
    }
    
    /**
     * Ouvre une fenêtre avec la vidéo du sponsor
     */
    private void ouvrirVideoSponsor(Sponsor sponsor) {
        javafx.stage.Stage videoStage = new javafx.stage.Stage();
        videoStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        videoStage.setTitle("🎬 " + sponsor.getNom_partenaire());
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #000000;");
        
        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #1F4D3A;");
        
        Label titleLabel = new Label("🎬 " + sponsor.getNom_partenaire());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12;"
        );
        closeBtn.setOnAction(e -> videoStage.close());
        
        header.getChildren().addAll(titleLabel, spacer, closeBtn);
        
        // WebView pour la vidéo
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        VBox.setVgrow(webView, Priority.ALWAYS);
        
        // Générer le HTML avec la vidéo YouTube
        String videoHtml = genererHTMLVideo(sponsor.getVideo_url(), sponsor.getNom_partenaire());
        webView.getEngine().loadContent(videoHtml);
        
        // Description du sponsor
        if (sponsor.getDescription() != null && !sponsor.getDescription().isEmpty()) {
            VBox descBox = new VBox(8);
            descBox.setPadding(new Insets(16, 20, 16, 20));
            descBox.setStyle("-fx-background-color: #f8fafc;");
            
            Label descLabel = new Label("À propos de " + sponsor.getNom_partenaire());
            descLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
            descLabel.setTextFill(Color.web("#1e293b"));
            
            Label descText = new Label(sponsor.getDescription());
            descText.setWrapText(true);
            descText.setFont(Font.font("System", 12));
            descText.setTextFill(Color.web("#64748b"));
            
            descBox.getChildren().addAll(descLabel, descText);
            root.getChildren().addAll(header, webView, descBox);
        } else {
            root.getChildren().addAll(header, webView);
        }
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
        videoStage.setScene(scene);
        videoStage.show();
    }
    
    /**
     * Ouvre une galerie avec toutes les vidéos des sponsors
     */
    private void ouvrirGalerieVideos(List<Sponsor> sponsors) {
        javafx.stage.Stage galerieStage = new javafx.stage.Stage();
        galerieStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        galerieStage.setTitle("🎬 Galerie Vidéos Sponsors");
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");
        
        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #1F4D3A;");
        
        Label titleLabel = new Label("🎬 Vidéos de nos Sponsors");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12;"
        );
        closeBtn.setOnAction(e -> galerieStage.close());
        
        header.getChildren().addAll(titleLabel, spacer, closeBtn);
        
        // ScrollPane avec les vidéos
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        FlowPane flowPane = new FlowPane(20, 20);
        flowPane.setPadding(new Insets(20));
        flowPane.setStyle("-fx-background-color: white;");
        
        for (Sponsor s : sponsors) {
            if (s.getVideo_url() != null && !s.getVideo_url().isEmpty()) {
                VBox videoCard = creerCarteVideo(s);
                flowPane.getChildren().add(videoCard);
            }
        }
        
        scrollPane.setContent(flowPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().addAll(header, scrollPane);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
        galerieStage.setScene(scene);
        galerieStage.show();
    }
    
    /**
     * Crée une carte vidéo pour la galerie
     */
    private VBox creerCarteVideo(Sponsor sponsor) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
            "-fx-padding: 12;" +
            "-fx-cursor: hand;"
        );
        
        // Miniature vidéo
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(160);
        thumbnail.setStyle(
            "-fx-background-color: linear-gradient(135deg, #1F4D3A 0%, #2E7D5A 100%);" +
            "-fx-background-radius: 8;"
        );
        
        Label playIcon = new Label("▶");
        playIcon.setFont(Font.font(40));
        playIcon.setTextFill(Color.WHITE);
        playIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        
        thumbnail.getChildren().add(playIcon);
        
        // Nom du sponsor
        Label nomLabel = new Label(sponsor.getNom_partenaire());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nomLabel.setTextFill(Color.web("#1e293b"));
        
        // Type
        Label typeLabel = new Label(sponsor.getType() != null ? sponsor.getType() : "Sponsor");
        typeLabel.setFont(Font.font("System", 11));
        typeLabel.setTextFill(Color.web("#64748b"));
        
        card.getChildren().addAll(thumbnail, nomLabel, typeLabel);
        
        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #1F4D3A;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(31,77,58,0.3), 12, 0, 0, 4);" +
                "-fx-padding: 12;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
                "-fx-padding: 12;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        card.setOnMouseClicked(e -> ouvrirVideoSponsor(sponsor));
        
        return card;
    }
    
    /**
     * Génère le HTML pour afficher une vidéo YouTube
     */
    private String genererHTMLVideo(String videoUrl, String sponsorName) {
        // Convertir l'URL YouTube en format embed si nécessaire
        String embedUrl = videoUrl;
        if (videoUrl.contains("youtube.com/watch")) {
            embedUrl = videoUrl.replace("watch?v=", "embed/");
        } else if (videoUrl.contains("youtu.be/")) {
            embedUrl = videoUrl.replace("youtu.be/", "youtube.com/embed/");
        }
        
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <meta charset=\"utf-8\">" +
            "    <style>" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
            "        body, html { height: 100%; width: 100%; overflow: hidden; background: #000; }" +
            "        iframe { width: 100%; height: 100%; border: none; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <iframe src=\"" + embedUrl + "?autoplay=0&rel=0&modestbranding=1\" " +
            "            allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" " +
            "            allowfullscreen>" +
            "    </iframe>" +
            "</body>" +
            "</html>";
    }

    // ── Formulaire de paiement professionnel ────────────────────────
    private void ouvrirFormulairePaiement(Evenement ev) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle((ev.getPrix() > 0 ? "💳 Paiement" : "📝 Inscription") + " — " + ev.getNom());
        stage.setResizable(false);

        // ── Header ──
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: #1F4D3A;");
        Label headerTitle = new Label(ev.getPrix() > 0 ? "💳 Paiement sécurisé" : "📝 Inscription gratuite");
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerTitle.setTextFill(Color.WHITE);
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;" +
            "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 4 10;");
        closeBtn.setOnAction(e -> stage.close());
        header.getChildren().addAll(headerTitle, hSpacer, closeBtn);

        // ── Récapitulatif événement ──
        VBox recapBox = new VBox(10);
        recapBox.setStyle("-fx-background-color: linear-gradient(135deg,#1F4D3A 0%,#2E7D5A 100%); -fx-padding: 20 24;");
        Label evTitle = new Label("📅 " + ev.getNom());
        evTitle.setFont(Font.font("System", FontWeight.BOLD, 17));
        evTitle.setTextFill(Color.WHITE); evTitle.setWrapText(true);
        Label evDetails = new Label(
            "📍 " + ev.getLieu() + "\n" +
            "🗓️  " + ev.getDate_debut().toLocalDate() + " à " +
            ev.getDate_debut().toLocalTime().toString().substring(0, 5) + "\n" +
            "👤 Coach : " + ev.getCoach_name());
        evDetails.setFont(Font.font("System", 14)); evDetails.setTextFill(Color.WHITE);
        evDetails.setWrapText(true); evDetails.setLineSpacing(4);
        Separator recapSep = new Separator();
        recapSep.setStyle("-fx-background-color: rgba(255,255,255,0.4);");
        HBox prixBadge = new HBox(10);
        prixBadge.setAlignment(Pos.CENTER_LEFT);
        prixBadge.setPadding(new Insets(10, 14, 10, 14));
        prixBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10;");
        Label prixIcon = new Label(ev.getPrix() > 0 ? "💰" : "🎁"); prixIcon.setFont(Font.font(22));
        Label prixLbl = new Label(ev.getPrix() > 0
            ? String.format("Montant à payer : %.2f TND", ev.getPrix())
            : "Inscription gratuite — 0.00 TND");
        prixLbl.setFont(Font.font("System", FontWeight.BOLD, 19));
        prixLbl.setTextFill(Color.WHITE);
        prixBadge.getChildren().addAll(prixIcon, prixLbl);
        recapBox.getChildren().addAll(evTitle, evDetails, recapSep, prixBadge);

        // ── Infos personnelles ──
        VBox infoBox = new VBox(12);
        infoBox.setPadding(new Insets(22, 24, 0, 24));
        infoBox.setStyle("-fx-background-color: white;");
        Label infoHdr = new Label("👤 Vos informations");
        infoHdr.setFont(Font.font("System", FontWeight.BOLD, 16));
        infoHdr.setTextFill(Color.web("#1e293b"));
        TextField tfNom   = buildField("Nom complet *");
        TextField tfEmail = buildField("Email *");
        TextField tfTel   = buildField("Téléphone *");
        infoBox.getChildren().addAll(infoHdr, new Separator(), tfNom, tfEmail, tfTel);

        // ── Paiement ──
        VBox payBox = new VBox(12);
        payBox.setPadding(new Insets(20, 24, 0, 24));
        payBox.setStyle("-fx-background-color: white;");
        boolean isPaid = ev.getPrix() > 0;

        Label payHdr = new Label("💳 Informations de paiement");
        payHdr.setFont(Font.font("System", FontWeight.BOLD, 16));
        payHdr.setTextFill(Color.web("#1e293b"));

        ToggleGroup tg = new ToggleGroup();
        RadioButton rbCarte  = new RadioButton("💳 Carte bancaire");
        RadioButton rbPaypal = new RadioButton("🅿️ PayPal");
        rbCarte.setToggleGroup(tg); rbCarte.setSelected(true);
        rbPaypal.setToggleGroup(tg);
        rbCarte.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        rbPaypal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        HBox methodeBox = new HBox(16, rbCarte, rbPaypal);
        methodeBox.setAlignment(Pos.CENTER_LEFT);
        methodeBox.setPadding(new Insets(6, 0, 6, 0));

        VBox carteFields = new VBox(10);
        TextField tfCarte = buildField("Numéro de carte (16 chiffres) *");
        tfCarte.textProperty().addListener((o, ov, nv) -> {
            if (!nv.matches("\\d*")) tfCarte.setText(nv.replaceAll("[^\\d]", ""));
            if (nv.length() > 16) tfCarte.setText(nv.substring(0, 16));
        });
        HBox expCvvBox = new HBox(12);
        TextField tfExp = buildField("MM/AA *"); tfExp.setPrefWidth(160);
        tfExp.textProperty().addListener((o, ov, nv) -> {
            if (!nv.matches("[\\d/]*")) { tfExp.setText(nv.replaceAll("[^\\d/]", "")); return; }
            if (nv.length() == 2 && ov.length() == 1) tfExp.setText(nv + "/");
            if (nv.length() > 5) tfExp.setText(nv.substring(0, 5));
        });
        TextField tfCvv = buildField("CVV *"); tfCvv.setPrefWidth(130);
        tfCvv.textProperty().addListener((o, ov, nv) -> {
            if (!nv.matches("\\d*")) tfCvv.setText(nv.replaceAll("[^\\d]", ""));
            if (nv.length() > 3) tfCvv.setText(nv.substring(0, 3));
        });
        expCvvBox.getChildren().addAll(tfExp, tfCvv);
        carteFields.getChildren().addAll(tfCarte, expCvvBox);
        carteFields.setVisible(isPaid); carteFields.setManaged(isPaid);
        if (!isPaid) { methodeBox.setVisible(false); methodeBox.setManaged(false); }
        rbPaypal.setOnAction(e -> { carteFields.setVisible(false); carteFields.setManaged(false); });
        rbCarte.setOnAction(e  -> { carteFields.setVisible(isPaid); carteFields.setManaged(isPaid); });

        // Badge sécurité
        HBox secBadge = new HBox(10);
        secBadge.setAlignment(Pos.CENTER);
        secBadge.setPadding(new Insets(12, 16, 12, 16));
        secBadge.setStyle("-fx-background-color: #F0FDF4; -fx-background-radius: 10;" +
            "-fx-border-color: #86EFAC; -fx-border-radius: 10; -fx-border-width: 2;");
        Label secIcon = new Label("🔒"); secIcon.setFont(Font.font(20));
        Label secLbl  = new Label("Paiement 100% sécurisé SSL");
        secLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        secLbl.setTextFill(Color.web("#166534"));
        secBadge.getChildren().addAll(secIcon, secLbl);

        payBox.getChildren().addAll(payHdr, new Separator(), methodeBox, carteFields, secBadge);

        // ── Footer ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(18, 24, 22, 24));
        footer.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefHeight(46); btnAnnuler.setPrefWidth(120);
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569;" +
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> stage.close());
        Button btnPayer = new Button(isPaid
            ? "💳 PAYER " + String.format("%.2f TND", ev.getPrix())
            : "✅ CONFIRMER L'INSCRIPTION");
        btnPayer.setPrefHeight(46);
        btnPayer.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10;" +
            "-fx-cursor: hand; -fx-padding: 0 24;");
        btnPayer.setOnMouseEntered(e -> btnPayer.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 0 24;"));
        btnPayer.setOnMouseExited(e -> btnPayer.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 0 24;"));
        footer.getChildren().addAll(btnAnnuler, btnPayer);

        // ── Assemblage ──
        VBox formContent = new VBox(0, infoBox, payBox);
        formContent.setStyle("-fx-background-color: white;");
        ScrollPane scroll = new ScrollPane(formContent);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox root = new VBox(0, header, recapBox, scroll, footer);
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new javafx.scene.Scene(root, 520, 680));

        // ── Action PAYER ──
        btnPayer.setOnAction(e -> {
            String nom   = tfNom.getText().trim();
            String email = tfEmail.getText().trim();
            String tel   = tfTel.getText().trim();
            String carte = tfCarte.getText().trim();
            String exp   = tfExp.getText().trim();
            String cvv   = tfCvv.getText().trim();
            String methode = rbCarte.isSelected() ? "Carte bancaire" : "PayPal";

            if (nom.isEmpty() || !email.contains("@") || tel.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires (*).").showAndWait();
                return;
            }
            if (isPaid && rbCarte.isSelected()) {
                if (carte.length() != 16 || exp.length() != 5 || cvv.length() != 3) {
                    new Alert(Alert.AlertType.WARNING,
                        "Vérifiez :\n• Numéro de carte : 16 chiffres\n• Date : MM/AA\n• CVV : 3 chiffres").showAndWait();
                    return;
                }
            }
            if (getPaiementService() != null && getPaiementService().aDejaPayePourEvenement(email, ev.getId())) {
                new Alert(Alert.AlertType.INFORMATION,
                    "Vous êtes déjà inscrit(e) à cet événement.\n📧 " + email).showAndWait();
                return;
            }
            stage.close();

            Alert loader = new Alert(Alert.AlertType.INFORMATION);
            loader.setTitle("⏳ Traitement");
            loader.setHeaderText(isPaid ? "Traitement du paiement..." : "Enregistrement...");
            loader.setContentText(isPaid
                ? "💳 Vérification de la carte\n🔒 Connexion SSL\n⏳ Veuillez patienter..."
                : "📝 Enregistrement de votre inscription\n⏳ Veuillez patienter...");
            loader.show();

            new Thread(() -> {
                try {
                    Paiement paiementResult = null;
                    if (getPaiementService() != null) {
                        paiementResult = getPaiementService().traiterPaiement(
                            ev.getId(), nom, email, tel, ev.getPrix(), methode, carte, cvv, exp);
                    } else {
                        // Fallback si PaiementService non disponible : créer un paiement simulé
                        paiementResult = new Paiement(ev.getId(), nom, email, tel,
                            ev.getPrix(), "valide", "txn_" + java.util.UUID.randomUUID().toString().substring(0, 16), methode);
                    }
                    final Paiement paiement = paiementResult;
                    javafx.application.Platform.runLater(() -> {
                        loader.close();
                        if (paiement != null && "valide".equals(paiement.getStatut())) {
                            // Incrémenter le nombre de participants
                            if (getEventService() != null) {
                                getEventService().incrementerParticipants(ev.getId());
                            }
                            String msg = (isPaid
                                ? "Paiement validé !\n\n💳 Montant : " + String.format("%.2f TND", ev.getPrix()) + "\n"
                                : "Inscription confirmée !\n\n🎁 Gratuit\n") +
                                "🔑 Réf : " + paiement.getTransactionId() + "\n\n" +
                                "📅 " + ev.getNom() + "\n📍 " + ev.getLieu() + "\n\n" +
                                "📧 Email de confirmation envoyé à :\n     " + email;
                            Alert ok = new Alert(Alert.AlertType.INFORMATION, msg);
                            ok.setTitle("✅ " + (isPaid ? "Paiement Réussi !" : "Inscription Confirmée !"));
                            ok.setHeaderText("🎉 Félicitations " + nom + " !");
                            ok.showAndWait();
                            // Envoyer l'email dans un thread séparé avec feedback
                            new Thread(() -> {
                                MeteoService.MeteoResult meteo = meteoService.getMeteo(ev);
                                boolean emailEnvoye = getEmailService() != null
                                    && getEmailService().envoyerEmailConfirmation(nom, email, tel, ev, meteo);
                                javafx.application.Platform.runLater(() -> {
                                    if (emailEnvoye) {
                                        Alert emailOk = new Alert(Alert.AlertType.INFORMATION);
                                        emailOk.setTitle("📧 Email envoyé");
                                        emailOk.setHeaderText("Email de confirmation envoyé !");
                                        emailOk.setContentText("✉️ Un email de confirmation a été envoyé à :\n     " + email
                                            + "\n\n💡 Vérifiez votre boîte de réception (et le dossier spam).");
                                        emailOk.showAndWait();
                                    } else {
                                        Alert emailWarn = new Alert(Alert.AlertType.WARNING);
                                        emailWarn.setTitle("⚠️ Email non envoyé");
                                        emailWarn.setHeaderText("Votre inscription est confirmée");
                                        emailWarn.setContentText("✅ Inscription enregistrée avec succès.\n\n"
                                            + "⚠️ Cependant, l'email de confirmation n'a pas pu être envoyé.\n\n"
                                            + "Raisons possibles :\n"
                                            + "• Problème de connexion internet\n"
                                            + "• Adresse email invalide\n\n"
                                            + "💡 Votre inscription reste valide.");
                                        emailWarn.showAndWait();
                                    }
                                });
                            }).start();
                            // Rafraîchir les cartes pour mettre à jour la barre de capacité
                            refreshCards(null);
                        } else {
                            new Alert(Alert.AlertType.ERROR,
                                "Paiement refusé.\n\nRaisons possibles :\n• Carte invalide\n• Fonds insuffisants\n• Carte expirée")
                                .showAndWait();
                        }
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        loader.close();
                        new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
                    });
                }
            }).start();
        });

        stage.showAndWait();
    }

    /** Champ de saisie stylisé avec effet hover/focus */
    private TextField buildField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        String base = "-fx-background-radius: 8; -fx-pref-height: 46px;" +
            "-fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 2;" +
            "-fx-padding: 8 16; -fx-font-size: 14px; -fx-background-color: white;";
        String active = "-fx-background-radius: 8; -fx-pref-height: 46px;" +
            "-fx-border-color: #1F4D3A; -fx-border-radius: 8; -fx-border-width: 2;" +
            "-fx-padding: 8 16; -fx-font-size: 14px; -fx-background-color: white;";
        tf.setStyle(base);
        tf.setOnMouseEntered(e -> tf.setStyle(active));
        tf.setOnMouseExited(e -> { if (!tf.isFocused()) tf.setStyle(base); });
        tf.focusedProperty().addListener((o, ov, focused) -> tf.setStyle(focused ? active : base));
        return tf;
    }

    // ── Formulaire de participation ────────────────────────
    private void ouvrirFormulaireParticipation(Evenement ev) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Inscription — " + ev.getNom());
        dialog.setHeaderText("📋  Rejoindre l'événement : " + ev.getNom());

        // Champs
        TextField tfNom   = new TextField();
        tfNom.setPromptText("Votre nom complet *");
        tfNom.setStyle("-fx-background-radius: 8; -fx-pref-height: 38px;");

        TextField tfEmail = new TextField();
        tfEmail.setPromptText("votre@email.com *");
        tfEmail.setStyle("-fx-background-radius: 8; -fx-pref-height: 38px;");

        TextField tfTel   = new TextField();
        tfTel.setPromptText("Téléphone (optionnel)");
        tfTel.setStyle("-fx-background-radius: 8; -fx-pref-height: 38px;");

        // Récap événement
        VBox recap = new VBox(4);
        recap.setStyle(
            "-fx-background-color: #F0FDF4; -fx-background-radius: 10;" +
            "-fx-border-color: #BBF7D0; -fx-border-radius: 10; -fx-padding: 12;"
        );
        recap.getChildren().addAll(
            styledLabel("📅  " + ev.getDate_debut().toLocalDate() + "  ·  "
                    + ev.getDate_debut().toLocalTime().toString().substring(0, 5), "#166534"),
            styledLabel("📍  " + ev.getLieu(), "#166534"),
            styledLabel("👤  Coach : " + ev.getCoach_name(), "#166534")
        );

        VBox content = new VBox(10,
            recap,
            styledLabel("Nom complet *", "#475569"), tfNom,
            styledLabel("Email *", "#475569"), tfEmail,
            styledLabel("Téléphone", "#475569"), tfTel
        );
        content.setPadding(new Insets(16));
        content.setPrefWidth(420);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("✅  Confirmer l'inscription");
        okBtn.setStyle(
            "-fx-background-color: #1F4D3A; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 8;"
        );

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nom   = tfNom.getText().trim();
            String email = tfEmail.getText().trim();
            String tel   = tfTel.getText().trim();

            if (nom.isEmpty() || !email.contains("@")) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setHeaderText("Champs invalides");
                warn.setContentText("Veuillez renseigner un nom valide et un email valide (ex: nom@email.com).");
                warn.showAndWait();
            } else {
                // Enregistrer l'inscription en base de données
                System.out.println("[PARTICIPATION] " + nom + " <" + email + "> → " + ev.getNom());
                if (getPaiementService() != null) {
                    getPaiementService().traiterPaiement(
                        ev.getId(), nom, email, tel, 0.0, "Gratuit", "", "", "");
                }
                if (getEventService() != null) {
                    getEventService().incrementerParticipants(ev.getId());
                }
                
                // Afficher un message de confirmation immédiat
                Alert confirmationRapide = new Alert(Alert.AlertType.INFORMATION);
                confirmationRapide.setTitle("Inscription confirmée !");
                confirmationRapide.setHeaderText("✅  Bienvenue " + nom + " !");
                confirmationRapide.setContentText(
                    "Vous êtes inscrit(e) à :\n" +
                    "📅  " + ev.getNom() + "\n" +
                    "📍  " + ev.getLieu() + "\n\n" +
                    "📧  Envoi de l'email de confirmation en cours..."
                );
                confirmationRapide.show();

                // Envoyer l'email de confirmation dans un thread séparé
                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Petit délai pour que l'utilisateur voie le message
                        MeteoService.MeteoResult meteo = meteoService.getMeteo(ev);
                        boolean emailEnvoye = getEmailService() != null && getEmailService().envoyerEmailConfirmation(nom, email, tel, ev, meteo);
                        
                        // Fermer le message de confirmation rapide et afficher le résultat
                        javafx.application.Platform.runLater(() -> {
                            confirmationRapide.close();
                            
                            if (emailEnvoye) {
                                Alert success = new Alert(Alert.AlertType.INFORMATION);
                                success.setTitle("✅ Inscription Complète");
                                success.setHeaderText("Email de confirmation envoyé !");
                                success.setContentText(
                                    "🎉  Félicitations " + nom + " !\n\n" +
                                    "Vous êtes inscrit(e) à :\n" +
                                    "📅  " + ev.getNom() + "\n" +
                                    "📍  " + ev.getLieu() + "\n\n" +
                                    "✉️  Un email de confirmation a été envoyé à :\n" +
                                    "     " + email + "\n\n" +
                                    "💡  Vérifiez votre boîte de réception (et le dossier spam)."
                                );
                                success.showAndWait();
                            } else {
                                Alert warning = new Alert(Alert.AlertType.WARNING);
                                warning.setTitle("⚠️ Inscription Enregistrée");
                                warning.setHeaderText("Inscription confirmée");
                                warning.setContentText(
                                    "✅  Vous êtes bien inscrit(e) à l'événement :\n" +
                                    "📅  " + ev.getNom() + "\n" +
                                    "📍  " + ev.getLieu() + "\n\n" +
                                    "⚠️  Cependant, l'email de confirmation n'a pas pu être envoyé.\n\n" +
                                    "Raisons possibles :\n" +
                                    "• Configuration email non effectuée\n" +
                                    "• Problème de connexion internet\n" +
                                    "• Email invalide\n\n" +
                                    "💡  Votre inscription est enregistrée.\n" +
                                    "     Contactez l'administrateur si nécessaire."
                                );
                                warning.showAndWait();
                            }
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            confirmationRapide.close();
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("❌ Erreur");
                            error.setHeaderText("Erreur lors de l'envoi de l'email");
                            error.setContentText(
                                "✅  Votre inscription est enregistrée.\n\n" +
                                "❌  Mais une erreur s'est produite lors de l'envoi de l'email.\n\n" +
                                "Erreur : " + e.getMessage()
                            );
                            error.showAndWait();
                        });
                    }
                }).start();
            }
        }
    }

    private Label styledLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
        return l;
    }
    
    /**
     * Ouvre Google Maps dans l'application desktop (JavaFX WebView).
     * Utilise l'URL d'embed Google Maps — aucune clé API requise.
     */
    private void ouvrirGoogleMapsDansNavigateur(Evenement ev) {
        String lieu = ev.getLieu() != null && !ev.getLieu().trim().isEmpty()
                      ? MeteoService.getLieuPropre(ev.getLieu()) : "Tunis, Tunisie";

        String lieuEncodeTemp;
        try {
            lieuEncodeTemp = java.net.URLEncoder.encode(lieu, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            lieuEncodeTemp = lieu.replace(" ", "+");
        }
        final String lieuEncode = lieuEncodeTemp;

        // ── URL Google Maps Embed (sans clé API) ──
        String googleMapsUrl = "https://maps.google.com/maps?q=" + lieuEncode
                               + "&output=embed&hl=fr&z=15";

        // ── Fenêtre ──
        javafx.stage.Stage mapStage = new javafx.stage.Stage();
        mapStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        mapStage.setTitle("📍 " + lieu);
        mapStage.setWidth(1050);
        mapStage.setHeight(700);
        mapStage.setResizable(true);

        // ── Header style Google Maps ──
        HBox header = new HBox(12);
        header.setPadding(new javafx.geometry.Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#E0E0E0;-fx-border-width:0 0 1 0;");

        Label logoG  = new Label("G");  logoG.setFont(Font.font("System",FontWeight.BOLD,20)); logoG.setStyle("-fx-text-fill:#4285F4;");
        Label logoO1 = new Label("o"); logoO1.setFont(Font.font("System",FontWeight.BOLD,20)); logoO1.setStyle("-fx-text-fill:#EA4335;");
        Label logoO2 = new Label("o"); logoO2.setFont(Font.font("System",FontWeight.BOLD,20)); logoO2.setStyle("-fx-text-fill:#FBBC05;");
        Label logoG2 = new Label("g"); logoG2.setFont(Font.font("System",FontWeight.BOLD,20)); logoG2.setStyle("-fx-text-fill:#4285F4;");
        Label logoL  = new Label("l");  logoL.setFont(Font.font("System",FontWeight.BOLD,20)); logoL.setStyle("-fx-text-fill:#34A853;");
        Label logoE  = new Label("e");  logoE.setFont(Font.font("System",FontWeight.BOLD,20)); logoE.setStyle("-fx-text-fill:#EA4335;");
        Label logoMaps = new Label(" Maps"); logoMaps.setFont(Font.font("System",FontWeight.BOLD,15)); logoMaps.setStyle("-fx-text-fill:#5F6368;");
        HBox logoBox = new HBox(0, logoG, logoO1, logoO2, logoG2, logoL, logoE, logoMaps);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Label searchLbl = new Label("🔍  " + lieu);
        searchLbl.setPrefWidth(380);
        searchLbl.setStyle("-fx-background-color:#F1F3F4;-fx-background-radius:24;-fx-padding:9 18;-fx-font-size:13px;-fx-text-fill:#3C4043;");

        // Bouton ouvrir dans le navigateur
        Button openBrowserBtn = new Button("🌐 Ouvrir dans le navigateur");
        openBrowserBtn.setStyle("-fx-background-color:#1A73E8;-fx-text-fill:white;-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;-fx-cursor:hand;");
        openBrowserBtn.setOnMouseEntered(e -> openBrowserBtn.setStyle("-fx-background-color:#1557B0;-fx-text-fill:white;-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;-fx-cursor:hand;"));
        openBrowserBtn.setOnMouseExited(e  -> openBrowserBtn.setStyle("-fx-background-color:#1A73E8;-fx-text-fill:white;-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;-fx-cursor:hand;"));
        openBrowserBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                    new java.net.URI("https://www.google.com/maps/search/?api=1&query=" + lieuEncode));
            } catch (Exception ex) {
                System.err.println("Impossible d'ouvrir le navigateur : " + ex.getMessage());
            }
        });

        javafx.scene.layout.Region spacerH = new javafx.scene.layout.Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#5F6368;-fx-font-size:16px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 10;-fx-background-radius:20;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color:#F1F3F4;-fx-text-fill:#3C4043;-fx-font-size:16px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 10;-fx-background-radius:20;"));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#5F6368;-fx-font-size:16px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 10;-fx-background-radius:20;"));
        closeBtn.setOnAction(e -> mapStage.close());

        header.getChildren().addAll(logoBox, searchLbl, spacerH, openBrowserBtn, closeBtn);

        // ── WebView plein écran ──
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        webView.setMaxWidth(Double.MAX_VALUE);
        webView.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(webView, Priority.ALWAYS);
        webView.setContextMenuEnabled(false);

        // ── Barre de statut ──
        HBox statusBar = new HBox(8);
        statusBar.setPadding(new javafx.geometry.Insets(5, 16, 5, 16));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color:#F8F9FA;-fx-border-color:#E0E0E0;-fx-border-width:1 0 0 0;");
        Label statusLbl = new Label("📍 " + lieu);
        statusLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5F6368;");
        javafx.scene.layout.Region stSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(stSpacer, Priority.ALWAYS);
        Label gmLbl = new Label("© Google Maps");
        gmLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9AA0A6;");
        statusBar.getChildren().addAll(statusLbl, stSpacer, gmLbl);

        // ── Assemblage ──
        VBox root = new VBox(header, webView, statusBar);
        root.setStyle("-fx-background-color:white;");
        mapStage.setScene(new javafx.scene.Scene(root));

        // ── Chargement Google Maps directement dans le WebView ──
        // HTML avec iframe Google Maps embed (fonctionne sans clé API)
        String html =
            "<!DOCTYPE html><html><head><meta charset='UTF-8'/>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "html,body{width:100%;height:100%;overflow:hidden;background:#e8eaed}" +
            "iframe{width:100%;height:100%;border:none;display:block}" +
            "#loader{position:absolute;top:0;left:0;width:100%;height:100%;background:#e8eaed;" +
            "  display:flex;flex-direction:column;align-items:center;justify-content:center;" +
            "  z-index:9999;font-family:sans-serif;color:#5F6368;pointer-events:none}" +
            ".sp{width:44px;height:44px;border:4px solid #dadce0;border-top-color:#1A73E8;" +
            "  border-radius:50%;animation:spin .9s linear infinite;margin-bottom:14px}" +
            "@keyframes spin{to{transform:rotate(360deg)}}" +
            "</style></head><body>" +
            "<div id='loader'><div class='sp'></div>" +
            "<div style='font-size:14px;font-weight:500'>Chargement de Google Maps...</div></div>" +
            "<iframe id='gmap' src='" + googleMapsUrl + "' " +
            "  allowfullscreen loading='lazy' referrerpolicy='no-referrer-when-downgrade'" +
            "  onload=\"document.getElementById('loader').style.display='none';\"></iframe>" +
            "</body></html>";

        webView.getEngine().loadContent(html);

        mapStage.show();
        System.out.println("✅ Google Maps ouvert dans l'app pour : " + lieu);
    }

    /**
     * Ouvre l'assistant IA pour les événements
     */
    @FXML
    public void handleAssistantIA() {
        // Récupérer l'instance globale de l'assistant IA
        AssistantIAService assistantIA = AssistantIAService.getInstance();
        // Passer la liste des événements à l'IA
        List<Evenement> liste = getEventService() != null ? getEventService().getAll() : new java.util.ArrayList<>();
        assistantIA.setEvenements(liste);

        // Créer la fenêtre du chat
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("🤖 Assistant IA — Nutri Coach");
        stage.setResizable(true);
        stage.setMinWidth(480);
        stage.setMinHeight(600);

        // ── Header ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
        );

        Label robotIcon = new Label("🤖");
        robotIcon.setFont(Font.font("Segoe UI Emoji", 28));

        VBox headerText = new VBox(2);
        Label headerTitle = new Label("Assistant IA Nutri Coach");
        headerTitle.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 16));
        headerTitle.setTextFill(Color.WHITE);
        Label headerSub = new Label("Mode intelligent local");
        headerSub.setFont(Font.font("Segoe UI Emoji", 11));
        headerSub.setTextFill(Color.web("#FFFFFF", 0.8));
        headerText.getChildren().addAll(headerTitle, headerSub);

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20;" +
            "-fx-cursor: hand; -fx-padding: 4 10;"
        );
        closeBtn.setOnAction(e -> stage.close());
        header.getChildren().addAll(robotIcon, headerText, hSpacer, closeBtn);

        // ── Zone de messages ──
        VBox messagesBox = new VBox(12);
        messagesBox.setPadding(new Insets(16));
        messagesBox.setStyle("-fx-background-color: #f8fafc;");

        ScrollPane scrollMessages = new ScrollPane(messagesBox);
        scrollMessages.setFitToWidth(true);
        scrollMessages.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollMessages.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollMessages.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        VBox.setVgrow(scrollMessages, Priority.ALWAYS);

        // Message de bienvenue dynamique
        List<Evenement> listeWelcome = getEventService() != null ? getEventService().getAll() : new java.util.ArrayList<>();
        long gratuits = listeWelcome.stream().filter(ev -> ev.getPrix() <= 0).count();
        long payants = listeWelcome.size() - gratuits;
        ajouterMessageIA(messagesBox, scrollMessages,
            "👋 Bonjour ! Je suis votre assistant IA Nutri Coach.\n\n" +
            "📊 " + listeWelcome.size() + " événement" + (listeWelcome.size() > 1 ? "s" : "") + " disponible" +
            (listeWelcome.size() > 1 ? "s" : "") + " : " +
            gratuits + " gratuit" + (gratuits > 1 ? "s" : "") + " · " +
            payants + " payant" + (payants > 1 ? "s" : "") + "\n\n" +
            "Je peux vous aider à :\n" +
            "• 🎁 Trouver des événements gratuits\n" +
            "• 📅 Voir le prochain événement\n" +
            "• 🔍 Chercher par nom, lieu ou coach\n" +
            "• 📊 Voir les statistiques\n\n" +
            "Que souhaitez-vous savoir ? 😊");

        // ── Zone de saisie ──
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(12, 16, 16, 16));
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;"
        );

        TextField tfQuestion = new TextField();
        tfQuestion.setPromptText("Posez votre question...");
        tfQuestion.setStyle(
            "-fx-background-radius: 24; -fx-pref-height: 44px;" +
            "-fx-border-color: #e2e8f0; -fx-border-radius: 24; -fx-border-width: 2;" +
            "-fx-padding: 8 16; -fx-font-size: 14px; -fx-background-color: #f8fafc;"
        );
        HBox.setHgrow(tfQuestion, Priority.ALWAYS);

        Button btnEnvoyer = new Button("➤");
        btnEnvoyer.setPrefSize(44, 44);
        btnEnvoyer.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;" +
            "-fx-background-radius: 22; -fx-cursor: hand;"
        );

        inputBox.getChildren().addAll(tfQuestion, btnEnvoyer);

        // ── Suggestions rapides ──
        HBox suggestionsBox = new HBox(8);
        suggestionsBox.setPadding(new Insets(0, 16, 8, 16));
        suggestionsBox.setStyle("-fx-background-color: white;");
        suggestionsBox.setAlignment(Pos.CENTER_LEFT);

        // Paires : [texte affiché, question envoyée]
        String[][] suggestions = {
            {"🎁 Gratuits",     "Quels événements sont gratuits ?"},
            {"📅 Prochain",     "Quel est le prochain événement ?"},
            {"📊 Statistiques", "Statistiques des événements"},
            {"📋 Tous",         "Montre-moi tous les événements"}
        };
        for (String[] sug : suggestions) {
            Button btnSug = new Button(sug[0]);
            btnSug.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;" +
                "-fx-font-size: 11px; -fx-background-radius: 20; -fx-cursor: hand;" +
                "-fx-padding: 5 12; -fx-border-color: #e2e8f0; -fx-border-radius: 20; -fx-border-width: 1;"
            );
            btnSug.setOnMouseEntered(ev -> btnSug.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white;" +
                "-fx-font-size: 11px; -fx-background-radius: 20; -fx-cursor: hand;" +
                "-fx-padding: 5 12; -fx-border-color: #6366f1; -fx-border-radius: 20; -fx-border-width: 1;"
            ));
            btnSug.setOnMouseExited(ev -> btnSug.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;" +
                "-fx-font-size: 11px; -fx-background-radius: 20; -fx-cursor: hand;" +
                "-fx-padding: 5 12; -fx-border-color: #e2e8f0; -fx-border-radius: 20; -fx-border-width: 1;"
            ));
            // Envoyer la question précise (pas le texte du bouton)
            btnSug.setOnAction(ev -> {
                tfQuestion.setText(sug[1]);
                envoyerMessage(tfQuestion, messagesBox, scrollMessages);
            });
            suggestionsBox.getChildren().add(btnSug);
        }

        // ── Action envoi ──
        Runnable envoi = () -> envoyerMessage(tfQuestion, messagesBox, scrollMessages);
        btnEnvoyer.setOnAction(e -> envoi.run());
        tfQuestion.setOnAction(e -> envoi.run());

        // ── Assemblage ──
        VBox root = new VBox(0, header, scrollMessages, suggestionsBox, inputBox);
        root.setStyle("-fx-background-color: white;");

        stage.setScene(new javafx.scene.Scene(root, 500, 650));
        stage.show();
    }

    /** Envoie un message et affiche la réponse de l'IA */
    private void envoyerMessage(TextField tfQuestion, VBox messagesBox, ScrollPane scrollMessages) {
        String question = tfQuestion.getText().trim();
        if (question.isEmpty()) return;

        tfQuestion.clear();

        // Afficher le message de l'utilisateur
        ajouterMessageUtilisateur(messagesBox, scrollMessages, question);

        // Indicateur de chargement
        HBox loadingBox = creerBulleChargement();
        messagesBox.getChildren().add(loadingBox);
        scrollVersBasChat(scrollMessages);

        // Appel IA en arrière-plan
        String questionFinal = question;
        new Thread(() -> {
            String reponse = AssistantIAService.getInstance().poserQuestion(questionFinal);
            javafx.application.Platform.runLater(() -> {
                messagesBox.getChildren().remove(loadingBox);
                ajouterMessageIA(messagesBox, scrollMessages, reponse);
            });
        }).start();
    }

    /** Ajoute un message de l'utilisateur dans le chat */
    private void ajouterMessageUtilisateur(VBox messagesBox, ScrollPane scroll, String texte) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);

        Label bubble = new Label(texte);
        bubble.setWrapText(true);
        bubble.setMaxWidth(320);
        bubble.setFont(Font.font("Segoe UI Emoji", 13));
        bubble.setTextFill(Color.WHITE);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-background-radius: 18 18 4 18;"
        );

        row.getChildren().add(bubble);
        messagesBox.getChildren().add(row);
        scrollVersBasChat(scroll);
    }

    /** Ajoute un message de l'IA dans le chat */
    private void ajouterMessageIA(VBox messagesBox, ScrollPane scroll, String texte) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label avatar = new Label("🤖");
        avatar.setFont(Font.font("Segoe UI Emoji", 20));
        avatar.setPadding(new Insets(4, 0, 0, 0));

        Label bubble = new Label(texte);
        bubble.setWrapText(true);
        bubble.setMaxWidth(340);
        bubble.setFont(Font.font("Segoe UI Emoji", 13));
        bubble.setTextFill(Color.web("#1e293b"));
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18 18 18 4;" +
            "-fx-border-color: #e2e8f0; -fx-border-radius: 18 18 18 4; -fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        );

        row.getChildren().addAll(avatar, bubble);
        messagesBox.getChildren().add(row);
        scrollVersBasChat(scroll);
    }

    /** Crée une bulle de chargement animée */
    private HBox creerBulleChargement() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label avatar = new Label("🤖");
        avatar.setFont(Font.font("Segoe UI Emoji", 20));

        Label loading = new Label("En train de réfléchir...");
        loading.setFont(Font.font("Segoe UI Emoji", 12));
        loading.setTextFill(Color.web("#94a3b8"));
        loading.setPadding(new Insets(10, 14, 10, 14));
        loading.setStyle(
            "-fx-background-color: #f1f5f9;" +
            "-fx-background-radius: 18; -fx-font-style: italic;"
        );

        // Animation de pulsation
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(600), loading);
        fade.setFromValue(0.4);
        fade.setToValue(1.0);
        fade.setCycleCount(javafx.animation.Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        row.getChildren().addAll(avatar, loading);
        return row;
    }

    /** Fait défiler le chat vers le bas */
    private void scrollVersBasChat(ScrollPane scroll) {
        javafx.application.Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    /**
     * Ouvre le calendrier des événements — appelé depuis le FXML via onAction="#handleVoirCalendrier"
     */
    @FXML
    public void handleVoirCalendrier() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/FrontCalendrier.fxml")
            );
            javafx.scene.Parent calendrierView = loader.load();

            // Trouver le contentArea (StackPane parent)
            javafx.scene.Node currentNode = flowPane;
            while (currentNode != null && !(currentNode.getParent() instanceof javafx.scene.layout.StackPane)) {
                currentNode = currentNode.getParent();
            }

            if (currentNode != null && currentNode.getParent() instanceof javafx.scene.layout.StackPane) {
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) currentNode.getParent();
                javafx.scene.Node oldContent = contentArea.getChildren().get(0);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldContent);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(calendrierView);
                    calendrierView.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), calendrierView);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    TranslateTransition slide = new TranslateTransition(Duration.millis(300), calendrierView);
                    slide.setFromY(20);
                    slide.setToY(0);
                    fadeIn.play();
                    slide.play();
                });
                fadeOut.play();
            }
        } catch (Exception ex) {
            System.err.println("❌ Erreur ouverture calendrier : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
