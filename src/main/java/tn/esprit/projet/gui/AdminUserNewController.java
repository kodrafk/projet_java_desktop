package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Toasts;
import tn.esprit.projet.utils.Validator;

public class AdminUserNewController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     firstNameField;
    @FXML private TextField     lastNameField;
    @FXML private DatePicker    birthdayPicker;
    @FXML private TextField     weightField;
    @FXML private TextField     heightField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label         errEmail;
    @FXML private Label         errPassword;
    @FXML private Label         errFirstName;
    @FXML private Label         errLastName;
    @FXML private Label         errBirthday;
    @FXML private Label         errWeight;
    @FXML private Label         errHeight;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        if (roleCombo != null)
            roleCombo.setItems(FXCollections.observableArrayList("ROLE_USER", "ROLE_ADMIN"));
        if (roleCombo != null) roleCombo.setValue("ROLE_USER");
    }

    @FXML
    private void handleSave() {
        boolean ok = true;

        String emailErr = Validator.email(emailField.getText());
        if (emailErr == null && repo.emailExistsExcluding(emailField.getText().trim(), 0))
            emailErr = "This email is already registered.";
        ok &= Validator.apply(emailField, errEmail, emailErr);

        ok &= Validator.apply(passwordField, errPassword, Validator.password(passwordField.getText()));
        ok &= Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        ok &= Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));

        String bdErr = Validator.birthday(birthdayPicker.getValue());
        if (errBirthday != null) errBirthday.setText(bdErr != null ? bdErr : "");
        if (bdErr != null) ok = false;

        ok &= Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        ok &= Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));
        if (!ok) return;

        User u = new User();
        u.setEmail(emailField.getText().trim());
        u.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(10)));
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setBirthday(birthdayPicker.getValue());
        u.setWeight(Double.parseDouble(weightField.getText().trim()));
        u.setHeight(Double.parseDouble(heightField.getText().trim()));
        u.setRole(roleCombo != null ? roleCombo.getValue() : "ROLE_USER");
        u.setActive(true);

        repo.save(u);
        Stage stage = (Stage) emailField.getScene().getWindow();
        Toasts.show(stage, "User created successfully!", Toasts.Type.SUCCESS);
        stage.close();
    }

    @FXML private void handleCancel() {
        ((Stage) emailField.getScene().getWindow()).close();
    }
}
