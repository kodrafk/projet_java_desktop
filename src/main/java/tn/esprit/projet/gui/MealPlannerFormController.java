package tn.esprit.projet.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.MealPlan;
import tn.esprit.projet.services.MealPlannerService;
import tn.esprit.projet.utils.MyBDConnexion;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Map;
import java.util.ResourceBundle;

public class MealPlannerFormController implements Initializable {

    @FXML private Label labelNom;
    @FXML private Label labelPoids;
    @FXML private Label labelTaille;
    @FXML private Label labelObjectif;

    @FXML private Label labelUrgent;
    @FXML private Label labelBientot;
    @FXML private Label labelOk;

    @FXML private RadioButton radioVegetarien;
    @FXML private RadioButton radioVegan;
    @FXML private RadioButton radioHalal;
    @FXML private ToggleGroup toggleRegime;

    @FXML private CheckBox checkLactose;
    @FXML private CheckBox checkGluten;
    @FXML private CheckBox checkNoix;
    @FXML private CheckBox checkOeufs;

    @FXML private ChoiceBox<String> choiceObjectif;

    @FXML private VBox        vboxLoading;
    @FXML private ProgressBar progressBar;
    @FXML private Label       labelProgress;
    @FXML private Label       labelStep;
    @FXML private VBox        vboxForm;

    private final MealPlannerService plannerService = new MealPlannerService();
    private int currentUserId = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupObjectifChoiceBox();
        chargerProfilUser();
        chargerStatsStock();
    }

    private void setupObjectifChoiceBox() {
        choiceObjectif.getItems().addAll(
                "Weight Loss",
                "Maintain",
                "Mass Gain"
        );
        choiceObjectif.setValue("Weight Loss");
    }

    private void chargerProfilUser() {
        String sql = "SELECT nom, prenom, poids, taille FROM user WHERE id = ?";
        try (PreparedStatement ps = MyBDConnexion.getInstance()
                .getCnx().prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                labelNom   .setText(rs.getString("prenom") + " " + rs.getString("nom"));
                labelPoids .setText(rs.getDouble("poids")  + " kg");
                labelTaille.setText(rs.getDouble("taille") + " cm");
            }
        } catch (SQLException e) {
            System.err.println("❌ chargerProfilUser : " + e.getMessage());
        }
    }

    private void chargerStatsStock() {
        Map<String, Integer> stats = plannerService.getStatsStock();
        labelUrgent .setText("● " + stats.get("urgent")  + " urgent ingredients (≤2 days)");
        labelBientot.setText("● " + stats.get("bientot") + " ingredients to use soon");
        labelOk     .setText("● " + stats.get("ok")      + " ingredients available");
    }

    @FXML
    private void onGenererPlan() {
        vboxForm.setVisible(false);
        vboxForm.setManaged(false);
        vboxLoading.setVisible(true);
        vboxLoading.setManaged(true);
        lancerAnimationLoading();
    }

    private void lancerAnimationLoading() {
        String[] steps = {
                "Analyzing your stock...",
                "Detecting urgent ingredients",
                "Computing anti-waste score",
                "Filtering diet & allergies",
                "Optimizing weekly plan...",
                "Generating personalized plan..."
        };

        Timeline timeline = new Timeline();
        for (int i = 0; i < steps.length; i++) {
            final int    idx      = i;
            final double progress = (double)(i + 1) / steps.length;

            KeyFrame kf = new KeyFrame(
                    Duration.seconds(0.5 * (i + 1)),
                    e -> {
                        labelStep    .setText(steps[idx]);
                        progressBar  .setProgress(progress);
                        labelProgress.setText((int)(progress * 100) + "%");
                    }
            );
            timeline.getKeyFrames().add(kf);
        }

        timeline.setOnFinished(e -> genererEtOuvrir());
        timeline.play();
    }

    private void genererEtOuvrir() {
        MealPlan.Regime   regime   = getRegimeChoisi();
        MealPlan.Objectif objectif = getObjectifChoisi();
        boolean lactose = checkLactose.isSelected();
        boolean gluten  = checkGluten .isSelected();
        boolean noix    = checkNoix   .isSelected();
        boolean oeufs   = checkOeufs  .isSelected();

        new Thread(() -> {
            MealPlan plan = plannerService.genererPlan(
                    currentUserId, regime, objectif,
                    lactose, gluten, noix, oeufs);

            Platform.runLater(() -> ouvrirResultat(plan));
        }).start();
    }

    private MealPlan.Regime getRegimeChoisi() {
        RadioButton selected = (RadioButton) toggleRegime.getSelectedToggle();
        if (selected == null) return MealPlan.Regime.STANDARD;

        String text = selected.getText();
        if ("Végétarien".equals(text)) return MealPlan.Regime.VEGETARIEN;
        if ("Végan".equals(text))      return MealPlan.Regime.VEGAN;
        if ("Halal".equals(text))      return MealPlan.Regime.HALAL;
        return MealPlan.Regime.STANDARD;
    }

    private MealPlan.Objectif getObjectifChoisi() {
        String val = choiceObjectif.getValue();
        if (val == null) return MealPlan.Objectif.MAINTIEN;
        if (val.contains("Loss"))  return MealPlan.Objectif.PERTE_POIDS;
        if (val.contains("Gain"))  return MealPlan.Objectif.PRISE_MASSE;
        return MealPlan.Objectif.MAINTIEN;
    }

    private void ouvrirResultat(MealPlan plan) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/meal_plan_result.fxml"));
            Parent root = loader.load();

            tn.esprit.projet.controllers.MealPlanResultController ctrl = loader.getController();
            ctrl.setPlan(plan);

            Stage stage = (Stage) vboxForm.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Optimized Meal Plan");
        } catch (IOException e) {
            System.err.println("❌ ouvrirResultat : " + e.getMessage());
            e.printStackTrace();
        }
    }
}