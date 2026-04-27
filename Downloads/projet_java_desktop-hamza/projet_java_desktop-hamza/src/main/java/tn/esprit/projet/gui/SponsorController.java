package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.services.EvenementService;
import tn.esprit.projet.services.SponsorService;

import java.io.IOException;

public class SponsorController {

    @FXML private TextField tfNom, tfType, tfLogo;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<Evenement> cbEvenement;

    private final SponsorService sponsorService = new SponsorService();
    private final EvenementService eventService = new EvenementService();

    @FXML
    public void initialize() {
        // Charger les événements pour la ComboBox de jointure
        if (cbEvenement != null) {
            cbEvenement.setItems(FXCollections.observableArrayList(eventService.getAll()));

            cbEvenement.setCellFactory(lv -> new ListCell<Evenement>() {
                @Override protected void updateItem(Evenement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNom());
                }
            });
            cbEvenement.setButtonCell(cbEvenement.getCellFactory().call(null));
        }
    }

    @FXML
    public void handleAjouter() {
        Evenement ev = cbEvenement.getValue();
        if (ev == null || tfNom.getText().isEmpty()) {
            System.out.println("Erreur : Champs vides ou événement non sélectionné.");
            return;
        }

        Sponsor s = new Sponsor(0, tfNom.getText(), tfType.getText(),
                taDescription.getText(), "Actif", tfLogo.getText(), null, ev.getId());

        sponsorService.ajouter(s);
        retourListe();
    }

    // CETTE MÉTHODE RÉPARE L'ERREUR 'RESOLVING ONACTION=#RETOURLISTE'
    @FXML
    public void retourListe() {
        try {
            // Assurez-vous que le chemin /fxml/AfficherEvenement.fxml est correct
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AfficherEvenement.fxml"));
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Erreur lors du retour à la liste : " + e.getMessage());
        }
    }
}