package tn.esprit.projet.gui;

import com.github.sarxos.webcam.Webcam;
import com.google.gson.JsonObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.esprit.projet.models.Additive;
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.INCIService;
import tn.esprit.projet.services.OpenFoodFactsService;
import tn.esprit.projet.services.ScannerService;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.stage.Stage;

public class ScannerController implements Initializable {

    // ===========================
    // FXML ELEMENTS - LEFT SIDE
    // ===========================
    @FXML private ImageView webcamImageView;
    @FXML private VBox cameraOffOverlay;
    @FXML private Rectangle scanFrame;
    @FXML private Button btnToggleCamera;
    @FXML private Button btnScan;
    @FXML private TextField manualBarcodeField;
    @FXML private Circle statusCircle;
    @FXML private Label statusLabel;

    // ===========================
    // FXML ELEMENTS - RIGHT SIDE
    // ===========================
    @FXML private VBox waitingPane;
    @FXML private VBox productInfoPane;
    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label productBrand;
    @FXML private Label productBarcode;

    @FXML private VBox boycottPane;
    @FXML private HBox boycottBadge;
    @FXML private Label boycottStatusLabel;
    @FXML private Label boycottReason;
    @FXML private VBox alternativesBox;
    @FXML private Label alternativesLabel;

    @FXML private VBox ecoScorePane;
    @FXML private VBox ecoA, ecoB, ecoC, ecoD, ecoE;
    @FXML private Label ecoScoreDesc;

    @FXML private VBox inciPane;
    @FXML private Label inciScoreLabel;
    @FXML private ProgressBar inciProgressBar;
    @FXML private VBox additivesList;
    @FXML private Label inciWarning;

    @FXML private HBox actionButtons;
    @FXML private Button btnInciDetails;
    // ===========================
    // SERVICES
    // ===========================
    private ScannerService scannerService;
    private OpenFoodFactsService foodFactsService;
    private BoycottService boycottService;
    private INCIService inciService;

    // ===========================
    // VARIABLES
    // ===========================
    private Timeline cameraTimeline;
    private boolean cameraRunning = false;
    private String lastScannedBarcode = "";
    private boolean isBoycotted = false;
    private String currentEcoScore = "N/A";
    private List<Additive> lastAdditives = new ArrayList<>();
    private int lastInciScore = 100;
    private String lastProductName = "";
    // ===========================
    // INITIALISATION
    // ===========================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            scannerService   = new ScannerService();
            foodFactsService = new OpenFoodFactsService();
            boycottService   = new BoycottService();
            inciService      = new INCIService();

