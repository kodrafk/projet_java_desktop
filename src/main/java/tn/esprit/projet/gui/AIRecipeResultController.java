package tn.esprit.projet.gui;

import javafx.application.Platform;
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
import tn.esprit.projet.models.AIRecipeRequest;
import tn.esprit.projet.models.AIRecipeResult;
import tn.esprit.projet.services.AIRecipeService;
import tn.esprit.projet.services.AIRecipeSaveService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AIRecipeResultController implements Initializable {

    // ─── Image ────────────────────────────────────────
    @FXML private ImageView imageRecette;
    @FXML private Button    btnRegenerateImage;

    // ─── Infos de base ────────────────────────────────
    @FXML private TextField txtNom;
    @FXML private Label     lblType;
    @FXML private Label     lblDifficulte;
    @FXML private Label     lblTemps;
    @FXML private Label     lblServings;
    @FXML private TextArea  txtDescription;

    // ─── Ingrédients ──────────────────────────────────
    @FXML private VBox vboxIngredients;

    // ─── Étapes ───────────────────────────────────────
    @FXML private VBox vboxSteps;

    // ─── Nutrition ────────────────────────────────────
    @FXML private Label lblCalories;
    @FXML private Label lblProteines;
    @FXML private Label lblGlucides;
    @FXML private Label lblLipides;

    // ─── Tags ─────────────────────────────────────────
    @FXML private HBox hboxTags;

    // ─── Boutons ──────────────────────────────────────
    @FXML private Button btnGenerateAnother;
    @FXML private Button btnSave;

    // ─── Loading image ────────────────────────────────
    @FXML private Label lblImageLoading;

    // ─── Données ──────────────────────────────────────
    private AIRecipeResult  result;
    private AIRecipeRequest request;

    private final AIRecipeService     aiService   = new AIRecipeService();
    private final AIRecipeSaveService saveService = new AIRecipeSaveService();

    // ═════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    // ─── Injecter résultat ────────────────────────────
    public void setResult(AIRecipeResult result, AIRecipeRequest request) {
        this.result  = result;
        this.request = request;
        afficherResultat();
    }

    // ═════════════════════════════════════════════════
    // AFFICHER RÉSULTAT COMPLET
    // ═════════════════════════════════════════════════
    private void afficherResultat() {
        afficherImage();
        afficherInfosBase();
        afficherIngredients();
        afficherSteps();
        afficherNutrition();
        afficherTags();
    }

    // ─── Image ────────────────────────────────────────
    private void afficherImage() {
        if (result.getImageUrl() == null
                || result.getImageUrl().isEmpty()) return;

        System.out.println("🖼️ Loading : " + result.getImageUrl());

        if (lblImageLoading != null) {
            lblImageLoading.setVisible(true);
        }

        new Thread(() -> {
            try {
                // ✅ Télécharger via HttpClient avec headers corrects
                java.net.http.HttpClient client =
                        java.net.http.HttpClient.newHttpClient();

                java.net.http.HttpRequest request =
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(result.getImageUrl()))
                                .header("User-Agent",
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .header("Referer", "https://www.pexels.com/")
                                .GET()
                                .build();

                java.net.http.HttpResponse<java.io.InputStream> response =
                        client.send(request,
                                java.net.http.HttpResponse.BodyHandlers.ofInputStream());

                System.out.println("📡 Image HTTP status : "
                        + response.statusCode());

                if (response.statusCode() == 200) {
                    // Lire le stream et créer l'image
                    java.io.InputStream inputStream = response.body();
                    Image image = new Image(inputStream);

                    System.out.println("✅ Image ready : "
                            + image.getWidth() + "x" + image.getHeight());

                    Platform.runLater(() -> {
                        imageRecette.setImage(image);
                        imageRecette.setFitWidth(308);
                        imageRecette.setFitHeight(220);

                        // Clip arrondi
                        javafx.scene.shape.Rectangle clip =
                                new javafx.scene.shape.Rectangle(308, 220);
                        clip.setArcWidth(20);
                        clip.setArcHeight(20);
                        imageRecette.setClip(clip);

                        if (lblImageLoading != null)
                            lblImageLoading.setVisible(false);
                    });
                } else {
                    System.err.println("❌ HTTP " + response.statusCode());
                    Platform.runLater(() -> {
                        if (lblImageLoading != null)
                            lblImageLoading.setVisible(false);
                    });
                }

            } catch (Exception e) {
                System.err.println("❌ Image load error : " + e.getMessage());
                Platform.runLater(() -> {
                    if (lblImageLoading != null)
                        lblImageLoading.setVisible(false);
                });
            }
        }).start();
    }
    // ─── Infos de base ────────────────────────────────
    private void afficherInfosBase() {
        txtNom      .setText(result.getNom());
        txtDescription.setText(result.getDescription());
        lblType     .setText(result.getType());
        lblDifficulte.setText(result.getDifficulte());
        lblTemps    .setText(result.getTempsPreparation() + " min");
        lblServings .setText(result.getServings() + " servings");
    }

    // ─── Ingrédients ──────────────────────────────────
    private void afficherIngredients() {
        vboxIngredients.getChildren().clear();

        List<String> ingredients = result.getIngredients();
        if (ingredients == null) return;

        for (int i = 0; i < ingredients.size(); i++) {
            HBox ligne = creerLigneIngredient(ingredients.get(i), i);
            vboxIngredients.getChildren().add(ligne);
        }
    }

    private HBox creerLigneIngredient(String ingredient, int index) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 12, 8, 12));

        // Couleur alternée
        String bg = (index % 2 == 0) ? "#F8FAFC" : "white";
        box.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-background-radius: 8;");

        // Bullet point
        Label bullet = new Label("•");
        bullet.setStyle("-fx-text-fill: #1F4D3A; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold;");
        bullet.setMinWidth(20);

        // Texte ingrédient
        Label lblIngredient = new Label(ingredient);
        lblIngredient.setStyle("-fx-font-size: 13px; " +
                "-fx-text-fill: #1E293B;");
        HBox.setHgrow(lblIngredient, Priority.ALWAYS);

        box.getChildren().addAll(bullet, lblIngredient);
        return box;
    }

    // ─── Étapes ───────────────────────────────────────
    private void afficherSteps() {
        vboxSteps.getChildren().clear();

        List<String> steps = result.getSteps();
        if (steps == null) return;

        for (int i = 0; i < steps.size(); i++) {
            HBox ligne = creerLigneStep(steps.get(i), i + 1);
            vboxSteps.getChildren().add(ligne);
            if (i < steps.size() - 1) {
                Separator sep = new Separator();
                sep.setPadding(new Insets(2, 0, 2, 0));
                vboxSteps.getChildren().add(sep);
            }
        }
    }

    private HBox creerLigneStep(String step, int numero) {
        HBox box = new HBox(14);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(10, 12, 10, 12));

        // Numéro step
        Label lblNumero = new Label(String.valueOf(numero));
        lblNumero.setMinWidth(32);
        lblNumero.setMinHeight(32);
        lblNumero.setAlignment(Pos.CENTER);
        lblNumero.setStyle("-fx-background-color: #1F4D3A; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 50%;");

        // Texte step
        Label lblStep = new Label(step);
        lblStep.setWrapText(true);
        lblStep.setStyle("-fx-font-size: 13px; " +
                "-fx-text-fill: #475569; " +
                "-fx-line-spacing: 2;");
        HBox.setHgrow(lblStep, Priority.ALWAYS);

        box.getChildren().addAll(lblNumero, lblStep);
        return box;
    }

    // ─── Nutrition ────────────────────────────────────
    private void afficherNutrition() {
        lblCalories .setText(result.getCalories()              + " kcal");
        lblProteines.setText(result.getProteines()             + "g");
        lblGlucides .setText(result.getGlucides()              + "g");
        lblLipides  .setText(result.getLipides()               + "g");
    }

    // ─── Tags ─────────────────────────────────────────
    private void afficherTags() {
        hboxTags.getChildren().clear();

        // Tag cuisine
        if (request.getCuisineStyle() != null) {
            hboxTags.getChildren().add(creerTag(request.getCuisineStyle(), "#1F4D3A", "white"));
        }

        // Tag dietary
        if (request.isHalal())      hboxTags.getChildren().add(creerTag("Halal",       "#10B981", "white"));
        if (request.isVegetarian()) hboxTags.getChildren().add(creerTag("Vegetarian",  "#34D399", "white"));
        if (request.isVegan())      hboxTags.getChildren().add(creerTag("Vegan",        "#059669", "white"));
        if (request.isGlutenFree()) hboxTags.getChildren().add(creerTag("Gluten Free", "#F59E0B", "white"));

        // Tag calorie
        if (request.getCalorieRange() != null) {
            hboxTags.getChildren().add(creerTag(
                    request.getCalorieRange() + " cal", "#64748B", "white"));
        }
    }

    private Label creerTag(String text, String bgColor, String textColor) {
        Label tag = new Label(text);
        tag.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-text-fill: "        + textColor + "; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 4 10; " +
                "-fx-background-radius: 20;");
        return tag;
    }

    // ═════════════════════════════════════════════════
    // BOUTON — Regénérer Image
    // ═════════════════════════════════════════════════
    @FXML
    private void onRegenerateImage() {
        if (result == null) return;

        // Désactiver bouton pendant chargement
        if (btnRegenerateImage != null) {
            btnRegenerateImage.setDisable(true);
            btnRegenerateImage.setText("⏳ Loading...");
        }

        // Afficher loading
        if (lblImageLoading != null) {
            lblImageLoading.setVisible(true);
        }

        new Thread(() -> {
            try {
                // Keywords de la recette actuelle
                String keywords = result.getImageKeywords();
                String style    = request != null
                        ? request.getImageStyle()
                        : "Professional";

                System.out.println("🔄 Regenerating image for : " + keywords);

                // Appel Pexels
                String newUrl = aiService.regenerateImage(keywords, style);

                // Mettre à jour le résultat
                result.setImageUrl(newUrl);

                Platform.runLater(() -> {
                    // Recharger image
                    afficherImage();

                    // Réactiver bouton
                    if (btnRegenerateImage != null) {
                        btnRegenerateImage.setDisable(false);
                        btnRegenerateImage.setText("🔄 Regenerate Image");
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ onRegenerateImage error : "
                        + e.getMessage());

                Platform.runLater(() -> {
                    if (btnRegenerateImage != null) {
                        btnRegenerateImage.setDisable(false);
                        btnRegenerateImage.setText("🔄 Regenerate Image");
                    }
                    if (lblImageLoading != null) {
                        lblImageLoading.setVisible(false);
                    }
                });
            }
        }).start();
    }

    // ═════════════════════════════════════════════════
    // BOUTON — Generate Another
    // ═════════════════════════════════════════════════
    @FXML
    private void onGenerateAnother() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ai_recipe_form.fxml"));
            Parent root = loader.load();

            // Restaurer les valeurs du formulaire
            AIRecipeFormController ctrl = loader.getController();
            if (request != null) {
                ctrl.restoreRequest(request);
            }

            // Trouver le stage depuis n'importe quel nœud disponible
            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(new Scene(root, 800, 700));
                stage.setTitle("🤖 AI Recipe Chef");
            }

        } catch (IOException e) {
            System.err.println("❌ onGenerateAnother : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Stage getStage() {
        if (btnSave != null
                && btnSave.getScene() != null) {
            return (Stage) btnSave.getScene().getWindow();
        }
        if (btnRegenerateImage != null
                && btnRegenerateImage.getScene() != null) {
            return (Stage) btnRegenerateImage.getScene().getWindow();
        }
        if (imageRecette != null
                && imageRecette.getScene() != null) {
            return (Stage) imageRecette.getScene().getWindow();
        }
        if (txtNom != null
                && txtNom.getScene() != null) {
            return (Stage) txtNom.getScene().getWindow();
        }
        return null;
    }

    // ─── Succès sauvegarde ────────────────────────────
    private void afficherSucces() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recipe Saved !");
        alert.setHeaderText(null);
        alert.setContentText("✅ \"" + result.getNom() +
                "\" has been saved successfully !");
        alert.showAndWait();

        // Fermer fenêtre
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    // ─── Alerte erreur ────────────────────────────────
    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void onClose() {
        Stage stage = null;

        if (btnSave != null && btnSave.getScene() != null) {
            stage = (Stage) btnSave.getScene().getWindow();
        } else if (btnRegenerateImage != null
                && btnRegenerateImage.getScene() != null) {
            stage = (Stage) btnRegenerateImage.getScene().getWindow();
        } else if (imageRecette != null
                && imageRecette.getScene() != null) {
            stage = (Stage) imageRecette.getScene().getWindow();
        }

        if (stage != null) {
            stage.close();
        }
    }
    @FXML
    private void onSave() {
        if (result == null) {
            System.err.println("❌ result is null !");
            return;
        }

        // Récupérer les valeurs éditées
        String nom  = txtNom        .getText().trim();
        String desc = txtDescription.getText().trim();

        if (nom.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a recipe name !");
            alert.showAndWait();
            return;
        }

        result.setNom        (nom);
        result.setDescription(desc);

        // Désactiver bouton pendant sauvegarde
        btnSave.setDisable(true);
        btnSave.setText("Saving...");

        new Thread(() -> {
            try {
                AIRecipeSaveService saveService = new AIRecipeSaveService();
                int currentUserId = tn.esprit.projet.utils.SessionManager.getCurrentUser() != null 
                        ? tn.esprit.projet.utils.SessionManager.getCurrentUser().getId() 
                        : 1;
                int recetteId = saveService.sauvegarder(result, currentUserId);

                javafx.application.Platform.runLater(() -> {
                    if (recetteId != -1) {
                        System.out.println("✅ Recette sauvegardée → id=" + recetteId);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Saved !");
                        alert.setHeaderText(null);
                        alert.setContentText("Recipe \"" + result.getNom() +
                                "\" saved successfully !");
                        alert.showAndWait();

                        // Fermer fenêtre
                        Stage stage = (Stage) btnSave.getScene().getWindow();
                        stage.close();

                    } else {
                        btnSave.setDisable(false);
                        btnSave.setText("SAVE TO DATABASE");

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to save recipe. Please try again.");
                        alert.showAndWait();
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ onSave error : " + e.getMessage());
                e.printStackTrace();

                javafx.application.Platform.runLater(() -> {
                    btnSave.setDisable(false);
                    btnSave.setText("SAVE TO DATABASE");

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
}