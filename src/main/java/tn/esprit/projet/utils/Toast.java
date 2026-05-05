package tn.esprit.projet.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {

    public enum Type { SUCCESS, ERROR, INFO }

    public static void show(Stage owner, String message, Type type) {
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setMaxWidth(380);

        String bg = switch (type) {
            case SUCCESS -> "#16A34A";
            case ERROR   -> "#DC2626";
            case INFO    -> "#2563EB";
        };

        lbl.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 12 20; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0, 0, 4);"
        );

        Popup popup = new Popup();
        popup.getContent().add(lbl);
        popup.setAutoFix(true);

        // Position bottom-right of owner window
        double x = owner.getX() + owner.getWidth() - 420;
        double y = owner.getY() + owner.getHeight() - 80;
        popup.show(owner, x, y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), lbl);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), lbl);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }
}
