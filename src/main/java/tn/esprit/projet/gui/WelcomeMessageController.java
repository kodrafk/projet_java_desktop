package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

public class WelcomeMessageController {

    @FXML private TextArea messageArea;
    @FXML private Label    charCountLabel;
    @FXML private Label    errorLabel;
    @FXML private Button   saveButton;

    private final UserRepository repo = new UserRepository();
    private static final int MAX = 500;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        User u = Session.getCurrentUser();
        if (u != null && u.getWelcomeMessage() != null)
            messageArea.setText(u.getWelcomeMessage());

        updateCounter();
        messageArea.textProperty().addListener((o, a, b) -> updateCounter());
    }

    private void updateCounter() {
        int len = messageArea.getText().length();
        charCountLabel.setText(len + " / " + MAX);
        if (len > MAX) {
            errorLabel.setText("Welcome message is too long. Maximum 500 characters.");
            errorLabel.setVisible(true);
            saveButton.setDisable(true);
        } else {
            errorLabel.setVisible(false);
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void handleSave() {
        String msg = messageArea.getText();
        if (msg.length() > MAX) return;
        User u = Session.getCurrentUser();
        repo.updateWelcomeMessage(u.getId(), msg);
        u.setWelcomeMessage(msg);
        Session.login(u);
        Stage stage = (Stage) messageArea.getScene().getWindow();
        Toasts.show(stage, "Your welcome message has been updated successfully!", Toasts.Type.SUCCESS);
        stage.close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) messageArea.getScene().getWindow()).close();
    }
}
