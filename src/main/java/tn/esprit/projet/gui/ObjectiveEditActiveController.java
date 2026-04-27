package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import tn.esprit.projet.models.NutritionObjective;

public class ObjectiveEditActiveController {
    @FXML private Label lblHeaderTitle, lblCurrentTitle, lblCurrentProgress;
    @FXML private Label lblCurrentCal, lblCurrentProtein, lblCurrentCarbs, lblCurrentFats;
    @FXML private VBox goalsSection, plansSection;
    @FXML private GridPane goalsGrid;
    @FXML private HBox plansContainer;
    @FXML private Label lblSelectedGoal;

    private NutritionObjective objective;

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        lblHeaderTitle.setText(obj.getTitle());
        lblCurrentTitle.setText(obj.getTitle());
        lblCurrentProgress.setText(obj.getProgressPercentage() + "% complete · " + obj.getDaysRemaining() + " days left");
        lblCurrentCal.setText("🔥 " + obj.getTargetCalories() + " kcal");
        lblCurrentProtein.setText("💪 " + (int)obj.getTargetProtein() + "g");
        lblCurrentCarbs.setText("🌾 " + (int)obj.getTargetCarbs() + "g");
        lblCurrentFats.setText("🥑 " + (int)obj.getTargetFats() + "g");
    }

    @FXML private void handleChangeGoal()    { goalsSection.setVisible(true); goalsSection.setManaged(true); }
    @FXML private void handleChangeSettings(){ navigateTo("/fxml/objective_edit_settings.fxml"); }
    @FXML private void handleCustomPlan()    { navigateTo("/fxml/objective_edit_active_custom.fxml"); }
    @FXML private void handleBack()          { navigateTo("/fxml/objective_show.fxml"); }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) lblHeaderTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
