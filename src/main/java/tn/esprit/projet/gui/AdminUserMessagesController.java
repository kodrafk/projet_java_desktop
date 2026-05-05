package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.projet.models.ChatMessage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.ChatMessageRepository;
import tn.esprit.projet.services.TwilioService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Backoffice chat — admin ↔ user bidirectional.
 * Features: text, images, edit, delete, SMS via Twilio.
 */
public class AdminUserMessagesController {

    @FXML private Label      lblAvatar;
    @FXML private Label      lblUserName;
    @FXML private Label      lblUserEmail;
    @FXML private Label      lblUserPhone;
    @FXML private Label      lblUnread;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox       messagesContainer;
    @FXML private HBox       imagePreviewBox;
    @FXML private ImageView  imgPreview;
    @FXML private Label      lblImageName;
    @FXML private TextArea   txtMessage;
    @FXML private Label      lblCharCount;
    @FXML private Label      lblSmsStatus;
    @FXML private CheckBox   chkSendSms;
    @FXML private Button     btnSend;

    private User   targetUser;
    private File   pendingImageFile;

    private final ChatMessageRepository repo   = new ChatMessageRepository();
    private final TwilioService         twilio = TwilioService.getInstance();

    private static final int MAX_CHARS = 500;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final String CHAT_DIR = "uploads/chat/";

    // ── Init ──────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        repo.ensureTableExists();
        new File(CHAT_DIR).mkdirs();

        txtMessage.textProperty().addListener((obs, old, val) -> {
            int len = val != null ? val.length() : 0;
            lblCharCount.setText(len + " / " + MAX_CHARS);
            lblCharCount.setStyle("-fx-font-size:10px;-fx-text-fill:" +
                    (len > MAX_CHARS ? "#DC2626;-fx-font-weight:bold;" : "#94A3B8;"));
        });

        chkSendSms.selectedProperty().addListener((obs, old, val) -> refreshSmsLabel());

