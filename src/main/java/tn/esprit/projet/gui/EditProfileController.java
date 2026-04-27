package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.BadgeService;
import tn.esprit.projet.services.NutritionService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;
import tn.esprit.projet.utils.Validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class EditProfileController {

    @FXML private TextField  firstNameField;
    @FXML private TextField  lastNameField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextField  weightField;
    @FXML private TextField  heightField;
    @FXML private ImageView  photoView;
    @FXML private Label      lblPhotoHint;
    @FXML private Label      errFirstName;
    @FXML private Label      errLastName;
    @FXML private Label      errBirthday;
    @FXML private Label      errWeight;
    @FXML private Label      errHeight;
    @FXML private Label      errPhoto;

    private File newPhotoFile;
    private final UserRepository  repo             = new UserRepository();
    private final NutritionService nutritionService = new NutritionService();
    private final BadgeService     badgeService     = new BadgeService();

    @FXML
    public void initialize() {
        User u = Session.getCurrentUser();
        if (u == null) return;
        firstNameField.setText(nvl(u.getFirstName()));
        lastNameField.setText(nvl(u.getLastName()));
        birthdayPicker.setValue(u.getBirthday());
        weightField.setText(u.getWeight() > 0 ? String.valueOf(u.getWeight()) : "");
        heightField.setText(u.getHeight() > 0 ? String.valueOf(u.getHeight()) : "");

        // Show current photo
        if (photoView != null && u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) {
                photoView.setImage(new Image(f.toURI().toString()));
                if (lblPhotoHint != null) lblPhotoHint.setText("✅ Photo uploaded");
            }
        }

        // Live BMI preview as user types weight/height
        weightField.textProperty().addListener((o, a, b) -> updateBmiPreview());
        heightField.textProperty().addListener((o, a, b) -> updateBmiPreview());
    }

    private void updateBmiPreview() {
        try {
            double w = Double.parseDouble(weightField.getText().trim());
            double h = Double.parseDouble(heightField.getText().trim());
            if (w > 0 && h > 0) {
                double bmi = w / Math.pow(h / 100.0, 2);
                String cat = bmi < 18.5 ? "Underweight" :
                             bmi < 25.0 ? "Normal weight ✅" :
                             bmi < 30.0 ? "Overweight" : "Obesity";
                if (errWeight != null)
                    errWeight.setText(String.format("Live BMI: %.1f — %s", bmi, cat));
                if (errWeight != null)
                    errWeight.setStyle(bmi >= 18.5 && bmi < 25.0
                            ? "-fx-text-fill:#16A34A;-fx-font-size:11px;"
                            : "-fx-text-fill:#D97706;-fx-font-size:11px;");
            }
        } catch (NumberFormatException ignored) {
            if (errWeight != null) { errWeight.setText(""); }
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a profile photo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        File file = fc.showOpenDialog(firstNameField.getScene().getWindow());
        if (file == null) return;

        if (file.length() > 2L * 1024 * 1024) {
            if (errPhoto != null) errPhoto.setText("❌ File too large. Maximum 2MB.");
            return;
        }
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".png")
                && !name.endsWith(".gif") && !name.endsWith(".webp")) {
            if (errPhoto != null) errPhoto.setText("❌ Invalid type. JPEG, PNG, GIF or WebP only.");
            return;
        }

        if (errPhoto != null) errPhoto.setText("");
        newPhotoFile = file;
        if (photoView != null) photoView.setImage(new Image(file.toURI().toString()));
        if (lblPhotoHint != null) {
            lblPhotoHint.setText("✅ " + file.getName() + " selected");
            lblPhotoHint.setStyle("-fx-text-fill:#16A34A;-fx-font-size:11px;");
        }
    }

    @FXML
    private void handleSave() {
        boolean ok = true;
        ok &= Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        ok &= Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));

        String bdErr = Validator.birthday(birthdayPicker.getValue());
        if (errBirthday != null) errBirthday.setText(bdErr != null ? bdErr : "");
        if (bdErr != null) ok = false;

        ok &= Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        ok &= Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));
        if (!ok) return;

        User u = Session.getCurrentUser();
        double oldWeight = u.getWeight();

        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setBirthday(birthdayPicker.getValue());
        u.setWeight(Double.parseDouble(weightField.getText().trim()));
        u.setHeight(Double.parseDouble(heightField.getText().trim()));

        // Save photo
        if (newPhotoFile != null) {
            String filename = savePhoto(newPhotoFile, u.getPhotoFilename());
            if (filename != null) {
                u.setPhotoFilename(filename);
                repo.updatePhoto(u.getId(), filename);
            }
        }

        repo.update(u);
        Session.login(u);

        // Auto-check badges after profile update
        badgeService.refreshBadges(u);

        Stage stage = (Stage) firstNameField.getScene().getWindow();
        Toasts.show(stage, "✅ Profile updated successfully!", Toasts.Type.SUCCESS);

        // Show health analysis popup
        NutritionService.HealthAnalysis analysis = nutritionService.analyse(u);
        if (analysis != null) {
            showHealthAnalysis(analysis, oldWeight, u.getWeight());
        }

        stage.close();
    }

    /**
     * Shows a personalized health analysis popup after saving the profile.
     * Tells the user their BMI, ideal weight, and how many kg to lose/gain.
     */
    private void showHealthAnalysis(NutritionService.HealthAnalysis a, double oldWeight, double newWeight) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Your Health Analysis");
        popup.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:white;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:" + a.bmiColor + ";-fx-padding:18 24;");
        Label emoji = new Label(a.progressEmoji);
        emoji.setStyle("-fx-font-size:28px;");
        VBox headerText = new VBox(3);
        Label h1 = new Label(a.motivationTitle);
        h1.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label h2 = new Label("BMI: " + a.bmi + " — " + a.bmiCategory);
        h2.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.85);");
        headerText.getChildren().addAll(h1, h2);
        header.getChildren().addAll(emoji, headerText);

        // Content
        VBox content = new VBox(14);
        content.setStyle("-fx-padding:20 24;");

        // BMI gauge
        VBox bmiSection = new VBox(6);
        Label bmiTitle = new Label("Your BMI");
        bmiTitle.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#64748B;");
        ProgressBar bmiBar = new ProgressBar(Math.min(1.0, a.bmi / 40.0));
        bmiBar.setPrefWidth(400); bmiBar.setPrefHeight(10);
        bmiBar.setStyle("-fx-accent:" + a.bmiColor + ";");
        HBox bmiLabels = new HBox();
        Label bmiMin = new Label("18.5");
        bmiMin.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label bmiVal = new Label(a.bmi + "");
        bmiVal.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + a.bmiColor + ";");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        Label bmiMax = new Label("40");
        bmiMax.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");
        bmiLabels.getChildren().addAll(bmiMin, sp, bmiVal, sp2, bmiMax);
        bmiSection.getChildren().addAll(bmiTitle, bmiBar, bmiLabels);

        // Weight objective
        VBox weightSection = new VBox(6);
        weightSection.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:10;-fx-padding:12 14;");
        Label wtTitle = new Label("🎯 Weight Goal");
        wtTitle.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        Label wtRange = new Label(String.format("Ideal weight: %.1f – %.1f kg", a.idealWeightMin, a.idealWeightMax));
        wtRange.setStyle("-fx-font-size:12px;-fx-text-fill:#64748B;");

        Label wtGoal;
        if (a.kgToLose > 0) {
            wtGoal = new Label(String.format("📉 To lose: %.1f kg", a.kgToLose));
            wtGoal.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#EF4444;");
        } else if (a.kgToLose < 0) {
            wtGoal = new Label(String.format("📈 To gain: %.1f kg", Math.abs(a.kgToLose)));
            wtGoal.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#3B82F6;");
        } else {
            wtGoal = new Label("✅ You are in the ideal range!");
            wtGoal.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#16A34A;");
        }

        if (oldWeight > 0 && oldWeight != newWeight) {
            double diff = newWeight - oldWeight;
            Label changeLbl = new Label(diff > 0
                    ? String.format("⬆ +%.1f kg since last update", diff)
                    : String.format("⬇ %.1f kg since last update", diff));
            changeLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + (diff < 0 ? "#16A34A" : "#D97706") + ";");
            weightSection.getChildren().addAll(wtTitle, wtRange, wtGoal, changeLbl);
        } else {
            weightSection.getChildren().addAll(wtTitle, wtRange, wtGoal);
        }

        // Daily calories
        Label calLbl = new Label("🔥 Estimated daily calories: " + a.dailyCalories + " kcal");
        calLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#64748B;");

        // Advice
        VBox adviceBox = new VBox(4);
        adviceBox.setStyle("-fx-background-color:#F0FDF4;-fx-background-radius:10;-fx-padding:12 14;" +
                "-fx-border-color:#BBF7D0;-fx-border-radius:10;-fx-border-width:1;");
        Label advTitle = new Label("💡 Personalized advice");
        advTitle.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#166534;");
        Label advText = new Label(a.actionAdvice);
        advText.setWrapText(true); advText.setMaxWidth(380);
        advText.setStyle("-fx-font-size:12px;-fx-text-fill:#166534;");
        adviceBox.getChildren().addAll(advTitle, advText);

        // Close button
        Button btnClose = new Button("Got it, thanks! 💪");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setPrefHeight(44);
        btnClose.setStyle("-fx-background-color:" + a.bmiColor + ";-fx-text-fill:white;" +
                "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:12;-fx-cursor:hand;-fx-border-color:transparent;");
        btnClose.setOnAction(e -> popup.close());

        content.getChildren().addAll(bmiSection, weightSection, calLbl, adviceBox, btnClose);
        root.getChildren().addAll(header, content);

        popup.setScene(new Scene(root, 440, 480));
        popup.showAndWait();
    }

    @FXML
    private void handleCancel() {
        ((Stage) firstNameField.getScene().getWindow()).close();
    }

    private String savePhoto(File src, String oldFilename) {
        try {
            if (oldFilename != null && !oldFilename.isBlank())
                new File("uploads/profiles/" + oldFilename).delete();
            Path dir = Paths.get("uploads/profiles");
            Files.createDirectories(dir);
            String ext = src.getName().substring(src.getName().lastIndexOf('.'));
            String filename = UUID.randomUUID() + ext;
            Files.copy(src.toPath(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
