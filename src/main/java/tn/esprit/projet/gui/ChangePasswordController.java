package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.PasswordUtil;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.UserValidator;

public class ChangePasswordController {

    @FXML private PasswordField fieldCurrent;
    @FXML private PasswordField fieldNew;
    @FXML private PasswordField fieldConfirm;
    @FXML private Label         errCurrent;
    @FXML private Label         errNew;
    @FXML private Label         errConfirm;
    @FXML private Button        btnUpdate;

    private final UserDAO dao = new UserDAO();

    @FXML
    public void initialize() {
        fieldCurrent.focusedProperty().addListener((o, was, now) -> {
            if (!now && !fieldCurrent.getText().isBlank())
                applyField(fieldCurrent, errCurrent, null); // current checked on submit
            updateButton();
        });
        fieldNew.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldNew, errNew,
                    UserValidator.validatePassword(fieldNew.getText()));
            // re-check confirm
            if (!fieldConfirm.getText().isBlank())
                applyField(fieldConfirm, errConfirm,
                        UserValidator.validateConfirmPassword(
                                fieldNew.getText(), fieldConfirm.getText()));
            updateButton();
        });
        fieldConfirm.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldConfirm, errConfirm,
                    UserValidator.validateConfirmPassword(
                            fieldNew.getText(), fieldConfirm.getText()));
            updateButton();
        });

        fieldCurrent.textProperty().addListener((o, a, b) -> updateButton());
        fieldNew.textProperty().addListener((o, a, b) -> updateButton());
        fieldConfirm.textProperty().addListener((o, a, b) -> updateButton());

        updateButton();
    }

    @FXML
    private void handleSave() {
        boolean ok = true;

        User u = SessionManager.getCurrentUser();
        if (u == null) return;
        User fresh = dao.findById(u.getId());

        // Validate current password
        String current = fieldCurrent.getText();
        if (current.isBlank()) {
            applyField(fieldCurrent, errCurrent, "Current password is required.");
            ok = false;
        } else if (fresh == null || !PasswordUtil.checkPassword(current, fresh.getPassword())) {
            applyField(fieldCurrent, errCurrent, "Current password is incorrect.");
            ok = false;
        } else {
            applyField(fieldCurrent, errCurrent, null);
        }

        // Validate new password
        ok &= applyField(fieldNew, errNew,
                UserValidator.validatePassword(fieldNew.getText()));

        // Validate confirm
        ok &= applyField(fieldConfirm, errConfirm,
                UserValidator.validateConfirmPassword(
                        fieldNew.getText(), fieldConfirm.getText()));

        if (!ok) return;

        u.setPassword(PasswordUtil.hashPassword(fieldNew.getText()));
        if (dao.update(u)) {
            Stage owner = (Stage) fieldCurrent.getScene().getWindow();
            owner.close();
            AlertUtil.show(AlertUtil.Type.SUCCESS, "Password Updated", "Your password has been changed successfully.");
        } else {
            applyField(fieldCurrent, errCurrent, "Failed to update password. Please try again.");
        }
    }

    private boolean applyField(Control field, Label errLabel, String error) {
        if (error != null) {
            errLabel.setText(error);
            field.setStyle(UserValidator.getErrorStyle());
            return false;
        } else {
            errLabel.setText("");
            field.setStyle(UserValidator.getOkStyle());
            return true;
        }
    }

    private void updateButton() {
        if (btnUpdate == null) return;
        btnUpdate.setDisable(
                fieldCurrent.getText().isBlank() ||
                fieldNew.getText().isBlank() ||
                fieldConfirm.getText().isBlank());
    }

    @FXML private void handleCancel() {
        ((Stage) fieldCurrent.getScene().getWindow()).close();
    }
}
