package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.WeightLog;
import tn.esprit.projet.repository.WeightRepository;

import java.io.File;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminUserGalleryController {

    @FXML private Label lblUserName;
    @FXML private Label lblUserEmail;
    @FXML private Label lblAvatarInitial;
    @FXML private ImageView profilePhoto;
    @FXML private Label lblPhotoStatus;
    @FXML private Label lblPhotoDate;
    @FXML private Label lblAccessBadge;
    @FXML private Label lblWeightPhotoCount;
    @FXML private FlowPane weightPhotoGrid;
    @FXML private VBox lblNoWeightPhotos;   // VBox container, not Label
    @FXML private VBox lblAccessDenied;     // VBox container, not Label

    private User user;
    private final WeightRepository weightRepo = new WeightRepository();
    private static final DateTimeFormatter DTSHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DTFULL  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setUser(User u) {
        this.user = u;
        loadGallery();
    }

    private void loadGallery() {
        if (user == null) return;

        // Header
        lblUserName.setText(user.getFullName() + "'s Gallery");
        lblUserEmail.setText(user.getEmail());

        if (user.getFirstName() != null && !user.getFirstName().isEmpty())
            lblAvatarInitial.setText(String.valueOf(user.getFirstName().charAt(0)).toUpperCase());

        // Profile photo
        if (user.getPhotoFilename() != null && !user.getPhotoFilename().isBlank()) {
            File f = new File("uploads/profiles/" + user.getPhotoFilename());
            if (f.exists()) {
                profilePhoto.setImage(new Image(f.toURI().toString()));
                profilePhoto.setVisible(true);
                lblAvatarInitial.setVisible(false);
                lblPhotoStatus.setText("Profile photo uploaded");
                lblPhotoStatus.setStyle("-fx-font-size:13px;-fx-text-fill:#16A34A;-fx-font-weight:bold;");
                if (f.lastModified() > 0)
                    lblPhotoDate.setText("Last updated: " +
                        new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(f.lastModified())));
            }
        }

        // Gallery access badge
        boolean hasAccess = user.isGalleryAccessEnabled();
        if (lblAccessBadge != null) {
            if (hasAccess) {
                lblAccessBadge.setText("Gallery: PUBLIC");
                lblAccessBadge.setStyle("-fx-background-color:#DCFCE7;-fx-text-fill:#16A34A;" +
                    "-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 12;");
            } else {
                lblAccessBadge.setText("Gallery: PRIVATE");
                lblAccessBadge.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#DC2626;" +
                    "-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 12;");
            }
        }

        // Weight log photos — only show if user granted access
        if (!hasAccess) {
            if (lblAccessDenied != null) {
                lblAccessDenied.setVisible(true);
                lblAccessDenied.setManaged(true);
            }
            if (weightPhotoGrid != null) { weightPhotoGrid.setVisible(false); weightPhotoGrid.setManaged(false); }
            if (lblNoWeightPhotos != null) { lblNoWeightPhotos.setVisible(false); lblNoWeightPhotos.setManaged(false); }
            if (lblWeightPhotoCount != null) lblWeightPhotoCount.setText("(access restricted)");
            return;
        }

        if (lblAccessDenied != null) { lblAccessDenied.setVisible(false); lblAccessDenied.setManaged(false); }

        List<WeightLog> logs = weightRepo.findLogsByUser(user.getId());
        List<WeightLog> withPhotos = logs.stream()
            .filter(l -> l.getPhoto() != null && !l.getPhoto().isBlank())
            .toList();

        if (lblWeightPhotoCount != null)
            lblWeightPhotoCount.setText("(" + withPhotos.size() + " photo" + (withPhotos.size() != 1 ? "s" : "") + ")");

        if (withPhotos.isEmpty()) {
            if (lblNoWeightPhotos != null) { lblNoWeightPhotos.setVisible(true); lblNoWeightPhotos.setManaged(true); }
            if (weightPhotoGrid != null) { weightPhotoGrid.setVisible(false); weightPhotoGrid.setManaged(false); }
            return;
        }

        if (lblNoWeightPhotos != null) { lblNoWeightPhotos.setVisible(false); lblNoWeightPhotos.setManaged(false); }
        if (weightPhotoGrid != null) {
            weightPhotoGrid.setVisible(true);
            weightPhotoGrid.setManaged(true);
            weightPhotoGrid.getChildren().clear();

            for (WeightLog log : withPhotos) {
                File photoFile = getPhotoFile(log.getPhoto());
                if (photoFile != null)
                    weightPhotoGrid.getChildren().add(createWeightPhotoCard(log, photoFile));
            }
        }
    }

    private VBox createWeightPhotoCard(WeightLog log, File photoFile) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(180);
        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:14;" +
            "-fx-padding:10;" +
            "-fx-border-color:#E2E8F0;" +
            "-fx-border-radius:14;" +
            "-fx-border-width:1;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);" +
            "-fx-cursor:hand;");

        // Image with rounded clip
        ImageView imgView = new ImageView(new Image(photoFile.toURI().toString()));
        imgView.setFitWidth(160);
        imgView.setFitHeight(160);
        imgView.setPreserveRatio(false);
        Rectangle clip = new Rectangle(160, 160);
        clip.setArcWidth(10); clip.setArcHeight(10);
        imgView.setClip(clip);

        // Weight badge
        Label lblWeight = new Label(String.format("⚖️ %.1f kg", log.getWeight()));
        lblWeight.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;" +
            "-fx-background-color:#F0FDF4;-fx-background-radius:8;-fx-padding:3 8;");

        // Date
        Label lblDate = new Label(log.getLoggedAt() != null ? log.getLoggedAt().format(DTSHORT) : "—");
        lblDate.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");

        card.getChildren().addAll(imgView, lblWeight, lblDate);

        // Note if present
        if (log.getNote() != null && !log.getNote().isBlank()) {
            Label lblNote = new Label(log.getNote());
            lblNote.setStyle("-fx-font-size:11px;-fx-text-fill:#475569;-fx-wrap-text:true;");
            lblNote.setMaxWidth(160);
            lblNote.setWrapText(true);
            card.getChildren().add(lblNote);
        }

        // Click to enlarge
        card.setOnMouseClicked(e -> showPhotoFullscreen(photoFile, log));
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("#E2E8F0", "#16A34A")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#16A34A", "#E2E8F0")));

        return card;
    }

    private void showPhotoFullscreen(File f, WeightLog log) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("📸 " + log.getWeight() + " kg — " +
            (log.getLoggedAt() != null ? log.getLoggedAt().format(DTFULL) : ""));

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color:#0D1117;-fx-padding:24;");

        ImageView iv = new ImageView(new Image(f.toURI().toString()));
        iv.setFitWidth(480); iv.setFitHeight(520); iv.setPreserveRatio(true);

        Label info = new Label(String.format("⚖️ %.1f kg   •   📅 %s",
            log.getWeight(),
            log.getLoggedAt() != null ? log.getLoggedAt().format(DTFULL) : "—"));
        info.setStyle("-fx-font-size:14px;-fx-text-fill:white;-fx-font-weight:bold;");

        root.getChildren().addAll(iv, info);

        if (log.getNote() != null && !log.getNote().isBlank()) {
            Label noteLbl = new Label("📝 " + log.getNote());
            noteLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;");
            root.getChildren().add(noteLbl);
        }

        Button close = new Button("Close");
        close.setStyle("-fx-background-color:#16A34A;-fx-text-fill:white;-fx-font-size:13px;" +
            "-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-padding:8 28;");
        close.setOnAction(e -> popup.close());
        root.getChildren().add(close);

        popup.setScene(new Scene(root, 520, 640));
        popup.showAndWait();
    }

    private File getPhotoFile(String filename) {
        if (filename == null || filename.isBlank()) return null;
        File abs = Paths.get(System.getProperty("user.dir"), "uploads", "objectives", filename).toFile();
        if (abs.exists()) return abs;
        File rel = new File("uploads/objectives/" + filename);
        return rel.exists() ? rel : null;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCloseHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn)
            btn.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:20px;" +
                "-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:5 15;-fx-background-radius:5;");
    }

    @FXML
    private void handleCloseExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn)
            btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#64748B;-fx-font-size:20px;" +
                "-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:5 15;-fx-background-radius:5;");
    }
}
