package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.ChatMessage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.WeightLog;
import tn.esprit.projet.models.WeightObjective;
import tn.esprit.projet.repository.ChatMessageRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.repository.WeightRepository;
import tn.esprit.projet.services.BadgeService;
import tn.esprit.projet.utils.CoachNotification;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class WeightObjectiveController {

    @FXML private TextField  targetWeightField;
    @FXML private Label      lblCurrentWeight;
    @FXML private Label      lblObjectivePreview;
    @FXML private ImageView  startPhotoView;
    @FXML private Label      lblPhotoStatus;

    @FXML private VBox       progressSection;
    @FXML private Label      lblProgressTitle;
    @FXML private Label      lblProgressDetail;
    @FXML private ProgressBar progressBar;
    @FXML private Label      lblProgressPct;
    @FXML private Label      lblDaysLeft;
    @FXML private Label      lblMotivation;

    @FXML private HBox       photoGallery;
    @FXML private Label      lblGalleryEmpty;

    @FXML private TextField  logWeightField;
    @FXML private TextField  logNoteField;
    @FXML private ImageView  logPhotoPreview;
    @FXML private Label      lblLogPhotoStatus;

    @FXML private VBox       logList;
    @FXML private javafx.scene.chart.LineChart<String, Number>  weightLineChart;
    @FXML private javafx.scene.chart.CategoryAxis               chartXAxis;
    @FXML private javafx.scene.chart.NumberAxis                 chartYAxis;

    private File startPhotoFile;
    private File logPhotoFile;
    private int  selectedWeeks = 4;
    private static final DateTimeFormatter FMT       = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_SHORT = DateTimeFormatter.ofPattern("dd/MM");

    // In-memory cache — avoids stale DB reads after insert
    private List<WeightLog>    cachedLogs = new java.util.ArrayList<>();
    private WeightObjective    cachedObj  = null;

    private final WeightRepository weightRepo   = new WeightRepository();
    private final UserRepository   userRepo     = new UserRepository();
    private final BadgeService     badgeService = new BadgeService();
    private final ChatMessageRepository chatRepo = new ChatMessageRepository();

    @FXML private Button btnDur1W, btnDur2W, btnDur1M, btnDur3M, btnDur6M;
    @FXML private Button btnGalleryToggle;
    @FXML private Label  lblGalleryAccessStatus;

    // Chat fields
    @FXML private VBox       messagesSection;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox       messagesContainer;
    @FXML private Label      lblMessageCount;
    @FXML private Label      lblNoMessages;
    @FXML private HBox       userImagePreviewBox;
    @FXML private ImageView  userImgPreview;
    @FXML private Label      lblUserImageName;
    @FXML private TextArea   txtUserReply;
    @FXML private Button     btnUserSend;

    private File pendingUserImage;
    private static final String CHAT_DIR = "uploads/chat/";
    private static final DateTimeFormatter CHAT_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    @FXML
    public void initialize() {
        User u = Session.getCurrentUser();
        set(lblCurrentWeight, u.getWeight() > 0
                ? "Current weight: " + u.getWeight() + " kg"
                : "⚠️ Add your weight in your profile first");

        targetWeightField.textProperty().addListener((o, a, b) -> updatePreview());
        highlightDuration(4);

        // Init gallery access toggle
        boolean galleryEnabled = u.isGalleryAccessEnabled();
        updateGalleryAccessLabel(galleryEnabled);
        // Defer loading until after layout is complete
        Platform.runLater(() -> {
            loadAll();
        });
    }

    private void loadAll() {
        User u = Session.getCurrentUser();
        cachedObj  = weightRepo.findObjectiveByUser(u.getId());
        cachedLogs = weightRepo.findLogsByUser(u.getId());

        System.out.println("[loadAll] User: " + u.getId() + ", Logs: " + cachedLogs.size() + ", Objective: " + (cachedObj != null));
        for (WeightLog log : cachedLogs) {
            System.out.println("  - Log: " + log.getWeight() + " kg, Photo: " + log.getPhoto() + ", Date: " + log.getLoggedAt());
        }

        refreshUI();
    }

    private void refreshUI() {
        User u = Session.getCurrentUser();
        loadPersonalizedMessages(u);
        loadProgress(cachedObj, u);
        loadPhotoGallery(cachedLogs, cachedObj);
        drawWeightChart(cachedLogs, cachedObj);
        loadLogHistory(cachedLogs, cachedObj);
    }
    
    /**
     * Load full bidirectional chat between this user and their admin coach.
     */
    private void loadPersonalizedMessages(User u) {
        if (messagesSection == null || messagesContainer == null) return;
        messagesSection.setVisible(true);
        messagesSection.setManaged(true);
        chatRepo.ensureTableExists();
        new File(CHAT_DIR).mkdirs();

        messagesContainer.getChildren().clear();

        // Find the admin who last messaged this user (or any admin)
        int adminId = findAdminId(u.getId());

        List<ChatMessage> msgs = adminId > 0
                ? chatRepo.getConversation(adminId, u.getId())
                : List.of();

        if (msgs.isEmpty()) {
            if (lblNoMessages != null) { lblNoMessages.setVisible(true); lblNoMessages.setManaged(true); }
            if (lblMessageCount != null) { lblMessageCount.setVisible(false); lblMessageCount.setManaged(false); }
        } else {
            if (lblNoMessages != null) { lblNoMessages.setVisible(false); lblNoMessages.setManaged(false); }

            // Mark admin→user messages as read
            if (adminId > 0) chatRepo.markAsRead(adminId, u.getId());

            long unread = msgs.stream().filter(m -> m.isFromAdmin() && !m.isRead()).count();
            if (lblMessageCount != null) {
                if (unread > 0) {
                    lblMessageCount.setText(unread + " new");
                    lblMessageCount.setVisible(true); lblMessageCount.setManaged(true);
                    // Notification
                    Platform.runLater(() -> {
                        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> {
                            Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
                            if (stage != null) CoachNotification.show(stage, (int) unread);
                        });
                    });
                } else {
                    lblMessageCount.setVisible(false); lblMessageCount.setManaged(false);
                }
            }

            for (ChatMessage m : msgs) {
                messagesContainer.getChildren().add(buildUserChatBubble(m, u.getId()));
            }
        }

        // Scroll to bottom
        if (chatScrollPane != null)
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    /** Find the admin ID from existing chat messages for this user */
    private int findAdminId(int userId) {
        // Try to find from existing chat messages
        try {
            java.sql.Connection c = tn.esprit.projet.utils.DatabaseConnection.getInstance().getConnection();
            java.sql.PreparedStatement ps = c.prepareStatement(
                "SELECT sender_id FROM chat_messages WHERE receiver_id=? AND sender_type='ADMIN' " +
                "ORDER BY sent_at DESC LIMIT 1");
            ps.setInt(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            // Also check if user sent to admin
            ps = c.prepareStatement(
                "SELECT receiver_id FROM chat_messages WHERE sender_id=? AND sender_type='USER' " +
                "ORDER BY sent_at DESC LIMIT 1");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }

    /** Build a chat bubble for the front-office user view */
    private VBox buildUserChatBubble(ChatMessage m, int myId) {
        boolean isMe = (m.getSenderId() == myId); // user's own message

        VBox wrapper = new VBox(2);
        wrapper.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(8, 12, 7, 12));
        bubble.setMaxWidth(380);

        if (m.isDeleted()) {
            bubble.setStyle("-fx-background-color:#F1F5F9;-fx-background-radius:10;");
            Label del = new Label("🗑 Message deleted");
            del.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;-fx-font-style:italic;");
            bubble.getChildren().add(del);
        } else {
            if (isMe) {
                bubble.setStyle("-fx-background-color:#3B82F6;-fx-background-radius:12 12 2 12;");
            } else {
                bubble.setStyle("-fx-background-color:#EFF6FF;-fx-background-radius:12 12 12 2;" +
                        "-fx-border-color:#BFDBFE;-fx-border-radius:12 12 12 2;-fx-border-width:1;");
                Label from = new Label("👨‍⚕️ Coach");
                from.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#1D4ED8;");
                bubble.getChildren().add(from);
            }

            // Image
            if (m.hasImage()) {
                File f = new File(m.getImagePath());
                if (f.exists()) {
                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setFitWidth(200); iv.setFitHeight(160); iv.setPreserveRatio(true);
                    iv.setStyle("-fx-cursor:hand;");
                    iv.setOnMouseClicked(e -> showUserImageFullscreen(f));
                    bubble.getChildren().add(iv);
                }
            }

            // Text
            if (m.hasText()) {
                Label txt = new Label(m.getContent());
                txt.setWrapText(true);
                txt.setStyle("-fx-font-size:12px;-fx-text-fill:" +
                        (isMe ? "white;" : "#1E293B;") + "-fx-line-spacing:2;");
                bubble.getChildren().add(txt);
            }

            // Footer
            HBox footer = new HBox(5);
            footer.setAlignment(Pos.CENTER_LEFT);
            String timeStr = m.getSentAt() != null ? m.getSentAt().format(CHAT_FMT) : "";
            if (m.isEdited()) timeStr += " ✏";
            Label time = new Label(timeStr);
            time.setStyle("-fx-font-size:9px;-fx-text-fill:" + (isMe ? "#BFDBFE;" : "#94A3B8;"));
            footer.getChildren().add(time);
            bubble.getChildren().add(footer);

            // Edit/Delete for own messages
            if (isMe && !m.isDeleted()) {
                HBox acts = new HBox(5);
                acts.setAlignment(Pos.CENTER_RIGHT);
                Button ed = userSmallBtn("✏", "#3B82F6");
                ed.setOnAction(e -> handleUserEdit(m));
                Button dl = userSmallBtn("🗑", "#EF4444");
                dl.setOnAction(e -> handleUserDelete(m));
                acts.getChildren().addAll(ed, dl);
                bubble.getChildren().add(acts);
            }
        }

        wrapper.getChildren().add(bubble);
        return wrapper;
    }

    // ── User send reply ───────────────────────────────────────────────────────

    @FXML
    private void handleUserSend() {
        User u = Session.getCurrentUser();
        if (u == null) return;

        String text = txtUserReply != null ? txtUserReply.getText().trim() : "";
        boolean hasText  = !text.isBlank();
        boolean hasImage = pendingUserImage != null;

        if (!hasText && !hasImage) {
            Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
            Toasts.show(stage, "Type a message or attach an image.", Toasts.Type.WARNING);
            return;
        }

        int adminId = findAdminId(u.getId());
        if (adminId <= 0) {
            Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
            Toasts.show(stage, "No coach assigned yet. Wait for your coach to message you first.", Toasts.Type.INFO);
            return;
        }

        if (btnUserSend != null) btnUserSend.setDisable(true);

        // Save image
        String savedImg = null;
        if (hasImage) {
            savedImg = saveUserImage(pendingUserImage);
        }

        ChatMessage msg = new ChatMessage(
                u.getId(), adminId,
                ChatMessage.SenderType.USER,
                hasText ? text : null,
                savedImg, false);

        ChatMessage saved = chatRepo.save(msg);
        if (saved != null) {
            if (txtUserReply != null) txtUserReply.clear();
            clearUserImagePreview();
            loadPersonalizedMessages(u);
            Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
            Toasts.show(stage, "Reply sent ✓", Toasts.Type.SUCCESS);
        }

        if (btnUserSend != null) btnUserSend.setDisable(false);
    }

    @FXML
    private void handleUserAttachImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose an image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.webp"));
        Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
        File f = fc.showOpenDialog(stage);
        if (f == null) return;
        pendingUserImage = f;
        if (userImgPreview != null) userImgPreview.setImage(new Image(f.toURI().toString()));
        if (lblUserImageName != null) lblUserImageName.setText(f.getName());
        if (userImagePreviewBox != null) { userImagePreviewBox.setVisible(true); userImagePreviewBox.setManaged(true); }
    }

    @FXML
    private void handleUserRemoveImage() {
        clearUserImagePreview();
    }

    private void clearUserImagePreview() {
        pendingUserImage = null;
        if (userImgPreview != null) userImgPreview.setImage(null);
        if (lblUserImageName != null) lblUserImageName.setText("");
        if (userImagePreviewBox != null) { userImagePreviewBox.setVisible(false); userImagePreviewBox.setManaged(false); }
    }

    private void handleUserEdit(ChatMessage m) {
        String current = m.getContent() != null ? m.getContent() : "";
        TextInputDialog dlg = new TextInputDialog(current);
        dlg.setTitle("Edit message"); dlg.setHeaderText("Edit your message:"); dlg.setContentText(null);
        dlg.getEditor().setPrefWidth(360);
        dlg.showAndWait().ifPresent(newText -> {
            if (newText.isBlank()) return;
            if (chatRepo.edit(m.getId(), newText)) {
                loadPersonalizedMessages(Session.getCurrentUser());
                Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
                Toasts.show(stage, "Message updated ✓", Toasts.Type.SUCCESS);
            }
        });
    }

    private void handleUserDelete(ChatMessage m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this message?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES && chatRepo.delete(m.getId())) {
                loadPersonalizedMessages(Session.getCurrentUser());
                Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
                Toasts.show(stage, "Deleted.", Toasts.Type.INFO);
            }
        });
    }

    private void showUserImageFullscreen(File f) {
        Stage popup = new Stage();
        popup.setTitle("Image");
        VBox root = new VBox(12);
        root.setStyle("-fx-background-color:#0D1117;-fx-alignment:center;-fx-padding:20;");
        ImageView iv = new ImageView(new Image(f.toURI().toString()));
        iv.setFitWidth(600); iv.setFitHeight(500); iv.setPreserveRatio(true);
        Button close = new Button("Close");
        close.setStyle("-fx-background-color:#3B82F6;-fx-text-fill:white;-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 24;");
        close.setOnAction(e -> popup.close());
        root.getChildren().addAll(iv, close);
        popup.setScene(new javafx.scene.Scene(root, 640, 560));
        popup.show();
    }

    private Button userSmallBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color +
                ";-fx-font-size:10px;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 7;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:" + color +
                ";-fx-text-fill:white;-fx-font-size:10px;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 7;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color +
                ";-fx-font-size:10px;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 7;"));
        return b;
    }

    private String saveUserImage(File src) {
        try {
            new File(CHAT_DIR).mkdirs();
            String ext = src.getName().contains(".")
                    ? src.getName().substring(src.getName().lastIndexOf('.')) : ".jpg";
            String name = UUID.randomUUID() + ext;
            Path dest = Paths.get(CHAT_DIR + name);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return CHAT_DIR + name;
        } catch (IOException e) {
            System.err.println("[Chat] saveUserImage: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void drawWeightChart(List<WeightLog> logs, WeightObjective obj) {
        if (weightLineChart == null) return;

        weightLineChart.getData().clear();

        // Style the chart container
        weightLineChart.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-plot-background-color:#F8FAFC;" +
            "-fx-horizontal-grid-lines-visible:true;" +
            "-fx-vertical-grid-lines-visible:false;"
        );
        weightLineChart.setCreateSymbols(true);
        weightLineChart.setLegendVisible(logs.size() > 0 && obj != null);

        if (logs.isEmpty()) return;

        // ── Weight series ──────────────────────────────────────────────────────
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Weight (kg)");

        for (WeightLog log : logs) {
            String label = log.getLoggedAt() != null
                    ? log.getLoggedAt().format(FMT_SHORT)
                    : "?";
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(label, log.getWeight()));
        }
        weightLineChart.getData().add(series);

        // ── Target line ────────────────────────────────────────────────────────
        if (obj != null && logs.size() >= 1) {
            javafx.scene.chart.XYChart.Series<String, Number> targetSeries = new javafx.scene.chart.XYChart.Series<>();
            targetSeries.setName("Target: " + obj.getTargetWeight() + " kg");
            for (WeightLog log : logs) {
                String label = log.getLoggedAt() != null ? log.getLoggedAt().format(FMT_SHORT) : "?";
                targetSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(label, obj.getTargetWeight()));
            }
            weightLineChart.getData().add(targetSeries);

            Platform.runLater(() -> {
                if (targetSeries.getNode() != null)
                    targetSeries.getNode().setStyle(
                        "-fx-stroke:#EF4444;-fx-stroke-width:2px;-fx-stroke-dash-array:8 5;");
                for (javafx.scene.chart.XYChart.Data<String, Number> d : targetSeries.getData())
                    if (d.getNode() != null) d.getNode().setVisible(false);
            });
        }

        // ── Style weight line and dots ─────────────────────────────────────────
        Platform.runLater(() -> {
            if (series.getNode() != null)
                series.getNode().setStyle(
                    "-fx-stroke:#2E7D32;-fx-stroke-width:3px;");

            for (javafx.scene.chart.XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle(
                        "-fx-background-color:#2E7D32,white;" +
                        "-fx-background-insets:0,2.5;" +
                        "-fx-background-radius:6px;" +
                        "-fx-padding:6px;");
                    // Tooltip
                    javafx.scene.control.Tooltip tip = new javafx.scene.control.Tooltip(
                            d.getXValue() + "\n" + d.getYValue() + " kg");
                    tip.setStyle("-fx-font-size:12px;-fx-font-weight:bold;");
                    javafx.scene.control.Tooltip.install(d.getNode(), tip);
                }
            }

            // Style axes
            if (chartXAxis != null)
                chartXAxis.setStyle("-fx-tick-label-font-size:10px;-fx-text-fill:#64748B;");
            if (chartYAxis != null)
                chartYAxis.setStyle("-fx-tick-label-font-size:10px;-fx-text-fill:#64748B;");
        });
    }

    private void loadProgress(WeightObjective obj, User u) {
        if (obj == null) {
            if (progressSection != null) { progressSection.setVisible(false); progressSection.setManaged(false); }
            return;
        }
        if (progressSection != null) { progressSection.setVisible(true); progressSection.setManaged(true); }

        double current  = u.getWeight();
        double progress = obj.getProgress(current);
        double kgDone   = obj.getKgLost(current);
        long   daysLeft = obj.getDaysRemaining();

        set(lblProgressTitle, obj.isLossGoal()
                ? String.format("🎯 Lose %.1f kg before %s", obj.getTotalKg(), obj.getTargetDate().format(FMT))
                : String.format("🎯 Gain %.1f kg before %s", Math.abs(obj.getTotalKg()), obj.getTargetDate().format(FMT)));

        set(lblProgressDetail, String.format(
                "%.1f kg → %.1f kg  •  Start: %s",
                obj.getStartWeight(), obj.getTargetWeight(), obj.getStartDate().format(FMT)));

        if (progressBar != null) {
            progressBar.setProgress(progress);
            String color = progress >= 1.0 ? "#16A34A" : progress >= 0.5 ? "#2E7D32" : "#D97706";
            progressBar.setStyle("-fx-accent:" + color + ";-fx-background-color:#E8F5E9;-fx-background-radius:6;");
        }

        set(lblProgressPct, String.format("%.0f%% complete  •  %.1f kg %s of %.1f kg",
                progress * 100, Math.abs(kgDone),
                obj.isLossGoal() ? "lost" : "gained", Math.abs(obj.getTotalKg())));

        if (daysLeft > 0) {
            set(lblDaysLeft, "⏰ " + daysLeft + " days left");
        } else if (daysLeft == 0) {
            set(lblDaysLeft, "🎉 Today is the day!");
        } else {
            set(lblDaysLeft, "⚠️ Deadline passed by " + Math.abs(daysLeft) + " days");
        }

        set(lblMotivation, getMotivation(progress, kgDone, obj, daysLeft));
    }

    private String getMotivation(double progress, double kgDone, WeightObjective obj, long daysLeft) {
        if (progress >= 1.0)  return "🏆 GOAL ACHIEVED! You're amazing! Set a new goal!";
        if (progress >= 0.75) return "🔥 Almost there! " + String.format("%.1f%%", progress * 100) + " complete — keep going!";
        if (progress >= 0.5)  return "💪 Halfway! " + String.format("%.1f", Math.abs(kgDone)) + " kg " + (obj.isLossGoal() ? "lost" : "gained") + " — you're on track!";
        if (progress >= 0.25) return "⭐ Good start! Every gram counts. Keep it up!";
        if (kgDone > 0)       return "🌱 You've started! " + String.format("%.1f", Math.abs(kgDone)) + " kg progress. Great!";
        if (daysLeft < 7)     return "⚡ Only " + daysLeft + " days left! Give it your all!";
        return "💡 Log your weight regularly with a photo to see your transformation!";
    }

    private void loadPhotoGallery(List<WeightLog> logs, WeightObjective obj) {
        if (photoGallery == null) return;
        photoGallery.getChildren().clear();

        List<WeightLog> withPhotos = logs.stream()
                .filter(l -> l.getPhoto() != null && !l.getPhoto().isBlank())
                .toList();

        if (withPhotos.isEmpty()) {
            if (lblGalleryEmpty != null) { lblGalleryEmpty.setVisible(true); lblGalleryEmpty.setManaged(true); }
            return;
        }
        if (lblGalleryEmpty != null) { lblGalleryEmpty.setVisible(false); lblGalleryEmpty.setManaged(false); }

        for (WeightLog log : withPhotos) {
            File f = getPhotoFile(log.getPhoto());
            if (f == null) {
                System.out.println("[Photo] Not found: " + log.getPhoto());
                continue;
            }            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:8;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,2);" +
                    "-fx-border-color:#E8F5E9;-fx-border-radius:12;-fx-border-width:1;");
            card.setPrefWidth(130);

            ImageView iv = new ImageView(new Image(f.toURI().toString()));
            iv.setFitWidth(114); iv.setFitHeight(140); iv.setPreserveRatio(false);
            iv.setStyle("-fx-background-radius:8;");

            Label weightLbl = new Label(log.getWeight() + " kg");
            weightLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");

            Label dateLbl = new Label(log.getLoggedAt() != null ? log.getLoggedAt().format(FMT_SHORT) : "");
            dateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");

            if (obj != null) {
                double diff = log.getWeight() - obj.getStartWeight();
                String diffStr = diff == 0 ? "Start" : (diff < 0 ? String.format("%.1f kg", diff) : String.format("+%.1f kg", diff));
                String diffColor = diff < 0 ? "#16A34A" : diff > 0 ? "#EF4444" : "#64748B";
                Label diffLbl = new Label(diffStr);
                diffLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + diffColor + ";");
                card.getChildren().addAll(iv, weightLbl, diffLbl, dateLbl);
            } else {
                card.getChildren().addAll(iv, weightLbl, dateLbl);
            }

            card.setOnMouseClicked(e -> showPhotoFullscreen(f, log, cachedObj));
            card.setStyle(card.getStyle() + "-fx-cursor:hand;");

            // Delete button
            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#DC2626;-fx-font-size:11px;" +
                    "-fx-background-radius:6;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 6;");
            delBtn.setOnAction(e -> {
                e.consume();
                deleteLog(log);
            });
            card.getChildren().add(delBtn);

            photoGallery.getChildren().add(card);
        }
    }

    private void showPhotoFullscreen(File f, WeightLog log, WeightObjective obj) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Photo — " + log.getWeight() + " kg");

        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color:#0D1117;-fx-padding:20;");

        ImageView iv = new ImageView(new Image(f.toURI().toString()));
        iv.setFitWidth(400); iv.setFitHeight(500); iv.setPreserveRatio(true);

        Label info = new Label(log.getWeight() + " kg  •  " +
                (log.getLoggedAt() != null ? log.getLoggedAt().format(FMT) : "") +
                (log.getNote() != null && !log.getNote().isBlank() ? "  •  " + log.getNote() : ""));
        info.setStyle("-fx-font-size:13px;-fx-text-fill:white;-fx-font-weight:bold;");

        if (obj != null) {
            double diff = log.getWeight() - obj.getStartWeight();
            String diffStr = diff == 0 ? "Start photo" : (diff < 0 ? String.format("%.1f kg lost", Math.abs(diff)) : String.format("+%.1f kg gained", diff));
            String diffColor = diff < 0 ? "#00FF88" : diff > 0 ? "#FF6B6B" : "#94A3B8";
            Label diffLbl = new Label(diffStr);
            diffLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + diffColor + ";");
            root.getChildren().addAll(iv, info, diffLbl);
        } else {
            root.getChildren().addAll(iv, info);
        }

        Button close = new Button("Close");
        close.setStyle("-fx-background-color:#2E7D32;-fx-text-fill:white;-fx-font-size:13px;" +
                "-fx-background-radius:10;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:8 24;");
        close.setOnAction(e -> popup.close());
        root.getChildren().add(close);

        popup.setScene(new Scene(root, 440, 600));
        popup.showAndWait();
    }

    private void loadLogHistory(List<WeightLog> logs, WeightObjective obj) {
        if (logList == null) return;
        logList.getChildren().clear();

        if (logs.isEmpty()) {
            Label empty = new Label("No entries. Log your first weight!");
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-font-style:italic;");
            logList.getChildren().add(empty);
            return;
        }

        // Reverse for display (newest first)
        List<WeightLog> reversed = new java.util.ArrayList<>(logs);
        java.util.Collections.reverse(reversed);

        for (WeightLog log : reversed) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-padding:10 14;" +
                    "-fx-border-color:#F1F5F9;-fx-border-radius:10;-fx-border-width:1;");

            Label dateLbl = new Label(log.getLoggedAt() != null ? log.getLoggedAt().format(FMT) : "—");
            dateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;-fx-min-width:75;");

            Label weightLbl = new Label(log.getWeight() + " kg");
            weightLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");

            if (obj != null) {
                double diff = log.getWeight() - obj.getStartWeight();
                String diffStr = diff == 0 ? "Start" : (diff < 0 ? String.format("%.1f", diff) : String.format("+%.1f", diff)) + " kg";
                String diffColor = diff < 0 ? "#16A34A" : diff > 0 ? "#EF4444" : "#64748B";
                Label diffLbl = new Label(diffStr);
                diffLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + diffColor + ";");
                row.getChildren().add(diffLbl);
            }

            if (log.getNote() != null && !log.getNote().isBlank()) {
                Label noteLbl = new Label(log.getNote());
                noteLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
                HBox.setHgrow(noteLbl, Priority.ALWAYS);
                row.getChildren().addAll(dateLbl, weightLbl, noteLbl);
            } else {
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(dateLbl, weightLbl, sp);
            }

            if (log.getPhoto() != null && !log.getPhoto().isBlank()) {
                File f = getPhotoFile(log.getPhoto());
                if (f != null) {
                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setFitWidth(36); iv.setFitHeight(36); iv.setPreserveRatio(true);
                    Circle clip = new Circle(18, 18, 18);
                    iv.setClip(clip);
                    row.getChildren().add(iv);
                }
            }

            // Delete button
            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#DC2626;-fx-font-size:11px;" +
                    "-fx-background-radius:6;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:4 8;");
            delBtn.setOnAction(e -> deleteLog(log));
            row.getChildren().add(delBtn);

            logList.getChildren().add(row);
        }
    }

    private void deleteLog(WeightLog log) {
        // Confirm
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setResizable(false);
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color:white;-fx-padding:28;-fx-background-radius:14;");
        Label title = new Label("🗑  Delete this entry?");
        title.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        Label msg = new Label(log.getWeight() + " kg — " +
                (log.getLoggedAt() != null ? log.getLoggedAt().format(FMT) : ""));
        msg.setStyle("-fx-font-size:13px;-fx-text-fill:#64748B;");
        HBox btns = new HBox(12);
        btns.setAlignment(Pos.CENTER);
        Button yes = new Button("Delete");
        yes.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:13px;" +
                "-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 20;");
        Button no = new Button("Cancel");
        no.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#374151;-fx-font-size:13px;" +
                "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 20;");
        yes.setOnAction(e -> {
            weightRepo.deleteLog(log.getId());
            cachedLogs.remove(log);
            popup.close();
            refreshUI();
        });
        no.setOnAction(e -> popup.close());
        btns.getChildren().addAll(yes, no);
        root.getChildren().addAll(title, msg, btns);
        popup.setScene(new javafx.scene.Scene(root, 320, 160));
        popup.showAndWait();
    }

    @FXML private void handleDuration1W()  { selectedWeeks = 1;  highlightDuration(1);  updatePreview(); }
    @FXML private void handleDuration2W()  { selectedWeeks = 2;  highlightDuration(2);  updatePreview(); }
    @FXML private void handleDuration1M()  { selectedWeeks = 4;  highlightDuration(4);  updatePreview(); }
    @FXML private void handleDuration3M()  { selectedWeeks = 12; highlightDuration(12); updatePreview(); }
    @FXML private void handleDuration6M()  { selectedWeeks = 24; highlightDuration(24); updatePreview(); }

    private void highlightDuration(int weeks) {
        String active   = "-fx-background-color:#2E7D32;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:8 14;";
        String inactive = "-fx-background-color:#F0F7F0;-fx-text-fill:#2E7D32;-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-border-color:#C8E6C9;-fx-border-width:1;-fx-padding:8 14;";
        if (btnDur1W != null) btnDur1W.setStyle(weeks == 1  ? active : inactive);
        if (btnDur2W != null) btnDur2W.setStyle(weeks == 2  ? active : inactive);
        if (btnDur1M != null) btnDur1M.setStyle(weeks == 4  ? active : inactive);
        if (btnDur3M != null) btnDur3M.setStyle(weeks == 12 ? active : inactive);
        if (btnDur6M != null) btnDur6M.setStyle(weeks == 24 ? active : inactive);
    }

    private void updatePreview() {
        try {
            double target  = Double.parseDouble(targetWeightField.getText().trim());
            double current = Session.getCurrentUser().getWeight();
            if (current <= 0) return;

            double diff    = current - target;
            double perWeek = selectedWeeks > 0 ? Math.abs(diff) / selectedWeeks : 0;

            String action  = diff > 0 ? "lose" : "gain";
            String color   = diff > 0 ? "#16A34A" : "#2563EB";
            String warning = perWeek > 1.0 ? "  ⚠️ High pace" : perWeek > 0.5 ? "  ✅ Healthy pace" : "  ✅ Gradual";

            set(lblObjectivePreview, String.format(
                "→ %s %.1f kg in %d weeks  •  %.2f kg/week%s",
                action, Math.abs(diff), selectedWeeks, perWeek, warning));
            if (lblObjectivePreview != null)
                lblObjectivePreview.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        } catch (Exception ignored) {
            set(lblObjectivePreview, "Enter your target weight");
        }
    }

    @FXML
    private void handleChooseStartPhoto() {
        File f = choosePhoto();
        if (f == null) return;
        startPhotoFile = f;
        if (startPhotoView != null) startPhotoView.setImage(new Image(f.toURI().toString()));
        set(lblPhotoStatus, "✅ " + f.getName());
        if (lblPhotoStatus != null) lblPhotoStatus.setStyle("-fx-font-size:10px;-fx-text-fill:#16A34A;");
    }

    @FXML
    private void handleSetObjective() {
        User u = Session.getCurrentUser();
        if (u.getWeight() <= 0) { showAlert("Add your weight in your profile first."); return; }

        String targetStr = targetWeightField.getText().trim();
        if (targetStr.isEmpty()) { showAlert("Enter a target weight."); return; }

        double target;
        try { target = Double.parseDouble(targetStr); }
        catch (NumberFormatException e) { showAlert("Invalid weight."); return; }
        if (target < 20 || target > 500) { showAlert("Weight must be between 20 and 500 kg."); return; }
        if (target == u.getWeight()) { showAlert("Target weight must be different from your current weight."); return; }

        LocalDate targetDate = LocalDate.now().plusWeeks(selectedWeeks);

        double diff    = Math.abs(u.getWeight() - target);
        double perWeek = diff / selectedWeeks;
        if (perWeek > 2.0) {
            showAlert(String.format(
                "⚠️ This pace (%.1f kg/week) is too fast and dangerous.\n\n" +
                "Recommendation: max 0.5–1 kg per week.\n" +
                "Choose a longer duration or less ambitious goal.", perWeek));
            return;
        }

        WeightObjective obj = new WeightObjective();
        obj.setUserId(u.getId());
        obj.setStartWeight(u.getWeight());
        obj.setTargetWeight(target);
        obj.setStartDate(LocalDate.now());
        obj.setTargetDate(targetDate);

        if (startPhotoFile != null) {
            String fn = savePhoto(startPhotoFile);
            if (fn != null) obj.setStartPhoto(fn);
        }

        try {
            weightRepo.saveObjective(obj);
        } catch (RuntimeException ex) {
            showAlert("❌ Database error saving goal:\n" + ex.getMessage() + "\n\nCheck that MySQL is running and the database structure is correct.");
            return;
        }
        cachedObj = obj;

        WeightLog startLog = new WeightLog();
        startLog.setUserId(u.getId());
        startLog.setWeight(u.getWeight());
        startLog.setPhoto(obj.getStartPhoto());
        startLog.setNote("📸 Start photo — goal launched!");
        startLog.setLoggedAt(java.time.LocalDateTime.now());
        try {
            weightRepo.saveLog(startLog);
        } catch (RuntimeException ex) {
            showAlert("❌ Database error saving start log:\n" + ex.getMessage());
            return;
        }
        cachedLogs.add(startLog);

        Stage stage = (Stage) targetWeightField.getScene().getWindow();
        Toasts.show(stage, String.format("🎯 Goal launched! %.1f kg in %d weeks. Good luck!", diff, selectedWeeks), Toasts.Type.SUCCESS);
        WeeklyChallengesController.notifyEvent(u.getId(), "objective", stage);
        refreshUI();
    }

    @FXML
    private void handleChooseLogPhoto() {
        File f = choosePhoto();
        if (f == null) return;
        logPhotoFile = f;
        if (logPhotoPreview != null) logPhotoPreview.setImage(new Image(f.toURI().toString()));
        set(lblLogPhotoStatus, "✅ " + f.getName());
        if (lblLogPhotoStatus != null) lblLogPhotoStatus.setStyle("-fx-font-size:10px;-fx-text-fill:#16A34A;");
    }

    @FXML
    private void handleLogWeight() {
        try {
            System.out.println("[handleLogWeight] START");
            User u = Session.getCurrentUser();

            String weightStr = logWeightField.getText().trim();
            if (weightStr.isEmpty()) { showAlert("Enter your weight."); return; }

            double weight;
            try { weight = Double.parseDouble(weightStr); }
            catch (NumberFormatException e) { showAlert("Invalid weight."); return; }
            if (weight < 20 || weight > 500) { showAlert("Weight must be between 20 and 500 kg."); return; }

            // Save photo
            String photoFn = logPhotoFile != null ? savePhoto(logPhotoFile) : null;
            System.out.println("[handleLogWeight] photoFn=" + photoFn);

            // Build log object
            WeightLog log = new WeightLog();
            log.setUserId(u.getId());
            log.setWeight(weight);
            log.setPhoto(photoFn);
            log.setNote(logNoteField != null ? logNoteField.getText().trim() : "");
            log.setLoggedAt(java.time.LocalDateTime.now());

            // Save to DB
            try {
                weightRepo.saveLog(log);
            } catch (RuntimeException ex) {
                showAlert("❌ Database error:\n" + ex.getMessage() + "\n\nCheck that MySQL is running.");
                return;
            }
            System.out.println("[handleLogWeight] saved log id=" + log.getId());

            // Add to in-memory cache immediately — no DB re-read needed
            cachedLogs.add(log);

            // Update user weight
            u.setWeight(weight);
            userRepo.update(u);
            Session.login(u);

            // Show toast
            Stage stage = (Stage) logWeightField.getScene().getWindow();
            Toasts.show(stage, "✅ " + weight + " kg saved!", Toasts.Type.SUCCESS);

            // Notify weekly challenges
            WeeklyChallengesController.notifyEvent(u.getId(), "weight_log", stage);
            if (photoFn != null)
                WeeklyChallengesController.notifyEvent(u.getId(), "weight_photo", stage);

            // Reset form
            logWeightField.clear();
            if (logNoteField != null) logNoteField.clear();
            logPhotoFile = null;
            if (logPhotoPreview != null) logPhotoPreview.setImage(null);
            set(lblLogPhotoStatus, "");
            set(lblCurrentWeight, "Current weight: " + weight + " kg");

            // Refresh UI from cache — instant, no DB read
            System.out.println("[handleLogWeight] Refreshing UI with " + cachedLogs.size() + " logs");
            refreshUI();

            // Badge notifications in background
            new Thread(() -> {
                List<String> newBadges = badgeService.refreshBadges(u);
                if (!newBadges.isEmpty()) {
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        for (String b : newBadges)
                            Toasts.show(stage, "🏆 Badge unlocked: " + b, Toasts.Type.SUCCESS);
                    });
                }
                if (cachedObj != null) {
                    Platform.runLater(() -> checkMilestone(cachedObj.getProgress(weight), cachedObj.getKgLost(weight), cachedObj, stage));
                }
            }).start();

            System.out.println("[handleLogWeight] DONE");
        } catch (Exception e) {
            System.err.println("[handleLogWeight] ERROR: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    private void checkMilestone(double progress, double kgDone, WeightObjective obj, Stage owner) {
        String emoji = null, title = null, msg = null;
        if (progress >= 1.0 && progress < 1.05) {
            emoji = "🏆"; title = "GOAL ACHIEVED!";
            msg = "Congratulations! You've " + (obj.isLossGoal() ? "lost " : "gained ")
                + String.format("%.1f kg", Math.abs(obj.getTotalKg())) + "!\nYou're amazing! 💪";
        } else if (progress >= 0.75 && progress < 0.80) {
            emoji = "🔥"; title = "75% complete!";
            msg = "You've " + String.format("%.1f kg", Math.abs(kgDone))
                + (obj.isLossGoal() ? " lost" : " gained") + " of "
                + String.format("%.1f", Math.abs(obj.getTotalKg())) + " kg.\nThe finish line is close!";
        } else if (progress >= 0.5 && progress < 0.55) {
            emoji = "⭐"; title = "Halfway there!";
            msg = "You're at 50% of your goal.\nKeep going, you can do it!";
        } else if (progress >= 0.25 && progress < 0.30) {
            emoji = "💪"; title = "25% complete!";
            msg = "Good start! Every gram counts.\nStay motivated!";
        }
        if (emoji != null) {
            final String fe = emoji, ft = title, fm = msg;
            new Thread(() -> {
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> showMilestoneDialog(fe, ft, fm));
            }).start();
        }
    }

    private File choosePhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a photo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        Stage stage = (Stage) (targetWeightField != null ? targetWeightField : logWeightField).getScene().getWindow();
        File f = fc.showOpenDialog(stage);
        if (f == null) return null;
        if (f.length() > 5L * 1024 * 1024) { showAlert("Photo too large (max 5MB)."); return null; }
        return f;
    }

    private String savePhoto(File src) {
        try {
            Path dir = Paths.get(System.getProperty("user.dir"), "uploads", "objectives");
            Files.createDirectories(dir);
            String ext = src.getName().substring(src.getName().lastIndexOf('.'));
            String fn = UUID.randomUUID() + ext;
            Files.copy(src.toPath(), dir.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Photo] Saved to: " + dir.resolve(fn));
            return fn;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private File getPhotoFile(String filename) {
        if (filename == null || filename.isBlank()) return null;
        File abs = Paths.get(System.getProperty("user.dir"), "uploads", "objectives", filename).toFile();
        if (abs.exists()) {
            System.out.println("[Photo] Found: " + abs.getAbsolutePath());
            return abs;
        }
        File rel = new File("uploads/objectives/" + filename);
        if (rel.exists()) return rel;
        System.out.println("[Photo] Not found: " + filename);
        return null;
    }

    private void showBeautifulDialog(String emoji, String title, String message, String color, String bgColor) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:white;-fx-background-radius:16;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:" + bgColor + ";-fx-padding:18 24;-fx-background-radius:16 16 0 0;");
        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size:28px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        header.getChildren().addAll(emojiLbl, titleLbl);

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(360);
        msgLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-padding:16 24;");

        Button btn = new Button("Got it!");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-size:13px;" +
                "-fx-font-weight:bold;-fx-background-radius:0 0 16 16;-fx-cursor:hand;" +
                "-fx-border-color:transparent;-fx-padding:14;");
        btn.setOnAction(e -> popup.close());

        root.getChildren().addAll(header, msgLbl, btn);
        popup.setScene(new Scene(root, 400, 200));
        popup.showAndWait();
    }

    @FXML
    private void handleGalleryAccessToggle() {
        User u = Session.getCurrentUser();
        boolean newState = !u.isGalleryAccessEnabled(); // flip
        userRepo.setGalleryAccess(u.getId(), newState);
        u.setGalleryAccessEnabled(newState);
        Session.login(u);
        updateGalleryAccessLabel(newState);
    }

    private void updateGalleryAccessLabel(boolean enabled) {
        if (lblGalleryAccessStatus != null) {
            if (enabled) {
                lblGalleryAccessStatus.setText("PUBLIC — admin can view your photos");
                lblGalleryAccessStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#16A34A;-fx-font-weight:bold;");
            } else {
                lblGalleryAccessStatus.setText("PRIVATE — only you can see your photos");
                lblGalleryAccessStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
            }
        }
        if (btnGalleryToggle != null) {
            if (enabled) {
                btnGalleryToggle.setText("PUBLIC");
                btnGalleryToggle.setStyle(
                    "-fx-background-color:#16A34A;-fx-text-fill:white;" +
                    "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;" +
                    "-fx-cursor:hand;-fx-border-color:#15803D;-fx-border-radius:6;" +
                    "-fx-border-width:1;-fx-padding:7 16;");
            } else {
                btnGalleryToggle.setText("PRIVATE");
                btnGalleryToggle.setStyle(
                    "-fx-background-color:#E2E8F0;-fx-text-fill:#475569;" +
                    "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;" +
                    "-fx-cursor:hand;-fx-border-color:#CBD5E1;-fx-border-radius:6;" +
                    "-fx-border-width:1;-fx-padding:7 16;");
            }
        }
    }

    private void showAlert(String msg) {
        showBeautifulDialog("⚠️", "Warning", msg, "#D97706", "#FFFBEB");
    }
    private void showSuccess(String title, String msg) {
        showBeautifulDialog("✅", title, msg, "#16A34A", "#F0FDF4");
    }

    private void showMilestoneDialog(String emoji, String title, String msg) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-padding:32 28;");

        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size:52px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1a2e1a;");

        Label msgLbl = new Label(msg);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(340);
        msgLbl.setAlignment(Pos.CENTER);
        msgLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-text-alignment:center;-fx-alignment:center;");

        Button btn = new Button("💪  Continue!");
        btn.setStyle("-fx-background-color:#2E7D32;-fx-text-fill:white;-fx-font-size:14px;" +
                "-fx-font-weight:bold;-fx-background-radius:12;-fx-cursor:hand;" +
                "-fx-border-color:transparent;-fx-padding:12 32;" +
                "-fx-effect:dropshadow(gaussian,rgba(46,125,50,0.35),10,0,0,3);");
        btn.setOnAction(e -> popup.close());

        root.getChildren().addAll(emojiLbl, titleLbl, msgLbl, btn);
        popup.setScene(new Scene(root, 400, 280));
        popup.showAndWait();
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val != null ? val : ""); }

    @FXML private void handleClose() {
        Stage stage = (Stage) lblCurrentWeight.getScene().getWindow();
        stage.close();
    }

    @FXML private void handleCloseHover(javafx.scene.input.MouseEvent e) {
        ((javafx.scene.control.Button) e.getSource()).setStyle(
            "-fx-background-color:transparent;-fx-text-fill:#EF4444;-fx-font-size:16px;-fx-font-weight:bold;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:0 0 0 16;");
    }

    @FXML private void handleCloseUnhover(javafx.scene.input.MouseEvent e) {
        ((javafx.scene.control.Button) e.getSource()).setStyle(
            "-fx-background-color:transparent;-fx-text-fill:#94A3B8;-fx-font-size:16px;-fx-font-weight:bold;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:0 0 0 16;");
    }
}
