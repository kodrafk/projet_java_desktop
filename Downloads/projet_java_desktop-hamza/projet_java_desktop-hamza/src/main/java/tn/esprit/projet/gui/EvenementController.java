package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class EvenementController {

    @FXML private FlowPane flowPane;
    @FXML private TextField tfNom, tfLieu, tfCoach, tfHeureDebut;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDateDebut;

    private final EvenementService eventService = new EvenementService();
    private final SponsorService sponsorService = new SponsorService();
    private static Evenement evenementAModifier = null;

    @FXML
    public void initialize() {
        if (flowPane != null) {
            refreshCards();
        }
        if (tfNom != null && evenementAModifier != null) {
            preRemplirChamps();
        }
    }

    // --- CONTRÔLE DE SAISIE (BACK) ---
    private boolean validerChamps() {
        StringBuilder sb = new StringBuilder();
        if (tfNom.getText().trim().isEmpty()) sb.append("- Titre obligatoire\n");
        if (tfLieu.getText().trim().isEmpty()) sb.append("- Lieu obligatoire\n");
        if (tfCoach.getText().trim().isEmpty()) sb.append("- Coach obligatoire\n");
        if (dpDateDebut.getValue() == null) sb.append("- Date obligatoire\n");
        try {
            LocalTime.parse(tfHeureDebut.getText().trim());
        } catch (DateTimeParseException e) {
            sb.append("- Heure invalide (Format HH:mm requis)\n");
        }

        if (sb.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez corriger :\n" + sb.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    public void handleAjouterEtNaviguer() {
        if (!validerChamps()) return;
        try {
            LocalDateTime debut = LocalDateTime.of(dpDateDebut.getValue(), LocalTime.parse(tfHeureDebut.getText().trim()));
            if (evenementAModifier == null) {
                eventService.ajouter(new Evenement(tfNom.getText(), debut, debut.plusHours(2),
                        tfLieu.getText(), "Actif", "logo.png", taDescription.getText(), tfCoach.getText(), "", 0.0));
            } else {
                evenementAModifier.setNom(tfNom.getText());
                evenementAModifier.setLieu(tfLieu.getText());
                evenementAModifier.setCoach_name(tfCoach.getText());
                evenementAModifier.setDescription(taDescription.getText());
                evenementAModifier.setDate_debut(debut);
                eventService.modifier(evenementAModifier);
                evenementAModifier = null;
            }
            retourListe();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement : " + e.getMessage()).show();
        }
    }

    public void refreshCards() {
        if (flowPane == null) return;
        flowPane.getChildren().clear();
        for (Evenement ev : eventService.getAll()) {
            flowPane.getChildren().add(createCard(ev));
        }
    }

    // --- CRÉATION DE LA CARTE (FRONT + BACK) ---
    private VBox createCard(Evenement ev) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // TITRE
        Label title = new Label(ev.getNom().toUpperCase());
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        // COACH & DATE
        Label coach = new Label("👤 Coach: " + ev.getCoach_name());
        Label date = new Label("🗓 " + ev.getDate_debut().toString().replace("T", " "));
        coach.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        date.setStyle("-fx-text-fill: #64748b;");

        // DESCRIPTION
        Label desc = new Label(ev.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-style: italic; -fx-text-fill: #334155;");

        // BOUTON PARTICIPATION
        Button btnParticiper = new Button("PARTICIPER MAINTENANT");
        btnParticiper.setMaxWidth(Double.MAX_VALUE);
        btnParticiper.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnParticiper.setOnAction(e -> ouvrirFormulaireParticipation(ev));

        // SECTION SPONSORS
        VBox sponsorBox = new VBox(5);
        Label spHeader = new Label("Sponsors :");
        spHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #4F46E5;");
        sponsorBox.getChildren().add(spHeader);
        for (Sponsor s : sponsorService.getSponsorsByEvenement(ev.getId())) {
            Label sLabel = new Label("• " + s.getNom_partenaire());
            sLabel.setStyle("-fx-text-fill: #2563eb;");
            sponsorBox.getChildren().add(sLabel);
        }

        // BOUTONS ADMIN (MODIFIER / SUPPRIMER)
        HBox adminBox = new HBox(10);
        Button btnEdit = new Button("Modifier");
        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> { evenementAModifier = ev; changerScene("/fxml/AjouterEvenement.fxml"); });

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> { eventService.supprimer(ev.getId()); refreshCards(); });

        adminBox.getChildren().addAll(btnEdit, btnDelete);

        card.getChildren().addAll(title, coach, date, desc, btnParticiper, new Separator(), sponsorBox, adminBox);
        return card;
    }

    // --- FORMULAIRE INSCRIPTION (FRONT) ---
    private void ouvrirFormulaireParticipation(Evenement ev) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Participation");
        dialog.setHeaderText("Inscription : " + ev.getNom());

        TextField name = new TextField(); name.setPromptText("Nom complet");
        TextField mail = new TextField(); mail.setPromptText("votre@email.com");

        VBox content = new VBox(10, new Label("Nom :"), name, new Label("Email :"), mail);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (!mail.getText().contains("@") || name.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Email ou Nom invalide").show();
            } else {
                System.out.println("Inscription réussie pour : " + name.getText());
            }
        }
    }

    private void preRemplirChamps() {
        tfNom.setText(evenementAModifier.getNom());
        tfLieu.setText(evenementAModifier.getLieu());
        tfCoach.setText(evenementAModifier.getCoach_name());
        taDescription.setText(evenementAModifier.getDescription());
        dpDateDebut.setValue(evenementAModifier.getDate_debut().toLocalDate());
        tfHeureDebut.setText(evenementAModifier.getDate_debut().toLocalTime().toString());
    }

    private void changerScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            if (stage != null) stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void allerPageAjout() { evenementAModifier = null; changerScene("/fxml/AjouterEvenement.fxml"); }
    @FXML public void retourListe() { evenementAModifier = null; changerScene("/fxml/AfficherEvenement.fxml"); }
}