package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.ImageGeneratorService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.utils.AlertUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AdminEvenementController {

    // --- List components ---
    @FXML private FlowPane flowPane;
    @FXML private TextField tfRechercheAdmin;

    // --- Add form components ---
    @FXML private TextField tfNom;
    @FXML private TextField tfLieu;
    @FXML private DatePicker dpDateDebut;
    @FXML private TextField tfCoach;
    @FXML private TextField tfPrix;
    @FXML private TextField tfCapacite;
    @FXML private ImageView ivAperçu;

    private final EvenementService eventService   = new EvenementService();
    private AdminLayoutController parentController;
    private String currentImageUrl = null;

    public void setParentController(AdminLayoutController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        if (flowPane != null) refreshCards(null);
    }

    public void refreshCards(String filtre) {
        if (flowPane == null) return;
        flowPane.getChildren().clear();
        for (Evenement ev : eventService.getAll()) {
            if (filtre == null || ev.getNom().toLowerCase().contains(filtre.toLowerCase())) {
                flowPane.getChildren().add(createAdminCard(ev));
            }
        }
    }

    private VBox createAdminCard(Evenement ev) {
        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(ev.getImage() != null ? ev.getImage() : ImageGeneratorService.genererImageParDefaut(), true));
        } catch (Exception e) {
            img.setImage(new Image(ImageGeneratorService.genererImageParDefaut()));
        }
        img.setFitHeight(120);
        img.setFitWidth(290);
        img.setPreserveRatio(true);

        Label title = new Label(ev.getNom());
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label info = new Label("📍 " + ev.getLieu() + "\n👤 " + ev.getCoach_name());
        info.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-cursor: hand;");
        
        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            eventService.supprimer(ev.getId());
            refreshCards(null);
        });

        btns.getChildren().addAll(btnEdit, btnDelete);
        card.getChildren().addAll(img, title, info, btns);
        return card;
    }

    @FXML
    public void handleRechercheAdmin() {
        refreshCards(tfRechercheAdmin.getText());
    }

    @FXML
    public void handleGenererImage() {
        String titre = tfNom.getText();
        if (titre == null || titre.trim().isEmpty()) {
            AlertUtil.showError("Erreur", "Veuillez saisir un nom d'événement pour générer une image.");
            return;
        }
        currentImageUrl = ImageGeneratorService.genererImageDepuisTitre(titre);
        ivAperçu.setImage(new Image(currentImageUrl, true));
    }

    @FXML
    public void handleVerifierLocalisation() {
        String lieu = tfLieu.getText();
        if (lieu == null || lieu.trim().isEmpty()) {
            AlertUtil.showError("Erreur", "Veuillez saisir un lieu.");
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("Vérification : " + lieu);
        WebView wv = new WebView();
        String lieuEncoded = lieu.replace(" ", "+");
        String mapUrl = "https://www.google.com/maps?q=" + lieuEncoded + "&output=embed&hl=fr";
        String html = "<!DOCTYPE html><html><head>" +
                      "<style>body,html{margin:0;padding:0;height:100%;overflow:hidden;}</style>" +
                      "</head><body>" +
                      "<iframe width=\"100%\" height=\"100%\" frameborder=\"0\" style=\"border:0\" src=\"" + mapUrl + "\" allowfullscreen></iframe>" +
                      "</body></html>";
        wv.getEngine().loadContent(html, "text/html");
        stage.setScene(new Scene(wv, 800, 600));
        stage.show();
    }

    @FXML
    public void handleAjouter() {
        try {
            String nom = tfNom.getText();
            String lieu = tfLieu.getText();
            
            if (dpDateDebut.getValue() == null) {
                AlertUtil.showError("Erreur", "Veuillez choisir une date.");
                return;
            }
            
            LocalDateTime date = dpDateDebut.getValue().atTime(LocalTime.NOON);
            String coach = tfCoach.getText();
            
            if (nom.isEmpty() || lieu.isEmpty() || coach.isEmpty()) {
                AlertUtil.showError("Erreur", "Tous les champs sont obligatoires.");
                return;
            }

            double prix = 0;
            int capacite = 0;
            try {
                prix = Double.parseDouble(tfPrix.getText());
                capacite = Integer.parseInt(tfCapacite.getText());
            } catch (NumberFormatException e) {
                AlertUtil.showError("Erreur", "Prix et Capacité doivent être des nombres.");
                return;
            }

            if (currentImageUrl == null) {
                currentImageUrl = ImageGeneratorService.genererImageDepuisTitre(nom);
            }

            // Générer une description par IA
            String description = tn.esprit.projet.utils.GeminiService.generateEventDescription(nom, coach, lieu);
            if (description == null) description = "Événement de sport et nutrition animé par " + coach + ".";

            Evenement ev = new Evenement(nom, date, date.plusHours(2), lieu, "A venir", currentImageUrl, description, coach, "Objectifs sportifs", prix, capacite);
            eventService.ajouter(ev);
            
            AlertUtil.showSuccess("Succès", "L'événement a été créé avec succès ! ✨");
            retourListe();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    public void allerPageAjout() {
        if (parentController != null) {
            parentController.loadPageInContent("/fxml/AjouterEvenementAdmin.fxml", this);
        }
    }

    @FXML
    public void retourListe() {
        if (parentController != null) {
            parentController.loadPageInContent("/fxml/AdminEvenement.fxml", this);
        }
    }
}

