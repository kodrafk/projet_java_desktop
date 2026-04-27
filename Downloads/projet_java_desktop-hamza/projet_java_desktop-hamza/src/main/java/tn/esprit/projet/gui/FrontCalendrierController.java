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
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.services.CalendrierService;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.MeteoService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Contrôleur pour la vue calendrier côté client
 * Affiche les événements dans un calendrier mensuel interactif
 */
public class FrontCalendrierController {

    @FXML private Label lblMoisAnnee;
    @FXML private GridPane calendrierGrid;
    @FXML private VBox evenementsJourBox;
    @FXML private Label lblDateSelectionnee;
    @FXML private ScrollPane scrollEvenements;
    @FXML private HBox statsBox;
    @FXML private Label lblTotalMois;
    @FXML private Label lblAvenir;
    @FXML private Label lblSemaine;
    
    private final CalendrierService calendrierService = new CalendrierService();
    private final EvenementService evenementService = new EvenementService();
    private final MeteoService meteoService = new MeteoService();
    
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

    /**
     * Affiche le calendrier du mois courant
     */
    private void afficherCalendrier() {
        if (calendrierGrid == null) return;
        
        calendrierGrid.getChildren().clear();
        calendrierGrid.getRowConstraints().clear();
        calendrierGrid.getColumnConstraints().clear();
        
        // Mise à jour du titre
        if (lblMoisAnnee != null) {
            String moisTexte = moisCourant.format(FORMAT_MOIS);
            lblMoisAnnee.setText(moisTexte.substring(0, 1).toUpperCase() + moisTexte.substring(1));
        }
        
        // Configuration des colonnes (7 jours)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            calendrierGrid.getColumnConstraints().add(col);
        }
        
        // En-têtes des jours de la semaine
        for (int i = 0; i < 7; i++) {
            Label header = new Label(JOURS_SEMAINE[i]);
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            header.setFont(Font.font("System", FontWeight.BOLD, 12));
            header.setTextFill(Color.web("#1F4D3A"));
            header.setStyle(
                "-fx-background-color: #E8F5E9;" +
                "-fx-padding: 10;" +
                "-fx-border-color: #C8E6C9;" +
                "-fx-border-width: 0 0 2 0;"
            );
            calendrierGrid.add(header, i, 0);
        }
        
        // Récupérer le compteur d'événements pour le mois
        Map<LocalDate, Integer> compteurEvenements = calendrierService.getCompteurEvenementsMois(moisCourant);
        
        // Calculer le premier jour du mois et le nombre de jours
        LocalDate premierJour = moisCourant.atDay(1);
        int jourSemainePremier = premierJour.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche
        int nombreJours = moisCourant.lengthOfMonth();
        
        // Remplir le calendrier
        int ligne = 1;
        int colonne = jourSemainePremier - 1; // Ajuster pour commencer à Lundi
        
        for (int jour = 1; jour <= nombreJours; jour++) {
            LocalDate date = moisCourant.atDay(jour);
            int nbEvenements = compteurEvenements.getOrDefault(date, 0);
            
            VBox cellule = creerCelluleJour(jour, date, nbEvenements);
            calendrierGrid.add(cellule, colonne, ligne);
            
            colonne++;
            if (colonne == 7) {
                colonne = 0;
                ligne++;
            }
        }
        
