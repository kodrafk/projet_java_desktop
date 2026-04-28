package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import org.controlsfx.control.Rating;
import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.services.ComplaintService;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.Toast;
import tn.esprit.projet.utils.GeminiService;
import tn.esprit.projet.utils.BadWordAPI;

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

        // Bad words validation
        if (BadWordAPI.hasProfanity(title)) {
            showError("Title contains inappropriate language. Please modify your text.");
            return;
        }

        if (BadWordAPI.hasProfanity(description)) {
            showError("Description contains inappropriate language. Please modify your text.");
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

    // Test method for BadWord API - can be called from FXML button
    @FXML
    private void testBadWordAPI() {
        System.out.println("=== Testing BadWord API ===");
        
        // Test cases
        String[] testTexts = {
            "Hello world, this is clean",
            "This contains fuck word",
            "You are stupid",
            "Great service!",
            "This is shit quality"
        };
        
        boolean apiWorking = false;
        for (String text : testTexts) {
            boolean hasProfanity = BadWordAPI.hasProfanity(text);
            System.out.println("\"" + text + "\" -> " + (hasProfanity ? "❌ BLOCKED" : "✅ ALLOWED"));
            if (hasProfanity) apiWorking = true;
        }
        
        String message = apiWorking ? 
            "✅ BadWord API is working! Check console for details." :
            "⚠️ BadWord API may not be detecting words. Check console.";
            
        Toast.show((javafx.stage.Stage)fldTitle.getScene().getWindow(), message, 
            apiWorking ? Toast.Type.SUCCESS : Toast.Type.ERROR);
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
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        card.setPrefWidth(260);

        Label dateLbl = new Label(c.getDateOfComplaint().toLocalDate().toString());
        dateLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        Label titleLbl = new Label(c.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 14px;");

        Label statusLbl = new Label(c.getStatus());
        String statusColor = c.getStatus().equalsIgnoreCase("RESOLVED") ? "#10B981" : c.getStatus().equalsIgnoreCase("REJECTED") ? "#EF4444" : "#F59E0B";
        statusLbl.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox header = new HBox(titleLbl, new Region(), statusLbl);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);

        Rating r = new Rating(5, c.getRate());
        r.setPartialRating(false);
        r.setMouseTransparent(true);
        r.setScaleX(0.6);
        r.setScaleY(0.6);
        r.setTranslateX(-20);

        card.getChildren().addAll(dateLbl, header, r);

        // --- NEW: Description ---
        Label descLbl = new Label(c.getDescription());
        descLbl.setWrapText(true);
        descLbl.setMaxHeight(60);
        descLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        card.getChildren().add(descLbl);

        // --- NEW: Image Preview ---
        if (c.getImagePath() != null && !c.getImagePath().isEmpty()) {
            try {
                String firstPath = c.getImagePathsList().get(0);
                File imgFile = new File(firstPath);
                if (imgFile.exists()) {
                    ImageView imgView = new ImageView(new Image(imgFile.toURI().toString()));
                    imgView.setFitWidth(230);
                    imgView.setFitHeight(120);
                    imgView.setPreserveRatio(true);
                    imgView.setStyle("-fx-background-radius: 5;");
                    
                    VBox imgContainer = new VBox(imgView);
                    imgContainer.setAlignment(Pos.CENTER);
                    imgContainer.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 5; -fx-padding: 5;");
                    card.getChildren().add(imgContainer);
                }
            } catch (Exception ignored) {}
        }

        if (c.getAdminResponse() != null) {
            Label respTitle = new Label("Response:");
            respTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #475569;");
            Label respContent = new Label(c.getAdminResponse());
            respContent.setWrapText(true);
            respContent.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-font-style: italic;");
            card.getChildren().addAll(new Separator(), respTitle, respContent);
        }

        return card;
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
