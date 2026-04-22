package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;
import tn.esprit.projet.utils.Validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class EditProfileController {

    @FXML private TextField  firstNameField;
    @FXML private TextField  lastNameField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextField  weightField;
    @FXML private TextField  heightField;
    @FXML private ImageView  photoView;
    @FXML private Label      errFirstName;
    @FXML private Label      errLastName;
    @FXML private Label      errBirthday;
    @FXML private Label      errWeight;
    @FXML private Label      errHeight;
    @FXML private Label      errPhoto;

    private File newPhotoFile;
    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        User u = Session.getCurrentUser();
        if (u == null) return;
        firstNameField.setText(nvl(u.getFirstName()));
        lastNameField.setText(nvl(u.getLastName()));
        birthdayPicker.setValue(u.getBirthday());
        weightField.setText(u.getWeight() > 0 ? String.valueOf(u.getWeight()) : "");
        heightField.setText(u.getHeight() > 0 ? String.valueOf(u.getHeight()) : "");

        if (photoView != null && u.getPhotoFilename() != null) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) photoView.setImage(new Image(f.toURI().toString()));
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Profile Photo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        File file = fc.showOpenDialog(firstNameField.getScene().getWindow());
        if (file == null) return;
        if (file.length() > 2L * 1024 * 1024) {
            if (errPhoto != null) errPhoto.setText("File size must be less than 2MB.");
            return;
        }
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".png")
                && !name.endsWith(".gif") && !name.endsWith(".webp")) {
            if (errPhoto != null) errPhoto.setText("Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed.");
            return;
        }
        if (errPhoto != null) errPhoto.setText("");
        newPhotoFile = file;
        if (photoView != null) photoView.setImage(new Image(file.toURI().toString()));
    }

    @FXML
    private void handleSave() {
        boolean ok = true;
        ok &= Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        ok &= Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));

        String bdErr = Validator.birthday(birthdayPicker.getValue());
        if (errBirthday != null) errBirthday.setText(bdErr != null ? bdErr : "");
        if (bdErr != null) ok = false;

        ok &= Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        ok &= Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));
        if (!ok) return;

        User u = Session.getCurrentUser();
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setBirthday(birthdayPicker.getValue());
        u.setWeight(Double.parseDouble(weightField.getText().trim()));
        u.setHeight(Double.parseDouble(heightField.getText().trim()));

        if (newPhotoFile != null) {
            String filename = savePhoto(newPhotoFile, u.getPhotoFilename());
            if (filename != null) {
                u.setPhotoFilename(filename);
                repo.updatePhoto(u.getId(), filename);
            }
        }

        repo.update(u);
        Session.login(u);

        Stage stage = (Stage) firstNameField.getScene().getWindow();
        Toasts.show(stage, "Your profile has been updated successfully!", Toasts.Type.SUCCESS);
        stage.close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) firstNameField.getScene().getWindow()).close();
    }

    private String savePhoto(File src, String oldFilename) {
        try {
            // Delete old photo
            if (oldFilename != null && !oldFilename.isBlank()) {
                new File("uploads/profiles/" + oldFilename).delete();
            }
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
