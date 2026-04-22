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
    @FXML private Button btnSubmit;

    private String selectedImagePath;
    private Complaint editTarget = null;

    @FXML private TableView<Complaint> tvComplaints;
    @FXML private TableColumn<Complaint, LocalDateTime> colDate;
    @FXML private TableColumn<Complaint, String> colTitle;
    @FXML private TableColumn<Complaint, String> colStatus;
    @FXML private TableColumn<Complaint, String> colResponse;
    @FXML private TableColumn<Complaint, Void> colActions;

    private ComplaintService service;
    private User currentUser;

    @FXML
    public void initialize() {
        service = new ComplaintService();
        currentUser = SessionManager.getCurrentUser();

        ratingControl.setRating(1);
        
        // Default date to today
        dpDate.setValue(LocalDate.now());

        setupActionsColumn();

        refreshTable();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");
                btnDelete.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");

                btnEdit.setOnAction(e -> {
                    Complaint c = getTableView().getItems().get(getIndex());
                    populateEditForm(c);
                });
                btnDelete.setOnAction(e -> {
                    Complaint c = getTableView().getItems().get(getIndex());
                    service.supprimer(c.getId());
                    refreshTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Complaint c = getTableView().getItems().get(getIndex());
                    if (currentUser != null && c.getUserId() == currentUser.getId()) {
                        setGraphic(pane);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void populateEditForm(Complaint c) {
        editTarget = c;
        fldTitle.setText(c.getTitle());
        fldDescription.setText(c.getDescription());
        fldPhone.setText(c.getPhoneNumber());
        ratingControl.setRating(c.getRate());
        dpDate.setValue(c.getDateOfComplaint().toLocalDate());
        btnSubmit.setText("Update Complaint");
        
        if (c.getImagePath() != null && !c.getImagePath().isEmpty()) {
            File f = new File(c.getImagePath());
            lblImageName.setText(f.getName());
            lblImageName.setVisible(true);
            lblImageName.setManaged(true);
        } else {
            lblImageName.setText("");
            lblImageName.setVisible(false);
            lblImageName.setManaged(false);
        }
        selectedImagePath = c.getImagePath();
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(fldTitle.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            lblImageName.setText(selectedFile.getName());
            lblImageName.setVisible(true);
            lblImageName.setManaged(true);
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

        if (editTarget != null) {
            editTarget.setTitle(title);
            editTarget.setDescription(description);
            editTarget.setPhoneNumber(phone);
            editTarget.setRate(rate);
            editTarget.setDateOfComplaint(LocalDateTime.of(date, LocalTime.now()));
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
            complaint.setDateOfComplaint(LocalDateTime.of(date, LocalTime.now()));
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

        refreshTable();
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        if (currentUser != null) {
            List<Complaint> list = service.getAll();
            ObservableList<Complaint> oList = FXCollections.observableArrayList(list);
            tvComplaints.setItems(oList);
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
