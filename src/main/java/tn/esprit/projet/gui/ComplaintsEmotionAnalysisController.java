package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.models.EmotionAnalysis;
import tn.esprit.projet.services.ComplaintService;
import tn.esprit.projet.services.EmotionAnalysisService;
import tn.esprit.projet.utils.Toast;
import org.controlsfx.control.Rating;

import java.util.List;
import java.util.stream.Collectors;

public class ComplaintsEmotionAnalysisController {

    @FXML private javafx.scene.layout.FlowPane cardsContainer;
    @FXML private TextField fldSearch;
    @FXML private ComboBox<String> cmbFilterUrgency;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private Label lblAnalysisStatus;

    private ComplaintService complaintService;
    private ObservableList<Complaint> masterData;
    private Complaint selectedComplaint;

    @FXML
    public void initialize() {
        complaintService = new ComplaintService();

        cmbFilterUrgency.setItems(FXCollections.observableArrayList(
                "All Urgency Levels",
                "🔴 CRITICAL (5)",
                "🔴 URGENT (4)",
                "🟠 HIGH (3)",
                "🟡 MEDIUM (2)",
                "🟢 LOW (1)"
        ));
        cmbFilterUrgency.setValue("All Urgency Levels");

        cmbSortBy.setItems(FXCollections.observableArrayList(
                "Urgency (High to Low)",
                "Emotion Score (High to Low)",
                "Date (Recent First)",
                "Rating (Low to High)"
        ));
        cmbSortBy.setValue("Urgency (High to Low)");

        fldSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshDisplay());
        cmbFilterUrgency.valueProperty().addListener((obs, oldVal, newVal) -> refreshDisplay());
        cmbSortBy.valueProperty().addListener((obs, oldVal, newVal) -> refreshDisplay());

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
            stream = stream.filter(c -> 
                c.getTitle().toLowerCase().contains(searchText) ||
                c.getDescription().toLowerCase().contains(searchText) ||
                c.getDetectedEmotion().toLowerCase().contains(searchText)
            );
        }

        if (!urgencyFilter.equals("All Urgency Levels")) {
            int urgencyLevel = extractUrgencyLevel(urgencyFilter);
            stream = stream.filter(c -> c.getUrgencyLevel() == urgencyLevel);
        }

        if (sortBy != null) {
            switch (sortBy) {
                case "Urgency (High to Low)":
                    stream = stream.sorted((a, b) -> Integer.compare(b.getUrgencyLevel(), a.getUrgencyLevel()));
                    break;
                case "Emotion Score (High to Low)":
                    stream = stream.sorted((a, b) -> Double.compare(b.getEmotionScore(), a.getEmotionScore()));
                    break;
                case "Date (Recent First)":
                    stream = stream.sorted((a, b) -> b.getDateOfComplaint().compareTo(a.getDateOfComplaint()));
                    break;
                case "Rating (Low to High)":
                    stream = stream.sorted((a, b) -> Integer.compare(a.getRate(), b.getRate()));
                    break;
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
        if (filter.contains("(1)")) return 1;
        return 1;
    }

    private VBox createComplaintCard(Complaint c) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
        card.setPrefWidth(300);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String urgencyEmoji = EmotionAnalysisService.getUrgencyEmoji(c.getUrgencyLevel());
        String urgencyLabel = EmotionAnalysisService.getUrgencyLabel(c.getUrgencyLevel());
        Label urgencyLbl = new Label(urgencyEmoji + " " + urgencyLabel);
        urgencyLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label titleLbl = new Label(c.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label emotionLbl = new Label(c.getDetectedEmotion());
        emotionLbl.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 3 8; -fx-background-radius: 4; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;");

        header.getChildren().addAll(urgencyLbl, titleLbl, spacer, emotionLbl);

        VBox emotionBox = new VBox(5);
        emotionBox.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 6; " +
                           "-fx-border-color: #E2E8F0; -fx-border-radius: 6;");

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
        card.setOnMouseClicked(e -> showComplaintDetails(c));

        return card;
    }

    private void showComplaintDetails(Complaint c) {
        selectedComplaint = c;
    }

    @FXML
    private void handleAnalyzeAllComplaints() {
        if (masterData == null || masterData.isEmpty()) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), 
                      "No complaints to analyze.", Toast.Type.ERROR);
            return;
        }

        // Check if API quota is available
        System.out.println("=== STARTING EMOTION ANALYSIS ===");
        System.out.println("Total complaints to analyze: " + masterData.size());
        
        lblAnalysisStatus.setText("Analyzing emotions... Please wait (this may take a while)");
        
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            int analyzed = 0;
            int saved = 0;
            int failed = 0;
            int skipped = 0;
            String lastError = "";
            
            for (Complaint complaint : masterData) {
                // Only analyze complaints that haven't been analyzed yet
                if (complaint.getUrgencyLevel() == 1 && complaint.getEmotionScore() == 0) {
                    System.out.println("\n[EmotionAnalysis] ========================================");
                    System.out.println("[EmotionAnalysis] Analyzing complaint ID: " + complaint.getId());
                    System.out.println("[EmotionAnalysis] Title: " + complaint.getTitle());
                    System.out.println("[EmotionAnalysis] Description: " + complaint.getDescription().substring(0, Math.min(50, complaint.getDescription().length())) + "...");
                    
                    try {
                        EmotionAnalysis analysis = EmotionAnalysisService.analyzeComplaintEmotion(
                            complaint.getTitle(),
                            complaint.getDescription()
                        );
                        
                        if (analysis != null && !analysis.getPrimaryEmotion().equals("NEUTRAL") && analysis.getEmotionScore() > 0) {
                            System.out.println("[EmotionAnalysis] ✅ Analysis successful!");
                            System.out.println("[EmotionAnalysis]    Emotion: " + analysis.getPrimaryEmotion());
                            System.out.println("[EmotionAnalysis]    Score: " + analysis.getEmotionScore());
                            System.out.println("[EmotionAnalysis]    Urgency: " + analysis.getUrgencyLevel());
                            
                            complaint.setDetectedEmotion(analysis.getPrimaryEmotion());
                            complaint.setEmotionScore(analysis.getEmotionScore());
                            complaint.setUrgencyLevel(analysis.getUrgencyLevel());
                            complaint.setEmotionRecommendation(analysis.getRecommendation());
                            
                            // SAVE TO DATABASE
                            try {
                                complaintService.modifier(complaint);
                                saved++;
                                System.out.println("[EmotionAnalysis] ✅ Saved to database successfully!");
                            } catch (Exception e) {
                                System.err.println("[EmotionAnalysis] ❌ Failed to save to database: " + e.getMessage());
                                e.printStackTrace();
                                failed++;
                                lastError = "Database save error: " + e.getMessage();
                            }
                            
                            analyzed++;
                        } else {
                            System.err.println("[EmotionAnalysis] ❌ Analysis returned null or default values");
                            System.err.println("[EmotionAnalysis]    This usually means API quota exhausted or API error");
                            failed++;
                            lastError = "API quota exhausted or API error - check console for details";
                        }
                    } catch (Exception e) {
                        System.err.println("[EmotionAnalysis] ❌ Exception during analysis: " + e.getMessage());
                        e.printStackTrace();
                        failed++;
                        lastError = e.getMessage();
                    }
                    
                    // Rate limiting - wait between requests
                    try {
                        Thread.sleep(2000); // 2 seconds between requests to avoid rate limiting
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    skipped++;
                    System.out.println("[EmotionAnalysis] ⏭️ Skipping complaint ID " + complaint.getId() + " (already analyzed)");
                }
            }
            
            final int finalAnalyzed = analyzed;
            final int finalSaved = saved;
            final int finalFailed = failed;
            final int finalSkipped = skipped;
            final String finalError = lastError;
            
            System.out.println("\n=== EMOTION ANALYSIS COMPLETE ===");
            System.out.println("Analyzed: " + finalAnalyzed);
            System.out.println("Saved: " + finalSaved);
            System.out.println("Failed: " + finalFailed);
            System.out.println("Skipped: " + finalSkipped);
            if (!finalError.isEmpty()) {
                System.out.println("Last Error: " + finalError);
            }
            
            javafx.application.Platform.runLater(() -> {
                String statusMsg = String.format("Analyzed: %d | Saved: %d | Failed: %d | Skipped: %d", 
                                                 finalAnalyzed, finalSaved, finalFailed, finalSkipped);
                lblAnalysisStatus.setText(statusMsg);
                
                if (finalSaved > 0) {
                    loadComplaints(); // Reload from database to show updated data
                    Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), 
                              "Success! Analyzed: " + finalAnalyzed + ", Saved: " + finalSaved, 
                              Toast.Type.SUCCESS);
                } else if (finalFailed > 0) {
                    Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), 
                              "Analysis failed: " + finalError, 
                              Toast.Type.ERROR);
                } else {
                    Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), 
                              "All complaints already analyzed (skipped: " + finalSkipped + ")", 
                              Toast.Type.INFO);
                }
            });
        }).exceptionally(ex -> {
            System.err.println("[EmotionAnalysis] ❌ FATAL ERROR: " + ex.getMessage());
            ex.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                lblAnalysisStatus.setText("Fatal error: " + ex.getMessage());
                Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), 
                          "Fatal error during analysis: " + ex.getMessage(), 
                          Toast.Type.ERROR);
            });
            return null;
        });
    }

    @FXML
    private void handleRefresh() {
        loadComplaints();
    }
}
