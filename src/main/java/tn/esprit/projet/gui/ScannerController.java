package tn.esprit.projet.gui;

import com.google.gson.JsonObject;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.projet.models.Additive;
import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.services.BoycottService;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.INCIService;
import tn.esprit.projet.services.OpenFoodFactsService;
import tn.esprit.projet.services.ScannerService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ScannerController implements Initializable {

    // ===========================
    // FXML ELEMENTS - LEFT SIDE
    // ===========================
    @FXML private ImageView qrPreviewImage;
    @FXML private Label     qrPlaceholderLabel;
    @FXML private Button    btnUploadQR;
    @FXML private Button    btnScan;
    @FXML private TextField manualBarcodeField;
    @FXML private Circle    statusCircle;
    @FXML private Label     statusLabel;

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
    private File uploadedQRFile = null;
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
        }
    }

    // ===========================
    // UPLOAD QR CODE IMAGE
    // ===========================
    @FXML
    private void uploadQRCode() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Barcode Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        Stage stage = (Stage) btnUploadQR.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            uploadedQRFile = file;
            Image preview = new Image(file.toURI().toString());
            qrPreviewImage.setImage(preview);
            qrPreviewImage.setStyle("-fx-opacity: 1.0;");
            if (qrPlaceholderLabel != null) {
                qrPlaceholderLabel.setText(file.getName());
            }
            btnScan.setDisable(false);
            statusCircle.setFill(Color.web("#F39C12"));
            statusLabel.setText("Barcode image loaded — click Decode");
        }
    }

    // ===========================
    // DECODE QR CODE
    // ===========================
    @FXML
    private void scanBarcode() {
        if (uploadedQRFile == null) {
            showAlert("No Image", "Please upload a barcode image first.");
            return;
        }

        statusLabel.setText("Decoding barcode...");

        new Thread(() -> {
            try {
                BufferedImage buffered = ImageIO.read(uploadedQRFile);
                LuminanceSource source = new BufferedImageLuminanceSource(buffered);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(bitmap);
                String barcode = result.getText();

                Platform.runLater(() -> {
                    lastScannedBarcode = barcode;
                    statusCircle.setFill(Color.web("#2ECC71"));
                    statusLabel.setText("Decoded: " + barcode);
                    EthicalPointsManager.addPoints("Product scanned", 5);
                    analyzeProduct(barcode);
                });

            } catch (NotFoundException e) {
                Platform.runLater(() -> {
                    statusCircle.setFill(Color.web("#E74C3C"));
                    statusLabel.setText("No barcode found in image.");
                    showAlert("Not Found", "Could not decode a barcode from this image. Try a clearer image.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusCircle.setFill(Color.web("#E74C3C"));
                    statusLabel.setText("Error: " + e.getMessage());
                });
            }
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

                String name           = foodFactsService.getProductName(product);
                String brand          = foodFactsService.getBrand(product);
                String eco            = foodFactsService.getEcoScore(product);
                String imgUrl         = foodFactsService.getImageUrl(product);
                List<String> addCodes = foodFactsService.getAdditives(product);

                currentEcoScore = eco;

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
            try { productImage.setImage(new Image(imgUrl, true)); }
            catch (Exception e) { System.err.println("Could not load image: " + e.getMessage()); }
        }

        productInfoPane.setVisible(true);
        productInfoPane.setManaged(true);
    }

    // ===========================
    // AFFICHAGE BOYCOTT
    // ===========================
    private void displayBoycottStatus(BoycottBrand boycott) {
        if (boycott != null) {
            boycottBadge.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 10; -fx-padding: 15;");
            boycottStatusLabel.setText("🔴 BOYCOTTED");
            boycottReason.setText(boycott.getReason());
            if (boycott.getAlternatives() != null) {
                alternativesLabel.setText(boycott.getAlternatives());
                alternativesBox.setVisible(true);
                alternativesBox.setManaged(true);
            }
        } else {
            boycottBadge.setStyle("-fx-background-color: #2ECC71; -fx-background-radius: 10; -fx-padding: 15;");
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
            case "A": ecoA.setStyle("-fx-background-color: #1A9850; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🌱 Excellent environmental impact."; break;
            case "B": ecoB.setStyle("-fx-background-color: #66BD63; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🌿 Good environmental impact."; break;
            case "C": ecoC.setStyle("-fx-background-color: #FEE08B; -fx-background-radius: 10; -fx-padding: 8;");
                description = "⚠️ Moderate environmental impact."; break;
            case "D": ecoD.setStyle("-fx-background-color: #F46D43; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🏭 High environmental impact."; break;
            case "E": ecoE.setStyle("-fx-background-color: #D73027; -fx-background-radius: 10; -fx-padding: 8;");
                description = "🔴 Very high environmental impact."; break;
            default:  description = "No eco-score data available.";
        }
        ecoScoreDesc.setText(description);
        ecoScorePane.setVisible(true);
        ecoScorePane.setManaged(true);
    }

    private void resetEcoScoreColors() {
        String s = "-fx-background-color: #ECF0F1; -fx-background-radius: 10; -fx-padding: 8;";
        if (ecoA != null) ecoA.setStyle(s);
        if (ecoB != null) ecoB.setStyle(s);
        if (ecoC != null) ecoC.setStyle(s);
        if (ecoD != null) ecoD.setStyle(s);
        if (ecoE != null) ecoE.setStyle(s);
    }

    // ===========================
    // AFFICHAGE INCI
    // ===========================
    private void displayInciAnalysis(List<Additive> additives, int score) {
        additivesList.getChildren().clear();
        inciScoreLabel.setText(score + "/100");
        inciProgressBar.setProgress(score / 100.0);

        String barColor = score >= 70 ? "#2ECC71" : score >= 40 ? "#F39C12" : "#E74C3C";
        inciProgressBar.setStyle("-fx-accent: " + barColor + ";");
        inciScoreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + barColor + ";");

        int dangerCount = 0;
        for (Additive a : additives) {
            HBox row = new HBox(10);
            row.setStyle("-fx-background-color: " + getAdditiveRowColor(a.getDangerLevel()) + "; -fx-background-radius: 8; -fx-padding: 8;");
            Label codeLabel = new Label(a.getCode());
            codeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + a.getDangerColor() + "; -fx-min-width: 50;");
            Label nameLabel = new Label(a.getName());
            nameLabel.setStyle("-fx-text-fill: #2C3E50;");
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            Label levelLabel = new Label(getDangerEmoji(a.getDangerLevel()) + " " + a.getDangerLabel());
            levelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + a.getDangerColor() + ";");
            row.getChildren().addAll(codeLabel, nameLabel, levelLabel);
            additivesList.getChildren().add(row);
            if (a.getDangerLevel() >= 7) dangerCount++;
        }

        if (dangerCount > 0) {
            inciWarning.setText("⚠️ " + dangerCount + " dangerous additive(s) detected!");
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
        if (btnInciDetails != null) { btnInciDetails.setVisible(true); btnInciDetails.setManaged(true); }
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
        EthicalPointsManager.incrementScanCount();
        EthicalPointsManager.addPoints("Product added to stock", 10);

        if (!isBoycotted && currentEcoScore.equalsIgnoreCase("A")) {
            EthicalPointsManager.addPoints("Eco-Score A product added", 15);
        }

        showAlert("Added to Stock", "Product added successfully.");
        resetView();
    }

    @FXML
    private void rejectProduct() {
        EthicalPointsManager.incrementScanCount();

        if (isBoycotted) {
            EthicalPointsManager.incrementBoycottRejectCount();
            EthicalPointsManager.addPoints("Boycotted product rejected", 15);
            showAlert("Product Rejected", "Product rejected. You earned 15 ethical points!");
        } else {
            showAlert("Product Rejected", "Product rejected.");
        }
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

        uploadedQRFile = null;
        if (qrPreviewImage != null) { qrPreviewImage.setImage(null); qrPreviewImage.setStyle("-fx-opacity: 0.6;"); }
        if (qrPlaceholderLabel != null) qrPlaceholderLabel.setText("📂  Click to upload a barcode image");
        btnScan.setDisable(true);
        statusCircle.setFill(Color.web("#E74C3C"));
        statusLabel.setText("No barcode image loaded");
        if (btnInciDetails != null) { btnInciDetails.setVisible(false); btnInciDetails.setManaged(false); }
        lastAdditives = new ArrayList<>();
        lastInciScore = 100;
        lastProductName = "";
        lastScannedBarcode = "";
        isBoycotted = false;
        currentEcoScore = "N/A";
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

    public void cleanup() {}

    @FXML
    private void openInciDetail() {
        Stage detailStage = new Stage();
        detailStage.setTitle("🔬 INCI Detailed Analysis");

        VBox root = new VBox(16);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 24;");

        Label title = new Label("🔬 INCI Detailed Analysis");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label product = new Label("Product: " + lastProductName);
        product.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");
        Separator sep1 = new Separator();

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
        String verdict = lastInciScore >= 70 ? "🟢 GOOD - Safe for consumption"
                : lastInciScore >= 40 ? "🟡 MODERATE - Precautions needed"
                : "🔴 BAD - Consider avoiding this product";
        Label verdictLabel = new Label(verdict);
        verdictLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor + ";");
        scoreBox.getChildren().addAll(scoreTitle, scoreValue, scoreBar, verdictLabel);

        Separator sep2 = new Separator();
        Label tableTitle = new Label("INGREDIENT DETAILS");
        tableTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        VBox tableBox = new VBox(0);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12;");
        HBox headerRow = new HBox();
        headerRow.setStyle("-fx-background-color: #1F4D3A; -fx-padding: 12; -fx-background-radius: 12 12 0 0;");
        Label h1 = new Label("Code"); h1.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 80;");
        Label h2 = new Label("Name"); h2.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 180;");
        Label h3 = new Label("Score"); h3.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 80;");
        Label h4 = new Label("Risk"); h4.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px;");
        HBox.setHgrow(h4, Priority.ALWAYS);
        headerRow.getChildren().addAll(h1, h2, h3, h4);
        tableBox.getChildren().add(headerRow);

        for (int i = 0; i < lastAdditives.size(); i++) {
            Additive a = lastAdditives.get(i);
            HBox row = new HBox();
            row.setStyle("-fx-background-color: " + (i % 2 == 0 ? "#FFFFFF" : "#F8FAFC") + "; -fx-padding: 10 12;");
            Label c1 = new Label(a.getCode()); c1.setStyle("-fx-font-weight: bold; -fx-text-fill: " + a.getDangerColor() + "; -fx-min-width: 80;");
            Label c2 = new Label(a.getName()); c2.setStyle("-fx-text-fill: #1E293B; -fx-min-width: 180;");
            Label c3 = new Label(getDangerEmoji(a.getDangerLevel()) + " " + a.getDangerLevel() + "/10"); c3.setStyle("-fx-text-fill: " + a.getDangerColor() + "; -fx-min-width: 80;");
            Label c4 = new Label(a.getHealthEffects() != null ? a.getHealthEffects() : a.getDangerLabel()); c4.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;"); c4.setWrapText(true);
            HBox.setHgrow(c4, Priority.ALWAYS);
            row.getChildren().addAll(c1, c2, c3, c4);
            tableBox.getChildren().add(row);
        }

        if (lastAdditives.isEmpty()) {
            Label noAdd = new Label("✅ No additives detected");
            noAdd.setStyle("-fx-font-size: 13px; -fx-text-fill: #2ECC71; -fx-font-weight: bold; -fx-padding: 20;");
            tableBox.getChildren().add(noAdd);
        }

        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;");
        btnClose.setOnAction(e -> detailStage.close());
        HBox btnBox = new HBox(btnClose);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

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
