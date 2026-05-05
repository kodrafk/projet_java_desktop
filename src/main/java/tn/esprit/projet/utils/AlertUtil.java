package tn.esprit.projet.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Fully styled JavaFX dialogs — no OS system dialogs.
 */
public class AlertUtil {

    public enum Type { SUCCESS, ERROR, WARNING, INFO, CONFIRM }

    /** Show a non-blocking info/success/error/warning message. */
    public static void show(Type type, String title, String message) {
        Stage stage = buildStage(type, title, message, false);
        stage.showAndWait();
    }

    public static void showError(String title, String message) {
        show(Type.ERROR, title, message);
    }

    public static void showSuccess(String title, String message) {
        show(Type.SUCCESS, title, message);
    }

    public static void showInfo(String title, String message) {
        show(Type.INFO, title, message);
    }

    public static void showWarning(String title, String message) {
        show(Type.WARNING, title, message);
    }

    /**
     * Show a confirmation dialog.
     * @return true if user clicked Confirm/Yes
     */
    public static boolean confirm(String title, String message) {
        final boolean[] result = {false};
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);

        // Icon + colors
        String icon  = "⚠";
        String color = "#F59E0B";

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 12, 20));
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-border-width: 0 0 1 0;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        header.getChildren().addAll(iconLbl, titleLbl);

        // Message
        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(340);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        VBox msgBox = new VBox(msgLbl);
        msgBox.setPadding(new Insets(16, 20, 16, 20));

        // Buttons
        Button btnCancel = new Button("Cancel");
        btnCancel.setPrefHeight(36); btnCancel.setPrefWidth(100);
        btnCancel.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> { result[0] = false; stage.close(); });

        Button btnConfirm = new Button("Confirm");
        btnConfirm.setPrefHeight(36); btnConfirm.setPrefWidth(100);
        btnConfirm.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnConfirm.setOnAction(e -> { result[0] = true; stage.close(); });

        HBox btnRow = new HBox(10, btnCancel, btnConfirm);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(12, 20, 16, 20));
        btnRow.setStyle("-fx-background-color: white; -fx-border-color: #F1F5F9 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        VBox root = new VBox(header, msgBox, btnRow);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);");
        root.setPrefWidth(400);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();
        return result[0];
    }

    /** Show a validation error dialog with field-specific messages. */
    public static void showValidationErrors(java.util.List<String> errors) {
        StringBuilder sb = new StringBuilder();
        for (String e : errors) sb.append("• ").append(e).append("\n");
        show(Type.ERROR, "Validation Error", sb.toString().trim());
    }

    // ── Internal builder ──────────────────────────────────────────────────────
    private static Stage buildStage(Type type, String title, String message, boolean hasCancel) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);

        String icon, headerColor, btnColor;
        switch (type) {
            case SUCCESS -> { icon = "✅"; headerColor = "#F0FDF4"; btnColor = "#16A34A"; }
            case ERROR   -> { icon = "❌"; headerColor = "#FFF5F5"; btnColor = "#EF4444"; }
            case WARNING -> { icon = "⚠";  headerColor = "#FFFBEB"; btnColor = "#F59E0B"; }
            default      -> { icon = "ℹ";  headerColor = "#EFF6FF"; btnColor = "#2563EB"; }
        }

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 12, 20));
        header.setStyle("-fx-background-color: " + headerColor + "; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-border-width: 0 0 1 0;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        header.getChildren().addAll(iconLbl, titleLbl);

        // Message
        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(340);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        VBox msgBox = new VBox(msgLbl);
        msgBox.setPadding(new Insets(16, 20, 16, 20));

        // OK button
        Button btnOk = new Button("OK");
        btnOk.setPrefHeight(36); btnOk.setPrefWidth(100);
        btnOk.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnOk.setOnAction(e -> stage.close());

        HBox btnRow = new HBox(btnOk);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(12, 20, 16, 20));
        btnRow.setStyle("-fx-background-color: white; -fx-border-color: #F1F5F9 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        VBox root = new VBox(header, msgBox, btnRow);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);");
        root.setPrefWidth(400);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        return stage;
    }
}
