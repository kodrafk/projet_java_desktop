package tn.esprit.projet.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.projet.services.AssistantIAService;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.IngredientService;

public class IADialogUtil {

    private static AssistantIAService assistantIA = null;

    public static void ouvrirAssistantIA() {
        if (assistantIA == null) {
            assistantIA = AssistantIAService.getInstance();
        }

        // Charger le contexte global (Événements + Ingrédients)
        try {
            EvenementService evSvc = new EvenementService();
            IngredientService ingSvc = new IngredientService();
            assistantIA.setEvenements(evSvc.getAll());
            assistantIA.setIngredients(ingSvc.getAll());
        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement contexte IA : " + e.getMessage());
        }

        Stage stage = new Stage();
        stage.setTitle("🤖 Assistant IA Nutri Coach - Global ✨");
        stage.setResizable(true);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f8fafc;");

        // --- HEADER ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #3D5A3D);");

        Label botIcon = new Label("🤖");
        botIcon.setFont(Font.font(24));
        
        VBox titleBox = new VBox(2);
        Label title = new Label("Assistant IA Nutri Coach");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Intelligence Artificielle Global ✨");
        subtitle.setFont(Font.font("System", 10));
        subtitle.setTextFill(Color.web("#E0E7FF"));
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        header.getChildren().addAll(botIcon, titleBox, spacer, closeBtn);

        // --- MESSAGES AREA ---
        VBox messagesVBox = new VBox(12);
        messagesVBox.setPadding(new Insets(20));
        messagesVBox.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(messagesVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Message de bienvenue
        ajouterMessageIA(messagesVBox, scrollPane, "👋 Bonjour ! Je suis votre assistant IA **Global**. \n\nJe connais tout sur Nutri Coach Pro : les événements, la nutrition, les ingrédients, et bien plus encore. Comment puis-je vous aider ? ✨");

        // --- INPUT AREA ---
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15, 20, 20, 20));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        TextField textField = new TextField();
        textField.setPromptText("Posez n'importe quelle question...");
        textField.setPrefHeight(45);
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setStyle("-fx-background-radius: 25; -fx-padding: 0 20; -fx-border-color: #E2E8F0; -fx-border-radius: 25;");

        Button sendBtn = new Button("✈");
        sendBtn.setPrefSize(45, 45);
        sendBtn.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-size: 18; -fx-cursor: hand;");

        inputArea.getChildren().addAll(textField, sendBtn);

        // --- ACTIONS ---
        Runnable sendMessage = () -> {
            String q = textField.getText().trim();
            if (q.isEmpty()) return;
            textField.clear();

            ajouterMessageUtilisateur(messagesVBox, scrollPane, q);

            // Bulle de chargement
            HBox loadingBox = new HBox(10);
            loadingBox.setAlignment(Pos.CENTER_LEFT);
            Label loadingLabel = new Label("⌛ L'IA réfléchit...");
            loadingLabel.setStyle("-fx-text-fill: #64748B; -fx-font-style: italic; -fx-font-size: 12;");
            loadingBox.getChildren().add(loadingLabel);
            messagesVBox.getChildren().add(loadingBox);

            new Thread(() -> {
                String response = assistantIA.poserQuestion(q);
                Platform.runLater(() -> {
                    messagesVBox.getChildren().remove(loadingBox);
                    ajouterMessageIA(messagesVBox, scrollPane, response);
                });
            }).start();
        };

        sendBtn.setOnAction(e -> sendMessage.run());
        textField.setOnAction(e -> sendMessage.run());

        root.getChildren().addAll(header, scrollPane, inputArea);

        Scene scene = new Scene(root, 550, 700);
        stage.setScene(scene);
        stage.show();
    }

    private static void ajouterMessageUtilisateur(VBox container, ScrollPane scroll, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-padding: 12 16; -fx-background-radius: 18 18 2 18; -fx-font-size: 13;");
        
        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER_RIGHT);
        container.getChildren().add(row);
        
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    private static void ajouterMessageIA(VBox container, ScrollPane scroll, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #1E293B; -fx-padding: 12 16; -fx-background-radius: 18 18 18 2; -fx-font-size: 13; -fx-border-color: #E2E8F0; -fx-border-radius: 18 18 18 2;");
        
        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(row);
        
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }
}
