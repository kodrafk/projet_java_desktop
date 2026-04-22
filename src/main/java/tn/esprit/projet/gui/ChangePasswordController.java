package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;
import tn.esprit.projet.utils.Validator;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Button        changeButton;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleChange() {
        errorLabel.setVisible(false);
        User u = Session.getCurrentUser();
        if (u == null) return;

        String current = currentPasswordField.getText();
        if (current.isEmpty()) { show("Current password is required."); return; }

        // Verify current password
        User fresh = repo.findById(u.getId());
        if (fresh == null || !BCrypt.checkpw(current, fresh.getPassword())) {
            show("Current password is incorrect.");
            return;
        }

        String newPwd = newPasswordField.getText();
        String pwdErr = Validator.password(newPwd);
        if (pwdErr != null) { show(pwdErr); return; }

        String confirmErr = Validator.confirmPassword(newPwd, confirmNewPasswordField.getText());
        if (confirmErr != null) { show(confirmErr); return; }

        String hashed = BCrypt.hashpw(newPwd, BCrypt.gensalt(10));
        repo.updatePassword(u.getId(), hashed);
        u.setPassword(hashed);
        Session.login(u);

        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        Toasts.show(stage, "Your password has been changed successfully!", Toasts.Type.SUCCESS);
        stage.close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) currentPasswordField.getScene().getWindow()).close();
    }

    private void show(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
