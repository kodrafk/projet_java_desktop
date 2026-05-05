package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
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
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.services.EmailService;
import tn.esprit.projet.services.EmailServiceModerne;
import tn.esprit.projet.services.ImageGeneratorService;

import java.util.List;

public class FrontEvenementController {

    @FXML private FlowPane flowPane;
    @FXML private TextField tfRecherche;
    @FXML private Label     lblCompteur;

    private EvenementService eventService   = new EvenementService();
    private SponsorService   sponsorService = new SponsorService();
    private EmailService     emailService   = new EmailService();

    @FXML
    public void initialize() {
        refreshCards(null);
    }

    @FXML
    public void handleRecherche() {
        String f = tfRecherche.getText().trim().toLowerCase();
        refreshCards(f.isEmpty() ? null : f);
    }

    public void refreshCards(String filtre) {
        if (flowPane == null) return;
        flowPane.getChildren().clear();

        List<Evenement> liste = eventService.getAll();
        int count = 0;
        int delay = 0;

        for (Evenement ev : liste) {
            if (filtre == null
                    || ev.getNom().toLowerCase().contains(filtre)
                    || ev.getLieu().toLowerCase().contains(filtre)) {
                
                VBox card = createCard(ev);
                flowPane.getChildren().add(card);
                animateCardEntrance(card, delay);
                delay += 80;
                count++;
            }
        }

        if (lblCompteur != null) {
            lblCompteur.setText(count + " événement" + (count > 1 ? "s" : ""));
        }
    }

