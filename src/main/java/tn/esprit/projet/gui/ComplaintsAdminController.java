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
import javafx.stage.FileChooser;

public class ComplaintsAdminController {

    @FXML private TableView<Complaint> tvComplaints;
    @FXML private TableColumn<Complaint, Integer> colId;
    @FXML private TableColumn<Complaint, String> colUserName;
    @FXML private TableColumn<Complaint, LocalDateTime> colDate;
    @FXML private TableColumn<Complaint, String> colTitle;
    @FXML private TableColumn<Complaint, String> colStatus;
    
    @FXML private TextField fldSearch;

    // Detail Panel
    @FXML private Label lblTitle;
    @FXML private Label lblPhone;
    @FXML private Label lblRate;
    @FXML private TextArea lblDescription;
    @FXML private Button btnViewImage;
    
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

        tvComplaints.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showComplaintDetails(newSel);
            }
        });

        fldSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));

        refreshData();
    }

    private void showComplaintDetails(Complaint c) {
        selectedComplaint = c;
        lblTitle.setText(c.getTitle());
        lblPhone.setText(c.getPhoneNumber());
        lblRate.setText(String.valueOf(c.getRate()));
        lblDescription.setText(c.getDescription());

        cmbStatus.setValue(c.getStatus());
        fldResponse.setText(c.getAdminResponse() != null ? c.getAdminResponse() : "");

        if (c.getImagePath() != null && !c.getImagePath().trim().isEmpty()) {
            btnViewImage.setVisible(true);
            btnViewImage.setManaged(true);
        } else {
            btnViewImage.setVisible(false);
            btnViewImage.setManaged(false);
        }
    }

    @FXML
    private void handleSave() {
        if (selectedComplaint == null) {
            Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Please select a complaint first.", Toast.Type.ERROR);
            return;
        }

        String newStatus = cmbStatus.getValue();
        String newResponse = fldResponse.getText().trim();

        if (newStatus == null || newResponse.isEmpty()) {
            Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Please provide a status and response.", Toast.Type.ERROR);
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

        Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Response saved successfully!", Toast.Type.SUCCESS);
        refreshData();
    }

    @FXML
    private void handleDeleteResponse() {
        if (selectedComplaint == null) {
            Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Please select a complaint first.", Toast.Type.ERROR);
            return;
        }
        // Reset status on the complaint
        selectedComplaint.setStatus("PENDING");
        service.modifier(selectedComplaint);

        // Delete from complaint_response table
        responseService.supprimerByComplaintId(selectedComplaint.getId());
        selectedComplaint.setResponseObj(null);

        showComplaintDetails(selectedComplaint);
        Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Response deleted.", Toast.Type.SUCCESS);
        refreshData();
    }

    @FXML
    private void handleExportExcel() {
        if (masterData == null || masterData.isEmpty()) {
            Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "No data to export.", Toast.Type.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Complaints_Export.xlsx");
        File file = fileChooser.showSaveDialog(tvComplaints.getScene().getWindow());

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
                Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Export successful!", Toast.Type.SUCCESS);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Export failed: " + ex.getMessage(), Toast.Type.ERROR);
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
        
        FilteredList<Complaint> filteredData = new FilteredList<>(masterData, c -> {
            if (keyword == null || keyword.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = keyword.toLowerCase();
            return c.getTitle().toLowerCase().contains(lowerCaseFilter)
                    || c.getStatus().toLowerCase().contains(lowerCaseFilter);
        });
        tvComplaints.setItems(filteredData);
    }
    
    private void clearSelection() {
        selectedComplaint = null;
        lblTitle.setText("-");
        lblPhone.setText("-");
        lblRate.setText("-");
        lblDescription.clear();
        cmbStatus.getSelectionModel().clearSelection();
        fldResponse.clear();
        if (btnViewImage != null) {
            btnViewImage.setVisible(false);
            btnViewImage.setManaged(false);
        }
    }

    @FXML
    private void handleViewImage() {
        if (selectedComplaint != null && selectedComplaint.getImagePath() != null) {
            File file = new File(selectedComplaint.getImagePath());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);
                imageView.setFitHeight(400);
                
                StackPane root = new StackPane();
                root.getChildren().add(imageView);
                
                Stage stage = new Stage();
                stage.setTitle("Attached Image - " + selectedComplaint.getTitle());
                stage.setScene(new Scene(root, 650, 450));
                stage.show();
            } else {
                Toast.show((javafx.stage.Stage)tvComplaints.getScene().getWindow(), "Image file not found on disk.", Toast.Type.ERROR);
            }
        }
    }
}
