package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.esprit.projet.models.MealPlan;
import tn.esprit.projet.models.MealPlanItem;
import tn.esprit.projet.services.MealPlanService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MealPlanResultController implements Initializable {

    @FXML private Label labelIngredientsSauves;
    @FXML private Label labelTotalCalories;
    @FXML private Label labelPourcentageStock;
    @FXML private GridPane gridPlan;
    @FXML private Label labelCaloriesSemaine;
    @FXML private Label labelGaspillageEvite;
    @FXML private Button btnRegenerer;
    @FXML private Button btnValider;

    private MealPlan plan;
    private final MealPlanService planService = new MealPlanService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setPlan(MealPlan plan) {
        this.plan = plan;
        afficherPlan();
        afficherResume();
    }

    private void afficherPlan() {
        if (plan == null || plan.getItems() == null) return;

        List<MealPlanItem> items = plan.getItems();

        String[] moments = {"Breakfast", "Lunch", "Dinner"};
        for (int col = 0; col < moments.length; col++) {
            Label header = new Label(moments[col]);
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-text-fill: #1E293B; -fx-padding: 8px; -fx-alignment: center;");
            header.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(header, Priority.ALWAYS);
            gridPlan.add(header, col + 1, 0);
        }

        MealPlanItem.JourNom[] jours = MealPlanItem.JourNom.values();
        for (int row = 0; row < jours.length; row++) {
            Label labelJour = new Label(jours[row].name());
            labelJour.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; " +
                    "-fx-padding: 8px; -fx-background-color: #F8FAFC; " +
                    "-fx-background-radius: 6; -fx-text-fill: #1E293B;");
            labelJour.setMinWidth(90);
            labelJour.setAlignment(Pos.CENTER);
            gridPlan.add(labelJour, 0, row + 1);

            MealPlanItem.MomentRepas[] momentRepas = MealPlanItem.MomentRepas.values();
            for (int col = 0; col < momentRepas.length; col++) {
                final MealPlanItem.JourNom jour = jours[row];
                final MealPlanItem.MomentRepas moment = momentRepas[col];

                MealPlanItem item = items.stream()
                        .filter(i -> i.getJourNom() == jour && i.getMomentRepas() == moment)
                        .findFirst().orElse(null);

                VBox cellule = creerCelluleRepas(item);
                gridPlan.add(cellule, col + 1, row + 1);
            }
        }
    }

    private VBox creerCelluleRepas(MealPlanItem item) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setMinWidth(170);
        box.setMinHeight(120);

        if (item == null) {
            box.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
            box.getChildren().add(new Label("—"));
            return box;
        }

        String borderColor;
        String bgColor;
        switch (item.getUrgenceNiveau()) {
            case URGENT  -> { borderColor = "#DC2626"; bgColor = "#FEF2F2"; }
            case BIENTOT -> { borderColor = "#F59E0B"; bgColor = "#FFFBEB"; }
            default      -> { borderColor = "#10B981"; bgColor = "#F0FDF4"; }
        }

        box.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-border-width: 2;");

        // Image
        ImageView imageView = creerImageView(item.getRecetteImage(), 60, 60);
        if (imageView != null) {
            box.getChildren().add(imageView);
        }

        // Urgence
        Label emoji = new Label(item.getUrgenceEmoji());
        emoji.setStyle("-fx-font-size: 12px;");

        // Nom
        Label nom = new Label(item.getRecetteNom());
        nom.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1E293B;");
        nom.setWrapText(true);
        nom.setAlignment(Pos.CENTER);

        // Calories
        Label cal = new Label(item.getRecetteCalories() + " kcal");
        cal.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        box.getChildren().addAll(emoji, nom, cal);
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
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            iv.setClip(clip);

            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    private void afficherResume() {
        if (plan == null || plan.getItems() == null) return;

        List<MealPlanItem> items = plan.getItems();

        int totalCal = items.stream().mapToInt(MealPlanItem::getRecetteCalories).sum();
        labelCaloriesSemaine.setText(totalCal + " kcal estimated this week");
        labelTotalCalories.setText(totalCal + " kcal total");

        long nbUrgents = items.stream()
                .filter(i -> i.getUrgenceNiveau() == MealPlanItem.UrgenceNiveau.URGENT).count();
        long nbBientot = items.stream()
                .filter(i -> i.getUrgenceNiveau() == MealPlanItem.UrgenceNiveau.BIENTOT).count();

        labelIngredientsSauves.setText((nbUrgents + nbBientot) + " products saved from waste");
        labelGaspillageEvite.setText(nbUrgents + " urgent + " + nbBientot + " soon");

        long nbAvecStock = items.stream()
                .filter(i -> i.getUrgenceNiveau() != MealPlanItem.UrgenceNiveau.OK).count();
        int pct = items.size() > 0 ? (int)((double) nbAvecStock / items.size() * 100) : 0;
        labelPourcentageStock.setText(pct + "% recipes use expiring stock");
    }

    @FXML
    private void onRegenerer() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/meal_planner_form.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegenerer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onValider() {
        if (plan == null) return;

        int planId = planService.sauvegarderPlan(plan, plan.getItems());
        if (planId == -1) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/meal_plan_tracking.fxml"));
            Parent root = loader.load();

            tn.esprit.projet.controllers.MealPlanTrackingController ctrl = loader.getController();
            ctrl.setPlanId(planId);

            Stage stage = (Stage) btnValider.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}