    private void animateCardEntrance(VBox card, int delayMs) {
        card.setOpacity(0);
        card.setTranslateY(30);
        PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setFromValue(0); fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
            slide.setFromY(30); slide.setToY(0);
            new ParallelTransition(fade, slide).play();
        });
        pause.play();
    }

    private VBox createCard(Evenement ev) {
        VBox card = new VBox(0);
        card.setPrefWidth(360);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #D6A46D; -fx-border-radius: 14; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3); -fx-cursor: hand;");

        // Hover Effect
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02); st.setToY(1.02); st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        // Content
        VBox body = new VBox(10);
        body.setPadding(new Insets(16));

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(ev.getImage() != null ? ev.getImage() : ImageGeneratorService.genererImageParDefaut(), true));
        } catch (Exception e) {
            img.setImage(new Image(ImageGeneratorService.genererImageParDefaut()));
        }
        img.setFitWidth(328);
        img.setFitHeight(180);
        img.setPreserveRatio(false);
        img.setStyle("-fx-background-radius: 10;");

        Label title = new Label(ev.getNom());
        title.setFont(Font.font("System", FontWeight.BOLD, 17));
        title.setTextFill(Color.web("#1E293B"));

        Label coach = new Label("👤 Coach: " + ev.getCoach_name());
        coach.setStyle("-fx-text-fill: #64748B;");
        Label lieu  = new Label("📍 " + ev.getLieu());
        Label date  = new Label("📅 " + ev.getDate_debut().toString().replace("T", " "));

        // Barre de participation
        VBox participationBox = creerBarreParticipation(ev);

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button btnMap = new Button("📍 Carte");
        btnMap.setPrefWidth(110);
        btnMap.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 8; -fx-cursor: hand;");
        btnMap.setOnAction(e -> ouvrirCarte(ev));

        Button btnIA = new Button("✨ Aide IA");
        btnIA.setPrefWidth(110);
        btnIA.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-background-radius: 8; -fx-cursor: hand;");
        btnIA.setOnAction(e -> handleAideIA(ev));

        actionButtons.getChildren().addAll(btnMap, btnIA);

        Button btnInscrire = new Button(ev.getPrix() > 0 ? "S'inscrire (💰 " + ev.getPrix() + " TND)" : "Participer (GRATUIT)");
        btnInscrire.setMaxWidth(Double.MAX_VALUE);
        btnInscrire.setPrefHeight(40);
        btnInscrire.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        btnInscrire.setOnAction(e -> handleInscription(ev));

        body.getChildren().addAll(img, title, coach, lieu, date, participationBox, actionButtons, btnInscrire);
        card.getChildren().add(body);
        return card;
    }

    /**
     * Crée une barre de progression de participation ultra-professionnelle
     */
    private VBox creerBarreParticipation(Evenement ev) {
        VBox container = new VBox(6);
        container.setPadding(new Insets(8, 0, 8, 0));
        
        // Header avec icône et texte
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font(14));
        
        Label textLabel;
        if (ev.estIllimite()) {
            textLabel = new Label("Participation : " + ev.getNbParticipants() + " inscrits");
            textLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            textLabel = new Label("Participation : " + ev.getNbParticipants() + " / " + ev.getCapacite());
            textLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label percentLabel = new Label();
        if (!ev.estIllimite()) {
            int percent = (int) (ev.getTauxRemplissage() * 100);
            percentLabel.setText(percent + "%");
            percentLabel.setStyle("-fx-text-fill: #2E7D5A; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
        
        header.getChildren().addAll(iconLabel, textLabel, spacer, percentLabel);
        
        // Barre de progression
        if (!ev.estIllimite()) {
            StackPane progressBar = new StackPane();
            progressBar.setPrefHeight(8);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            
            // Background de la barre
            Region background = new Region();
            background.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 4;");
            background.setPrefHeight(8);
            background.setMaxWidth(Double.MAX_VALUE);
            
            // Barre de progression remplie
            Region fill = new Region();
            double tauxRemplissage = ev.getTauxRemplissage();
            
            // Couleur selon le taux de remplissage
            String couleur;
            if (tauxRemplissage >= 0.9) {
                couleur = "linear-gradient(to right, #EF4444, #DC2626)"; // Rouge si presque complet
            } else if (tauxRemplissage >= 0.7) {
                couleur = "linear-gradient(to right, #F59E0B, #D97706)"; // Orange si 70%+
            } else {
                couleur = "linear-gradient(to right, #2E7D5A, #1F4D3A)"; // Vert sinon
            }
            
            fill.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 4;");
            fill.setPrefHeight(8);
            fill.setMaxWidth(328 * tauxRemplissage); // Largeur proportionnelle
            
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            progressBar.getChildren().addAll(background, fill);
            
            // Badge de statut
            HBox statusBox = new HBox(5);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(4, 0, 0, 0));
            
            Label statusBadge = new Label();
            if (ev.estComplet()) {
                statusBadge.setText("🔴 COMPLET");
                statusBadge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                                   "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            } else if (ev.getPlacesRestantes() <= 5) {
                statusBadge.setText("⚠️ " + ev.getPlacesRestantes() + " places restantes");
                statusBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; " +
                                   "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            } else {
                statusBadge.setText("✅ " + ev.getPlacesRestantes() + " places disponibles");
                statusBadge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #059669; " +
                                   "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            }
            
            statusBox.getChildren().add(statusBadge);
            
            container.getChildren().addAll(header, progressBar, statusBox);
        } else {
            // Pour les événements illimités, juste afficher le header
            Label illimiteLabel = new Label("♾️ Places illimitées");
            illimiteLabel.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; " +
                                  "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            container.getChildren().addAll(header, illimiteLabel);
        }
        
        return container;
    }

    private void handleAideIA(Evenement ev) {
        // Ouvrir directement le chat IA sans dialogue intermédiaire
        tn.esprit.projet.utils.IADialogUtil.ouvrirAssistantIA();
    }


    private void ouvrirCarte(Evenement ev) {
        Stage stage = new Stage();
        stage.setTitle("📍 Localisation : " + ev.getLieu());

        WebView wv = new WebView();

        // Enable JavaScript
        wv.getEngine().setJavaScriptEnabled(true);

        // Encode location for Google Maps query
        String lieuEncoded;
        try {
            lieuEncoded = java.net.URLEncoder.encode(ev.getLieu(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            lieuEncoded = ev.getLieu().replace(" ", "+");
        }

        // Professional Google Maps Embed inside an iframe wrapper
        String mapUrl = "https://www.google.com/maps?q=" + lieuEncoded + "&output=embed&hl=fr";
        String html = "<!DOCTYPE html><html><head>" +
                      "<style>body,html{margin:0;padding:0;height:100%;overflow:hidden;}</style>" +
                      "</head><body>" +
                      "<iframe width=\"100%\" height=\"100%\" frameborder=\"0\" style=\"border:0\" src=\"" + mapUrl + "\" allowfullscreen></iframe>" +
                      "</body></html>";
        wv.getEngine().loadContent(html, "text/html");

        VBox root = new VBox();
        root.getChildren().add(wv);
        VBox.setVgrow(wv, Priority.ALWAYS);

        stage.setScene(new Scene(root, 850, 620));
        stage.show();
    }

    private void handleInscription(Evenement ev) {
        // Utiliser le dialogue d'inscription ultra-professionnel
        InscriptionDialog inscriptionDialog = new InscriptionDialog(ev);
        boolean confirmed = inscriptionDialog.showAndWait();
        
        if (confirmed) {
            String nom = inscriptionDialog.getNom();
            String email = inscriptionDialog.getEmail();
            String telephone = inscriptionDialog.getTelephone();
            
            // Envoyer l'email moderne
            EmailServiceModerne emailServiceModerne = new EmailServiceModerne();
            boolean success = emailServiceModerne.envoyerEmailConfirmation(nom, email, telephone, ev);
            
            if (success) {
                tn.esprit.projet.utils.AlertUtil.showSuccess("Inscription réussie", 
                    "Un email de confirmation moderne vous a été envoyé ! ✨\nConsultez votre boîte mail.");
            } else {
                tn.esprit.projet.utils.AlertUtil.showWarning("Inscription enregistrée", 
                    "Votre inscription est confirmée mais l'email n'a pas pu être envoyé.");
            }
        }
    }


    @FXML
    public void handleAssistantIA() {
        tn.esprit.projet.utils.IADialogUtil.ouvrirAssistantIA();
    }

    @FXML
    public void handleCalendrier() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/FrontCalendrier.fxml"));
            javafx.scene.Parent calendrier = loader.load();
            
            // Trouver le contentArea du MainLayout
            javafx.scene.Parent parent = flowPane.getParent();
            javafx.scene.layout.StackPane contentArea = null;
            
            // Remonter dans la hiérarchie pour trouver le contentArea
            while (parent != null) {
                if (parent instanceof javafx.scene.layout.StackPane && parent.getId() != null && parent.getId().equals("contentArea")) {
                    contentArea = (javafx.scene.layout.StackPane) parent;
                    break;
                }
                // Si on trouve un StackPane sans ID, c'est probablement le contentArea
                if (parent instanceof javafx.scene.layout.StackPane) {
                    contentArea = (javafx.scene.layout.StackPane) parent;
                }
                parent = parent.getParent();
            }
            
            // Remplacer le contenu
            if (contentArea != null) {
                contentArea.getChildren().setAll(calendrier);
            } else {
                tn.esprit.projet.utils.AlertUtil.showError("Erreur", "Impossible de trouver la zone de contenu");
            }
        } catch (Exception e) {
            e.printStackTrace();
            tn.esprit.projet.utils.AlertUtil.showError("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
        }
    }
}
