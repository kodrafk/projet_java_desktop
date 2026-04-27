package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.services.MeteoService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur ADMIN Événements + Sponsors.
 * Toutes les vues sont chargées dans le contentArea du AdminLayoutController.
 * Ce contrôleur ne change JAMAIS de scène principale.
 */
public class AdminEvenementController {

    // ── Vue liste événements ──
    @FXML private FlowPane flowPane;
    @FXML private TextField tfRechercheAdmin;

    // ── Formulaire ajout/modification événement ──
    @FXML private TextField tfNom, tfLieu, tfCoach, tfImageUrl, tfPrix, tfCapacite;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDateDebut;
    @FXML private Spinner<Integer> spHeure, spMinute;
    @FXML private Label lblFormTitre, lblImageName;
    @FXML private javafx.scene.image.ImageView imgPreview;
    @FXML private CheckBox cbPleinAir;
    
    private String selectedImageFileName = null; // Nom du fichier image sélectionné

    // ── Formulaire sponsor (page AdminSponsor) ──
    @FXML private TextField tfSponsorNom, tfSponsorType, tfSponsorLogo;
    @FXML private TextArea taSponsorDesc;
    @FXML private ComboBox<Evenement> cbEvenement;
    @FXML private TableView<Sponsor> tableSponsors;
    @FXML private TableColumn<Sponsor, String> colSponsorNom, colSponsorType, colSponsorStatut;
    @FXML private TableColumn<Sponsor, Integer> colSponsorEvenement;
    @FXML private Label lblLogoName;
    @FXML private javafx.scene.image.ImageView imgLogoPreview;
    
    private String selectedLogoFileName = null; // Nom du fichier logo sélectionné

    private final EvenementService eventService   = new EvenementService();
    private final SponsorService   sponsorService = new SponsorService();

    // Référence vers le layout parent pour naviguer dans le contentArea
    private AdminLayoutController parentController;

    // Événement en cours de modification (partagé entre les vues)
    private static Evenement evenementAModifier = null;

