package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.Toasts;

import javafx.stage.Stage;
import java.util.List;

public class AdminSponsorController {

    // ── List ──────────────────────────────────────────────────────────────────
    @FXML private FlowPane flowPane;
    @FXML private TextField tfRecherche;
    @FXML private Label lblCount;

    // ── Form ──────────────────────────────────────────────────────────────────
    @FXML private StackPane formOverlay;
    @FXML private Label lblFormTitle;
    @FXML private TextField tfNom;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private TextField tfLogo;
    @FXML private TextField tfVideo;
    @FXML private ComboBox<Evenement> cmbEvenement;
    @FXML private Label lblError;

    private final SponsorService  sponsorService  = new SponsorService();
    private final EvenementService evenementService = new EvenementService();
    private Sponsor currentSponsor = null;

    @FXML
    public void initialize() {
        setupForm();
        refreshCards(null);
    }

    private void setupForm() {
        if (cmbType != null)
            cmbType.setItems(FXCollections.observableArrayList("Gold", "Silver", "Bronze", "Platinum", "Partenaire"));
        if (cmbStatut != null)
            cmbStatut.setItems(FXCollections.observableArrayList("Actif", "Inactif", "En attente"));
        if (cmbEvenement != null) {
            List<Evenement> evs = evenementService.getAll();
            cmbEvenement.setItems(FXCollections.observableArrayList(evs));
            cmbEvenement.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Evenement e, boolean empty) {
                    super.updateItem(e, empty);
                    setText(empty || e == null ? null : e.getNom());
                }
            });
            cmbEvenement.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Evenement e, boolean empty) {
                    super.updateItem(e, empty);
                    setText(empty || e == null ? "Aucun événement" : e.getNom());
                }
            });
        }
    }

    // ── Refresh cards ─────────────────────────────────────────────────────────
    public void refreshCards(String filtre) {
        if (flowPane == null) return;
        flowPane.getChildren().clear();
        List<Sponsor> list = sponsorService.getAll();
        int count = 0;
        for (Sponsor s : list) {
            if (filtre == null || s.getNom_partenaire().toLowerCase().contains(filtre.toLowerCase())) {
                flowPane.getChildren().add(createCard(s));
                count++;
            }
        }
        if (lblCount != null) lblCount.setText(count + " sponsor(s)");
    }

    private VBox createCard(Sponsor s) {
        VBox card = new VBox(12);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 18; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-border-width: 1;");

        // Logo
        StackPane logoPane = new StackPane();
        logoPane.setPrefHeight(80);
        logoPane.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10;");
        if (s.getLogo() != null && !s.getLogo().isBlank()) {
            try {
                ImageView iv = new ImageView(new Image(s.getLogo(), true));
                iv.setFitWidth(120); iv.setFitHeight(70); iv.setPreserveRatio(true);
                logoPane.getChildren().add(iv);
            } catch (Exception e) {
                Label ph = new Label("🏢"); ph.setStyle("-fx-font-size: 32px;");
                logoPane.getChildren().add(ph);
            }
        } else {
            Label ph = new Label("🏢"); ph.setStyle("-fx-font-size: 32px;");
            logoPane.getChildren().add(ph);
        }

        // Nom
        Label nom = new Label(s.getNom_partenaire());
        nom.setFont(Font.font("System", FontWeight.BOLD, 15));
        nom.setTextFill(Color.web("#1E293B"));

        // Type badge
        String typeColor = switch (s.getType() != null ? s.getType() : "") {
            case "Gold"     -> "#F59E0B";
            case "Platinum" -> "#8B5CF6";
            case "Silver"   -> "#64748B";
            case "Bronze"   -> "#D97706";
            default         -> "#2E7D5A";
        };
        Label typeBadge = new Label(s.getType() != null ? s.getType() : "—");
        typeBadge.setStyle("-fx-background-color: " + typeColor + "22; -fx-text-fill: " + typeColor +
                "; -fx-background-radius: 8; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Statut
        String statutColor = "Actif".equalsIgnoreCase(s.getStatu()) ? "#10B981" :
                             "Inactif".equalsIgnoreCase(s.getStatu()) ? "#EF4444" : "#F59E0B";
        Label statutBadge = new Label(s.getStatu() != null ? s.getStatu() : "—");
        statutBadge.setStyle("-fx-background-color: " + statutColor + "22; -fx-text-fill: " + statutColor +
                "; -fx-background-radius: 8; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox badges = new HBox(8, typeBadge, statutBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        // Description
        if (s.getDescription() != null && !s.getDescription().isBlank()) {
            Label desc = new Label(s.getDescription().length() > 80
                    ? s.getDescription().substring(0, 80) + "…" : s.getDescription());
            desc.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
            desc.setWrapText(true);
            card.getChildren().addAll(logoPane, nom, badges, desc);
        } else {
            card.getChildren().addAll(logoPane, nom, badges);
        }

        // Événement lié
        if (s.getEvenement_id() > 0) {
            Label evLbl = new Label("📅 Événement #" + s.getEvenement_id());
            evLbl.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 11px;");
            card.getChildren().add(evLbl);
        }

        // Vidéo
        if (s.getVideo_url() != null && !s.getVideo_url().isBlank()) {
            Label vidLbl = new Label("🎬 Vidéo disponible");
            vidLbl.setStyle("-fx-text-fill: #8B5CF6; -fx-font-size: 11px;");
            card.getChildren().add(vidLbl);
        }

        // Actions
        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-size: 11px; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));
        btnEdit.setOnMouseExited(e  -> btnEdit.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));
        btnEdit.setOnAction(e -> openForm(s));

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-font-size: 11px; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
        btnDel.setOnMouseEntered(e -> btnDel.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));
        btnDel.setOnMouseExited(e  -> btnDel.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));
        btnDel.setOnAction(e -> handleDelete(s));

        HBox actions = new HBox(8, btnEdit, btnDel);
        actions.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().add(actions);

        return card;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    public void handleAddNew() {
        currentSponsor = null;
        if (lblFormTitle != null) lblFormTitle.setText("Ajouter un Sponsor");
        clearForm();
        showForm();
    }

    private void openForm(Sponsor s) {
        currentSponsor = s;
        if (lblFormTitle != null) lblFormTitle.setText("Modifier le Sponsor");
        fillForm(s);
        showForm();
    }

    private void handleDelete(Sponsor s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le sponsor \"" + s.getNom_partenaire() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmer la suppression");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                sponsorService.supprimer(s.getId());
                refreshCards(tfRecherche != null ? tfRecherche.getText() : null);
                Stage owner = (Stage) flowPane.getScene().getWindow();
                Toasts.show(owner, "Sponsor supprimé avec succès.", Toasts.Type.SUCCESS);
            }
        });
    }

    @FXML
    public void handleSave() {
        if (lblError != null) { lblError.setVisible(false); lblError.setManaged(false); }

        String nom = tfNom != null ? tfNom.getText().trim() : "";
        if (nom.isEmpty()) {
            showError("Le nom du partenaire est obligatoire.");
            return;
        }

        Sponsor s = currentSponsor != null ? currentSponsor : new Sponsor();
        s.setNom_partenaire(nom);
        s.setType(cmbType != null ? cmbType.getValue() : "Partenaire");
        s.setDescription(taDescription != null ? taDescription.getText().trim() : "");
        s.setStatu(cmbStatut != null && cmbStatut.getValue() != null ? cmbStatut.getValue() : "Actif");
        s.setLogo(tfLogo != null ? tfLogo.getText().trim() : "");
        s.setVideo_url(tfVideo != null ? tfVideo.getText().trim() : "");

        Evenement ev = cmbEvenement != null ? cmbEvenement.getValue() : null;
        s.setEvenement_id(ev != null ? ev.getId() : 0);

        if (currentSponsor != null) {
            sponsorService.modifier(s);
        } else {
            sponsorService.ajouter(s);
        }

        closeForm();
        refreshCards(null);
        Stage owner = (Stage) flowPane.getScene().getWindow();
        Toasts.show(owner, currentSponsor != null ? "Sponsor modifié !" : "Sponsor ajouté !", Toasts.Type.SUCCESS);
    }

    @FXML
    public void handleCloseForm() { closeForm(); }

    @FXML
    public void handleRecherche() {
        refreshCards(tfRecherche != null ? tfRecherche.getText().trim() : null);
    }

    // ── Form helpers ──────────────────────────────────────────────────────────

    private void showForm() {
        if (formOverlay != null) { formOverlay.setVisible(true); formOverlay.setManaged(true); }
    }

    private void closeForm() {
        if (formOverlay != null) { formOverlay.setVisible(false); formOverlay.setManaged(false); }
        currentSponsor = null;
    }

    private void clearForm() {
        if (tfNom != null) tfNom.clear();
        if (cmbType != null) cmbType.setValue(null);
        if (taDescription != null) taDescription.clear();
        if (cmbStatut != null) cmbStatut.setValue("Actif");
        if (tfLogo != null) tfLogo.clear();
        if (tfVideo != null) tfVideo.clear();
        if (cmbEvenement != null) cmbEvenement.setValue(null);
    }

    private void fillForm(Sponsor s) {
        if (tfNom != null) tfNom.setText(s.getNom_partenaire() != null ? s.getNom_partenaire() : "");
        if (cmbType != null) cmbType.setValue(s.getType());
        if (taDescription != null) taDescription.setText(s.getDescription() != null ? s.getDescription() : "");
        if (cmbStatut != null) cmbStatut.setValue(s.getStatu() != null ? s.getStatu() : "Actif");
        if (tfLogo != null) tfLogo.setText(s.getLogo() != null ? s.getLogo() : "");
        if (tfVideo != null) tfVideo.setText(s.getVideo_url() != null ? s.getVideo_url() : "");
        if (cmbEvenement != null && s.getEvenement_id() > 0) {
            evenementService.getAll().stream()
                    .filter(e -> e.getId() == s.getEvenement_id())
                    .findFirst()
                    .ifPresent(e -> cmbEvenement.setValue(e));
        }
    }

    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }
}
