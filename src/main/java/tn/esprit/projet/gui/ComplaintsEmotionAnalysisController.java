package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.models.EmotionAnalysis;
import tn.esprit.projet.services.ComplaintService;
import tn.esprit.projet.services.EmotionAnalysisService;
import tn.esprit.projet.utils.Toast;
import org.controlsfx.control.Rating;

public class ComplaintsEmotionAnalysisController {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField fldSearch;
    @FXML private ComboBox<String> cmbFilterUrgency;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private Label lblAnalysisStatus;

    private ComplaintService complaintService;
    private ObservableList<Complaint> masterData;

    @FXML
    public void initialize() {
        complaintService = new ComplaintService();
        cmbFilterUrgency.setItems(FXCollections.observableArrayList(
                "All Urgency Levels", "🔴 CRITICAL (5)", "🔴 URGENT (4)", "🟠 HIGH (3)", "🟡 MEDIUM (2)", "🟢 LOW (1)"));
        cmbFilterUrgency.setValue("All Urgency Levels");
        cmbSortBy.setItems(FXCollections.observableArrayList(
                "Urgency (High to Low)", "Emotion Score (High to Low)", "Date (Recent First)", "Rating (Low to High)"));
        cmbSortBy.setValue("Urgency (High to Low)");
        fldSearch.textProperty().addListener((obs, o, n) -> refreshDisplay());
        cmbFilterUrgency.valueProperty().addListener((obs, o, n) -> refreshDisplay());
        cmbSortBy.valueProperty().addListener((obs, o, n) -> refreshDisplay());
        loadComplaints();
    }

    private void loadComplaints() {
        masterData = FXCollections.observableArrayList(complaintService.getAll());
        lblAnalysisStatus.setText("Loaded " + masterData.size() + " complaints");
        refreshDisplay();
    }

    private void refreshDisplay() {
        if (masterData == null) return;
        String searchText = fldSearch.getText().toLowerCase();
        String urgencyFilter = cmbFilterUrgency.getValue();
        String sortBy = cmbSortBy.getValue();
        java.util.stream.Stream<Complaint> stream = masterData.stream();
        if (searchText != null && !searchText.isEmpty()) {
            stream = stream.filter(c -> c.getTitle().toLowerCase().contains(searchText) ||
                    c.getDescription().toLowerCase().contains(searchText) ||
                    c.getDetectedEmotion().toLowerCase().contains(searchText));
        }
        if (urgencyFilter != null && !urgencyFilter.equals("All Urgency Levels")) {
            int level = extractUrgencyLevel(urgencyFilter);
            stream = stream.filter(c -> c.getUrgencyLevel() == level);
        }
        if (sortBy != null) {
            switch (sortBy) {
                case "Urgency (High to Low)":        stream = stream.sorted((a, b) -> Integer.compare(b.getUrgencyLevel(), a.getUrgencyLevel())); break;
                case "Emotion Score (High to Low)":  stream = stream.sorted((a, b) -> Double.compare(b.getEmotionScore(), a.getEmotionScore())); break;
                case "Date (Recent First)":          stream = stream.sorted((a, b) -> b.getDateOfComplaint().compareTo(a.getDateOfComplaint())); break;
                case "Rating (Low to High)":         stream = stream.sorted((a, b) -> Integer.compare(a.getRate(), b.getRate())); break;
            }
        }
        cardsContainer.getChildren().clear();
        stream.forEach(c -> cardsContainer.getChildren().add(createComplaintCard(c)));
    }

    private int extractUrgencyLevel(String filter) {
        if (filter.contains("(5)")) return 5;
        if (filter.contains("(4)")) return 4;
        if (filter.contains("(3)")) return 3;
        if (filter.contains("(2)")) return 2;
        return 1;
    }

