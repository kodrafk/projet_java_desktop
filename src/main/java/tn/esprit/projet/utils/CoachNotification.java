package tn.esprit.projet.utils;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Notification coach — slide-down depuis le haut.
 * Clic sur la carte → exécute le callback onClickAction (ex: ouvrir le chat).
 */
public class CoachNotification {

    /**
     * @param ownerStage    La fenêtre principale
     * @param messageCount  Nombre de nouveaux messages
     * @param onClickAction Action à exécuter quand l'utilisateur clique (peut être null)
     */
    public static void show(Stage ownerStage, int messageCount, Runnable onClickAction) {
        if (ownerStage == null) return;

        Stage notifStage = new Stage();
        notifStage.initOwner(ownerStage);
        notifStage.initStyle(StageStyle.TRANSPARENT);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        // ── Card ──────────────────────────────────────────────────────────────
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setStyle(
            "-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB);" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 18 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 8);" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;"
        );

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER);

        Label icon = new Label("👨‍⚕️");
        icon.setStyle("-fx-font-size: 32px;");

        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Your Coach Wrote to You!");
        title.setStyle("-fx-font-size: 16px;-fx-font-weight: bold;-fx-text-fill: white;");

        String msgText = messageCount == 1 ? "You have 1 new message" : "You have " + messageCount + " new messages";
        Label subtitle = new Label(msgText);
        subtitle.setStyle("-fx-font-size: 13px;-fx-text-fill: rgba(255,255,255,0.9);");

        textBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(icon, textBox);

        // Action hint
        Label action = new Label("👆 Click here to open the chat");
        action.setWrapText(true);
        action.setMaxWidth(340);
        action.setAlignment(Pos.CENTER);
        action.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: rgba(255,255,255,0.9);" +
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12;" +
            "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(header, action);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 400, 140);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        notifStage.setScene(scene);

        // Position top-center
        notifStage.setX(ownerStage.getX() + (ownerStage.getWidth() - 400) / 2);
        notifStage.setY(ownerStage.getY() + 60);

        // ── Animations ────────────────────────────────────────────────────────
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), card);
        slideIn.setFromY(-150); slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        ParallelTransition showAnim = new ParallelTransition(slideIn, fadeIn);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), card);
        slideOut.setFromY(0); slideOut.setToY(-150);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        ParallelTransition hideAnim = new ParallelTransition(slideOut, fadeOut);
        hideAnim.setOnFinished(e -> notifStage.close());

        SequentialTransition sequence = new SequentialTransition(
            showAnim,
            new PauseTransition(Duration.seconds(5)),
            hideAnim
        );

        notifStage.show();
        sequence.play();

        // ── Click → open chat ─────────────────────────────────────────────────
        card.setOnMouseClicked(e -> {
            hideAnim.play();
            sequence.stop();
            if (onClickAction != null) onClickAction.run();
        });
    }

    /** Overload without callback (backward compat) */
    public static void show(Stage ownerStage, int messageCount) {
        show(ownerStage, messageCount, null);
    }
}
