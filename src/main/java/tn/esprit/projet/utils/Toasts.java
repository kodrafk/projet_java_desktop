package tn.esprit.projet.utils;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toasts {

    public enum Type { SUCCESS, ERROR, INFO, WARNING }

    public static void show(Stage owner, String message, Type type) {
        String bg = switch (type) {
            case SUCCESS -> "#16A34A";
            case ERROR   -> "#DC2626";
            case WARNING -> "#D97706";
            default      -> "#2563EB";
        };
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setMaxWidth(380);
        lbl.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-font-size:13px;" +
                "-fx-font-weight:bold;-fx-padding:12 20;-fx-background-radius:10;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.25),10,0,0,4);");

        Popup popup = new Popup();
        popup.getContent().add(lbl);
        popup.setAutoFix(true);
        double x = owner.getX() + owner.getWidth() - 420;
        double y = owner.getY() + owner.getHeight() - 80;
        popup.show(owner, x, y);

        FadeTransition fi = new FadeTransition(Duration.millis(200), lbl);
        fi.setFromValue(0); fi.setToValue(1);
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        FadeTransition fo = new FadeTransition(Duration.millis(400), lbl);
        fo.setFromValue(1); fo.setToValue(0);
        fo.setOnFinished(e -> popup.hide());
        new SequentialTransition(fi, pause, fo).play();
    }
}
