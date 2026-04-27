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
import tn.esprit.projet.services.CalendrierService;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.WeatherService;
import tn.esprit.projet.services.WeatherService.WeatherInfo;
import tn.esprit.projet.services.SmartWeatherService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.models.Sponsor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FrontCalendrierController {

    @FXML private Label lblMoisAnnee;
    @FXML private GridPane calendrierGrid;
    @FXML private VBox evenementsJourBox;
    @FXML private Label lblDateSelectionnee;
    @FXML private ScrollPane scrollEvenements;
    @FXML private Label lblTotalMois;
    @FXML private Label lblAvenir;
    @FXML private Label lblSemaine;
    
    private final CalendrierService calendrierService = new CalendrierService();
    private final SmartWeatherService smartWeatherService = new SmartWeatherService();
    private final SponsorService sponsorService = new SponsorService();
    
    private YearMonth moisCourant;
    private LocalDate dateSelectionnee;
    
    private static final String[] JOURS_SEMAINE = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
    private static final DateTimeFormatter FORMAT_MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    @FXML
    public void initialize() {
        moisCourant = YearMonth.now();
        dateSelectionnee = LocalDate.now();
        
        afficherCalendrier();
        afficherEvenementsJour(dateSelectionnee);
        afficherStatistiques();
    }

    private void afficherCalendrier() {
        if (calendrierGrid == null) return;
        calendrierGrid.getChildren().clear();
        calendrierGrid.getRowConstraints().clear();
        calendrierGrid.getColumnConstraints().clear();
        
        if (lblMoisAnnee != null) {
            String moisTexte = moisCourant.format(FORMAT_MOIS);
            lblMoisAnnee.setText(moisTexte.substring(0, 1).toUpperCase() + moisTexte.substring(1));
        }
        
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            calendrierGrid.getColumnConstraints().add(col);
        }
        
        for (int i = 0; i < 7; i++) {
            Label header = new Label(JOURS_SEMAINE[i]);
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            header.setFont(Font.font("System", FontWeight.BOLD, 12));
            header.setTextFill(Color.web("#1F4D3A"));
            header.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 10; -fx-border-color: #C8E6C9; -fx-border-width: 0 0 2 0;");
            calendrierGrid.add(header, i, 0);
        }
        
        LocalDate premierJour = moisCourant.atDay(1);
        int jourSemainePremier = premierJour.getDayOfWeek().getValue();
        int nombreJours = moisCourant.lengthOfMonth();
        
        int ligne = 1;
        int colonne = jourSemainePremier - 1;
        
        for (int jour = 1; jour <= nombreJours; jour++) {
            LocalDate date = moisCourant.atDay(jour);
            List<Evenement> evs = calendrierService.getEvenementsDuJour(date);
            int nbEvenements = evs.size();
            
            VBox cellule = creerCelluleJour(jour, date, nbEvenements);
            calendrierGrid.add(cellule, colonne, ligne);
            
            colonne++;
            if (colonne == 7) {
                colonne = 0;
                ligne++;
            }
        }
    }
    
    private VBox creerCelluleJour(int jour, LocalDate date, int nbEvenements) {
        VBox cellule = new VBox(4);
        cellule.setAlignment(Pos.TOP_CENTER);
        cellule.setPadding(new Insets(8));
        cellule.setMaxWidth(Double.MAX_VALUE);
        cellule.setPrefHeight(80);
        
        boolean estAujourdhui = date.equals(LocalDate.now());
        boolean estSelectionne = date.equals(dateSelectionnee);
        
        String style = "-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0.5; -fx-cursor: hand;";
        if (estAujourdhui) style = "-fx-background-color: #E8F5E9; -fx-border-color: #1F4D3A; -fx-border-width: 2; -fx-cursor: hand;";
        else if (estSelectionne) style = "-fx-background-color: #C8E6C9; -fx-border-color: #2E7D5A; -fx-border-width: 2; -fx-cursor: hand;";
        
        cellule.setStyle(style);
        
        Label lblJour = new Label(String.valueOf(jour));
        lblJour.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        cellule.getChildren().add(lblJour);
        if (nbEvenements > 0) {
            Label badge = new Label(nbEvenements + " 📅");
            badge.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 6; -fx-font-size: 9px;");
            cellule.getChildren().add(badge);
        }
        
        cellule.setOnMouseClicked(e -> {
            dateSelectionnee = date;
            afficherCalendrier();
            afficherEvenementsJour(date);
        });
        
        return cellule;
    }
    
    private void afficherEvenementsJour(LocalDate date) {
        if (evenementsJourBox == null) return;
        evenementsJourBox.getChildren().clear();
        
        if (lblDateSelectionnee != null) {
            lblDateSelectionnee.setText(date.format(FORMAT_DATE));
        }
        
        // PAS de barre météo globale - la météo est affichée dans chaque carte
        
        List<Evenement> evenements = calendrierService.getEvenementsDuJour(date);
        if (evenements.isEmpty()) {
            Label noEvent = new Label("Aucun événement ce jour");
            noEvent.setStyle("-fx-padding: 10; -fx-text-fill: #666;");
            evenementsJourBox.getChildren().add(noEvent);
        } else {
            for (Evenement ev : evenements) {
                VBox card = creerCarteEvenementAvecMeteo(ev);
                evenementsJourBox.getChildren().add(card);
            }
        }
    }
    
    
    private VBox creerCarteEvenementAvecMeteo(Evenement ev) {
        VBox card = new VBox(0);
        
        // Déterminer si c'est un événement extérieur
        boolean isOutdoor = smartWeatherService.isOutdoorEvent(ev);
        
        // Style de la carte ultra-professionnel avec ombre portée
        String borderColor = isOutdoor ? "#3B82F6" : "#F59E0B";
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 4);");
        
        // Barre de couleur en haut (accent visuel)
        Region topBar = new Region();
        topBar.setPrefHeight(6);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, " + borderColor + ", " + 
                       (isOutdoor ? "#60A5FA" : "#FBBF24") + "); " +
                       "-fx-background-radius: 16 16 0 0;");
        
        // Contenu principal avec padding
        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 18, 18, 18));
        
        // En-tête avec heure et badge de statut
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Heure avec fond
        HBox heureBox = new HBox(6);
        heureBox.setAlignment(Pos.CENTER);
        heureBox.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 8; -fx-padding: 6 12;");
        Label heureIcon = new Label("🕐");
        heureIcon.setFont(Font.font(13));
        Label heure = new Label(ev.getDate_debut().toLocalTime().toString());
        heure.setFont(Font.font("System", FontWeight.BOLD, 12));
        heure.setTextFill(Color.web("#475569"));
        heureBox.getChildren().addAll(heureIcon, heure);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Badge ACTIF avec animation
        Label badge = new Label("● ACTIF");
        badge.setStyle("-fx-background-color: linear-gradient(to right, #10B981, #059669); " +
                      "-fx-text-fill: white; " +
                      "-fx-background-radius: 20; -fx-padding: 6 14; " +
                      "-fx-font-size: 10px; -fx-font-weight: bold; " +
                      "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 8, 0, 0, 2);");
        
        header.getChildren().addAll(heureBox, spacer, badge);
        
        // Titre de l'événement avec style premium
        Label titre = new Label(ev.getNom());
        titre.setFont(Font.font("System", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#0F172A"));
        titre.setWrapText(true);
        titre.setStyle("-fx-padding: 4 0 0 0;");
        
        // Séparateur subtil
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 4 0;");
        
        // Section informations avec icônes colorées
        VBox infoSection = new VBox(10);
        
        // Coach avec design amélioré
        HBox coachBox = new HBox(10);
        coachBox.setAlignment(Pos.CENTER_LEFT);
        coachBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-padding: 10 14;");
        
        Label coachIcon = new Label("👤");
        coachIcon.setFont(Font.font(16));
        coachIcon.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 8; " +
                          "-fx-padding: 6; -fx-min-width: 32; -fx-alignment: center;");
        
        VBox coachInfo = new VBox(2);
        Label coachLabel = new Label("Coach");
        coachLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        coachLabel.setTextFill(Color.web("#64748B"));
        Label coachName = new Label(ev.getCoach_name() != null ? ev.getCoach_name() : "Non spécifié");
        coachName.setFont(Font.font("System", FontWeight.BOLD, 13));
        coachName.setTextFill(Color.web("#1E293B"));
        coachInfo.getChildren().addAll(coachLabel, coachName);
        
        coachBox.getChildren().addAll(coachIcon, coachInfo);
        
        // Lieu avec design amélioré
        HBox lieuBox = new HBox(10);
        lieuBox.setAlignment(Pos.CENTER_LEFT);
        lieuBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-padding: 10 14;");
        
        Label lieuIcon = new Label("📍");
        lieuIcon.setFont(Font.font(16));
        lieuIcon.setStyle("-fx-background-color: #FEE2E2; -fx-background-radius: 8; " +
                         "-fx-padding: 6; -fx-min-width: 32; -fx-alignment: center;");
        
        VBox lieuInfo = new VBox(2);
        Label lieuLabel = new Label("Lieu");
        lieuLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        lieuLabel.setTextFill(Color.web("#64748B"));
        Label lieuName = new Label(ev.getLieu() != null ? ev.getLieu() : "Non spécifié");
        lieuName.setFont(Font.font("System", FontWeight.BOLD, 13));
        lieuName.setTextFill(Color.web("#1E293B"));
        lieuName.setWrapText(true);
        lieuInfo.getChildren().addAll(lieuLabel, lieuName);
        
        lieuBox.getChildren().addAll(lieuIcon, lieuInfo);
        
        infoSection.getChildren().addAll(coachBox, lieuBox);
        
        // Météo - UNIQUEMENT pour événements extérieurs
        if (isOutdoor) {
            WeatherInfo weather = smartWeatherService.getWeatherIfOutdoor(ev);
            
            if (weather != null) {
                // Séparateur avant météo
                Separator sep2 = new Separator();
                sep2.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 4 0;");
                
                // Encadré météo premium
                VBox meteoContainer = new VBox(10);
                meteoContainer.setStyle("-fx-background-color: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%); " +
                                       "-fx-background-radius: 12; -fx-padding: 14; " +
                                       "-fx-border-color: #F59E0B; -fx-border-width: 2; -fx-border-radius: 12; " +
                                       "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.2), 8, 0, 0, 2);");
                
                HBox meteoHeader = new HBox(10);
                meteoHeader.setAlignment(Pos.CENTER_LEFT);
                
                Label meteoIcon = new Label(weather.getWeatherEmoji());
                meteoIcon.setFont(Font.font(28));
                
                VBox meteoInfo = new VBox(2);
                Label meteoDesc = new Label("Principalement dégagé");
                meteoDesc.setFont(Font.font("System", FontWeight.BOLD, 13));
                meteoDesc.setTextFill(Color.web("#92400E"));
                
                Label meteoTemp = new Label(weather.getFormattedTemp());
                meteoTemp.setFont(Font.font("System", FontWeight.BOLD, 16));
                meteoTemp.setTextFill(Color.web("#B45309"));
                
                meteoInfo.getChildren().addAll(meteoDesc, meteoTemp);
                meteoHeader.getChildren().addAll(meteoIcon, meteoInfo);
                
                // Conseil santé avec icône
                HBox conseilBox = new HBox(8);
                conseilBox.setAlignment(Pos.CENTER_LEFT);
                conseilBox.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 8; -fx-padding: 8 12;");
                
                Label conseilIcon = new Label("💧");
                conseilIcon.setFont(Font.font(14));
                
                Label conseil = new Label("STAY HYDRATED");
                conseil.setFont(Font.font("System", FontWeight.BOLD, 11));
                conseil.setTextFill(Color.web("#1E40AF"));
                conseil.setStyle("-fx-font-style: italic;");
                
                conseilBox.getChildren().addAll(conseilIcon, conseil);
                
                meteoContainer.getChildren().addAll(meteoHeader, conseilBox);
                
                content.getChildren().addAll(header, titre, sep1, infoSection, sep2, meteoContainer);
            } else {
                content.getChildren().addAll(header, titre, sep1, infoSection);
            }
        } else {
            // Événement intérieur - design élégant
            Separator sep2 = new Separator();
            sep2.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 4 0;");
            
            HBox indoorBox = new HBox(10);
            indoorBox.setAlignment(Pos.CENTER_LEFT);
            indoorBox.setStyle("-fx-background-color: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%); " +
                             "-fx-background-radius: 12; -fx-padding: 12 16; " +
                             "-fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-border-radius: 12;");
            
            Label indoorIcon = new Label("🏢");
            indoorIcon.setFont(Font.font(20));
            
            VBox indoorInfo = new VBox(2);
            Label indoorTitle = new Label("Événement en intérieur");
            indoorTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            indoorTitle.setTextFill(Color.web("#374151"));
            
            Label indoorDesc = new Label("Pas de météo nécessaire");
            indoorDesc.setFont(Font.font("System", FontWeight.NORMAL, 10));
            indoorDesc.setTextFill(Color.web("#6B7280"));
            indoorDesc.setStyle("-fx-font-style: italic;");
            
            indoorInfo.getChildren().addAll(indoorTitle, indoorDesc);
            indoorBox.getChildren().addAll(indoorIcon, indoorInfo);
            
            content.getChildren().addAll(header, titre, sep1, infoSection, sep2, indoorBox);
        }
        
        // Barre de participation
        VBox participationBox = creerBarreParticipationCalendrier(ev);
        content.getChildren().add(participationBox);
        
        // Bouton "Vidéos d'échauffement" premium avec gradient
        Button btnDetails = new Button("🎥 Vidéos d'échauffement");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                           "-fx-text-fill: white; " +
                           "-fx-background-radius: 10; -fx-padding: 12; " +
                           "-fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);");
        
        btnDetails.setOnMouseEntered(e -> {
            btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #2563EB, #1D4ED8); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 10; -fx-padding: 12; " +
                               "-fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.5), 12, 0, 0, 3);");
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btnDetails);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        
        btnDetails.setOnMouseExited(e -> {
            btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 10; -fx-padding: 12; " +
                               "-fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);");
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btnDetails);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        // Action du bouton : ouvrir les vidéos d'échauffement
        btnDetails.setOnAction(e -> {
            tn.esprit.projet.gui.VideoEchauffementController.ouvrirVideosEchauffement(ev);
        });
        
        content.getChildren().add(btnDetails);
        
        // Section Sponsors (si disponibles)
        List<Sponsor> sponsors = sponsorService.getByEvenementId(ev.getId());
        if (sponsors != null && !sponsors.isEmpty()) {
            // Séparateur avant sponsors
            Separator sepSponsors = new Separator();
            sepSponsors.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 4 0;");
            content.getChildren().add(sepSponsors);
            
            // Titre sponsors
            Label sponsorsTitle = new Label("🤝 Partenaires");
            sponsorsTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            sponsorsTitle.setTextFill(Color.web("#64748B"));
            sponsorsTitle.setStyle("-fx-padding: 4 0 0 0;");
            content.getChildren().add(sponsorsTitle);
            
            // Container des logos sponsors
            FlowPane sponsorsFlow = new FlowPane();
            sponsorsFlow.setHgap(8);
            sponsorsFlow.setVgap(8);
            sponsorsFlow.setAlignment(Pos.CENTER_LEFT);
            sponsorsFlow.setStyle("-fx-padding: 8 0 0 0;");
            
            for (Sponsor sponsor : sponsors) {
                if (sponsor.getLogo() != null && !sponsor.getLogo().trim().isEmpty()) {
                    VBox sponsorBox = new VBox(4);
                    sponsorBox.setAlignment(Pos.CENTER);
                    sponsorBox.setStyle("-fx-background-color: white; " +
                                       "-fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                                       "-fx-border-radius: 8; -fx-background-radius: 8; " +
                                       "-fx-padding: 8; -fx-cursor: hand; " +
                                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
                    
                    // Logo du sponsor
                    javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView();
                    try {
                        logoView.setImage(new javafx.scene.image.Image(sponsor.getLogo(), true));
                        logoView.setFitWidth(60);
                        logoView.setFitHeight(40);
                        logoView.setPreserveRatio(true);
                    } catch (Exception e) {
                        // Si l'image ne charge pas, afficher un placeholder
                        Label placeholder = new Label("🏢");
                        placeholder.setFont(Font.font(24));
                        sponsorBox.getChildren().add(placeholder);
                    }
                    
                    if (logoView.getImage() != null) {
                        sponsorBox.getChildren().add(logoView);
                    }
                    
                    // Nom du sponsor (petit)
                    Label sponsorName = new Label(sponsor.getNom_partenaire());
                    sponsorName.setFont(Font.font("System", FontWeight.NORMAL, 9));
                    sponsorName.setTextFill(Color.web("#94A3B8"));
                    sponsorName.setMaxWidth(60);
                    sponsorName.setWrapText(true);
                    sponsorName.setAlignment(Pos.CENTER);
                    sponsorName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                    sponsorBox.getChildren().add(sponsorName);
                    
                    // Effet hover
                    sponsorBox.setOnMouseEntered(e -> {
                        sponsorBox.setStyle("-fx-background-color: #F8FAFC; " +
                                           "-fx-border-color: #3B82F6; -fx-border-width: 2; " +
                                           "-fx-border-radius: 8; -fx-background-radius: 8; " +
                                           "-fx-padding: 8; -fx-cursor: hand; " +
                                           "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);");
                    });
                    
                    sponsorBox.setOnMouseExited(e -> {
                        sponsorBox.setStyle("-fx-background-color: white; " +
                                           "-fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                                           "-fx-border-radius: 8; -fx-background-radius: 8; " +
                                           "-fx-padding: 8; -fx-cursor: hand; " +
                                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
                    });
                    
                    sponsorsFlow.getChildren().add(sponsorBox);
                }
            }
            
            if (!sponsorsFlow.getChildren().isEmpty()) {
                content.getChildren().add(sponsorsFlow);
            }
        }
        
        // Assemblage final
        card.getChildren().addAll(topBar, content);
        
        // Animation d'entrée
        card.setOpacity(0);
        card.setTranslateY(10);
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
        slide.setFromY(10);
        slide.setToY(0);
        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setDelay(Duration.millis(50));
        animation.play();
        
        return card;
    }
    
    private void afficherStatistiques() {
        if (lblTotalMois != null) lblTotalMois.setText(String.valueOf(calendrierService.getEvenementsDuMois(moisCourant).size()));
        if (lblAvenir != null) lblAvenir.setText(String.valueOf(calendrierService.getEvenementsAvenir().size()));
    }

    @FXML public void handleMoisPrecedent() { moisCourant = moisCourant.minusMonths(1); afficherCalendrier(); afficherStatistiques(); }
    @FXML public void handleMoisSuivant() { moisCourant = moisCourant.plusMonths(1); afficherCalendrier(); afficherStatistiques(); }
    @FXML public void handleAujourdhui() { moisCourant = YearMonth.now(); dateSelectionnee = LocalDate.now(); afficherCalendrier(); afficherEvenementsJour(dateSelectionnee); afficherStatistiques(); }
    
    @FXML
    public void handleRetourEvenements() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Front/FrontEvenement.fxml"));
            javafx.scene.Parent evenements = loader.load();
            
            // Trouver le contentArea du MainLayout
            javafx.scene.Parent parent = calendrierGrid.getParent();
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
                contentArea.getChildren().setAll(evenements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crée une barre de progression de participation pour le calendrier
     */
    private VBox creerBarreParticipationCalendrier(Evenement ev) {
        VBox container = new VBox(6);
        container.setPadding(new Insets(8, 0, 8, 0));
        
        // Séparateur avant la barre
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 0 0 8 0;");
        container.getChildren().add(sep);
        
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
            fill.setMaxWidth(400 * tauxRemplissage); // Largeur proportionnelle
            
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
}
