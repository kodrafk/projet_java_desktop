package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class SharedMealController {

    @FXML private Region accentBar;
    @FXML private Label lblMealTitle, lblMealDate, lblCalories, lblProtein, lblCarbs, lblFats;
    @FXML private FlowPane foodTagsContainer;
    @FXML private VBox foodsSection;

    @FXML private void handleBack() {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/objectives.fxml"));
            StackPane contentArea = (StackPane) lblMealTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