    /** Appelé par AdminLayoutController après le chargement du FXML */
    public void setParentController(AdminLayoutController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        if (flowPane != null) {
            refreshCards(null);
        }
        if (tfNom != null) {
            // Initialiser les Spinners pour l'heure
            if (spHeure != null) {
                SpinnerValueFactory<Integer> heureFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8);
                spHeure.setValueFactory(heureFactory);
                spHeure.setEditable(true);
            }
            if (spMinute != null) {
                SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
                spMinute.setValueFactory(minuteFactory);
                spMinute.setEditable(true);
            }

            // Auto-détecter plein air quand l'utilisateur tape le lieu
            if (tfLieu != null && cbPleinAir != null) {
                tfLieu.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !cbPleinAir.isSelected()) {
                        cbPleinAir.setSelected(MeteoService.isOutdoor(newVal));
                    }
                });
            }
            
            // Configurer le DatePicker pour accepter uniquement les dates à partir de 2026
            if (dpDateDebut != null) {
                dpDateDebut.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(java.time.LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        // Désactiver les dates avant 2026
                        if (date.isBefore(java.time.LocalDate.of(2026, 1, 1))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #fecaca;");
                        }
                    }
                });
            }
            
            if (evenementAModifier != null) {
                preRemplirChamps();
                if (lblFormTitre != null) lblFormTitre.setText("Modifier l'événement");
            } else {
                if (lblFormTitre != null) lblFormTitre.setText("Nouvel Événement");
            }
        }
        if (cbEvenement != null) {
            initComboEvenement();
        }
        if (tableSponsors != null) {
            initTableSponsors();
        }
    }

    // ════════════════════════════════════════════════════════
    //  NAVIGATION — via le parentController
    // ════════════════════════════════════════════════════════

    private void naviguerVers(String fxmlPath) {
        if (parentController != null) {
            parentController.loadPageInContent(fxmlPath, this);
        }
    }

    @FXML public void allerPageAjout() {
        evenementAModifier = null;
        naviguerVers("/fxml/AjouterEvenementAdmin.fxml");
    }

    @FXML public void retourListe() {
        evenementAModifier = null;
        naviguerVers("/fxml/AdminEvenement.fxml");
    }

    // ════════════════════════════════════════════════════════
    //  SECTION ÉVÉNEMENTS — LISTE
    // ════════════════════════════════════════════════════════

    @FXML
    public void handleRechercheAdmin() {
        String filtre = tfRechercheAdmin != null ? tfRechercheAdmin.getText().trim().toLowerCase() : "";
        refreshCards(filtre.isEmpty() ? null : filtre);
    }

    public void refreshCards(String filtre) {
        if (flowPane == null) return;
        flowPane.getChildren().clear();
        for (Evenement ev : eventService.getAll()) {
            if (filtre == null
                    || ev.getNom().toLowerCase().contains(filtre)
                    || ev.getLieu().toLowerCase().contains(filtre)) {
                flowPane.getChildren().add(createAdminCard(ev));
            }
        }
    }

    private VBox createAdminCard(Evenement ev) {
        VBox card = new VBox(0);
        card.setPrefWidth(340);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 10, 0, 0, 4);"
        );

        // Bandeau coloré
        HBox banner = new HBox();
        banner.setPrefHeight(6);
        banner.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 14 14 0 0;");

        VBox body = new VBox(8);
        body.setPadding(new Insets(14, 16, 14, 16));

        // Titre + badge statut
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(ev.getNom());
        title.setFont(Font.font("System", FontWeight.BOLD, 15));
        title.setTextFill(Color.web("#1e293b"));
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);
        Label statut = new Label(ev.getStatut() != null ? ev.getStatut() : "Actif");
        statut.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-size: 10px; -fx-background-radius: 20; -fx-padding: 2 8;");
        titleRow.getChildren().addAll(title, statut);

        Label coach = new Label("👤  " + ev.getCoach_name());
        coach.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        Label lieu  = new Label("📍  " + ev.getLieu());
        lieu.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        Label date  = new Label("🗓  " + ev.getDate_debut().toLocalDate()
                + "  ·  " + ev.getDate_debut().toLocalTime().toString().substring(0, 5));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Nombre de sponsors liés
        int nbSponsors = sponsorService.getSponsorsByEvenement(ev.getId()).size();
        Label sponsorInfo = new Label("🤝  " + nbSponsors + " sponsor(s) lié(s)");
        sponsorInfo.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 11px; -fx-font-weight: bold;");

        Separator sep = new Separator();

        // Boutons d'action
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnSponsors = new Button("🤝 Sponsors");
        btnSponsors.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #4f46e5; -fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnSponsors.setOnAction(e -> ouvrirModalSponsors(ev));

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            evenementAModifier = ev;
            naviguerVers("/fxml/AjouterEvenementAdmin.fxml");
        });

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Supprimer \"" + ev.getNom() + "\" ?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirmation");
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                eventService.supprimer(ev.getId());
                refreshCards(null);
            }
        });

        btnRow.getChildren().addAll(btnSponsors, btnEdit, btnDelete);
        body.getChildren().addAll(titleRow, coach, lieu, date, sponsorInfo, sep, btnRow);
        card.getChildren().addAll(banner, body);
        return card;
    }

    // ════════════════════════════════════════════════════════
    //  SECTION ÉVÉNEMENTS — FORMULAIRE
    // ════════════════════════════════════════════════════════

    private boolean validerChamps() {
        StringBuilder sb = new StringBuilder();
        if (tfNom.getText().trim().isEmpty())   sb.append("- Titre obligatoire\n");
        if (tfLieu.getText().trim().isEmpty())  sb.append("- Lieu obligatoire\n");
        if (tfCoach.getText().trim().isEmpty()) sb.append("- Coach obligatoire\n");
        if (dpDateDebut.getValue() == null)     sb.append("- Date obligatoire\n");
        else if (dpDateDebut.getValue().isBefore(java.time.LocalDate.of(2026, 1, 1))) {
            sb.append("- La date doit être à partir de 2026\n");
        }
        // L'image n'est plus obligatoire - elle sera générée automatiquement si non fournie
        if (spHeure == null || spMinute == null) {
            sb.append("- Heure invalide\n");
        }
        // Validation du prix
        if (tfPrix != null && !tfPrix.getText().trim().isEmpty()) {
            try {
                double prix = Double.parseDouble(tfPrix.getText().trim());
                if (prix < 0) {
                    sb.append("- Le prix ne peut pas être négatif\n");
                }
            } catch (NumberFormatException e) {
                sb.append("- Prix invalide (utilisez des chiffres, ex: 50.00)\n");
            }
        }
        if (sb.length() > 0) {
            new Alert(Alert.AlertType.WARNING, "Veuillez corriger :\n" + sb).showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    public void handleEnregistrer() {
        if (!validerChamps()) return;
        try {
            // Récupérer l'heure depuis les Spinners
            int heure = spHeure.getValue();
            int minute = spMinute.getValue();
            LocalTime time = LocalTime.of(heure, minute);
            
            LocalDateTime debut = LocalDateTime.of(dpDateDebut.getValue(), time);
            
            // Récupérer le prix
            double prix = 0.0;
            if (tfPrix != null && !tfPrix.getText().trim().isEmpty()) {
                try { prix = Double.parseDouble(tfPrix.getText().trim()); } catch (NumberFormatException e) { prix = 0.0; }
            }

            // Récupérer la capacité
            int capacite = 0;
            if (tfCapacite != null && !tfCapacite.getText().trim().isEmpty()) {
                try { capacite = Integer.parseInt(tfCapacite.getText().trim()); } catch (NumberFormatException e) { capacite = 0; }
            }
            
            // Construire le lieu avec préfixe [OUTDOOR] si plein air
            String lieuSaisi = tfLieu.getText().trim();
            boolean pleinAir = (cbPleinAir != null && cbPleinAir.isSelected())
                               || MeteoService.isOutdoor(lieuSaisi);
            String lieuFinal = pleinAir && !lieuSaisi.startsWith("[OUTDOOR]")
                               ? "[OUTDOOR] " + lieuSaisi
                               : lieuSaisi;

            // GÉNÉRATION AUTOMATIQUE DE L'IMAGE si aucune image n'est sélectionnée
            String imageFinale = selectedImageFileName;
            if (imageFinale == null || imageFinale.trim().isEmpty()) {
                imageFinale = tn.esprit.projet.services.ImageGeneratorService.genererImageDepuisTitre(tfNom.getText());
                System.out.println("🎨 Image générée automatiquement : " + imageFinale);
            }
            
            if (evenementAModifier == null) {
                eventService.ajouter(new Evenement(
                        tfNom.getText(), debut, debut.plusHours(2),
                        lieuFinal, "Actif", imageFinale,
                        taDescription.getText(), tfCoach.getText(), "", prix, capacite
                ));
                showInfo("✅ Événement ajouté avec succès !\n🖼️ Image générée automatiquement basée sur le titre.");
            } else {
                evenementAModifier.setNom(tfNom.getText());
                evenementAModifier.setLieu(lieuFinal);
                evenementAModifier.setCoach_name(tfCoach.getText());
                evenementAModifier.setDescription(taDescription.getText());
                evenementAModifier.setDate_debut(debut);
                evenementAModifier.setImage(imageFinale);
                evenementAModifier.setPrix(prix);
                evenementAModifier.setCapacite(capacite);
                eventService.modifier(evenementAModifier);
                evenementAModifier = null;
                showInfo("✅ Événement modifié avec succès !");
            }
            naviguerVers("/fxml/AdminEvenement.fxml");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }

    private void preRemplirChamps() {
        tfNom.setText(evenementAModifier.getNom());
        // Afficher le lieu propre (sans préfixe [OUTDOOR])
        String lieuBrut = evenementAModifier.getLieu() != null ? evenementAModifier.getLieu() : "";
        tfLieu.setText(MeteoService.getLieuPropre(lieuBrut));
        // Cocher la case plein air si applicable
        if (cbPleinAir != null) {
            cbPleinAir.setSelected(MeteoService.isOutdoor(lieuBrut));
        }
        tfCoach.setText(evenementAModifier.getCoach_name());
        taDescription.setText(evenementAModifier.getDescription() != null ? evenementAModifier.getDescription() : "");
        dpDateDebut.setValue(evenementAModifier.getDate_debut().toLocalDate());
        
        // Remplir les Spinners avec l'heure
        if (spHeure != null && spMinute != null) {
            spHeure.getValueFactory().setValue(evenementAModifier.getDate_debut().getHour());
            spMinute.getValueFactory().setValue(evenementAModifier.getDate_debut().getMinute());
        }
        
        // Remplir le prix
        if (tfPrix != null) {
            tfPrix.setText(String.format("%.2f", evenementAModifier.getPrix()));
        }
        
        // Remplir la capacité
        if (tfCapacite != null) {
            tfCapacite.setText(String.valueOf(evenementAModifier.getCapacite()));
        }
        
        // Charger l'image existante
        selectedImageFileName = evenementAModifier.getImage();
        if (lblImageName != null && selectedImageFileName != null) {
            // Afficher juste le nom du fichier (pas le chemin complet)
            File f = new File(selectedImageFileName);
            lblImageName.setText("📁 " + f.getName());
        }
        
        // Prévisualiser l'image si disponible
        if (imgPreview != null && selectedImageFileName != null && !selectedImageFileName.isEmpty()) {
            javafx.scene.image.Image img = chargerImage(selectedImageFileName);
            if (img != null && !img.isError()) {
                imgPreview.setImage(img);
                imgPreview.setFitWidth(200);
                imgPreview.setFitHeight(150);
                imgPreview.setPreserveRatio(true);
            }
        }
    }
    
    /**
     * Ouvre un FileChooser pour sélectionner une image depuis le PC
     */
    @FXML
    public void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image pour l'événement");
        
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        // Ouvrir le dossier Images par défaut
        String userHome = System.getProperty("user.home");
        File initialDir = new File(userHome, "Pictures");
        if (!initialDir.exists()) initialDir = new File(userHome, "Images");
        if (!initialDir.exists()) initialDir = new File(userHome);
        fileChooser.setInitialDirectory(initialDir);
        
        Stage stage = (Stage) imgPreview.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            // Stocker le chemin absolu du fichier — pas besoin de copier
            selectedImageFileName = selectedFile.getAbsolutePath();
            
            if (lblImageName != null) {
                lblImageName.setText("📁 " + selectedFile.getName());
                lblImageName.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            }
            
            // Prévisualiser directement depuis le fichier
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(selectedFile.toURI().toString());
                if (!img.isError()) {
                    imgPreview.setImage(img);
                    imgPreview.setFitWidth(200);
                    imgPreview.setFitHeight(150);
                    imgPreview.setPreserveRatio(true);
                }
            } catch (Exception e) {
                System.err.println("Erreur prévisualisation : " + e.getMessage());
            }
        }
    }
    
    /**
     * Retourne le dossier images en cherchant le pom.xml pour trouver la racine du projet.
     * Fonctionne que l'app soit lancée depuis IntelliJ ou en ligne de commande.
     */
    private Path getImagesDirPath() throws IOException {
        // Chercher la racine du projet Maven (là où se trouve pom.xml)
        Path dir = Paths.get(System.getProperty("user.dir"));
        // Remonter jusqu'à trouver pom.xml (max 3 niveaux)
        for (int i = 0; i < 3; i++) {
            if (Files.exists(dir.resolve("pom.xml"))) {
                break;
            }
            Path parent = dir.getParent();
            if (parent != null) dir = parent;
        }
        Path imagesDir = dir.resolve("src/main/resources/images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        System.out.println("📁 Dossier images : " + imagesDir.toAbsolutePath());
        return imagesDir;
    }

    /**
     * Copie l'image sélectionnée dans le dossier resources/images/
     * @param sourceFile Fichier source sélectionné
     * @return Nom du fichier copié, ou null si erreur
     */
    private String copyImageToResources(File sourceFile) throws IOException {
        // Générer un nom de fichier unique
        String originalName = sourceFile.getName();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex);
            originalName = originalName.substring(0, dotIndex);
        }
        
        // Nettoyer le nom (enlever espaces et caractères spéciaux)
        String cleanName = originalName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
        String fileName = cleanName + extension;
        
        // Chemin de destination robuste
        Path targetDir = getImagesDirPath();
        Path targetFile = targetDir.resolve(fileName);
        
        // Si le fichier existe déjà, ajouter un timestamp
        if (Files.exists(targetFile)) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            fileName = cleanName + "_" + timestamp + extension;
            targetFile = targetDir.resolve(fileName);
        }
        
        // Copier le fichier
        Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("✅ Image copiée : " + targetFile.toAbsolutePath());
        
        return fileName;
    }
    
    /**
     * Prévisualise l'image dans le formulaire
     */
    @FXML
    public void handlePreviewImage() {
        if (tfImageUrl != null && imgPreview != null) {
            String url = tfImageUrl.getText().trim();
            if (!url.isEmpty()) {
                previewImage(url);
            }
        }
    }
    
    private void previewImage(String imageUrl) {
        try {
            javafx.scene.image.Image img = chargerImage(imageUrl);
            if (img != null && !img.isError()) {
                imgPreview.setImage(img);
                imgPreview.setFitWidth(200);
                imgPreview.setFitHeight(150);
                imgPreview.setPreserveRatio(true);
                System.out.println("✅ Aperçu de l'image chargé : " + imageUrl);
            } else {
                System.err.println("❌ Image introuvable : " + imageUrl);
                imgPreview.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement image : " + e.getMessage());
            imgPreview.setImage(null);
        }
    }

    /**
     * Charge une image depuis :
     * - Un chemin absolu (C:\...\image.jpg) — stocké en DB après upload
     * - Une URL externe (http/https)
     * - Un nom de fichier simple (classpath /images/)
     */
    static javafx.scene.image.Image chargerImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return null;
        try {
            // 1. URL externe
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                return new javafx.scene.image.Image(imageUrl, true);
            }

            // 2. Chemin absolu (Windows C:\ ou Unix /)
            if (imageUrl.length() > 1 && (imageUrl.charAt(1) == ':' || imageUrl.startsWith("/"))) {
                File f = new File(imageUrl);
                if (f.exists()) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(f.toURI().toString());
                    if (!img.isError()) {
                        System.out.println("✅ Image depuis chemin absolu : " + imageUrl);
                        return img;
                    }
                }
                System.err.println("⚠️ Fichier introuvable : " + imageUrl);
                return null;
            }

            // 3. Nom de fichier simple — chercher dans classpath puis système de fichiers
            java.io.InputStream is = AdminEvenementController.class.getResourceAsStream("/images/" + imageUrl);
            if (is != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(is);
                if (!img.isError()) {
                    System.out.println("✅ Image depuis classpath : " + imageUrl);
                    return img;
                }
            }

            // 4. Chercher dans src/main/resources/images/ en remontant depuis user.dir
            Path dir = Paths.get(System.getProperty("user.dir"));
            for (int i = 0; i < 3; i++) {
                Path candidate = dir.resolve("src/main/resources/images/" + imageUrl);
                if (Files.exists(candidate)) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(candidate.toUri().toString());
                    if (!img.isError()) {
                        System.out.println("✅ Image depuis fichier : " + candidate.toAbsolutePath());
                        return img;
                    }
                }
                Path parent = dir.getParent();
                if (parent == null) break;
                dir = parent;
            }

            // 5. Chercher dans le dossier Pictures/Images de l'utilisateur (fallback)
            String userHome = System.getProperty("user.home");
            String[] searchDirs = {"Pictures", "Images", "Downloads", "Desktop"};
            for (String searchDir : searchDirs) {
                File candidate = new File(userHome + File.separator + searchDir + File.separator + imageUrl);
                if (candidate.exists()) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(candidate.toURI().toString());
                    if (!img.isError()) {
                        System.out.println("✅ Image trouvée dans " + searchDir + " : " + candidate.getAbsolutePath());
                        return img;
                    }
                }
            }

            System.err.println("⚠️ Image non trouvée : " + imageUrl);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement image : " + e.getMessage());
        }
        return null;
    }

    // ════════════════════════════════════════════════════════
    //  SECTION SPONSORS — MODAL
    // ════════════════════════════════════════════════════════

    /** Modal de gestion des sponsors d'un événement */
    private void ouvrirModalSponsors(Evenement ev) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Sponsors — " + ev.getNom());
        modal.setMinWidth(540);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Titre
        Label titre = new Label("🤝  Sponsors de : " + ev.getNom());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web("#1e293b"));

        // ── Liste sponsors existants ──
        VBox listeBox = new VBox(6);
        Label listeHeader = new Label("Sponsors actuels :");
        listeHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");
        listeBox.getChildren().add(listeHeader);

        ScrollPane scrollListe = new ScrollPane();
        scrollListe.setFitToWidth(true);
        scrollListe.setPrefHeight(160);
        scrollListe.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox listeInterne = new VBox(6);
        listeInterne.setPadding(new Insets(4));
        refreshListeSponsors(listeInterne, ev.getId());
        scrollListe.setContent(listeInterne);
        listeBox.getChildren().add(scrollListe);

        Separator sep = new Separator();

        // ── Formulaire ajout rapide ──
        Label addHeader = new Label("➕  Ajouter un nouveau sponsor :");
        addHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");

        TextField fNom  = new TextField(); fNom.setPromptText("Nom du sponsor *");
        fNom.setStyle("-fx-background-radius: 8; -fx-pref-height: 38px;");
        TextField fType = new TextField(); fType.setPromptText("Type : Gold / Silver / Bronze");
        fType.setStyle("-fx-background-radius: 8; -fx-pref-height: 38px;");
        TextField fLogo = new TextField(); fLogo.setPromptText("URL du logo (optionnel)");
        fLogo.setStyle("-fx-background-radius: 8; -fx-pref-height: 38px;");

        Button btnAjouter = new Button("➕  Ajouter le sponsor");
        btnAjouter.setMaxWidth(Double.MAX_VALUE);
        btnAjouter.setPrefHeight(42);
        btnAjouter.setStyle(
            "-fx-background-color: #4f46e5; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
        );
        btnAjouter.setOnAction(e -> {
            if (fNom.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Le nom du sponsor est obligatoire.").showAndWait();
                return;
            }
            Sponsor nouveau = new Sponsor(
                    0, fNom.getText().trim(), fType.getText().trim(),
                    "", "Actif", fLogo.getText().trim(), null, ev.getId()
            );
            sponsorService.ajouter(nouveau);
            fNom.clear(); fType.clear(); fLogo.clear();
            // Rafraîchir la liste dans la modal
            refreshListeSponsors(listeInterne, ev.getId());
            // Rafraîchir les cards en arrière-plan
            refreshCards(null);
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setPrefHeight(38);
        btnFermer.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-background-radius: 10; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> modal.close());

        root.getChildren().addAll(titre, listeBox, sep, addHeader, fNom, fType, fLogo, btnAjouter, btnFermer);
        modal.setScene(new javafx.scene.Scene(root));
        modal.showAndWait();
    }

    /** Remplit la liste des sponsors dans la modal */
    private void refreshListeSponsors(VBox container, int evenementId) {
        container.getChildren().clear();
        List<Sponsor> liste = sponsorService.getSponsorsByEvenement(evenementId);
        if (liste.isEmpty()) {
            Label vide = new Label("Aucun sponsor pour cet événement.");
            vide.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            container.getChildren().add(vide);
        } else {
            for (Sponsor s : liste) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

                Label badge = new Label(s.getType() != null && !s.getType().isEmpty() ? s.getType() : "Standard");
                badge.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #4f46e5; -fx-font-size: 10px; -fx-background-radius: 20; -fx-padding: 2 8;");

                Label nom = new Label(s.getNom_partenaire());
                nom.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12px; -fx-font-weight: bold;");
                HBox.setHgrow(nom, Priority.ALWAYS);

                // Bouton supprimer sponsor
                Button btnDel = new Button("✕");
                btnDel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 10px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 2 6;");
                btnDel.setOnAction(e -> {
                    sponsorService.supprimer(s.getId());
                    refreshListeSponsors(container, evenementId);
                    refreshCards(null);
                });

                row.getChildren().addAll(badge, nom, btnDel);
                container.getChildren().add(row);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  SECTION SPONSORS — PAGE DÉDIÉE (AdminSponsor.fxml)
    // ════════════════════════════════════════════════════════

    private void initComboEvenement() {
        cbEvenement.setItems(FXCollections.observableArrayList(eventService.getAll()));
        cbEvenement.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        cbEvenement.setButtonCell(cbEvenement.getCellFactory().call(null));
    }

    private void initTableSponsors() {
        if (colSponsorNom != null)
            colSponsorNom.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue().getNom_partenaire()));
        if (colSponsorType != null)
            colSponsorType.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue().getType()));
        if (colSponsorStatut != null)
            colSponsorStatut.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue().getStatu()));
        if (colSponsorEvenement != null)
            colSponsorEvenement.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleIntegerProperty(d.getValue().getEvenement_id()).asObject());
        refreshTableSponsors();
    }

    public void refreshTableSponsors() {
        if (tableSponsors == null) return;
        java.util.List<Sponsor> tous = new java.util.ArrayList<>();
        for (Evenement ev : eventService.getAll()) {
            tous.addAll(sponsorService.getSponsorsByEvenement(ev.getId()));
        }
        tableSponsors.setItems(FXCollections.observableArrayList(tous));
    }

    @FXML
    public void handleAjouterSponsor() {
        Evenement ev = cbEvenement.getValue();
        if (ev == null || tfSponsorNom.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Nom du sponsor et événement obligatoires.").showAndWait();
            return;
        }
        
        // Utiliser le nom de fichier sélectionné ou le texte du champ
        String logoFinal = selectedLogoFileName != null ? selectedLogoFileName : tfSponsorLogo.getText().trim();
        
        Sponsor s = new Sponsor(
                0, tfSponsorNom.getText().trim(), tfSponsorType.getText().trim(),
                taSponsorDesc.getText().trim(), "Actif", logoFinal, null, ev.getId()
        );
        sponsorService.ajouter(s);
        showInfo("✅ Sponsor \"" + s.getNom_partenaire() + "\" lié à \"" + ev.getNom() + "\" !");
        tfSponsorNom.clear(); tfSponsorType.clear(); tfSponsorLogo.clear(); taSponsorDesc.clear();
        cbEvenement.setValue(null);
        selectedLogoFileName = null;
        if (lblLogoName != null) lblLogoName.setText("");
        if (imgLogoPreview != null) imgLogoPreview.setImage(null);
        refreshTableSponsors();
    }
    
    /**
     * Upload du logo sponsor depuis le PC
     */
    @FXML
    public void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le logo du sponsor");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp")
        );
        
        Stage stage = (Stage) tfSponsorNom.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Créer le dossier images s'il n'existe pas
                Path imagesDir = Paths.get("src/main/resources/images");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }
                
                // Nettoyer le nom du fichier
                String originalName = selectedFile.getName();
                String cleanName = originalName.toLowerCase()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-z0-9._-]", "");
                
                // Vérifier si le fichier existe déjà
                Path targetPath = imagesDir.resolve(cleanName);
                if (Files.exists(targetPath)) {
                    // Ajouter un timestamp pour éviter les doublons
                    String nameWithoutExt = cleanName.substring(0, cleanName.lastIndexOf('.'));
                    String ext = cleanName.substring(cleanName.lastIndexOf('.'));
                    cleanName = nameWithoutExt + "_" + System.currentTimeMillis() + ext;
                    targetPath = imagesDir.resolve(cleanName);
                }
                
                // Copier le fichier
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Mettre à jour l'interface
                selectedLogoFileName = cleanName;
                tfSponsorLogo.setText(cleanName);
                if (lblLogoName != null) {
                    lblLogoName.setText("✅ " + cleanName);
                    lblLogoName.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
                }
                
                // Prévisualisation
                if (imgLogoPreview != null) {
                    try {
                        javafx.scene.image.Image image = new javafx.scene.image.Image(
                            targetPath.toUri().toString()
                        );
                        imgLogoPreview.setImage(image);
                    } catch (Exception e) {
                        System.err.println("Erreur prévisualisation logo : " + e.getMessage());
                    }
                }
                
                System.out.println("✅ Logo copié : " + cleanName);
                
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la copie du logo : " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
