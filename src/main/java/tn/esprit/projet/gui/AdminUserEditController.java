package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.Toasts;
import tn.esprit.projet.utils.Validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class AdminUserEditController {

    @FXML private TextField     emailField;
    @FXML private TextField     firstNameField;
    @FXML private TextField     lastNameField;
    @FXML private DatePicker    birthdayPicker;
    @FXML private TextField     weightField;
    @FXML private TextField     heightField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox      activeCheckbox;
    @FXML private ImageView     photoView;
    @FXML private Button        enrollFaceButton;
    @FXML private Button        deleteFaceButton;
    @FXML private Button        testFaceButton;
    @FXML private Label         faceStatusLabel;
    @FXML private Label         faceInfoLabel;
    @FXML private Label         errEmail;
    @FXML private Label         errFirstName;
    @FXML private Label         errLastName;
    @FXML private Label         errBirthday;
    @FXML private Label         errWeight;
    @FXML private Label         errHeight;
    @FXML private Label         errPhoto;

    private User user;
    private File newPhotoFile;
    private final UserRepository repo = new UserRepository();
    private final FaceEmbeddingRepository faceRepo = new FaceEmbeddingRepository();

    public void setUser(User u) {
        this.user = u;
        populate();
    }

    @FXML
    public void initialize() {
        if (roleCombo != null)
            roleCombo.setItems(FXCollections.observableArrayList("ROLE_USER", "ROLE_ADMIN"));
    }

    private void populate() {
        if (user == null) return;
        emailField.setText(nvl(user.getEmail()));
        firstNameField.setText(nvl(user.getFirstName()));
        lastNameField.setText(nvl(user.getLastName()));
        birthdayPicker.setValue(user.getBirthday());
        weightField.setText(user.getWeight() > 0 ? String.valueOf(user.getWeight()) : "");
        heightField.setText(user.getHeight() > 0 ? String.valueOf(user.getHeight()) : "");
        if (roleCombo != null) roleCombo.setValue(user.getRole());
        if (activeCheckbox != null) activeCheckbox.setSelected(user.isActive());
        if (photoView != null && user.getPhotoFilename() != null) {
            File f = new File("uploads/profiles/" + user.getPhotoFilename());
            if (f.exists()) photoView.setImage(new Image(f.toURI().toString()));
        }
        
        // Check Face ID enrollment status
        updateFaceIdStatus();
    }
    
    private void updateFaceIdStatus() {
        if (faceStatusLabel == null) return;
        boolean enrolled = faceRepo.findByUserId(user.getId()) != null;
        
        if (enrolled) {
            faceStatusLabel.setText("✅ Face ID enrolled and active");
            faceStatusLabel.setStyle("-fx-text-fill:#16A34A;-fx-font-size:12px;-fx-font-weight:bold;");
            
            if (enrollFaceButton != null) enrollFaceButton.setText("🔄 Re-enroll Face ID");
            if (deleteFaceButton != null) deleteFaceButton.setVisible(true);
            if (testFaceButton != null) testFaceButton.setVisible(true);
            if (faceInfoLabel != null) faceInfoLabel.setText("Face ID is active. You can test, re-enroll, or delete it.");
        } else {
            faceStatusLabel.setText("❌ Face ID not enrolled");
            faceStatusLabel.setStyle("-fx-text-fill:#DC2626;-fx-font-size:12px;-fx-font-weight:bold;");
            
            if (enrollFaceButton != null) enrollFaceButton.setText("📷 Enroll Face ID");
            if (deleteFaceButton != null) deleteFaceButton.setVisible(false);
            if (testFaceButton != null) testFaceButton.setVisible(false);
            if (faceInfoLabel != null) faceInfoLabel.setText("Face ID allows secure biometric authentication. Click 'Enroll Face ID' to set it up.");
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        File file = fc.showOpenDialog(emailField.getScene().getWindow());
        if (file == null) return;
        if (file.length() > 2L * 1024 * 1024) {
            if (errPhoto != null) errPhoto.setText("File size must be less than 2MB."); return;
        }
        if (errPhoto != null) errPhoto.setText("");
        newPhotoFile = file;
        if (photoView != null) photoView.setImage(new Image(file.toURI().toString()));
    }
    
    @FXML
    private void handleEnrollFace() {
        if (user == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_enroll.fxml"));
            Parent root = loader.load();
            FaceIdEnrollController ctrl = loader.getController();
            ctrl.setTargetUser(user);
            ctrl.setOnEnrolled(() -> {
                Stage stage = (Stage) emailField.getScene().getWindow();
                Toasts.show(stage, "Face ID enrolled successfully!", Toasts.Type.SUCCESS);
                updateFaceIdStatus();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Enroll Face ID — " + user.getFirstName() + " " + user.getLastName());
            stage.setScene(new Scene(root, 520, 520));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Toasts.show(stage, "Could not open camera: " + e.getMessage(), Toasts.Type.ERROR);
        }
    }
    
    @FXML
    private void handleDeleteFace() {
        if (user == null) return;
        
        boolean confirmed = AlertUtil.confirm("Delete Face ID",
            "Delete Face ID for " + user.getFirstName() + " " + user.getLastName() + "?\n\n" +
            "This will remove all facial recognition data. The user will need to re-enroll to use Face ID login.");
        if (confirmed) {
            try {
                faceRepo.removeByUserId(user.getId());
                Stage stage = (Stage) emailField.getScene().getWindow();
                Toasts.show(stage, "Face ID deleted successfully!", Toasts.Type.SUCCESS);
                updateFaceIdStatus();
            } catch (Exception e) {
                e.printStackTrace();
                Stage stage = (Stage) emailField.getScene().getWindow();
                Toasts.show(stage, "Failed to delete Face ID: " + e.getMessage(), Toasts.Type.ERROR);
            }
        }
    }
    
    @FXML
    private void handleTestFace() {
        if (user == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_verify.fxml"));
            Parent root = loader.load();
            FaceIdVerifyController ctrl = loader.getController();
            ctrl.setTargetUser(user);
            ctrl.setOnSuccess(() -> {
                Stage stage = (Stage) emailField.getScene().getWindow();
                Toasts.show(stage, "✅ Face ID verification successful!", Toasts.Type.SUCCESS);
            });
            ctrl.setOnFailure(() -> {
                Stage stage = (Stage) emailField.getScene().getWindow();
                Toasts.show(stage, "❌ Face ID verification failed!", Toasts.Type.ERROR);
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Test Face ID — " + user.getFirstName() + " " + user.getLastName());
            stage.setScene(new Scene(root, 520, 520));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Toasts.show(stage, "Could not open camera: " + e.getMessage(), Toasts.Type.ERROR);
        }
    }

    @FXML
    private void handleSave() {
        boolean ok = true;

        // Email uniqueness excluding self
        String emailErr = Validator.email(emailField.getText());
        if (emailErr == null && repo.emailExistsExcluding(emailField.getText().trim(), user.getId()))
            emailErr = "This email is already registered.";
        ok &= Validator.apply(emailField, errEmail, emailErr);

        ok &= Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        ok &= Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));

        String bdErr = Validator.birthday(birthdayPicker.getValue());
        if (errBirthday != null) errBirthday.setText(bdErr != null ? bdErr : "");
        if (bdErr != null) ok = false;

        ok &= Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        ok &= Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));
        if (!ok) return;

        user.setEmail(emailField.getText().trim());
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setBirthday(birthdayPicker.getValue());
        user.setWeight(Double.parseDouble(weightField.getText().trim()));
        user.setHeight(Double.parseDouble(heightField.getText().trim()));
        if (roleCombo != null) user.setRole(roleCombo.getValue());
        if (activeCheckbox != null) user.setActive(activeCheckbox.isSelected());

        if (newPhotoFile != null) {
            String filename = savePhoto(newPhotoFile, user.getPhotoFilename());
            if (filename != null) {
                user.setPhotoFilename(filename);
                repo.updatePhoto(user.getId(), filename);
            }
        }

        repo.update(user);
        Stage stage = (Stage) emailField.getScene().getWindow();
        Toasts.show(stage, "User updated successfully!", Toasts.Type.SUCCESS);
        stage.close();
    }

    @FXML private void handleCancel() {
        ((Stage) emailField.getScene().getWindow()).close();
    }

    private String savePhoto(File src, String oldFilename) {
        try {
            if (oldFilename != null && !oldFilename.isBlank())
                new File("uploads/profiles/" + oldFilename).delete();
            Path dir = Paths.get("uploads/profiles");
            Files.createDirectories(dir);
            String ext = src.getName().substring(src.getName().lastIndexOf('.'));
            String filename = UUID.randomUUID() + ext;
            Files.copy(src.toPath(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