        // Ajouter des contraintes de ligne
        for (int i = 0; i <= ligne; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(80);
            row.setVgrow(Priority.ALWAYS);
            calendrierGrid.getRowConstraints().add(row);
        }
    }
    
    /**
     * Crée une cellule de jour pour le calendrier
     */
    private VBox creerCelluleJour(int jour, LocalDate date, int nbEvenements) {
        VBox cellule = new VBox(4);
        cellule.setAlignment(Pos.TOP_CENTER);
        cellule.setPadding(new Insets(8));
        cellule.setMaxWidth(Double.MAX_VALUE);
        cellule.setMaxHeight(Double.MAX_VALUE);
        
        boolean estAujourdhui = date.equals(LocalDate.now());
        boolean estSelectionne = date.equals(dateSelectionnee);
        boolean aEvenements = nbEvenements > 0;
        
        // Style de base
        String styleBase = "-fx-background-color: white;" +
                          "-fx-border-color: #E0E0E0;" +
                          "-fx-border-width: 0.5;" +
                          "-fx-cursor: hand;";
        
        if (estAujourdhui) {
            styleBase = "-fx-background-color: #E8F5E9;" +
                       "-fx-border-color: #1F4D3A;" +
                       "-fx-border-width: 2;" +
                       "-fx-cursor: hand;";
        } else if (estSelectionne) {
            styleBase = "-fx-background-color: #C8E6C9;" +
                       "-fx-border-color: #2E7D5A;" +
                       "-fx-border-width: 2;" +
                       "-fx-cursor: hand;";
        }
        
        final String styleBaseFinal = styleBase; // Copie finale pour la lambda
        cellule.setStyle(styleBase);
        
        // Numéro du jour
        Label lblJour = new Label(String.valueOf(jour));
        lblJour.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblJour.setTextFill(estAujourdhui ? Color.web("#1F4D3A") : Color.web("#333333"));
        
        // Badge nombre d'événements
        if (aEvenements) {
            HBox badge = new HBox();
            badge.setAlignment(Pos.CENTER);
            badge.setMaxWidth(50);
            badge.setStyle(
                "-fx-background-color: #1F4D3A;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 2 8;"
            );
            
            Label lblBadge = new Label(nbEvenements + " 📅");
            lblBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
            lblBadge.setTextFill(Color.WHITE);
            badge.getChildren().add(lblBadge);
            
            cellule.getChildren().addAll(lblJour, badge);
            
            // Animation du badge
            ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), badge);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        } else {
            cellule.getChildren().add(lblJour);
        }
        
        // Effet hover
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.rgb(31, 77, 58, 0.3));
        hoverShadow.setRadius(8);
        
        cellule.setOnMouseEntered(e -> {
            if (!estSelectionne) {
                cellule.setStyle(
                    "-fx-background-color: #F1F8F4;" +
                    "-fx-border-color: #2E7D5A;" +
                    "-fx-border-width: 2;" +
                    "-fx-cursor: hand;"
                );
            }
            cellule.setEffect(hoverShadow);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), cellule);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        cellule.setOnMouseExited(e -> {
            if (!estSelectionne) {
                cellule.setStyle(styleBaseFinal);
            }
            cellule.setEffect(null);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), cellule);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        // Clic pour sélectionner le jour
        cellule.setOnMouseClicked(e -> {
            dateSelectionnee = date;
            afficherCalendrier(); // Rafraîchir pour mettre à jour la sélection
            afficherEvenementsJour(date);
        });
        
        return cellule;
    }
    
    /**
     * Affiche les événements du jour sélectionné
     */
    private void afficherEvenementsJour(LocalDate date) {
        if (evenementsJourBox == null) return;
        
        evenementsJourBox.getChildren().clear();
        
        // Mise à jour du titre
        if (lblDateSelectionnee != null) {
            String dateTexte = date.format(FORMAT_DATE);
            lblDateSelectionnee.setText(dateTexte.substring(0, 1).toUpperCase() + dateTexte.substring(1));
        }
        
        List<Evenement> evenements = calendrierService.getEvenementsDuJour(date);
        
        if (evenements.isEmpty()) {
            // Message si aucun événement
            VBox emptyBox = new VBox(16);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            emptyBox.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 12;");
            
            Label icon = new Label("📅");
            icon.setFont(Font.font(48));
            
            Label message = new Label("Aucun événement ce jour");
            message.setFont(Font.font("System", FontWeight.BOLD, 14));
            message.setTextFill(Color.web("#64748B"));
            
            Label suggestion = new Label("Consultez les autres jours du calendrier");
            suggestion.setFont(Font.font("System", 12));
            suggestion.setTextFill(Color.web("#94A3B8"));
            
            emptyBox.getChildren().addAll(icon, message, suggestion);
            evenementsJourBox.getChildren().add(emptyBox);
        } else {
            // Afficher les événements
            int delay = 0;
            for (Evenement ev : evenements) {
                VBox card = creerCarteEvenement(ev);
                evenementsJourBox.getChildren().add(card);
                
                // Animation d'apparition
                animerEntree(card, delay);
                delay += 100;
            }
        }
    }
    
    /**
     * Crée une carte pour un événement
     */
    private VBox creerCarteEvenement(Evenement ev) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #D6A46D;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);" +
            "-fx-cursor: hand;"
        );
        
        // En-tête avec heure et statut
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label heure = new Label("🕐 " + ev.getDate_debut().toLocalTime().toString().substring(0, 5));
        heure.setFont(Font.font("System", FontWeight.BOLD, 13));
        heure.setTextFill(Color.web("#1F4D3A"));
        heure.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 4 10; -fx-background-radius: 8;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statut = new Label(ev.getStatut() != null ? ev.getStatut().toUpperCase() : "ACTIF");
        statut.setFont(Font.font("System", FontWeight.BOLD, 10));
        statut.setTextFill(Color.web("#166534"));
        statut.setStyle("-fx-background-color: #DCFCE7; -fx-padding: 3 10; -fx-background-radius: 12;");
        
        header.getChildren().addAll(heure, spacer, statut);
        
        // Titre
        Label titre = new Label(ev.getNom());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web("#1E293B"));
        titre.setWrapText(true);
        
        // Informations
        VBox infos = new VBox(6);
        
        Label coach = new Label("👤 Coach : " + ev.getCoach_name());
        coach.setFont(Font.font("System", 12));
        coach.setTextFill(Color.web("#475569"));
        
        Label lieu = new Label("📍 Lieu : " + MeteoService.getLieuPropre(ev.getLieu()));
        lieu.setFont(Font.font("System", 12));
        lieu.setTextFill(Color.web("#475569"));
        
        infos.getChildren().addAll(coach, lieu);
        
        // ── Badge météo (chargé en arrière-plan, uniquement pour plein air) ──
        HBox meteoBadge = new HBox(6);
        meteoBadge.setAlignment(Pos.CENTER_LEFT);
        meteoBadge.setPadding(new Insets(8, 12, 8, 12));
        meteoBadge.setStyle(
            "-fx-background-color: #F0F9FF;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #BAE6FD;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1.5;"
        );
        Label meteoLbl = new Label("🌍 Chargement météo...");
        meteoLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #0369A1; -fx-font-weight: bold;");
        meteoBadge.getChildren().add(meteoLbl);

        // Si pas plein air → masquer immédiatement, pas d'appel réseau
        if (!MeteoService.isOutdoor(ev.getLieu())) {
            meteoBadge.setVisible(false);
            meteoBadge.setManaged(false);
        } else {
            // Appel API en arrière-plan pour ne pas bloquer l'UI
            new Thread(() -> {
                MeteoService.MeteoResult meteo = meteoService.getMeteo(ev);
                javafx.application.Platform.runLater(() -> {
                    if (meteo.disponible) {
                        meteoLbl.setText(meteo.message);
                        // Couleur selon conditions
                        String bg, border, fg;
                        String desc = meteo.description.toLowerCase();
                        if (desc.contains("soleil") || desc.contains("dégagé") || desc.contains("clair")) {
                            bg = "#FEFCE8"; border = "#FDE047"; fg = "#854D0E";
                        } else if (desc.contains("pluie") || desc.contains("averse") || desc.contains("bruine")) {
                            bg = "#EFF6FF"; border = "#93C5FD"; fg = "#1E40AF";
                        } else if (desc.contains("orage") || desc.contains("tonnerre")) {
                            bg = "#F5F3FF"; border = "#C4B5FD"; fg = "#4C1D95";
                        } else if (desc.contains("neige") || desc.contains("grêle")) {
                            bg = "#F0F9FF"; border = "#7DD3FC"; fg = "#0C4A6E";
                        } else if (desc.contains("nuage") || desc.contains("couvert")) {
                            bg = "#F8FAFC"; border = "#CBD5E1"; fg = "#334155";
                        } else {
                            bg = "#F0FDF4"; border = "#86EFAC"; fg = "#166534";
                        }
                        meteoBadge.setStyle(
                            "-fx-background-color: " + bg + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: " + border + ";" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1.5;"
                        );
                        meteoLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + fg + "; -fx-font-weight: bold;");
                    } else {
                        // Plein air mais météo indisponible → afficher badge neutre
                        meteoLbl.setText("🌿 Plein air — météo indisponible");
                        meteoBadge.setStyle(
                            "-fx-background-color: #F0FDF4;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #86EFAC;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1.5;"
                        );
                        meteoLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #166534; -fx-font-weight: bold;");
                    }
                });
            }, "meteo-calendrier-" + ev.getId()).start();
        }
        
        infos.getChildren().add(meteoBadge);
        
        // Description
        if (ev.getDescription() != null && !ev.getDescription().isEmpty()) {
            Label desc = new Label(ev.getDescription());
            desc.setWrapText(true);
            desc.setFont(Font.font("System", 12));
            desc.setTextFill(Color.web("#64748B"));
            desc.setStyle("-fx-font-style: italic;");
            desc.setMaxHeight(60);
            infos.getChildren().add(desc);
        }
        
        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0;");
        
        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        
        Button btnDetails = new Button("📋 Voir détails");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDetails, Priority.ALWAYS);
        btnDetails.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;"
        );
        btnDetails.setOnMouseEntered(e -> btnDetails.setStyle(
            "-fx-background-color: #2563eb;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;"
        ));
        btnDetails.setOnMouseExited(e -> btnDetails.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;"
        ));
        btnDetails.setOnAction(e -> afficherDetailsEvenement(ev));
        
        // Bouton Voir détails prend toute la largeur
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDetails, Priority.ALWAYS);
        
        actions.getChildren().add(btnDetails);
        
        card.getChildren().addAll(header, titre, infos, sep, actions);
        
        // Effet hover sur la carte
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.rgb(214, 164, 109, 0.4));
        hoverShadow.setRadius(12);
        
        card.setOnMouseEntered(e -> {
            card.setEffect(hoverShadow);
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setEffect(null);
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        return card;
    }
    
    /**
     * Animation d'entrée pour les cartes
     */
    private void animerEntree(VBox card, int delayMs) {
        card.setOpacity(0);
        card.setTranslateX(-30);
        
        PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
            slide.setFromX(-30);
            slide.setToX(0);
            
            ParallelTransition parallel = new ParallelTransition(fade, slide);
            parallel.play();
        });
        pause.play();
    }
    
    /**
     * Affiche les statistiques du mois
     */
    private void afficherStatistiques() {
        if (lblTotalMois != null) {
            int total = calendrierService.getEvenementsDuMois(moisCourant).size();
            lblTotalMois.setText(String.valueOf(total));
        }
        
        if (lblAvenir != null) {
            int avenir = calendrierService.getEvenementsAvenir().size();
            lblAvenir.setText(String.valueOf(avenir));
        }
        
        if (lblSemaine != null) {
            int semaine = calendrierService.getEvenementsSemaine().size();
            lblSemaine.setText(String.valueOf(semaine));
        }
    }
    
    /**
     * Affiche les détails d'un événement dans une alerte
     */
    private void afficherDetailsEvenement(Evenement ev) {
        // Créer une fenêtre modale personnalisée professionnelle
        javafx.stage.Stage detailStage = new javafx.stage.Stage();
        detailStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails de l'événement");
        detailStage.setResizable(false);
        
        // Conteneur principal
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");
        
        // ═══ EN-TÊTE AVEC GRADIENT ═══
        VBox header = new VBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 28, 24, 28));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1F4D3A, #2E7D5A);"
        );
        
        // Titre de l'événement
        Label titre = new Label(ev.getNom());
        titre.setFont(Font.font("System", FontWeight.BOLD, 24));
        titre.setTextFill(Color.WHITE);
        titre.setWrapText(true);
        titre.setMaxWidth(450);
        
        // Badge statut
        HBox badgeBox = new HBox();
        badgeBox.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(ev.getStatut() != null ? ev.getStatut().toUpperCase() : "ACTIF");
        badge.setStyle(
            "-fx-background-color: rgba(255,255,255,0.25);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 4 14;"
        );
        badgeBox.getChildren().add(badge);
        
        header.getChildren().addAll(titre, badgeBox);
        
        // ═══ CORPS AVEC INFORMATIONS ═══
        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 28, 28, 28));
        
        // Grille d'informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(16);
        
        // Date
        addInfoRow(infoGrid, 0, "📅", "Date", 
            ev.getDate_debut().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)));
        
        // Heure
        addInfoRow(infoGrid, 1, "🕐", "Heure", 
            ev.getDate_debut().toLocalTime().toString().substring(0, 5) + 
            " - " + ev.getDate_fin().toLocalTime().toString().substring(0, 5));
        
        // Lieu
        addInfoRow(infoGrid, 2, "📍", "Lieu", ev.getLieu());
        
        // Coach
        addInfoRow(infoGrid, 3, "👤", "Coach", ev.getCoach_name());
        
        body.getChildren().add(infoGrid);
        
        // Description
        if (ev.getDescription() != null && !ev.getDescription().isEmpty()) {
            VBox descBox = new VBox(8);
            
            Label descLabel = new Label("📝 Description");
            descLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
            descLabel.setTextFill(Color.web("#1E293B"));
            
            Label descText = new Label(ev.getDescription());
            descText.setWrapText(true);
            descText.setMaxWidth(450);
            descText.setFont(Font.font("System", 13));
            descText.setTextFill(Color.web("#64748B"));
            descText.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 14;"
            );
            
            descBox.getChildren().addAll(descLabel, descText);
            body.getChildren().add(descBox);
        }
        
        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0;");
        body.getChildren().add(sep);
        
        // ═══ BOUTONS D'ACTION ═══
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Bouton Fermer
        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefWidth(120);
        btnFermer.setPrefHeight(40);
        btnFermer.setStyle(
            "-fx-background-color: #E2E8F0;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnFermer.setOnMouseEntered(e -> btnFermer.setStyle(
            "-fx-background-color: #CBD5E1;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btnFermer.setOnMouseExited(e -> btnFermer.setStyle(
            "-fx-background-color: #E2E8F0;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btnFermer.setOnAction(e -> detailStage.close());
        
        // Bouton Fermer centré
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(btnFermer);
        body.getChildren().add(buttonBox);
        
        root.getChildren().addAll(header, body);
        
        // Créer la scène
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 520, 480);
        detailStage.setScene(scene);
        
        // Animation d'ouverture
        root.setOpacity(0);
        root.setScaleX(0.9);
        root.setScaleY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), root);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.play();
        
        detailStage.showAndWait();
    }
    
    /**
     * Ajoute une ligne d'information dans la grille
     */
    private void addInfoRow(GridPane grid, int row, String icon, String label, String value) {
        // Icône
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));
        GridPane.setConstraints(iconLabel, 0, row);
        
        // Label
        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.BOLD, 13));
        labelText.setTextFill(Color.web("#64748B"));
        GridPane.setConstraints(labelText, 1, row);
        
        // Valeur
        Label valueText = new Label(value);
        valueText.setFont(Font.font("System", 14));
        valueText.setTextFill(Color.web("#1E293B"));
        valueText.setWrapText(true);
        valueText.setMaxWidth(300);
        GridPane.setConstraints(valueText, 2, row);
        
        grid.getChildren().addAll(iconLabel, labelText, valueText);
    }
    
    // ═══════════════════════════════════════════════════════
    //  NAVIGATION MOIS
    // ═══════════════════════════════════════════════════════
    
    @FXML
    public void handleMoisPrecedent() {
        moisCourant = moisCourant.minusMonths(1);
        afficherCalendrier();
        afficherStatistiques();
        
        // Animation de transition
        if (calendrierGrid != null) {
            TranslateTransition slide = new TranslateTransition(Duration.millis(300), calendrierGrid);
            slide.setFromX(30);
            slide.setToX(0);
            slide.play();
        }
    }
    
    @FXML
    public void handleMoisSuivant() {
        moisCourant = moisCourant.plusMonths(1);
        afficherCalendrier();
        afficherStatistiques();
        
        // Animation de transition
        if (calendrierGrid != null) {
            TranslateTransition slide = new TranslateTransition(Duration.millis(300), calendrierGrid);
            slide.setFromX(-30);
            slide.setToX(0);
            slide.play();
        }
    }
    
    @FXML
    public void handleAujourdhui() {
        moisCourant = YearMonth.now();
        dateSelectionnee = LocalDate.now();
        afficherCalendrier();
        afficherEvenementsJour(dateSelectionnee);
        afficherStatistiques();
        
        // Animation de zoom
        if (calendrierGrid != null) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), calendrierGrid);
            scale.setFromX(0.95);
            scale.setFromY(0.95);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        }
    }
}
