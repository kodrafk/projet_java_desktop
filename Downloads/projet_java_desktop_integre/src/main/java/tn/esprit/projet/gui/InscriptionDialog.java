package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.projet.models.Evenement;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialogue d'inscription ultra-professionnel et dynamique
 */
public class InscriptionDialog {
    
    private Stage dialog;
    private TextField tfNom;
    private TextField tfEmail;
    private TextField tfTelephone;
    private Button btnConfirmer;
    private Button btnAnnuler;
    private Label lblError;
    
    private String nom;
    private String email;
    private String telephone;
    private boolean confirmed = false;
    
    public InscriptionDialog(Evenement evenement) {
        createDialog(evenement);
    }
    
    private void createDialog(Evenement evenement) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Inscription");
        
        // Container principal avec effet de transparence
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        // Card principale
        VBox card = new VBox(0);
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);");
        
        // Header avec gradient
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 30, 25, 30));
        header.setStyle("-fx-background-color: linear-gradient(135deg, #2E7D5A 0%, #1F4D3A 100%); " +
                       "-fx-background-radius: 20 20 0 0;");
        
        // Icône
        Label icon = new Label("🎯");
        icon.setFont(Font.font(50));
        
        // Titre
        Label titre = new Label("Inscription");
        titre.setFont(Font.font("System", FontWeight.BOLD, 28));
        titre.setTextFill(Color.WHITE);
        
        // Sous-titre avec nom de l'événement
        Label sousTitre = new Label(evenement.getNom());
        sousTitre.setFont(Font.font("System", FontWeight.NORMAL, 15));
        sousTitre.setTextFill(Color.web("#D7E6DF"));
        sousTitre.setWrapText(true);
        sousTitre.setAlignment(Pos.CENTER);
        sousTitre.setMaxWidth(400);
        
        // Date et lieu
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String dateStr = evenement.getDate_debut().toLocalDate().format(dateFormatter);
        String timeStr = evenement.getDate_debut().toLocalTime().format(timeFormatter);
        
        Label infos = new Label("📅 " + dateStr + " à " + timeStr + "\n📍 " + evenement.getLieu());
        infos.setFont(Font.font("System", FontWeight.NORMAL, 12));
        infos.setTextFill(Color.web("#A8C5B8"));
        infos.setAlignment(Pos.CENTER);
        infos.setWrapText(true);
        
        header.getChildren().addAll(icon, titre, sousTitre, infos);
        
        // Corps du formulaire
        VBox body = new VBox(20);
        body.setPadding(new Insets(30));
        
        // Message d'introduction
        Label message = new Label("Complétez vos informations pour finaliser votre inscription");
        message.setFont(Font.font("System", FontWeight.NORMAL, 13));
        message.setTextFill(Color.web("#64748B"));
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);
        message.setMaxWidth(400);
        
        // Champ Nom complet
        VBox nomBox = createStyledField("👤 Nom complet", "Ex: Hamza Bezzine");
        tfNom = (TextField) ((HBox) nomBox.getChildren().get(1)).getChildren().get(0);
        
        // Champ Email
        VBox emailBox = createStyledField("📧 Email", "votre.email@example.com");
        tfEmail = (TextField) ((HBox) emailBox.getChildren().get(1)).getChildren().get(0);
        
        // Champ Téléphone
        VBox telBox = createStyledField("📱 Téléphone", "Ex: 52099735");
        tfTelephone = (TextField) ((HBox) telBox.getChildren().get(1)).getChildren().get(0);
        
        // Label d'erreur
        lblError = new Label();
        lblError.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblError.setTextFill(Color.web("#EF4444"));
        lblError.setVisible(false);
        lblError.setWrapText(true);
        lblError.setMaxWidth(400);
        
        body.getChildren().addAll(message, nomBox, emailBox, telBox, lblError);
        
        // Footer avec boutons
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(0, 30, 30, 30));
        footer.setAlignment(Pos.CENTER);
        
        // Bouton Annuler
        btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefWidth(150);
        btnAnnuler.setPrefHeight(45);
        btnAnnuler.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnAnnuler.setStyle("-fx-background-color: #F1F5F9; " +
                           "-fx-text-fill: #475569; " +
                           "-fx-background-radius: 10; " +
                           "-fx-cursor: hand;");
        
        btnAnnuler.setOnMouseEntered(e -> {
            btnAnnuler.setStyle("-fx-background-color: #E2E8F0; " +
                               "-fx-text-fill: #334155; " +
                               "-fx-background-radius: 10; " +
                               "-fx-cursor: hand;");
        });
        
        btnAnnuler.setOnMouseExited(e -> {
            btnAnnuler.setStyle("-fx-background-color: #F1F5F9; " +
                               "-fx-text-fill: #475569; " +
                               "-fx-background-radius: 10; " +
                               "-fx-cursor: hand;");
        });
        
        btnAnnuler.setOnAction(e -> {
            confirmed = false;
            playCloseAnimation();
        });
        
        // Bouton Confirmer
        btnConfirmer = new Button("Confirmer l'inscription");
        btnConfirmer.setPrefWidth(200);
        btnConfirmer.setPrefHeight(45);
        btnConfirmer.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnConfirmer.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 10; " +
                             "-fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.4), 10, 0, 0, 3);");
        
        btnConfirmer.setOnMouseEntered(e -> {
            btnConfirmer.setStyle("-fx-background-color: linear-gradient(to right, #1F4D3A, #163828); " +
                                 "-fx-text-fill: white; " +
                                 "-fx-background-radius: 10; " +
                                 "-fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.6), 15, 0, 0, 5);");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnConfirmer);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        btnConfirmer.setOnMouseExited(e -> {
            btnConfirmer.setStyle("-fx-background-color: linear-gradient(to right, #2E7D5A, #1F4D3A); " +
                                 "-fx-text-fill: white; " +
                                 "-fx-background-radius: 10; " +
                                 "-fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.4), 10, 0, 0, 3);");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnConfirmer);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        btnConfirmer.setOnAction(e -> validerFormulaire());
        
        footer.getChildren().addAll(btnAnnuler, btnConfirmer);
        
        // Assemblage
        card.getChildren().addAll(header, body, footer);
        
        root.getChildren().add(card);
        
        // Fermeture en cliquant sur le fond
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) {
                confirmed = false;
                playCloseAnimation();
            }
        });
        
        Scene scene = new Scene(root, 600, 700);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        
        // Animation d'ouverture
        playOpenAnimation(card);
    }
    
    private VBox createStyledField(String label, String placeholder) {
        VBox container = new VBox(8);
        
        // Label
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#1E293B"));
        
        // Container du champ avec icône
        HBox fieldBox = new HBox();
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        fieldBox.setStyle("-fx-background-color: #F8FAFC; " +
                         "-fx-border-color: #E2E8F0; " +
                         "-fx-border-width: 2; " +
                         "-fx-border-radius: 10; " +
                         "-fx-background-radius: 10;");
        
        // TextField
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setFont(Font.font("System", FontWeight.NORMAL, 14));
        tf.setStyle("-fx-background-color: transparent; " +
                   "-fx-border-color: transparent; " +
                   "-fx-text-fill: #1E293B; " +
                   "-fx-prompt-text-fill: #94A3B8;");
        tf.setPrefHeight(45);
        HBox.setHgrow(tf, Priority.ALWAYS);
        tf.setPadding(new Insets(0, 15, 0, 15));
        
        // Focus effects
        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                fieldBox.setStyle("-fx-background-color: white; " +
                                 "-fx-border-color: #2E7D5A; " +
                                 "-fx-border-width: 2; " +
                                 "-fx-border-radius: 10; " +
                                 "-fx-background-radius: 10; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(46,125,90,0.2), 8, 0, 0, 2);");
            } else {
                fieldBox.setStyle("-fx-background-color: #F8FAFC; " +
                                 "-fx-border-color: #E2E8F0; " +
                                 "-fx-border-width: 2; " +
                                 "-fx-border-radius: 10; " +
                                 "-fx-background-radius: 10;");
            }
        });
        
        fieldBox.getChildren().add(tf);
        container.getChildren().addAll(lbl, fieldBox);
        
        return container;
    }
    
    private void validerFormulaire() {
        lblError.setVisible(false);
        
        nom = tfNom.getText().trim();
        email = tfEmail.getText().trim();
        telephone = tfTelephone.getText().trim();
        
        // Validation
        if (nom.isEmpty()) {
            showError("⚠️ Veuillez saisir votre nom complet");
            tfNom.requestFocus();
            shakeField(tfNom);
            return;
        }
        
        if (email.isEmpty()) {
            showError("⚠️ Veuillez saisir votre adresse email");
            tfEmail.requestFocus();
            shakeField(tfEmail);
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("⚠️ Adresse email invalide");
            tfEmail.requestFocus();
            shakeField(tfEmail);
            return;
        }
        
        if (telephone.isEmpty()) {
            showError("⚠️ Veuillez saisir votre numéro de téléphone");
            tfTelephone.requestFocus();
            shakeField(tfTelephone);
            return;
        }
        
        if (!telephone.matches("^[0-9]{8,15}$")) {
            showError("⚠️ Numéro de téléphone invalide (8-15 chiffres)");
            tfTelephone.requestFocus();
            shakeField(tfTelephone);
            return;
        }
        
        // Validation réussie
        confirmed = true;
        playCloseAnimation();
    }
    
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        
        // Animation de l'erreur
        FadeTransition fade = new FadeTransition(Duration.millis(200), lblError);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    private void shakeField(TextField field) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), field.getParent());
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
    
    private void playOpenAnimation(VBox card) {
        card.setScaleX(0.8);
        card.setScaleY(0.8);
        card.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), card);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setToValue(1.0);
        
        ParallelTransition pt = new ParallelTransition(scale, fade);
        pt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        pt.play();
    }
    
    private void playCloseAnimation() {
        VBox card = (VBox) ((StackPane) dialog.getScene().getRoot()).getChildren().get(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
        scale.setToX(0.8);
        scale.setToY(0.8);
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), card);
        fade.setToValue(0);
        
        ParallelTransition pt = new ParallelTransition(scale, fade);
        pt.setOnFinished(e -> dialog.close());
        pt.play();
    }
    
    public boolean showAndWait() {
        dialog.showAndWait();
        return confirmed;
    }
    
    public String getNom() {
        return nom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getTelephone() {
        return telephone;
    }
}