    private VBox createComplaintCard(Complaint c) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
        card.setPrefWidth(300);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label urgencyLbl = new Label(EmotionAnalysisService.getUrgencyEmoji(c.getUrgencyLevel()) + " " + EmotionAnalysisService.getUrgencyLabel(c.getUrgencyLevel()));
        urgencyLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label titleLbl = new Label(c.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label emotionLbl = new Label(c.getDetectedEmotion());
        emotionLbl.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
        header.getChildren().addAll(urgencyLbl, titleLbl, spacer, emotionLbl);

        VBox emotionBox = new VBox(5);
        emotionBox.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");
        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);
        Label scoreLbl = new Label("Emotion Score: " + String.format("%.0f", c.getEmotionScore()) + "/100");
        scoreLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        ProgressBar scoreBar = new ProgressBar(c.getEmotionScore() / 100.0);
        scoreBar.setPrefWidth(150);
        scoreBar.setStyle("-fx-accent: " + EmotionAnalysisService.getUrgencyColor(c.getUrgencyLevel()) + ";");
        scoreBox.getChildren().addAll(scoreLbl, scoreBar);
        emotionBox.getChildren().add(scoreBox);
        if (c.getEmotionRecommendation() != null && !c.getEmotionRecommendation().isEmpty()) {
            Label recLbl = new Label(c.getEmotionRecommendation());
            recLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
            emotionBox.getChildren().add(recLbl);
        }

        Label dateLbl = new Label("Reported: " + c.getDateOfComplaint().toLocalDate());
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Label userLbl = new Label("User: " + (c.getUserName() != null ? c.getUserName() : "Unknown"));
        userLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        Rating ratingControl = new Rating(5, c.getRate());
        ratingControl.setPartialRating(false);
        ratingControl.setMouseTransparent(true);
        ratingControl.setScaleX(0.7);
        ratingControl.setScaleY(0.7);
        HBox ratingBox = new HBox(ratingControl);
        ratingBox.setPrefHeight(20);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, emotionBox, dateLbl, userLbl, ratingBox);
        return card;
    }

    @FXML
    private void handleAnalyzeAllComplaints() {
        if (masterData == null || masterData.isEmpty()) {
            Toast.show((javafx.stage.Stage) cardsContainer.getScene().getWindow(), "No complaints to analyze.", Toast.Type.ERROR);
            return;
        }
        lblAnalysisStatus.setText("Analyzing emotions... Please wait");
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            int analyzed = 0, saved = 0, failed = 0, skipped = 0;
            for (Complaint complaint : masterData) {
                if (complaint.getUrgencyLevel() == 1 && complaint.getEmotionScore() == 0) {
                    try {
                        EmotionAnalysis analysis = EmotionAnalysisService.analyzeComplaintEmotion(
                                complaint.getTitle(), complaint.getDescription());
                        if (analysis != null && !analysis.getPrimaryEmotion().equals("NEUTRAL") && analysis.getEmotionScore() > 0) {
                            complaint.setDetectedEmotion(analysis.getPrimaryEmotion());
                            complaint.setEmotionScore(analysis.getEmotionScore());
                            complaint.setUrgencyLevel(analysis.getUrgencyLevel());
                            complaint.setEmotionRecommendation(analysis.getRecommendation());
                            complaintService.modifier(complaint);
                            analyzed++; saved++;
                        } else { failed++; }
                    } catch (Exception e) { failed++; }
                    try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                } else { skipped++; }
            }
            final int fa = analyzed, fs = saved, ff = failed, fsk = skipped;
            javafx.application.Platform.runLater(() -> {
                lblAnalysisStatus.setText(String.format("Analyzed: %d | Saved: %d | Failed: %d | Skipped: %d", fa, fs, ff, fsk));
                if (fs > 0) {
                    loadComplaints();
                    Toast.show((javafx.stage.Stage) cardsContainer.getScene().getWindow(), "Success! Analyzed: " + fa + ", Saved: " + fs, Toast.Type.SUCCESS);
                } else if (ff > 0) {
                    Toast.show((javafx.stage.Stage) cardsContainer.getScene().getWindow(), "Analysis failed. Check API quota.", Toast.Type.ERROR);
                } else {
                    Toast.show((javafx.stage.Stage) cardsContainer.getScene().getWindow(), "All complaints already analyzed (skipped: " + fsk + ")", Toast.Type.INFO);
                }
            });
        });
    }

    @FXML
    private void handleRefresh() { loadComplaints(); }
}
