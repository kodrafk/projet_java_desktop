package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.models.ComplaintResponse;
import tn.esprit.projet.services.ComplaintService;
import tn.esprit.projet.services.ComplaintResponseService;
import tn.esprit.projet.utils.Toast;
import org.controlsfx.control.Rating;

import java.time.LocalDateTime;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;

public class ComplaintsAdminController {

    @FXML private javafx.scene.layout.FlowPane cardsContainer;
    
    @FXML private TextField fldSearch;
    @FXML private ComboBox<String> cmbSort;

    // Detail Panel
    @FXML private Label lblTitle;
    @FXML private Label lblPhone;
    @FXML private Label lblRate;
    @FXML private TextArea lblDescription;
    // Edit Panel
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextArea fldResponse;

    private ComplaintService service;
    private ComplaintResponseService responseService;
    private ObservableList<Complaint> masterData;
    private Complaint selectedComplaint;

    @FXML
    public void initialize() {
        service = new ComplaintService();
        responseService = new ComplaintResponseService();

        cmbStatus.setItems(FXCollections.observableArrayList("PENDING", "RESOLVED", "REJECTED"));

        cmbSort.setItems(FXCollections.observableArrayList("Recent First", "Oldest First", "Highest Rating", "Lowest Rating"));
        cmbSort.setValue("Recent First");

        // Listeners
        fldSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));
        cmbSort.valueProperty().addListener((obs, oldVal, newVal) -> filterData(fldSearch.getText()));

        refreshData();
    }

    private javafx.scene.layout.VBox createComplaintCard(Complaint c) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
        card.setPrefWidth(280);

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
        cardRating.setTranslateX(-25);
        
        javafx.scene.layout.HBox ratingBox = new javafx.scene.layout.HBox(cardRating);
        ratingBox.setPrefHeight(20);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label userLbl = new Label("User: " + (c.getUserName() != null ? c.getUserName() : "Unknown"));
        userLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        card.getChildren().addAll(dateLbl, header, ratingBox, userLbl);
        
        java.util.List<String> imagePaths = c.getImagePathsList();
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
                    imgContainer.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 5; -fx-background-radius: 6;");
                    
                    imagesBox.getChildren().add(imgContainer);
                }
            }
            if (!imagesBox.getChildren().isEmpty()) {
                card.getChildren().add(imagesBox);
            }
        }
        
        card.setOnMouseClicked(e -> showComplaintDetails(c));
        
        return card;
    }

    private void showComplaintDetails(Complaint c) {
        selectedComplaint = c;
        lblTitle.setText(c.getTitle());
        lblPhone.setText(c.getPhoneNumber());
        lblRate.setText(String.valueOf(c.getRate()));
        lblDescription.setText(c.getDescription());

        cmbStatus.setValue(c.getStatus());
        fldResponse.setText(c.getAdminResponse() != null ? c.getAdminResponse() : "");
    }

    @FXML
    private void handleSave() {
        if (selectedComplaint == null) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Please select a complaint first.", Toast.Type.ERROR);
            return;
        }

        String newStatus = cmbStatus.getValue();
        String newResponse = fldResponse.getText().trim();

        if (newStatus == null || newResponse.isEmpty()) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Please provide a status and response.", Toast.Type.ERROR);
            return;
        }

        selectedComplaint.setStatus(newStatus);
        // Update the status on the complaint table
        service.modifier(selectedComplaint);

        // Save or update the response in complaint_response table
        ComplaintResponse existing = selectedComplaint.getResponseObj();
        if (existing != null && existing.getId() > 0) {
            // Update existing response
            existing.setResponseContent(newResponse);
            responseService.modifier(existing);
        } else {
            // Insert a new response
            ComplaintResponse cr = new ComplaintResponse(selectedComplaint.getId(), newResponse);
            responseService.ajouter(cr);
            selectedComplaint.setResponseObj(cr);
        }

        // Fire email asynchronously if user has an email
        String email = selectedComplaint.getUserEmail();
        System.out.println("DEBUG: Target email found for this complaint: " + email);
        if (email != null && !email.isEmpty() && email.contains("@")) {
            String subject = "Complaint Update: " + selectedComplaint.getTitle();
            String message = "<h2>Your Complaint was Reviewed!</h2>"
                           + "<p><strong>Status:</strong> " + newStatus + "</p>"
                           + "<p><strong>Admin Response:</strong> " + newResponse + "</p>"
                           + "<br/><p>Thank you for using NutriLife.</p>";
            tn.esprit.projet.utils.EmailService.sendEmailAsync(email, subject, message);
        }

        Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Response saved & notification triggered!", Toast.Type.SUCCESS);
        refreshData();
    }

    @FXML
    private void handleDeleteResponse() {
        if (selectedComplaint == null) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Please select a complaint first.", Toast.Type.ERROR);
            return;
        }
        // Reset status on the complaint
        selectedComplaint.setStatus("PENDING");
        service.modifier(selectedComplaint);

        // Delete from complaint_response table
        responseService.supprimerByComplaintId(selectedComplaint.getId());
        selectedComplaint.setResponseObj(null);

        showComplaintDetails(selectedComplaint);
        Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Response deleted.", Toast.Type.SUCCESS);
        refreshData();
    }

    @FXML
    private void handleSuggestResponseAI() {
        if (selectedComplaint == null) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Please select a complaint first.", Toast.Type.ERROR);
            return;
        }

        fldResponse.setPromptText("AI is writing a professional response...");
        fldResponse.setDisable(true);
        
        java.util.concurrent.CompletableFuture.supplyAsync(() -> 
            tn.esprit.projet.utils.GeminiService.suggestDetailedResponse(
                selectedComplaint.getTitle(),
                selectedComplaint.getDescription(),
                selectedComplaint.getRate()
            )
        ).thenAccept(suggestion -> {
            javafx.application.Platform.runLater(() -> {
                fldResponse.setDisable(false);
                if (suggestion != null && !suggestion.isEmpty()) {
                    fldResponse.setText(suggestion);
                    Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "AI response generated successfully!", Toast.Type.SUCCESS);
                } else {
                    Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), 
                        "AI quota limit reached for today (20 requests/day free tier). Please try again tomorrow or upgrade your API plan.", 
                        Toast.Type.ERROR);
                }
            });
        }).exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> {
                fldResponse.setDisable(false);
                Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Error: " + ex.getMessage(), Toast.Type.ERROR);
            });
            return null;
        });
    }

    @FXML
    private void handleExportExcel() {
        if (masterData == null || masterData.isEmpty()) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "No data to export.", Toast.Type.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Complaints_Export.xlsx");
        File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Complaints");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "User Name", "Title", "Date", "Status", "Rate", "Phone", "Description", "Admin Response"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                int rowNum = 1;
                for (Complaint c : masterData) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(c.getId());
                    row.createCell(1).setCellValue(c.getUserName() != null ? c.getUserName() : "");
                    row.createCell(2).setCellValue(c.getTitle() != null ? c.getTitle() : "");
                    row.createCell(3).setCellValue(c.getDateOfComplaint() != null ? c.getDateOfComplaint().toString() : "");
                    row.createCell(4).setCellValue(c.getStatus() != null ? c.getStatus() : "");
                    row.createCell(5).setCellValue(c.getRate());
                    row.createCell(6).setCellValue(c.getPhoneNumber() != null ? c.getPhoneNumber() : "");
                    row.createCell(7).setCellValue(c.getDescription() != null ? c.getDescription() : "");
                    row.createCell(8).setCellValue(c.getAdminResponse() != null ? c.getAdminResponse() : "");
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Export successful!", Toast.Type.SUCCESS);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "Export failed: " + ex.getMessage(), Toast.Type.ERROR);
            }
        }
    }

    @FXML
    private void handleExportPDF() {
        if (masterData == null || masterData.isEmpty()) {
            Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "No data to export.", Toast.Type.ERROR);
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("Complaints_Report.pdf");
        File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());

        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph("Complaints Management Report")
                        .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.DARK_GRAY));
                document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now().toString())
                        .setItalic().setFontSize(10).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));

                Table table = new Table(UnitValue.createPointArray(new float[]{20, 80, 60, 60, 40, 60}));
                table.setWidth(UnitValue.createPercentValue(100));

                String[] headers = {"ID", "User", "Title", "Date", "Rate", "Status"};
                for (String h : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
                }

                for (Complaint c : masterData) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(c.getId()))));
                    table.addCell(new Cell().add(new Paragraph(c.getUserName() != null ? c.getUserName() : "")));
                    table.addCell(new Cell().add(new Paragraph(c.getTitle() != null ? c.getTitle() : "")));
                    table.addCell(new Cell().add(new Paragraph(c.getDateOfComplaint().toLocalDate().toString())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(c.getRate()))));
                    
                    Cell statusCell = new Cell().add(new Paragraph(c.getStatus()));
                    if (c.getStatus().equalsIgnoreCase("RESOLVED")) statusCell.setFontColor(ColorConstants.GREEN);
                    else if (c.getStatus().equalsIgnoreCase("REJECTED")) statusCell.setFontColor(ColorConstants.RED);
                    table.addCell(statusCell);
                }

                document.add(table);
                document.close();
                
                Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "PDF Export successful!", Toast.Type.SUCCESS);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.show((javafx.stage.Stage)cardsContainer.getScene().getWindow(), "PDF Export failed: " + ex.getMessage(), Toast.Type.ERROR);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        clearSelection();
    }

    private void refreshData() {
        masterData = FXCollections.observableArrayList(service.getAll());
        filterData(fldSearch.getText());
    }

    private void filterData(String keyword) {
        if (masterData == null) return;
        
        String sort = cmbSort.getValue();
        java.util.stream.Stream<Complaint> stream = masterData.stream();

        // 1. Filter
        if (keyword != null && !keyword.isEmpty()) {
            String lowerCaseFilter = keyword.toLowerCase();
            stream = stream.filter(c -> c.getTitle().toLowerCase().contains(lowerCaseFilter)
                                     || c.getStatus().toLowerCase().contains(lowerCaseFilter));
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

        cardsContainer.getChildren().clear();
        stream.forEach(c -> cardsContainer.getChildren().add(createComplaintCard(c)));
    }
    
    private void clearSelection() {
        selectedComplaint = null;
        lblTitle.setText("-");
        lblPhone.setText("-");
        lblRate.setText("-");
        lblDescription.clear();
        cmbStatus.getSelectionModel().clearSelection();
        fldResponse.clear();
    }

}
