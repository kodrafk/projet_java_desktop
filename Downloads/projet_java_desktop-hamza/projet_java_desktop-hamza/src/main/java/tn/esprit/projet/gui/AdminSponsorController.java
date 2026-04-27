package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.services.SponsorImageService;

import java.util.List;

/**
 * Contrôleur moderne pour la gestion des sponsors avec affichage en cards
 * et génération automatique d'images intelligentes basées sur le nom de marque
 */
public class AdminSponsorController {

    @FXML private ComboBox<Evenement> cbEvenement;
    @FXML private TextField tfSponsorNom;
    @FXML private TextField tfSponsorType;
    @FXML private TextField tfSponsorLogo;
    @FXML private TextArea taSponsorDesc;
    @FXML private FlowPane flowPaneSponsors;
    @FXML private TextField tfRecherche;
    @FXML private Label lblCompteur;

    private final SponsorService sponsorService = new SponsorService();
    private final EvenementService eventService = new EvenementService();

    @FXML
    public void initialize() {
        // Charger les événements dans le ComboBox
        if (cbEvenement != null) {
            cbEvenement.setItems(FXCollections.observableArrayList(eventService.getAll()));
            cbEvenement.setCellFactory(lv -> new ListCell<Evenement>() {
                @Override
                protected void updateItem(Evenement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNom());
                }
            });
            cbEvenement.setButtonCell(cbEvenement.getCellFactory().call(null));
        }

        // Charger les sponsors en cards
        chargerSponsors();

