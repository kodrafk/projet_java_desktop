package tn.esprit.projet.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.esprit.projet.models.MealPlanItem;
import tn.esprit.projet.services.MealPlanService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MealPlanTrackingController implements Initializable {

    @FXML private ProgressBar progressGlobal;
    @FXML private Label       labelProgression;
    @FXML private VBox        vboxRepas;
    @FXML private Label       labelRepasEffectues;
    @FXML private Label       labelCaloriesConsommees;
    @FXML private Label       labelIngredientsSauves;
    @FXML private Label       labelImpactEco;
    @FXML private Button      btnRetour;

    private int                planId;
    private List<MealPlanItem> items;
    private final MealPlanService planService = new MealPlanService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setPlanId(int planId) {
        this.planId = planId;
        this.items  = planService.getItemsByPlanId(planId);
        afficherTousLesRepas();
        mettreAJourStats();
    }

    private void afficherTousLesRepas() {
        vboxRepas.getChildren().clear();

        MealPlanItem.JourNom[] jours = MealPlanItem.JourNom.values();

        for (MealPlanItem.JourNom jour : jours) {
            List<MealPlanItem> itemsDuJour = items.stream()
                    .filter(i -> i.getJourNom() == jour)
                    .toList();

            if (itemsDuJour.isEmpty()) continue;

            long nbFaits = itemsDuJour.stream()
                    .filter(MealPlanItem::isEaten).count();

            String statutJour;
            if      (nbFaits == 3) statutJour = "✅";
            else if (nbFaits > 0)  statutJour = "⚠️";
            else                   statutJour = "○";

            // Header jour
            HBox headerJour = new HBox(10);
            headerJour.setAlignment(Pos.CENTER_LEFT);
            headerJour.setPadding(new Insets(10, 16, 6, 16));
            headerJour.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; " +
                    "-fx-border-color: #E2E8F0; -fx-border-radius: 8;");

            Label labelStatut = new Label(statutJour);
            labelStatut.setStyle("-fx-font-size: 16px;");
            Label labelJour = new Label(jour.name());
            labelJour.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E293B;");

            headerJour.getChildren().addAll(labelStatut, labelJour);
            vboxRepas.getChildren().add(headerJour);

            // Repas du jour
            for (MealPlanItem item : itemsDuJour) {
                HBox ligneRepas = creerLigneRepas(item);
                vboxRepas.getChildren().add(ligneRepas);
            }

            Separator sep = new Separator();
            sep.setPadding(new Insets(4, 0, 4, 0));
            vboxRepas.getChildren().add(sep);
        }
    }

    private HBox creerLigneRepas(MealPlanItem item) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 16, 8, 32));

        String borderColor;
        String bgColor;
        switch (item.getUrgenceNiveau()) {
            case URGENT  -> { borderColor = "#FCA5A5"; bgColor = "#FEF2F2"; }
            case BIENTOT -> { borderColor = "#FCD34D"; bgColor = "#FFFBEB"; }
            default      -> { borderColor = "#E2E8F0"; bgColor = "white"; }
        }

        box.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 10; -fx-border-width: 1;");

        // Image
        ImageView imageView = creerImageView(item.getRecetteImage(), 45, 45);
        if (imageView != null) {
            box.getChildren().add(imageView);
        }

        // Urgence
        Label urgence = new Label(item.getUrgenceEmoji());
        urgence.setStyle("-fx-font-size: 14px;");
        urgence.setMinWidth(24);

        // Moment
        Label moment = new Label(item.getMomentLabel());
        moment.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-min-width: 80px;");

        // Nom
        Label nom = new Label(item.getRecetteNom());
        nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        HBox.setHgrow(nom, Priority.ALWAYS);

        // Calories
        Label cal = new Label(item.getRecetteCalories() + " kcal");
        cal.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        cal.setMinWidth(70);

        // CheckBox
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(item.isEaten());
        checkBox.setStyle("-fx-scale-x: 1.2; -fx-scale-y: 1.2;");

        checkBox.setOnAction(e -> {
            boolean checked = checkBox.isSelected();
            item.setEaten(checked);
            planService.updateStatutRepas(item.getId(), checked);
            mettreAJourStats();
            afficherTousLesRepas();
        });

        box.getChildren().addAll(urgence, moment, nom, cal, checkBox);
        return box;
    }

    private ImageView creerImageView(String url, double width, double height) {
        if (url == null || url.isEmpty()) return null;

        try {
            Image image = new Image(url, width, height, true, true, true);
            ImageView iv = new ImageView(image);
            iv.setFitWidth(width);
            iv.setFitHeight(height);
            iv.setPreserveRatio(true);

            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            iv.setClip(clip);

            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    private void mettreAJourStats() {
        if (items == null || items.isEmpty()) return;

        int total = items.size();
        long nbFaits = items.stream().filter(MealPlanItem::isEaten).count();

        double progress = (double) nbFaits / total;
        progressGlobal.setProgress(progress);
        labelProgression.setText(nbFaits + "/" + total + " meals completed");

        labelRepasEffectues.setText(nbFaits + " meals done");

        int calConso = items.stream()
                .filter(MealPlanItem::isEaten)
                .mapToInt(MealPlanItem::getRecetteCalories).sum();
        labelCaloriesConsommees.setText(calConso + " kcal consumed");

        long urgentsSauves = items.stream()
                .filter(i -> i.isEaten() && i.getUrgenceNiveau() == MealPlanItem.UrgenceNiveau.URGENT).count();
        long bientotSauves = items.stream()
                .filter(i -> i.isEaten() && i.getUrgenceNiveau() == MealPlanItem.UrgenceNiveau.BIENTOT).count();
        labelIngredientsSauves.setText((urgentsSauves + bientotSauves) + " ingredients saved");

        double kg = (urgentsSauves + bientotSauves) * 0.2;
        labelImpactEco.setText("~" + String.format("%.1f", kg) + " kg saved");
    }

    @FXML
    private void onRetour() {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.close();
    }
}