            resetEcoScoreColors();
        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR in ScannerController.initialize: " + t.getMessage());
            t.printStackTrace();
        }
    }

    // ===========================
    // TOGGLE CAMERA ON/OFF
    // ===========================
    @FXML
    private void toggleCamera() {
        if (!cameraRunning) {
            startCamera();
        } else {
            stopCamera();
        }
    }

    private void startCamera() {
        try {
            scannerService.startCamera();
            cameraRunning = true;

            btnToggleCamera.setText("⏹ Stop Camera");
            btnToggleCamera.setStyle(
                    "-fx-background-color: #E74C3C;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10;" +
                            "-fx-cursor: hand;"
            );
            btnScan.setDisable(false);
            cameraOffOverlay.setVisible(false);
            statusCircle.setFill(Color.web("#2ECC71"));
            statusLabel.setText("Camera active - Ready to scan");

            cameraTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                BufferedImage frame = scannerService.getFrame();
                if (frame != null) {
                    Image fxImage = SwingFXUtils.toFXImage(frame, null);
                    webcamImageView.setImage(fxImage);
                }
            }));
            cameraTimeline.setCycleCount(Timeline.INDEFINITE);
            cameraTimeline.play();

        } catch (Exception e) {
            showAlert("Camera Error", "Could not open camera: " + e.getMessage());
        }
    }

    private void stopCamera() {
        if (cameraTimeline != null) cameraTimeline.stop();
        scannerService.stopCamera();
        cameraRunning = false;

        webcamImageView.setImage(null);
        cameraOffOverlay.setVisible(true);
        btnToggleCamera.setText("▶ Start Camera");
        btnToggleCamera.setStyle(
                "-fx-background-color: #3498DB;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;"
        );
        btnScan.setDisable(true);
        statusCircle.setFill(Color.web("#E74C3C"));
        statusLabel.setText("Camera inactive");
    }

    // ===========================
    // SCAN BARCODE
    // ===========================
    @FXML
    private void scanBarcode() {
        statusLabel.setText("Scanning...");
        if (scanFrame != null) scanFrame.setVisible(true);

        new Thread(() -> {
            String barcode = scannerService.scanBarcode();
            Platform.runLater(() -> {
                if (scanFrame != null) scanFrame.setVisible(false);
                if (barcode != null && !barcode.isEmpty()) {
                    lastScannedBarcode = barcode;
                    statusLabel.setText("Barcode detected: " + barcode);

                    // +5 pts pour chaque scan
                    EthicalPointsManager.addPoints("Product scanned", 5);

                    analyzeProduct(barcode);
                } else {
                    statusLabel.setText("No barcode detected. Try again.");
                }
            });
        }).start();
    }

    // ===========================
    // SAISIE MANUELLE
    // ===========================
    @FXML
    private void manualSearch() {
        String barcode = manualBarcodeField.getText().trim();
        if (!barcode.isEmpty()) {
            lastScannedBarcode = barcode;

            // +5 pts pour chaque scan
            EthicalPointsManager.addPoints("Product scanned manually", 5);

            analyzeProduct(barcode);
        } else {
            showAlert("Error", "Please enter a barcode.");
        }
    }

    // ===========================
    // ANALYSE COMPLÈTE DU PRODUIT
    // ===========================
    private void analyzeProduct(String barcode) {
        statusLabel.setText("Fetching product info...");

        new Thread(() -> {
            JsonObject product = foodFactsService.getProductInfo(barcode);

            Platform.runLater(() -> {
                if (product == null) {
                    showAlert("Not Found", "Product not found in Open Food Facts database.");
                    statusLabel.setText("Product not found.");
                    return;
                }

                String name             = foodFactsService.getProductName(product);
                String brand            = foodFactsService.getBrand(product);
                String eco              = foodFactsService.getEcoScore(product);
                String imgUrl           = foodFactsService.getImageUrl(product);
                List<String> addCodes   = foodFactsService.getAdditives(product);

                currentEcoScore = eco;

                // Afficher les sections
                displayProductInfo(name, brand, barcode, imgUrl);

                BoycottBrand boycott = boycottService.checkBrand(brand);
                isBoycotted = (boycott != null);
                displayBoycottStatus(boycott);

                displayEcoScore(eco);

                List<Additive> additives = inciService.analyzeAdditives(addCodes);
                int score = inciService.calculateInciScore(additives);
                displayInciAnalysis(additives, score);

                showActionButtons();

                statusLabel.setText("Analysis complete ✓");
            });
        }).start();
    }

    // ===========================
    // AFFICHAGE INFO PRODUIT
    // ===========================
    private void displayProductInfo(String name, String brand, String barcode, String imgUrl) {
        waitingPane.setVisible(false);
        waitingPane.setManaged(false);
        lastProductName = name;
        productName.setText(name);
        productBrand.setText("Brand: " + brand);
        productBarcode.setText("Barcode: " + barcode);

        if (imgUrl != null && !imgUrl.isEmpty()) {
            try {
                productImage.setImage(new Image(imgUrl, true));
            } catch (Exception e) {
                System.err.println("Could not load image: " + e.getMessage());
            }
        }

        productInfoPane.setVisible(true);
        productInfoPane.setManaged(true);
    }

    // ===========================
    // AFFICHAGE BOYCOTT
    // ===========================
    private void displayBoycottStatus(BoycottBrand boycott) {
        if (boycott != null) {
            boycottBadge.setStyle(
                    "-fx-background-color: #E74C3C;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 15;"
            );
            boycottStatusLabel.setText("🔴 BOYCOTTED");
            boycottReason.setText(boycott.getReason());

            if (boycott.getAlternatives() != null) {
                alternativesLabel.setText(boycott.getAlternatives());
                alternativesBox.setVisible(true);
                alternativesBox.setManaged(true);
            }
        } else {
            boycottBadge.setStyle(
                    "-fx-background-color: #2ECC71;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 15;"
            );
            boycottStatusLabel.setText("🟢 CLEAN - Not Boycotted");
            boycottReason.setText("This brand has no reported ties to occupation activities.");
            alternativesBox.setVisible(false);
            alternativesBox.setManaged(false);
        }

        boycottPane.setVisible(true);
        boycottPane.setManaged(true);

    }

    // ===========================
    // AFFICHAGE ECO SCORE
    // ===========================
    private void displayEcoScore(String grade) {
        resetEcoScoreColors();

        String description;

        switch (grade.toUpperCase()) {
            case "A":
                ecoA.setStyle("-fx-background-color: #1A9850; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🌱 Excellent environmental impact. Low carbon footprint, eco-friendly packaging.";
                break;
            case "B":
                ecoB.setStyle("-fx-background-color: #66BD63; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🌿 Good environmental impact. Relatively eco-friendly product.";
                break;
            case "C":
                ecoC.setStyle("-fx-background-color: #FEE08B; -fx-background-radius: 10; -fx-padding: 8;");
                description = "⚠️ Moderate environmental impact. Some concerns about production.";
                break;
            case "D":
                ecoD.setStyle("-fx-background-color: #F46D43; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🏭 High environmental impact. Consider local alternatives.";
                break;
            case "E":
                ecoE.setStyle("-fx-background-color: #D73027; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🔴 Very high environmental impact. Please choose a better alternative.";
                break;
            default:
                description = "No eco-score data available for this product.";
        }

        ecoScoreDesc.setText(description);
        ecoScorePane.setVisible(true);
        ecoScorePane.setManaged(true);
    }

    private void resetEcoScoreColors() {
        String defaultStyle = "-fx-background-color: #ECF0F1; -fx-background-radius: 10; -fx-padding: 8;";
        if (ecoA != null) ecoA.setStyle(defaultStyle);
        if (ecoB != null) ecoB.setStyle(defaultStyle);
        if (ecoC != null) ecoC.setStyle(defaultStyle);
        if (ecoD != null) ecoD.setStyle(defaultStyle);
        if (ecoE != null) ecoE.setStyle(defaultStyle);
    }

    // ===========================
    // AFFICHAGE INCI
    // ===========================
    private void displayInciAnalysis(List<Additive> additives, int score) {
        additivesList.getChildren().clear();

        inciScoreLabel.setText(score + "/100");
        inciProgressBar.setProgress(score / 100.0);

        String barColor;
        if (score >= 70) barColor = "#2ECC71";
        else if (score >= 40) barColor = "#F39C12";
        else barColor = "#E74C3C";

        inciProgressBar.setStyle("-fx-accent: " + barColor + ";");
        inciScoreLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + barColor + ";"
        );

        int dangerCount = 0;

        for (Additive a : additives) {
            HBox row = new HBox(10);
            row.setStyle(
                    "-fx-background-color: " + getAdditiveRowColor(a.getDangerLevel()) + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8;"
            );

            Label codeLabel = new Label(a.getCode());
            codeLabel.setStyle(
                    "-fx-font-weight: bold;" +
                            "-fx-text-fill: " + a.getDangerColor() + ";" +
                            "-fx-min-width: 50;"
            );

            Label nameLabel = new Label(a.getName());
            nameLabel.setStyle("-fx-text-fill: #2C3E50;");
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Label levelLabel = new Label(getDangerEmoji(a.getDangerLevel()) + " " + a.getDangerLabel());
            levelLabel.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: " + a.getDangerColor() + ";"
            );

            row.getChildren().addAll(codeLabel, nameLabel, levelLabel);
            additivesList.getChildren().add(row);

            if (a.getDangerLevel() >= 7) dangerCount++;
        }

        if (dangerCount > 0) {
            inciWarning.setText("⚠️ " + dangerCount + " dangerous additive(s) detected! Consider choosing a safer alternative.");
            inciWarning.setVisible(true);
            inciWarning.setManaged(true);
        } else {
            inciWarning.setVisible(false);
            inciWarning.setManaged(false);
        }

        if (additives.isEmpty()) {
            Label noAddLabel = new Label("✅ No additives detected");
            noAddLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
            additivesList.getChildren().add(noAddLabel);
        }

        inciPane.setVisible(true);
        inciPane.setManaged(true);

        lastAdditives = additives;
        lastInciScore = score;
        if (btnInciDetails != null) {
            btnInciDetails.setVisible(true);
            btnInciDetails.setManaged(true);
        }
    }

    // ===========================
    // BOUTONS ACTIONS
    // ===========================
    private void showActionButtons() {
        actionButtons.setVisible(true);
        actionButtons.setManaged(true);
    }

    @FXML
    private void addToStock() {
        int pointsEarned = 10;
        String message = "Product added to stock successfully! ✅";

        // +15 pts si Eco-Score A
        if (currentEcoScore.equalsIgnoreCase("A")) {
            EthicalPointsManager.addPoints("Eco-Score A product added", 15);
            pointsEarned += 15;
            message += "\n🌱 Eco-Score A bonus: +15 pts";
        }

        // +10 pts produit clean ajouté
        if (!isBoycotted) {
            EthicalPointsManager.addPoints("Clean product added to stock", 10);
            message += "\n✅ Clean product: +10 pts";
        }

        message += "\n\n🏆 Total earned: +" + pointsEarned + " Ethical Points";
        message += "\n📊 Total: " + EthicalPointsManager.getTotalPoints() + " pts";
        message += "\n" + EthicalPointsManager.getLevelName();

        showAlert("✅ Added to Stock", message);
        resetView();
    }

    @FXML
    private void rejectProduct() {
        String message = "Product rejected ❌";

        if (isBoycotted) {
            // +15 pts pour avoir rejeté un produit boycotté
            EthicalPointsManager.addPoints("Boycotted product rejected", 15);
            message += "\n🇵🇸 Boycott avoided: +15 pts";
        }

        message += "\n\n🏆 Total: " + EthicalPointsManager.getTotalPoints() + " pts";
        message += "\n" + EthicalPointsManager.getLevelName();

        showAlert("❌ Product Rejected", message);
        resetView();
    }

    // ===========================
    // RESET VIEW
    // ===========================
    private void resetView() {
        waitingPane.setVisible(true);
        waitingPane.setManaged(true);

        productInfoPane.setVisible(false);  productInfoPane.setManaged(false);
        boycottPane.setVisible(false);      boycottPane.setManaged(false);
        ecoScorePane.setVisible(false);     ecoScorePane.setManaged(false);
        inciPane.setVisible(false);         inciPane.setManaged(false);
        actionButtons.setVisible(false);    actionButtons.setManaged(false);
        alternativesBox.setVisible(false);  alternativesBox.setManaged(false);
        inciWarning.setVisible(false);      inciWarning.setManaged(false);

        additivesList.getChildren().clear();
        manualBarcodeField.clear();
        resetEcoScoreColors();

        lastScannedBarcode = "";
        isBoycotted = false;
        currentEcoScore = "N/A";

        statusLabel.setText(cameraRunning ? "Camera active - Ready to scan" : "Camera inactive");
        if (btnInciDetails != null) {
            btnInciDetails.setVisible(false);
            btnInciDetails.setManaged(false);
        }
        lastAdditives = new ArrayList<>();
        lastInciScore = 100;
        lastProductName = "";
    }

    // ===========================
    // UTILITAIRES
    // ===========================
    private String getDangerEmoji(int level) {
        if (level <= 3) return "🟢";
        if (level <= 6) return "🟡";
        if (level <= 8) return "🟠";
        return "🔴";
    }

    private String getAdditiveRowColor(int level) {
        if (level <= 3) return "#F0FFF4";
        if (level <= 6) return "#FFFBF0";
        if (level <= 8) return "#FFF3F0";
        return "#FFF0F0";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===========================
    // CLEANUP QUAND ON QUITTE
    // ===========================
    public void cleanup() {
        stopCamera();
    }
    @FXML
    private void openInciDetail() {
        Stage detailStage = new Stage();
        detailStage.setTitle("🔬 INCI Detailed Analysis");

        VBox root = new VBox(16);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 24;");

        // Header
        Label title = new Label("🔬 INCI Detailed Analysis");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label product = new Label("Product: " + lastProductName);
        product.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        Separator sep1 = new Separator();

        // Score section
        VBox scoreBox = new VBox(10);
        scoreBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #E2E8F0; -fx-border-radius: 12;");

        Label scoreTitle = new Label("GLOBAL SCORE");
        scoreTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        String scoreColor = lastInciScore >= 70 ? "#2ECC71" : lastInciScore >= 40 ? "#F39C12" : "#E74C3C";
        Label scoreValue = new Label(lastInciScore + " / 100");
        scoreValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor + ";");

        ProgressBar scoreBar = new ProgressBar(lastInciScore / 100.0);
        scoreBar.setMaxWidth(Double.MAX_VALUE);
        scoreBar.setPrefHeight(14);
        scoreBar.setStyle("-fx-accent: " + scoreColor + ";");

        String verdict;
        if (lastInciScore >= 70) verdict = "🟢 GOOD - Safe for consumption";
        else if (lastInciScore >= 40) verdict = "🟡 MODERATE - Precautions needed";
        else verdict = "🔴 BAD - Consider avoiding this product";

        Label verdictLabel = new Label(verdict);
        verdictLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor + ";");

        scoreBox.getChildren().addAll(scoreTitle, scoreValue, scoreBar, verdictLabel);

        Separator sep2 = new Separator();

        // Table header
        Label tableTitle = new Label("INGREDIENT DETAILS");
        tableTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        // Table
        VBox tableBox = new VBox(0);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12;");

        // Header row
        HBox headerRow = new HBox();
        headerRow.setStyle("-fx-background-color: #1F4D3A; -fx-padding: 12; -fx-background-radius: 12 12 0 0;");
        Label h1 = new Label("Code");
        h1.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 80;");
        Label h2 = new Label("Name");
        h2.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 180;");
        Label h3 = new Label("Score");
        h3.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 80;");
        Label h4 = new Label("Risk");
        h4.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px;");
        HBox.setHgrow(h4, Priority.ALWAYS);
        headerRow.getChildren().addAll(h1, h2, h3, h4);
        tableBox.getChildren().add(headerRow);

        // Data rows
        for (int i = 0; i < lastAdditives.size(); i++) {
            Additive a = lastAdditives.get(i);
            HBox row = new HBox();
            String bg = (i % 2 == 0) ? "#FFFFFF" : "#F8FAFC";
            row.setStyle("-fx-background-color: " + bg + "; -fx-padding: 10 12;");

            Label c1 = new Label(a.getCode());
            c1.setStyle("-fx-font-weight: bold; -fx-text-fill: " + a.getDangerColor() + "; -fx-min-width: 80;");
            Label c2 = new Label(a.getName());
            c2.setStyle("-fx-text-fill: #1E293B; -fx-min-width: 180;");
            Label c3 = new Label(getDangerEmoji(a.getDangerLevel()) + " " + a.getDangerLevel() + "/10");
            c3.setStyle("-fx-text-fill: " + a.getDangerColor() + "; -fx-min-width: 80;");
            Label c4 = new Label(a.getHealthEffects() != null ? a.getHealthEffects() : a.getDangerLabel());
            c4.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
            c4.setWrapText(true);
            HBox.setHgrow(c4, Priority.ALWAYS);

            row.getChildren().addAll(c1, c2, c3, c4);
            tableBox.getChildren().add(row);
        }

        if (lastAdditives.isEmpty()) {
            Label noAdd = new Label("✅ No additives detected in this product");
            noAdd.setStyle("-fx-font-size: 13px; -fx-text-fill: #2ECC71; -fx-font-weight: bold; -fx-padding: 20;");
            tableBox.getChildren().add(noAdd);
        }

        // Close button
        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;");
        btnClose.setOnAction(e -> detailStage.close());

        HBox btnBox = new HBox(btnClose);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        // ScrollPane
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #F8FAFC; -fx-background-color: #F8FAFC;");

        root.getChildren().addAll(title, product, sep1, scoreBox, sep2, tableTitle, tableBox, btnBox);
        scroll.setContent(root);

        Scene scene = new Scene(scroll, 700, 550);
        detailStage.setScene(scene);
        detailStage.setResizable(false);
        detailStage.show();
    }
}