        // Send on Ctrl+Enter
        txtMessage.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && e.isControlDown()) handleSend();
        });
    }

    public void setUser(User u) {
        this.targetUser = u;

        // Guard: no messaging between admins
        if (u.isAdmin()) {
            lblUserName.setText("⛔ Cannot message an admin");
            lblUserEmail.setText(u.getEmail());
            txtMessage.setDisable(true);
            chkSendSms.setDisable(true);
            btnSend.setDisable(true);
            btnSend.setStyle("-fx-background-color:#94A3B8;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-border-color:transparent;");
            showBanner("⛔", "Admin accounts cannot receive messages",
                    "Messages can only be sent to regular users.");
            return;
        }

        // Header
        String initials = u.getFirstName() != null && !u.getFirstName().isEmpty()
                ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "U";
        lblAvatar.setText(initials);
        lblUserName.setText(u.getFullName());
        lblUserEmail.setText(u.getEmail());

        String phone = u.getPhone();
        if (phone != null && !phone.isBlank()) {
            lblUserPhone.setText("📱 " + phone);
        } else {
            lblUserPhone.setText("📱 No phone");
            lblUserPhone.setStyle("-fx-font-size:11px;-fx-text-fill:#EF4444;");
        }

        refreshSmsLabel();
        loadConversation();
    }

    // ── Load conversation ─────────────────────────────────────────────────────

    private void loadConversation() {
        messagesContainer.getChildren().clear();

        User admin = Session.getCurrentUser();
        if (admin == null) return;

        List<ChatMessage> msgs = repo.getConversation(admin.getId(), targetUser.getId());

        if (msgs.isEmpty()) {
            showBanner("💬", "No messages yet",
                    "Start the conversation below.\nThe user will see your messages in their app.");
        } else {
            // Mark user→admin messages as read
            repo.markAsRead(targetUser.getId(), admin.getId());

            for (ChatMessage m : msgs) {
                messagesContainer.getChildren().add(buildBubble(m, admin.getId()));
            }

            // Unread badge
            long unread = msgs.stream()
                    .filter(m -> m.isFromAdmin() && !m.isRead()).count();
            if (unread > 0) {
                lblUnread.setText(unread + " unread");
                lblUnread.setVisible(true);
                lblUnread.setManaged(true);
            }
        }

        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleSend() {
        String text = txtMessage.getText().trim();
        boolean hasText  = !text.isBlank();
        boolean hasImage = pendingImageFile != null;

        if (!hasText && !hasImage) {
            toast("Type a message or attach an image.", Toasts.Type.WARNING); return;
        }
        if (hasText && text.length() > MAX_CHARS) {
            toast("Message too long (max " + MAX_CHARS + " chars).", Toasts.Type.WARNING); return;
        }
        if (targetUser == null || targetUser.isAdmin()) {
            toast("Cannot send to admin accounts.", Toasts.Type.ERROR); return;
        }

        User admin = Session.getCurrentUser();
        if (admin == null) { toast("No session.", Toasts.Type.ERROR); return; }

        btnSend.setDisable(true);

        // ── Save image ────────────────────────────────────────────────────────
        String savedImagePath = null;
        if (hasImage) {
            savedImagePath = saveImage(pendingImageFile);
            if (savedImagePath == null) {
                toast("Failed to save image.", Toasts.Type.ERROR);
                btnSend.setDisable(false);
                return;
            }
        }

        boolean sendSms = chkSendSms.isSelected();

        ChatMessage msg = new ChatMessage(
                admin.getId(), targetUser.getId(),
                ChatMessage.SenderType.ADMIN,
                hasText ? text : null,
                savedImagePath,
                sendSms);

        ChatMessage saved = repo.save(msg);
        if (saved == null) {
            toast("Failed to save message.", Toasts.Type.ERROR);
            btnSend.setDisable(false);
            return;
        }

        // ── SMS ───────────────────────────────────────────────────────────────
        if (sendSms) {
            String phone = targetUser.getPhone();
            if (phone == null || phone.isBlank()) {
                repo.updateSmsStatus(saved.getId(), "no_phone");
                saved.setSmsStatus("no_phone");
            } else {
                if (!phone.startsWith("+")) phone = "+" + phone;
                String body = "📩 Message from your NutriLife Coach:\n\n" +
                        (hasText ? text : "[Image attached]");
                boolean sent = twilio.sendSms(phone, body);
                String st = sent ? "sent" : "failed";
                repo.updateSmsStatus(saved.getId(), st);
                saved.setSmsStatus(st);
            }
        }

        // ── Update UI ─────────────────────────────────────────────────────────
        txtMessage.clear();
        clearImagePreview();

        messagesContainer.getChildren().removeIf(n ->
                n instanceof VBox v && v.getStyleClass().contains("chat-banner"));
        messagesContainer.getChildren().add(buildBubble(saved, admin.getId()));
        Platform.runLater(() -> scrollPane.setVvalue(1.0));

        // Toast
        if (!sendSms) {
            toast("Sent ✓", Toasts.Type.SUCCESS);
        } else if ("no_phone".equals(saved.getSmsStatus())) {
            toast("Sent in-app — no phone number.", Toasts.Type.WARNING);
        } else if ("failed".equals(saved.getSmsStatus())) {
            toast("Sent in-app — SMS failed.", Toasts.Type.WARNING);
        } else {
            toast("Sent + SMS to " + targetUser.getPhone() + " ✓", Toasts.Type.SUCCESS);
        }

        btnSend.setDisable(false);
    }

    // ── Image attach ──────────────────────────────────────────────────────────

    @FXML
    private void handleAttachImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose an image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.webp"));
        File f = fc.showOpenDialog((Stage) txtMessage.getScene().getWindow());
        if (f == null) return;

        pendingImageFile = f;
        imgPreview.setImage(new Image(f.toURI().toString()));
        lblImageName.setText(f.getName());
        imagePreviewBox.setVisible(true);
        imagePreviewBox.setManaged(true);
    }

    @FXML
    private void handleRemoveImage() {
        clearImagePreview();
    }

    private void clearImagePreview() {
        pendingImageFile = null;
        imgPreview.setImage(null);
        lblImageName.setText("");
        imagePreviewBox.setVisible(false);
        imagePreviewBox.setManaged(false);
    }

    // ── Build bubble ──────────────────────────────────────────────────────────

    private VBox buildBubble(ChatMessage m, int myId) {
        boolean isMe = (m.getSenderId() == myId);

        VBox wrapper = new VBox(2);
        wrapper.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(6);
        bubble.setPadding(new Insets(10, 14, 8, 14));
        bubble.setMaxWidth(500);

        if (m.isDeleted()) {
            bubble.setStyle("-fx-background-color:#F1F5F9;-fx-background-radius:12;");
            Label del = new Label("🗑 Message deleted");
            del.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-font-style:italic;");
            bubble.getChildren().add(del);
        } else {
            if (isMe) {
                bubble.setStyle("-fx-background-color:#2E7D5A;-fx-background-radius:14 14 2 14;");
            } else {
                bubble.setStyle("-fx-background-color:white;-fx-background-radius:14 14 14 2;" +
                        "-fx-border-color:#E2E8F0;-fx-border-radius:14 14 14 2;-fx-border-width:1;");
            }

            // Sender label (only for user messages)
            if (!isMe) {
                Label sender = new Label("👤 " + targetUser.getFirstName());
                sender.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#2E7D5A;");
                bubble.getChildren().add(sender);
            }

            // Image
            if (m.hasImage()) {
                File imgFile = new File(m.getImagePath());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(260); iv.setFitHeight(200); iv.setPreserveRatio(true);
                    iv.setStyle("-fx-background-radius:8;");
                    // Click to enlarge
                    iv.setOnMouseClicked(e -> showImageFullscreen(imgFile));
                    iv.setStyle("-fx-cursor:hand;");
                    bubble.getChildren().add(iv);
                }
            }

            // Text
            if (m.hasText()) {
                Label txt = new Label(m.getContent());
                txt.setWrapText(true);
                txt.setStyle("-fx-font-size:13px;-fx-text-fill:" +
                        (isMe ? "white;" : "#1E293B;") + "-fx-line-spacing:2;");
                bubble.getChildren().add(txt);
            }

            // Footer
            HBox footer = new HBox(6);
            footer.setAlignment(Pos.CENTER_LEFT);

            String timeStr = m.getSentAt() != null ? m.getSentAt().format(FMT) : "";
            if (m.isEdited()) timeStr += " ✏";
            Label time = new Label(timeStr);
            time.setStyle("-fx-font-size:10px;-fx-text-fill:" + (isMe ? "#D1FAE5;" : "#94A3B8;"));
            footer.getChildren().add(time);

            if (isMe) {
                Label read = new Label(m.isRead() ? "✓✓" : "✓");
                read.setStyle("-fx-font-size:10px;-fx-text-fill:" + (m.isRead() ? "#86EFAC;" : "#D1FAE5;"));
                footer.getChildren().add(read);

                if (m.isSentViaSms()) {
                    String st = m.getSmsStatus();
                    Label sms = new Label("sent".equals(st) ? "📱✓" : "📱✗");
                    sms.setStyle("-fx-font-size:10px;-fx-text-fill:" +
                            ("sent".equals(st) ? "#86EFAC;" : "#FCA5A5;"));
                    Tooltip.install(sms, new Tooltip("sent".equals(st) ? "SMS delivered" : "SMS failed"));
                    footer.getChildren().add(sms);
                }
            }

            bubble.getChildren().add(footer);

            // Edit / Delete buttons (only own non-deleted messages)
            if (isMe && !m.isDeleted()) {
                HBox actions = new HBox(6);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button btnEdit = smallBtn("✏ Edit", "#3B82F6");
                btnEdit.setOnAction(e -> handleEdit(m, bubble));

                Button btnDel = smallBtn("🗑 Delete", "#EF4444");
                btnDel.setOnAction(e -> handleDelete(m, wrapper));

                actions.getChildren().addAll(btnEdit, btnDel);
                bubble.getChildren().add(actions);
            }
        }

        wrapper.getChildren().add(bubble);
        return wrapper;
    }

    // ── Edit ──────────────────────────────────────────────────────────────────

    private void handleEdit(ChatMessage m, VBox bubble) {
        // Find the text label in the bubble
        Label txtLabel = bubble.getChildren().stream()
                .filter(n -> n instanceof Label lbl && lbl.getStyle().contains("13px"))
                .map(n -> (Label) n)
                .findFirst().orElse(null);

        String current = m.getContent() != null ? m.getContent() : "";

        TextInputDialog dlg = new TextInputDialog(current);
        dlg.setTitle("Edit message");
        dlg.setHeaderText("Edit your message:");
        dlg.setContentText(null);
        dlg.getEditor().setPrefWidth(400);

        dlg.showAndWait().ifPresent(newText -> {
            if (newText.isBlank()) { toast("Message cannot be empty.", Toasts.Type.WARNING); return; }
            if (newText.length() > MAX_CHARS) { toast("Too long.", Toasts.Type.WARNING); return; }
            if (repo.edit(m.getId(), newText)) {
                m.setContent(newText);
                m.setEdited(true);
                if (txtLabel != null) txtLabel.setText(newText);
                // Refresh footer to show ✏
                loadConversation();
                toast("Message updated ✓", Toasts.Type.SUCCESS);
            }
        });
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    private void handleDelete(ChatMessage m, VBox wrapper) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this message? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete message");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES && repo.delete(m.getId())) {
                loadConversation();
                toast("Message deleted.", Toasts.Type.INFO);
            }
        });
    }

    // ── Image fullscreen ──────────────────────────────────────────────────────

    private void showImageFullscreen(File f) {
        Stage popup = new Stage();
        popup.setTitle("Image");
        VBox root = new VBox();
        root.setStyle("-fx-background-color:#0D1117;-fx-alignment:center;-fx-padding:20;");
        ImageView iv = new ImageView(new Image(f.toURI().toString()));
        iv.setFitWidth(700); iv.setFitHeight(600); iv.setPreserveRatio(true);
        Button close = new Button("Close");
        close.setStyle("-fx-background-color:#2E7D5A;-fx-text-fill:white;-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 24;-fx-margin:12 0 0 0;");
        close.setOnAction(e -> popup.close());
        root.getChildren().addAll(iv, close);
        popup.setScene(new javafx.scene.Scene(root, 740, 660));
        popup.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String saveImage(File src) {
        try {
            new File(CHAT_DIR).mkdirs();
            String ext = src.getName().contains(".")
                    ? src.getName().substring(src.getName().lastIndexOf('.')) : ".jpg";
            String name = UUID.randomUUID() + ext;
            Path dest = Paths.get(CHAT_DIR + name);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return CHAT_DIR + name;
        } catch (IOException e) {
            System.err.println("[Chat] saveImage: " + e.getMessage());
            return null;
        }
    }

    private Button smallBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color +
                ";-fx-font-size:10px;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 8;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:" + color +
                ";-fx-text-fill:white;-fx-font-size:10px;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 8;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color +
                ";-fx-font-size:10px;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 8;"));
        return b;
    }

    private void showBanner(String icon, String title, String sub) {
        VBox banner = new VBox(10);
        banner.getStyleClass().add("chat-banner");
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(60, 0, 40, 0));
        Label ic = new Label(icon); ic.setStyle("-fx-font-size:46px;");
        Label t  = new Label(title); t.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        Label s  = new Label(sub); s.setWrapText(true); s.setMaxWidth(380);
        s.setStyle("-fx-font-size:12px;-fx-text-fill:#64748B;-fx-text-alignment:center;");
        banner.getChildren().addAll(ic, t, s);
        messagesContainer.getChildren().add(banner);
    }

    private void refreshSmsLabel() {
        if (lblSmsStatus == null) return;
        if (!chkSendSms.isSelected()) { lblSmsStatus.setVisible(false); lblSmsStatus.setManaged(false); return; }
        boolean ok = twilio.isConfigured();
        String phone = targetUser != null ? targetUser.getPhone() : null;
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!ok) {
            lblSmsStatus.setText("⚠ Twilio not configured");
            lblSmsStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#EF4444;");
        } else if (!hasPhone) {
            lblSmsStatus.setText("⚠ No phone number");
            lblSmsStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#F59E0B;");
        } else {
            lblSmsStatus.setText("✓ SMS → " + phone);
            lblSmsStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#16A34A;");
        }
        lblSmsStatus.setVisible(true); lblSmsStatus.setManaged(true);
    }

    private void toast(String msg, Toasts.Type type) {
        Stage s = (Stage) lblUserName.getScene().getWindow();
        Toasts.show(s, msg, type);
    }

    @FXML private void handleClose() { ((Stage) lblUserName.getScene().getWindow()).close(); }

    @FXML private void handleCloseHover(javafx.scene.input.MouseEvent e) {
        if (e.getSource() instanceof Button b)
            b.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:18px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 12;-fx-background-radius:6;");
    }
    @FXML private void handleCloseExit(javafx.scene.input.MouseEvent e) {
        if (e.getSource() instanceof Button b)
            b.setStyle("-fx-background-color:transparent;-fx-text-fill:#94A3B8;-fx-font-size:18px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 12;-fx-background-radius:6;");
    }
    @FXML private void handleImgBtnHover(javafx.scene.input.MouseEvent e) {
        if (e.getSource() instanceof Button b)
            b.setStyle("-fx-background-color:#2E7D5A;-fx-text-fill:white;-fx-font-size:16px;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;");
    }
    @FXML private void handleImgBtnExit(javafx.scene.input.MouseEvent e) {
        if (e.getSource() instanceof Button b)
            b.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#475569;-fx-font-size:16px;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:#CBD5E1;-fx-border-radius:8;-fx-border-width:1;");
    }
}