        // Recherche en temps réel
        if (tfRecherche != null) {
            tfRecherche.textProperty().addListener((obs, old, nouveau) -> filtrerSponsors(nouveau));
        }
    }

    @FXML
    public void handleAjouterSponsor() {
        Evenement ev = cbEvenement.getValue();
        if (ev == null || tfSponsorNom.getText().isEmpty()) {
            afficherAlerte("Erreur", "Veuillez sélectionner un événement et saisir un nom.");
            return;
        }

        Sponsor s = new Sponsor(
            0,
            tfSponsorNom.getText().trim(),
            tfSponsorType.getText().isEmpty() ? "Standard" : tfSponsorType.getText().trim(),
            taSponsorDesc.getText(),
            "Actif",
            tfSponsorLogo.getText().trim(),
            null,
            ev.getId()
        );

        sponsorService.ajouter(s);

        // Réinitialiser le formulaire
        tfSponsorNom.clear();
        tfSponsorType.clear();
        tfSponsorLogo.clear();
        taSponsorDesc.clear();
        cbEvenement.setValue(null);

        // Recharger l'affichage
        chargerSponsors();
        afficherAlerte("Succès", "Sponsor ajouté avec succès !");
    }

    // ════════════════════════════════════════════════════════
    //  CHARGEMENT ET FILTRAGE
    // ════════════════════════════════════════════════════════

    private void chargerSponsors() {
        if (flowPaneSponsors == null) return;

        flowPaneSponsors.getChildren().clear();
        List<Sponsor> sponsors = sponsorService.getAll();

        System.out.println("📊 Chargement de " + sponsors.size() + " sponsors");

        // Mettre à jour le compteur
        if (lblCompteur != null) {
            lblCompteur.setText(sponsors.size() + " sponsor" + (sponsors.size() > 1 ? "s" : ""));
        }

        if (sponsors.isEmpty()) {
            Label lblVide = new Label("Aucun sponsor pour le moment.\nAjoutez-en un avec le formulaire ci-dessus !");
            lblVide.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 40;");
            lblVide.setWrapText(true);
            flowPaneSponsors.getChildren().add(lblVide);
        } else {
            for (Sponsor sponsor : sponsors) {
                System.out.println("  → Création card pour : " + sponsor.getNom_partenaire());
                flowPaneSponsors.getChildren().add(creerCardSponsor(sponsor));
            }
        }
    }

    private void filtrerSponsors(String recherche) {
        if (flowPaneSponsors == null) return;

        flowPaneSponsors.getChildren().clear();
        List<Sponsor> sponsors = sponsorService.getAll();

        String rech = recherche == null ? "" : recherche.toLowerCase().trim();
        long count = 0;
        for (Sponsor sponsor : sponsors) {
            if (rech.isEmpty()
                    || sponsor.getNom_partenaire().toLowerCase().contains(rech)
                    || sponsor.getType().toLowerCase().contains(rech)) {
                flowPaneSponsors.getChildren().add(creerCardSponsor(sponsor));
                count++;
            }
        }

        if (lblCompteur != null) {
            lblCompteur.setText(count + " sponsor" + (count > 1 ? "s" : ""));
        }
    }

    // ════════════════════════════════════════════════════════
    //  CRÉATION DE CARD
    // ════════════════════════════════════════════════════════

    private VBox creerCardSponsor(Sponsor sponsor) {
        VBox card = new VBox(14);
        card.setPrefWidth(280);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(0, 0, 20, 0));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #6366f1;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 20, 0, 0, 6);" +
            "-fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        ));

        // ── Image en haut de la card ──
        StackPane imageContainer = creerImageContainer(sponsor);

        // ── Contenu texte ──
        VBox content = new VBox(10);
        content.setPadding(new Insets(0, 16, 0, 16));
        content.setAlignment(Pos.TOP_CENTER);

        // Nom du sponsor
        Label lblNom = new Label(sponsor.getNom_partenaire());
        lblNom.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblNom.setTextFill(Color.web("#1e293b"));
        lblNom.setWrapText(true);
        lblNom.setAlignment(Pos.CENTER);
        lblNom.setMaxWidth(248);

        // Badge type
        Label lblType = new Label("  " + sponsor.getType() + "  ");
        lblType.setPadding(new Insets(4, 12, 4, 12));
        lblType.setStyle(getBadgeStyle(sponsor.getType()));
        lblType.setFont(Font.font("System", FontWeight.BOLD, 11));

        // Description
        String descText = (sponsor.getDescription() != null && !sponsor.getDescription().isEmpty())
                ? sponsor.getDescription()
                : "Partenaire officiel";
        Label lblDesc = new Label(descText);
        lblDesc.setFont(Font.font("System", 12));
        lblDesc.setTextFill(Color.web("#64748b"));
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(248);
        lblDesc.setAlignment(Pos.CENTER);
        lblDesc.setMaxHeight(52);

        // Statut
        HBox statusBox = new HBox(6);
        statusBox.setAlignment(Pos.CENTER);
        Circle statusDot = new Circle(4);
        statusDot.setFill("Actif".equalsIgnoreCase(sponsor.getStatu())
                ? Color.web("#10b981") : Color.web("#ef4444"));
        Label lblStatus = new Label(sponsor.getStatu());
        lblStatus.setFont(Font.font("System", 11));
        lblStatus.setTextFill(Color.web("#64748b"));
        statusBox.getChildren().addAll(statusDot, lblStatus);

        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button btnModifier = new Button("✏ Modifier");
        btnModifier.setStyle(
            "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;" +
            "-fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14;"
        );
        btnModifier.setOnMouseEntered(e -> btnModifier.setStyle(
            "-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b;" +
            "-fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14;"
        ));
        btnModifier.setOnMouseExited(e -> btnModifier.setStyle(
            "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;" +
            "-fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14;"
        ));
        btnModifier.setOnAction(e -> modifierSponsor(sponsor));

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle(
            "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;" +
            "-fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14;"
        );
        btnSupprimer.setOnMouseEntered(e -> btnSupprimer.setStyle(
            "-fx-background-color: #fecaca; -fx-text-fill: #b91c1c;" +
            "-fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14;"
        ));
        btnSupprimer.setOnMouseExited(e -> btnSupprimer.setStyle(
            "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;" +
            "-fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14;"
        ));
        btnSupprimer.setOnAction(e -> supprimerSponsor(sponsor));

        actions.getChildren().addAll(btnModifier, btnSupprimer);

        content.getChildren().addAll(lblNom, lblType, lblDesc, statusBox, actions);
        card.getChildren().addAll(imageContainer, content);

        return card;
    }

    // ════════════════════════════════════════════════════════
    //  IMAGE CONTAINER — chargement intelligent avec fallback
    // ════════════════════════════════════════════════════════

    private StackPane creerImageContainer(Sponsor sponsor) {
        StackPane container = new StackPane();
        container.setPrefWidth(280);
        container.setPrefHeight(160);
        container.setMaxHeight(160);

        // Couleur de fond selon le type (pendant le chargement)
        String bgColor = getCouleurFond(sponsor.getType());
        container.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 16 16 0 0;"
        );

        // Clip pour arrondir les coins supérieurs
        Rectangle clip = new Rectangle(280, 160);
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        container.setClip(clip);

        // Placeholder pendant le chargement
        VBox placeholder = creerPlaceholder(sponsor);
        container.getChildren().add(placeholder);

        // Déterminer l'URL de l'image
        String imageUrl = null;

        // 1. Si le sponsor a un logo manuel, l'utiliser en priorité
        if (sponsor.getLogo() != null && !sponsor.getLogo().trim().isEmpty()) {
            imageUrl = sponsor.getLogo().trim();
            System.out.println("🖼️ Logo manuel pour " + sponsor.getNom_partenaire() + ": " + imageUrl);
        } else {
            // 2. Générer intelligemment selon le nom et type
            imageUrl = SponsorImageService.genererImageSponsor(
                sponsor.getNom_partenaire(),
                sponsor.getType()
            );
        }

        // Charger l'image de façon asynchrone
        final String finalUrl = imageUrl;
        chargerImageAsync(finalUrl, container, placeholder, sponsor);

        return container;
    }

    /**
     * Charge une image de façon asynchrone et met à jour le container
     */
    private void chargerImageAsync(String imageUrl, StackPane container,
                                    VBox placeholder, Sponsor sponsor) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            System.out.println("⚠️ URL vide pour " + sponsor.getNom_partenaire() + " — placeholder affiché");
            return;
        }

        try {
            // Chargement asynchrone (background=true)
            Image image = new Image(imageUrl, 280, 160, false, true, true);

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    Platform.runLater(() -> {
                        if (!image.isError()) {
                            afficherImage(image, container, placeholder);
                        } else {
                            System.err.println("❌ Erreur image pour " + sponsor.getNom_partenaire()
                                    + " — URL: " + imageUrl);
                            // Essayer une image de fallback
                            chargerImageFallback(container, placeholder, sponsor);
                        }
                    });
                }
            });

            image.errorProperty().addListener((obs, oldErr, newErr) -> {
                if (newErr) {
                    Platform.runLater(() -> {
                        System.err.println("❌ Erreur chargement: " + imageUrl);
                        chargerImageFallback(container, placeholder, sponsor);
                    });
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Exception image: " + e.getMessage());
            chargerImageFallback(container, placeholder, sponsor);
        }
    }

    /**
     * Charge une image de fallback si la première échoue
     */
    private void chargerImageFallback(StackPane container, VBox placeholder, Sponsor sponsor) {
        String fallbackUrl = SponsorImageService.getImageParDefaut();
        try {
            Image fallback = new Image(fallbackUrl, 280, 160, false, true, true);
            fallback.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    Platform.runLater(() -> {
                        if (!fallback.isError()) {
                            afficherImage(fallback, container, placeholder);
                        }
                        // Si même le fallback échoue, le placeholder reste affiché
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Fallback aussi échoué: " + e.getMessage());
        }
    }

    /**
     * Affiche l'image dans le container en remplaçant le placeholder
     */
    private void afficherImage(Image image, StackPane container, VBox placeholder) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(280);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        // Overlay gradient léger pour améliorer la lisibilité
        Pane overlay = new Pane();
        overlay.setPrefSize(280, 160);
        overlay.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.0), rgba(0,0,0,0.15));");

        container.getChildren().remove(placeholder);
        container.getChildren().addAll(imageView, overlay);
    }

    /**
     * Crée un placeholder stylisé avec initiales et icône pendant le chargement
     */
    private VBox creerPlaceholder(Sponsor sponsor) {
        VBox placeholder = new VBox(8);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPrefSize(280, 160);

        // Initiales du sponsor
        String initiales = getInitiales(sponsor.getNom_partenaire());
        Label lblInitiales = new Label(initiales);
        lblInitiales.setFont(Font.font("System", FontWeight.BOLD, 36));
        lblInitiales.setTextFill(Color.WHITE);
        lblInitiales.setStyle("-fx-opacity: 0.9;");

        // Nom sous les initiales
        Label lblNom = new Label(sponsor.getNom_partenaire());
        lblNom.setFont(Font.font("System", 11));
        lblNom.setTextFill(Color.web("#FFFFFF", 0.7));

        placeholder.getChildren().addAll(lblInitiales, lblNom);
        return placeholder;
    }

    // ════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════

    /**
     * Retourne les initiales d'un nom (max 2 lettres)
     */
    private String getInitiales(String nom) {
        if (nom == null || nom.trim().isEmpty()) return "?";
        String[] mots = nom.trim().split("\\s+");
        if (mots.length == 1) {
            return mots[0].substring(0, Math.min(2, mots[0].length())).toUpperCase();
        }
        return (mots[0].charAt(0) + "" + mots[1].charAt(0)).toUpperCase();
    }

    /**
     * Retourne une couleur de fond selon le type de partenariat
     */
    private String getCouleurFond(String type) {
        if (type == null) return "linear-gradient(to bottom right, #6366f1, #8b5cf6)";
        switch (type.toLowerCase().trim()) {
            case "gold":
            case "or":
                return "linear-gradient(to bottom right, #f59e0b, #d97706)";
            case "silver":
            case "argent":
                return "linear-gradient(to bottom right, #64748b, #475569)";
            case "bronze":
                return "linear-gradient(to bottom right, #b45309, #92400e)";
            case "platinum":
            case "platine":
                return "linear-gradient(to bottom right, #6366f1, #4f46e5)";
            default:
                return "linear-gradient(to bottom right, #0ea5e9, #0284c7)";
        }
    }

    /**
     * Retourne le style CSS pour le badge selon le type
     */
    private String getBadgeStyle(String type) {
        if (type == null) type = "Standard";
        switch (type.toLowerCase().trim()) {
            case "gold":
            case "or":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 12;";
            case "silver":
            case "argent":
                return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 12;";
            case "bronze":
                return "-fx-background-color: #fed7aa; -fx-text-fill: #92400e; -fx-background-radius: 12;";
            case "platinum":
            case "platine":
                return "-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-background-radius: 12;";
            default:
                return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-background-radius: 12;";
        }
    }

    // ════════════════════════════════════════════════════════
    //  ACTIONS CRUD
    // ════════════════════════════════════════════════════════

    private void modifierSponsor(Sponsor sponsor) {
        tfSponsorNom.setText(sponsor.getNom_partenaire());
        tfSponsorType.setText(sponsor.getType());
        tfSponsorLogo.setText(sponsor.getLogo() != null ? sponsor.getLogo() : "");
        taSponsorDesc.setText(sponsor.getDescription() != null ? sponsor.getDescription() : "");

        for (Evenement ev : cbEvenement.getItems()) {
            if (ev.getId() == sponsor.getEvenement_id()) {
                cbEvenement.setValue(ev);
                break;
            }
        }
    }

    private void supprimerSponsor(Sponsor sponsor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le sponsor ?");
        confirm.setContentText("Voulez-vous vraiment supprimer « " + sponsor.getNom_partenaire() + " » ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            sponsorService.supprimer(sponsor.getId());
            chargerSponsors();
            afficherAlerte("Succès", "Sponsor supprimé avec succès !");
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
