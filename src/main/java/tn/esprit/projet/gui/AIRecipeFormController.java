package tn.esprit.projet.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.AIRecipeRequest;
import tn.esprit.projet.models.AIRecipeResult;
import tn.esprit.projet.services.AIRecipeService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AIRecipeFormController implements Initializable {

    // ─── Dish Type ────────────────────────────────────
    @FXML private ToggleButton btnEntree;
    @FXML private ToggleButton btnPlat;
    @FXML private ToggleButton btnDessert;
    @FXML private ToggleButton btnDrinks;

    // ─── Difficulty ───────────────────────────────────
    @FXML private ToggleButton btnEasy;
    @FXML private ToggleButton btnMedium;
    @FXML private ToggleButton btnHard;

    // ─── Cuisine ──────────────────────────────────────
    @FXML private ToggleButton btnTunisian;
    @FXML private ToggleButton btnItalian;
    @FXML private ToggleButton btnFrench;
    @FXML private ToggleButton btnAsian;
    @FXML private ToggleButton btnAmerican;
    @FXML private ToggleButton btnMexican;
    @FXML private ToggleButton btnOther;

    // ─── Calorie ──────────────────────────────────────
    @FXML private ToggleButton btnLight;
    @FXML private ToggleButton btnModerate;
    @FXML private ToggleButton btnRich;
    @FXML private ToggleButton btnAnyCalorie;

    // ─── Image Style ──────────────────────────────────
    @FXML private ToggleButton btnProfessional;
    @FXML private ToggleButton btnRustic;
    @FXML private ToggleButton btnMinimalist;
    @FXML private ToggleButton btnColorful;

    // ─── ToggleGroups créés en JAVA (pas FXML) ────────
    // ✅ FIX : créés manuellement dans initialize()
    private ToggleGroup groupDishType   = new ToggleGroup();
    private ToggleGroup groupDifficulty = new ToggleGroup();
    private ToggleGroup groupCuisine    = new ToggleGroup();
    private ToggleGroup groupCalorie    = new ToggleGroup();
    private ToggleGroup groupImageStyle = new ToggleGroup();

    // ─── Inputs ───────────────────────────────────────
    @FXML private TextField txtMaxTime;
    @FXML private TextField txtServings;
    @FXML private TextArea  txtExtraInstructions;

    // ─── Dietary ──────────────────────────────────────
    @FXML private CheckBox checkVegetarian;
    @FXML private CheckBox checkVegan;
    @FXML private CheckBox checkHalal;
    @FXML private CheckBox checkGlutenFree;
    @FXML private CheckBox checkNoLactose;
    @FXML private CheckBox checkNoNuts;
    @FXML private CheckBox checkNoEggs;

    // ─── Loading ──────────────────────────────────────
    @FXML private VBox        vboxForm;
    @FXML private VBox        vboxLoading;
    @FXML private ProgressBar progressBar;
    @FXML private Label       labelLoadingStep;
    @FXML private Label       labelError;

    private final AIRecipeService aiService = new AIRecipeService();

    // ═════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ✅ FIX : lier ToggleButtons aux ToggleGroups en Java
        setupToggleGroups();

        // Valeurs par défaut
        txtMaxTime .setText("30");
        txtServings.setText("4");

        // Cacher loading et erreur
        vboxLoading.setVisible(false);
        vboxLoading.setManaged(false);
        if (labelError != null) {
            labelError.setVisible(false);
            labelError.setManaged(false);
        }
    }

    // ─── Setup ToggleGroups ───────────────────────────
    private void setupToggleGroups() {

        // Dish Type
        btnEntree .setToggleGroup(groupDishType);
        btnPlat   .setToggleGroup(groupDishType);
        btnDessert.setToggleGroup(groupDishType);
        btnDrinks .setToggleGroup(groupDishType);
        btnPlat.setSelected(true); // default

        // Difficulty
        btnEasy  .setToggleGroup(groupDifficulty);
        btnMedium.setToggleGroup(groupDifficulty);
        btnHard  .setToggleGroup(groupDifficulty);
        btnMedium.setSelected(true); // default

        // Cuisine
        btnTunisian.setToggleGroup(groupCuisine);
        btnItalian .setToggleGroup(groupCuisine);
        btnFrench  .setToggleGroup(groupCuisine);
        btnAsian   .setToggleGroup(groupCuisine);
        btnAmerican.setToggleGroup(groupCuisine);
        btnMexican .setToggleGroup(groupCuisine);
        btnOther   .setToggleGroup(groupCuisine);
        btnTunisian.setSelected(true); // default

        // Calorie
        btnLight    .setToggleGroup(groupCalorie);
        btnModerate .setToggleGroup(groupCalorie);
        btnRich     .setToggleGroup(groupCalorie);
        btnAnyCalorie.setToggleGroup(groupCalorie);
        btnModerate.setSelected(true); // default

        // Image Style
        btnProfessional.setToggleGroup(groupImageStyle);
        btnRustic      .setToggleGroup(groupImageStyle);
        btnMinimalist  .setToggleGroup(groupImageStyle);
        btnColorful    .setToggleGroup(groupImageStyle);
        btnProfessional.setSelected(true); // default

        // ✅ Style CSS pour l'état sélectionné
        applyToggleStyle(groupDishType);
        applyToggleStyle(groupDifficulty);
        applyToggleStyle(groupCuisine);
        applyToggleStyle(groupCalorie);
        applyToggleStyle(groupImageStyle);
    }

    // ─── Appliquer style selected/unselected ─────────
    private void applyToggleStyle(ToggleGroup group) {
        String selected   = "-fx-background-color: #1F4D3A; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #1F4D3A; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 8 18; " +
                "-fx-cursor: hand;";

        String unselected = "-fx-background-color: white; " +
                "-fx-text-fill: #475569; " +
                "-fx-font-weight: normal; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 8 18; " +
                "-fx-cursor: hand;";

        for (Toggle t : group.getToggles()) {
            ToggleButton btn = (ToggleButton) t;

            // Style initial
            btn.setStyle(btn.isSelected() ? selected : unselected);

            // Listener pour changer le style
            btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                btn.setStyle(isSelected ? selected : unselected);
            });
        }
    }

    // ═════════════════════════════════════════════════
    // BOUTON GENERATE
    // ═════════════════════════════════════════════════
    @FXML
    private void onGenerate() {

        System.out.println("🔘 Generate clicked !");

        if (!validerFormulaire()) return;

        AIRecipeRequest request = buildRequest();

        System.out.println("📋 Request : " + request.getDishType()
                + " | " + request.getCuisineStyle()
                + " | " + request.getDifficulty());

        afficherLoading(true);

        new Thread(() -> {
            try {
                updateStep("Understanding your preferences...", 0.15);
                Thread.sleep(300);

                updateStep("Generating recipe content...", 0.40);
                AIRecipeResult result = aiService.generateFullRecipe(request);

                updateStep("Fetching beautiful image...", 0.80);
                Thread.sleep(300);

                updateStep("Finalizing...", 1.0);
                Thread.sleep(200);

                Platform.runLater(() -> ouvrirResultat(result, request));

            } catch (Exception e) {
                System.err.println("❌ ERREUR API : " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    afficherLoading(false);
                    afficherErreur("Generation failed: " + e.getMessage());
                });
            }
        }).start();
    }

    // ─── Build Request ────────────────────────────────
    private AIRecipeRequest buildRequest() {
        AIRecipeRequest req = new AIRecipeRequest();

        req.setDishType    (getSelected(groupDishType,    "Plat"));
        req.setDifficulty  (getSelected(groupDifficulty,  "Medium"));
        req.setCuisineStyle(getSelected(groupCuisine,     "Tunisian"));
        req.setCalorieRange(getSelected(groupCalorie,     "Moderate"));
        req.setImageStyle  (getSelected(groupImageStyle,  "Professional"));

        try {
            req.setMaxTime (Integer.parseInt(txtMaxTime .getText().trim()));
            req.setServings(Integer.parseInt(txtServings.getText().trim()));
        } catch (NumberFormatException e) {
            req.setMaxTime (30);
            req.setServings(4);
        }

        req.setVegetarian (checkVegetarian.isSelected());
        req.setVegan      (checkVegan     .isSelected());
        req.setHalal      (checkHalal     .isSelected());
        req.setGlutenFree (checkGlutenFree.isSelected());
        req.setNoLactose  (checkNoLactose .isSelected());
        req.setNoNuts     (checkNoNuts    .isSelected());
        req.setNoEggs     (checkNoEggs    .isSelected());
        req.setExtraInstructions(txtExtraInstructions.getText());

        return req;
    }

    // ─── Récupérer valeur userData du toggle sélectionné
    private String getSelected(ToggleGroup group, String defaultVal) {
        Toggle selected = group.getSelectedToggle();
        if (selected instanceof ToggleButton) {
            Object userData = ((ToggleButton) selected).getUserData();
            if (userData != null) return userData.toString();
        }
        return defaultVal;
    }

    // ─── Validation ───────────────────────────────────
    private boolean validerFormulaire() {
        if (groupDishType.getSelectedToggle() == null) {
            afficherErreur("Please select a dish type !");
            return false;
        }
        if (groupDifficulty.getSelectedToggle() == null) {
            afficherErreur("Please select a difficulty !");
            return false;
        }
        if (groupCuisine.getSelectedToggle() == null) {
            afficherErreur("Please select a cuisine style !");
            return false;
        }
        try {
            int t = Integer.parseInt(txtMaxTime.getText().trim());
            if (t <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("Please enter a valid time !");
            return false;
        }

        if (labelError != null) {
            labelError.setVisible(false);
            labelError.setManaged(false);
        }
        return true;
    }

    private void afficherErreur(String msg) {
        if (labelError != null) {
            labelError.setText("⚠️ " + msg);
            labelError.setVisible(true);
            labelError.setManaged(true);
        }
        System.err.println("⚠️ " + msg);
    }

    private void afficherLoading(boolean show) {
        vboxForm   .setVisible(!show);
        vboxForm   .setManaged(!show);
        vboxLoading.setVisible(show);
        vboxLoading.setManaged(show);
    }

    private void updateStep(String msg, double progress) {
        Platform.runLater(() -> {
            labelLoadingStep.setText(msg);
            progressBar     .setProgress(progress);
        });
    }

    private void ouvrirResultat(AIRecipeResult result, AIRecipeRequest request) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ai_recipe_result.fxml"));
            Parent root = loader.load();

            AIRecipeResultController ctrl = loader.getController();
            ctrl.setResult(result, request);

            Stage stage = (Stage) vboxForm.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 750));
            stage.setTitle("AI Generated Recipe");

        } catch (IOException e) {
            afficherErreur("Failed to open result: " + e.getMessage());
            afficherLoading(false);
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) vboxForm.getScene().getWindow();
        stage.close();
    }
}