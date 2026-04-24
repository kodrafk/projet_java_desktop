package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.ComplaintService;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.Toast;

import javafx.stage.FileChooser;
import java.io.File;
import org.controlsfx.control.Rating;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ComplaintsUserController {

    @FXML private TextField fldTitle;
    @FXML private TextField fldPhone;
    @FXML private Rating ratingControl;
    @FXML private DatePicker dpDate;
    @FXML private TextArea fldDescription;
    @FXML private Label lblError;
    @FXML private Label lblImageName;
    @FXML private javafx.scene.layout.FlowPane imagePreviewContainer;
    @FXML private Button btnSubmit;

    private String selectedImagePath;
    private Complaint editTarget = null;

    @FXML private javafx.scene.layout.FlowPane cardsContainer;
    @FXML private TextField fldSearchUser;
    @FXML private ComboBox<String> cmbSortUser;

    private List<Complaint> masterData = new java.util.ArrayList<>();

    private ComplaintService service;
    private User currentUser;

    @FXML
    public void initialize() {
        service = new ComplaintService();
        currentUser = SessionManager.getCurrentUser();

        ratingControl.setRating(1);
        
        // Default date to today
        dpDate.setValue(LocalDate.now());

        cmbSortUser.setItems(FXCollections.observableArrayList("Recent First", "Oldest First", "Highest Rating", "Lowest Rating"));
        cmbSortUser.setValue("Recent First");

        // Listeners for live filtering/sorting
        fldSearchUser.textProperty().addListener((obs, oldVal, newVal) -> refreshCards());
        cmbSortUser.valueProperty().addListener((obs, oldVal, newVal) -> refreshCards());

        refreshCards();
    }

    private javafx.scene.layout.VBox createComplaintCard(Complaint c) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(320);

        String repDate = c.getDateOfComplaint().toLocalDate().toString();
        String incDate = c.getIncidentDate() != null ? c.getIncidentDate().toString() : repDate;
        Label dateLbl = new Label("Reported: " + repDate + " | Incident: " + incDate);
        dateLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        Label titleLbl = new Label(c.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1E293B;");

        Label statusLbl = new Label(c.getStatus());
        String statusColor = c.getStatus().equalsIgnoreCase("RESOLVED") ? "#10B981" : c.getStatus().equalsIgnoreCase("REJECTED") ? "#EF4444" : "#F59E0B";
        statusLbl.setStyle("-fx-background-color: " + statusColor + "20; -fx-text-fill: " + statusColor + "; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10, titleLbl, new javafx.scene.layout.Region(), statusLbl);
        javafx.scene.layout.HBox.setHgrow(header.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Rating cardRating = new Rating(5, c.getRate());
        cardRating.setPartialRating(false);
        cardRating.setMouseTransparent(true);
        cardRating.setScaleX(0.7);
        cardRating.setScaleY(0.7);
        cardRating.setTranslateX(-25); // Adjust for scale offset
        
        javafx.scene.layout.HBox ratingBox = new javafx.scene.layout.HBox(cardRating);
        ratingBox.setPrefHeight(20);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label responseLbl = new Label("Admin Response:\n" + (c.getAdminResponse() != null ? c.getAdminResponse() : "Pending"));
        responseLbl.setWrapText(true);
        responseLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-background-color: #F8FAFC; -fx-padding: 8; -fx-background-radius: 6;");
        
        Label descLbl = new Label(c.getDescription());
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        javafx.scene.layout.VBox contentBox = new javafx.scene.layout.VBox(5, descLbl, responseLbl);

        List<String> imagePaths = c.getImagePathsList();
        if (!imagePaths.isEmpty()) {
            javafx.scene.layout.FlowPane imagesBox = new javafx.scene.layout.FlowPane(5, 5);
            for (String path : imagePaths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(file.toURI().toString()));
                    imgView.setFitWidth(120);
                    imgView.setFitHeight(80);
                    imgView.setPreserveRatio(true);

                    javafx.scene.layout.VBox imgContainer = new javafx.scene.layout.VBox(imgView);
                    imgContainer.setAlignment(javafx.geometry.Pos.CENTER);
                    imgContainer.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 5; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");
                    
                    imagesBox.getChildren().add(imgContainer);
                }
            }
            if (!imagesBox.getChildren().isEmpty()) {
                contentBox.getChildren().add(0, imagesBox);
            }
        }

        javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(10);
        if (currentUser != null && c.getUserId() == currentUser.getId()) {
            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 6;");
            btnEdit.setOnAction(e -> populateEditForm(c));

            Button btnDelete = new Button("Delete");
            btnDelete.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 6;");
            btnDelete.setOnAction(e -> {
                service.supprimer(c.getId());
                refreshCards();
            });
            actions.getChildren().addAll(btnEdit, btnDelete);
        }

        card.getChildren().addAll(dateLbl, header, ratingBox, contentBox, actions);
        return card;
    }

    @FXML
    private void handleGenerateTitleAI() {
        String description = fldDescription.getText();
        if (description == null || description.trim().isEmpty()) {
            showError("Please write a description first so the AI can suggest a title.");
            return;
        }

        fldTitle.setPromptText("Thinking...");
        java.util.concurrent.CompletableFuture.supplyAsync(() -> tn.esprit.projet.utils.GeminiService.suggestTitle(description))
            .thenAccept(title -> {
                javafx.application.Platform.runLater(() -> {
                    if (title != null && !title.isEmpty()) {
                        fldTitle.setText(title);
                    } else {
                        showError("AI could not generate a title. Check your internet.");
                    }
                });
            });
    }

    private void populateEditForm(Complaint c) {
        editTarget = c;
        fldTitle.setText(c.getTitle());
        fldDescription.setText(c.getDescription());
        fldPhone.setText(c.getPhoneNumber());
        ratingControl.setRating(c.getRate());
        dpDate.setValue(c.getIncidentDate() != null ? c.getIncidentDate() : c.getDateOfComplaint().toLocalDate());
        btnSubmit.setText("Update Complaint");
        
        List<String> imagePaths = c.getImagePathsList();
        if (!imagePaths.isEmpty()) {
            lblImageName.setText(imagePaths.size() + " files attached");
            lblImageName.setVisible(true);
            lblImageName.setManaged(true);
            
            imagePreviewContainer.getChildren().clear();
            for (String path : imagePaths) {
                File f = new File(path);
                if (f.exists()) {
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(f.toURI().toString()));
                    imgView.setFitWidth(100);
                    imgView.setFitHeight(60);
                    imgView.setPreserveRatio(true);
                    imagePreviewContainer.getChildren().add(imgView);
                }
            }
            if (!imagePreviewContainer.getChildren().isEmpty()) {
                imagePreviewContainer.setVisible(true);
                imagePreviewContainer.setManaged(true);
            } else {
                imagePreviewContainer.setVisible(false);
                imagePreviewContainer.setManaged(false);
            }
        } else {
            lblImageName.setText("");
            lblImageName.setVisible(false);
            lblImageName.setManaged(false);
            
            imagePreviewContainer.getChildren().clear();
            imagePreviewContainer.setVisible(false);
            imagePreviewContainer.setManaged(false);
        }
        selectedImagePath = c.getImagePath();
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(fldTitle.getScene().getWindow());
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            StringBuilder paths = new StringBuilder();
            imagePreviewContainer.getChildren().clear();
            
            for (File f : selectedFiles) {
                if (paths.length() > 0) paths.append(";");
                paths.append(f.getAbsolutePath());
                
                if (f.exists()) {
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(f.toURI().toString()));
                    imgView.setFitWidth(100);
                    imgView.setFitHeight(60);
                    imgView.setPreserveRatio(true);
                    imagePreviewContainer.getChildren().add(imgView);
                }
            }
            selectedImagePath = paths.toString();
            lblImageName.setText(selectedFiles.size() + " files selected");
            lblImageName.setVisible(true);
            lblImageName.setManaged(true);

            if (!imagePreviewContainer.getChildren().isEmpty()) {
                imagePreviewContainer.setVisible(true);
                imagePreviewContainer.setManaged(true);
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
        Integer rate = (int) ratingControl.getRating();
        LocalDate date = dpDate.getValue();

        if (title.isEmpty() || description.isEmpty() || date == null) {
            showError("Please fill out all required fields marked with *.");
            return;
        }

        if (title.length() < 2) {
            showError("The title must be at least 2 characters long.");
            return;
        }

        if (!phone.isEmpty() && !phone.matches("\\d{8}")) {
            showError("The phone number must be exactly 8 digits.");
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            showError("The incident date cannot be in the future.");
            return;
        }

        if (currentUser == null) {
            showError("No user logged in. Cannot submit complaint.");
            return;
        }

        // --- NEW: PROFANITY CHECK ---
        btnSubmit.setDisable(true); // Prevent double click
        if (tn.esprit.projet.utils.BadWordAPI.hasProfanity(title) || tn.esprit.projet.utils.BadWordAPI.hasProfanity(description)) {
            showError("Your complaint contains inappropriate language. Please revise it.");
            btnSubmit.setDisable(false);
            return;
        }
        btnSubmit.setDisable(false);
        // ----------------------------

        if (editTarget != null) {
            editTarget.setTitle(title);
            editTarget.setDescription(description);
            editTarget.setPhoneNumber(phone);
            editTarget.setRate(rate);
            editTarget.setIncidentDate(date);
            if (selectedImagePath != null) {
                editTarget.setImagePath(selectedImagePath);
            }
            service.modifier(editTarget);
            Toast.show((javafx.stage.Stage)fldTitle.getScene().getWindow(), "Complaint updated successfully!", Toast.Type.SUCCESS);
            editTarget = null;
            btnSubmit.setText("Submit Complaint");
        } else {
            Complaint complaint = new Complaint(
                    currentUser.getId(),
                    title,
                    description,
                    phone,
                    rate
            );
            complaint.setDateOfComplaint(LocalDateTime.now());
            complaint.setIncidentDate(date);
            complaint.setImagePath(selectedImagePath);

            service.ajouter(complaint);
            Toast.show((javafx.stage.Stage)fldTitle.getScene().getWindow(), "Complaint submitted successfully!", Toast.Type.SUCCESS);
        }

        // Clear fields
        fldTitle.clear();
        fldDescription.clear();
        fldPhone.clear();
        ratingControl.setRating(1);
        dpDate.setValue(LocalDate.now());
        selectedImagePath = null;
        if (lblImageName != null) {
            lblImageName.setText("");
            lblImageName.setVisible(false);
            lblImageName.setManaged(false);
        }
        if (imagePreviewContainer != null) {
            imagePreviewContainer.getChildren().clear();
            imagePreviewContainer.setVisible(false);
            imagePreviewContainer.setManaged(false);
        }

        refreshCards();
    }

    @FXML
    private void handleRefresh() {
        refreshCards();
    }

    private void refreshCards() {
        if (currentUser != null && cardsContainer != null) {
            cardsContainer.getChildren().clear();
            
            // Reload from DB only if needed or just use cached masterData
            masterData = service.getAll(); 
            
            String search = fldSearchUser.getText();
            String sort = cmbSortUser.getValue();

            java.util.stream.Stream<Complaint> stream = masterData.stream();

            // 1. Filter
            if (search != null && !search.isEmpty()) {
                String lowerSearch = search.toLowerCase();
                stream = stream.filter(c -> c.getTitle().toLowerCase().contains(lowerSearch) 
                                         || c.getDescription().toLowerCase().contains(lowerSearch)
                                         || c.getStatus().toLowerCase().contains(lowerSearch));
            }

            // 2. Sort
            if (sort != null) {
                switch (sort) {
                    case "Recent First":
                        stream = stream.sorted((a, b) -> b.getDateOfComplaint().compareTo(a.getDateOfComplaint()));
                        break;
                    case "Oldest First":
                        stream = stream.sorted((a, b) -> a.getDateOfComplaint().compareTo(b.getDateOfComplaint()));
                        break;
                    case "Highest Rating":
                        stream = stream.sorted((a, b) -> Integer.compare(b.getRate(), a.getRate()));
                        break;
                    case "Lowest Rating":
                        stream = stream.sorted((a, b) -> Integer.compare(a.getRate(), b.getRate()));
                        break;
                }
            }

            stream.forEach(c -> cardsContainer.getChildren().add(createComplaintCard(c)));
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
