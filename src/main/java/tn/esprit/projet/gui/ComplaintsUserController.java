package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.services.ComplaintService;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.Toast;
import tn.esprit.projet.utils.GeminiService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.esprit.projet.utils.SpeechToTextService;
import java.util.concurrent.CompletableFuture;

public class ComplaintsUserController {

    @FXML private TextField fldTitle;
    @FXML private TextField fldPhone;
    @FXML private Rating ratingControl;
    @FXML private DatePicker dpDate;
    @FXML private TextArea fldDescription;
    @FXML private Label lblImageName;
    @FXML private FlowPane imagePreviewContainer;
    @FXML private Label lblError;
    @FXML private Button btnSubmit;

    @FXML private TextField fldSearchUser;
    @FXML private ComboBox<String> cmbSortUser;
    @FXML private FlowPane cardsContainer;

    private ComplaintService service;
    private List<String> selectedImagePaths = new ArrayList<>();
    private ObservableList<Complaint> myComplaints = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        service = new ComplaintService();
        
        // Initialize UI components
        dpDate.setValue(LocalDate.now());
        cmbSortUser.setItems(FXCollections.observableArrayList("Recent First", "Oldest First", "Highest Rating", "Lowest Rating"));
        cmbSortUser.setValue("Recent First");

        // Search and Sort listeners
        fldSearchUser.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        cmbSortUser.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());

        refreshData();
    }

    @FXML
    private void handleGenerateTitleAI() {
        String description = fldDescription.getText().trim();
        if (description.isEmpty()) {
            Toast.show((javafx.stage.Stage)fldTitle.getScene().getWindow(), "Please write a description first.", Toast.Type.ERROR);
            return;
        }

        fldTitle.setPromptText("AI is thinking...");
        fldTitle.setDisable(true);

        CompletableFuture.supplyAsync(() -> GeminiService.suggestTitle(description))
            .thenAccept(title -> Platform.runLater(() -> {
                fldTitle.setDisable(false);
                if (title != null && !title.isEmpty()) {
                    fldTitle.setText(title.replace("\"", ""));
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> fldTitle.setDisable(false));
                return null;
            });
    }

    @FXML
    private void handleVoiceToText() {
        fldDescription.setPromptText("🎙️ Listening for 5 seconds...");
        fldDescription.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                byte[] audio = SpeechToTextService.recordAudio(5);
                String text = SpeechToTextService.convertAudioToText(audio);
                
                Platform.runLater(() -> {
                    fldDescription.setDisable(false);
                    fldDescription.setPromptText("Describe the issue in detail...");
                    if (text != null && !text.isEmpty()) {
                        String currentText = fldDescription.getText();
                        fldDescription.setText(currentText.isEmpty() ? text : currentText + " " + text);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    fldDescription.setDisable(false);
                    Toast.show((javafx.stage.Stage)fldDescription.getScene().getWindow(), "Voice Error: " + e.getMessage(), Toast.Type.ERROR);
                });
            }
        });
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Complaint Images");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(fldTitle.getScene().getWindow());

        if (selectedFiles != null) {
            imagePreviewContainer.getChildren().clear();
            selectedImagePaths.clear();
            imagePreviewContainer.setVisible(true);
            imagePreviewContainer.setManaged(true);

            for (File file : selectedFiles) {
                try {
                    // Create local storage if doesn't exist
                    File uploadDir = new File("uploads/complaints");
                    if (!uploadDir.exists()) uploadDir.mkdirs();

                    String fileName = UUID.randomUUID().toString() + "_" + file.getName();
                    File destFile = new File(uploadDir, fileName);
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    selectedImagePaths.add(destFile.getAbsolutePath());
                    
                    // Show preview
                    ImageView preview = new ImageView(new Image(destFile.toURI().toString()));
                    preview.setFitWidth(60);
                    preview.setFitHeight(60);
                    preview.setPreserveRatio(true);
                    imagePreviewContainer.getChildren().add(preview);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void handleSubmit() {
        lblError.setVisible(false);
        lblError.setManaged(false);

        String title = fldTitle.getText().trim();
        String description = fldDescription.getText().trim();
        String phone = fldPhone.getText().trim();
        int rate = (int) ratingControl.getRating();
        LocalDate incidentDate = dpDate.getValue();

        // Simple validation
        if (title.isEmpty() || description.isEmpty() || incidentDate == null || rate == 0) {
            showError("Please fill in all required fields (*)");
            return;
        }

        if (!phone.isEmpty() && !phone.matches("\\+?[0-9\\s-]{8,15}")) {
            showError("Invalid phone number format");
            return;
        }

        Complaint c = new Complaint();
        c.setUserId(SessionManager.getCurrentUser().getId());
        c.setTitle(title);
        c.setDescription(description);
        c.setPhoneNumber(phone);
        c.setRate(rate);
        c.setIncidentDate(incidentDate);
        c.setDateOfComplaint(LocalDateTime.now());
        c.setStatus("PENDING");
        c.setImagePath(String.join(";", selectedImagePaths));

        service.ajouter(c);
        
        Toast.show((javafx.stage.Stage)fldTitle.getScene().getWindow(), "Complaint submitted successfully!", Toast.Type.SUCCESS);
        clearForm();
        refreshData();
    }

    @FXML
    private void handleRefresh() {
        refreshData();
    }

    private void refreshData() {
        int userId = SessionManager.getCurrentUser().getId();
        myComplaints.setAll(service.getByUserId(userId));
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String search = fldSearchUser.getText().toLowerCase();
        String sort = cmbSortUser.getValue();

        java.util.stream.Stream<Complaint> stream = myComplaints.stream();

        if (!search.isEmpty()) {
            stream = stream.filter(c -> c.getTitle().toLowerCase().contains(search) 
                                     || c.getDescription().toLowerCase().contains(search));
        }

        if (sort != null) {
            switch (sort) {
                case "Recent First": stream = stream.sorted((a, b) -> b.getDateOfComplaint().compareTo(a.getDateOfComplaint())); break;
                case "Oldest First": stream = stream.sorted((a, b) -> a.getDateOfComplaint().compareTo(b.getDateOfComplaint())); break;
                case "Highest Rating": stream = stream.sorted((a, b) -> Integer.compare(b.getRate(), a.getRate())); break;
                case "Lowest Rating": stream = stream.sorted((a, b) -> Integer.compare(a.getRate(), b.getRate())); break;
            }
        }

        cardsContainer.getChildren().clear();
        stream.forEach(c -> cardsContainer.getChildren().add(createCard(c)));
    }

    private VBox createCard(Complaint c) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        card.setPrefWidth(260);

        // ── Date ──
        Label dateLbl = new Label(c.getDateOfComplaint().toLocalDate().toString());
        dateLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        // ── Title + Status ──
        Label titleLbl = new Label(c.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 14px;");
        titleLbl.setWrapText(true);

        String statusColor = c.getStatus().equalsIgnoreCase("RESOLVED") ? "#10B981"
                : c.getStatus().equalsIgnoreCase("REJECTED") ? "#EF4444" : "#F59E0B";
        Label statusLbl = new Label(c.getStatus());
        statusLbl.setStyle("-fx-text-fill: white; -fx-background-color: " + statusColor +
                "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 2 8;");

        HBox header = new HBox(8, titleLbl, new Region(), statusLbl);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Rating ──
        Rating r = new Rating(5, c.getRate());
        r.setPartialRating(false);
        r.setMouseTransparent(true);
        r.setScaleX(0.6);
        r.setScaleY(0.6);
        r.setTranslateX(-20);

        // ── Description ──
        Label descLbl = new Label(c.getDescription());
        descLbl.setWrapText(true);
        descLbl.setMaxHeight(60);
        descLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        card.getChildren().addAll(dateLbl, header, r, descLbl);

        // ── Image Preview ──
        if (c.getImagePath() != null && !c.getImagePath().isEmpty()) {
            try {
                String firstPath = c.getImagePathsList().get(0);
                File imgFile = new File(firstPath);
                if (imgFile.exists()) {
                    ImageView imgView = new ImageView(new Image(imgFile.toURI().toString()));
                    imgView.setFitWidth(230);
                    imgView.setFitHeight(120);
                    imgView.setPreserveRatio(true);
                    VBox imgContainer = new VBox(imgView);
                    imgContainer.setAlignment(Pos.CENTER);
                    imgContainer.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 5; -fx-padding: 5;");
                    card.getChildren().add(imgContainer);
                }
            } catch (Exception ignored) {}
        }

        // ── Admin Response ──
        if (c.getAdminResponse() != null) {
            Label respTitle = new Label("Admin Response:");
            respTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #475569;");
            Label respContent = new Label(c.getAdminResponse());
            respContent.setWrapText(true);
            respContent.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-font-style: italic;");
            card.getChildren().addAll(new Separator(), respTitle, respContent);
        }

        // ── Edit / Delete buttons (only if PENDING) ──
        boolean canEdit = "PENDING".equalsIgnoreCase(c.getStatus());

        Button btnEdit = new Button("✏ Edit");
        btnEdit.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-size: 11px; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;");
        btnEdit.setDisable(!canEdit);
        if (!canEdit) btnEdit.setOpacity(0.4);
        btnEdit.setOnMouseEntered(e -> { if (canEdit) btnEdit.setStyle("-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;"); });
        btnEdit.setOnMouseExited(e  -> { if (canEdit) btnEdit.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;"); });
        btnEdit.setOnAction(e -> showEditDialog(c, card));

        Button btnDelete = new Button("🗑 Delete");
        btnDelete.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-font-size: 11px; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;");
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;"));
        btnDelete.setOnMouseExited(e  -> btnDelete.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;"));
        btnDelete.setOnAction(e -> handleDelete(c));

        HBox actions = new HBox(8, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        if (!canEdit) {
            Label lockedLbl = new Label("🔒 Cannot edit — already " + c.getStatus().toLowerCase());
            lockedLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8; -fx-font-style: italic;");
            card.getChildren().add(lockedLbl);
        }

        card.getChildren().add(actions);
        return card;
    }

    // ── Edit Dialog ──────────────────────────────────────────────────────────
    private void showEditDialog(Complaint c, VBox card) {
        Stage owner = (Stage) cardsContainer.getScene().getWindow();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Edit Complaint");
        dialog.setHeaderText("Edit your complaint (only PENDING complaints can be modified)");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-font-size: 13px;");

        // Form fields
        TextField tfTitle = new TextField(c.getTitle());
        tfTitle.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");

        TextField tfPhone = new TextField(c.getPhoneNumber() != null ? c.getPhoneNumber() : "");
        tfPhone.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");

        TextArea taDesc = new TextArea(c.getDescription());
        taDesc.setPrefHeight(100);
        taDesc.setWrapText(true);
        taDesc.setStyle("-fx-control-inner-background: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-radius: 6;");

        DatePicker dpIncident = new DatePicker(c.getIncidentDate());

        Rating ratingEdit = new Rating(5, c.getRate());
        ratingEdit.setPartialRating(false);

        VBox content = new VBox(10,
            new Label("Title *"), tfTitle,
            new Label("Phone"), tfPhone,
            new Label("Description *"), taDesc,
            new Label("Incident Date"), dpIncident,
            new Label("Rating"), ratingEdit
        );
        content.setPadding(new Insets(10));
        content.setPrefWidth(420);

        // Style labels
        content.getChildren().stream()
            .filter(n -> n instanceof Label)
            .forEach(n -> ((Label) n).setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #475569;"));

        dialog.getDialogPane().setContent(new ScrollPane(content) {{
            setFitToWidth(true);
            setStyle("-fx-background-color: white; -fx-border-color: transparent;");
            setPrefHeight(420);
        }});

        // Style Save button
        javafx.scene.Node saveNode = dialog.getDialogPane().lookupButton(saveBtn);
        saveNode.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 16;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String newTitle = tfTitle.getText().trim();
                String newDesc  = taDesc.getText().trim();
                if (newTitle.isEmpty() || newDesc.isEmpty()) {
                    Toast.show(owner, "Title and description are required.", Toast.Type.ERROR);
                    return;
                }
                c.setTitle(newTitle);
                c.setDescription(newDesc);
                c.setPhoneNumber(tfPhone.getText().trim());
                c.setRate((int) ratingEdit.getRating());
                c.setIncidentDate(dpIncident.getValue());

                service.modifier(c);
                Toast.show(owner, "Complaint updated successfully!", Toast.Type.SUCCESS);
                refreshData();
            }
        });
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    private void handleDelete(Complaint c) {
        Stage owner = (Stage) cardsContainer.getScene().getWindow();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(owner);
        confirm.setTitle("Delete Complaint");
        confirm.setHeaderText("Delete \"" + c.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.getDialogPane().setStyle("-fx-font-size: 13px;");

        // Style buttons
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        javafx.scene.Node yesBtn = confirm.getDialogPane().lookupButton(ButtonType.YES);
        yesBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 16;");

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                service.supprimer(c.getId());
                Toast.show(owner, "Complaint deleted.", Toast.Type.SUCCESS);
                refreshData();
            }
        });
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void clearForm() {
        fldTitle.clear();
        fldPhone.clear();
        fldDescription.clear();
        ratingControl.setRating(0);
        dpDate.setValue(LocalDate.now());
        selectedImagePaths.clear();
        imagePreviewContainer.getChildren().clear();
        imagePreviewContainer.setVisible(false);
        imagePreviewContainer.setManaged(false);
    }
}
