package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.projet.models.Publication;
import tn.esprit.projet.models.PublicationComment;
import tn.esprit.projet.services.PublicationCommentService;
import tn.esprit.projet.services.PublicationService;
import tn.esprit.projet.utils.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

public class BlogAdminController {

    @FXML private TextField searchField;
    @FXML private ListView<Publication> publicationListView;
    @FXML private ListView<PublicationComment> commentListView;

    private final PublicationService pubService = new PublicationService();
    private final PublicationCommentService commentService = new PublicationCommentService();

    private ObservableList<Publication> allPublications = FXCollections.observableArrayList();
    private ObservableList<PublicationComment> currentComments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès Refusé", "Vous devez être administrateur pour voir cette page.");
            return;
        }

        publicationListView.setItems(allPublications);
        commentListView.setItems(currentComments);

        publicationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Publication item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitre() + " - par " + item.getAuthorName() + " (" + item.getCreatedAt().toLocalDate() + ")");
                }
            }
        });

        commentListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PublicationComment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getAuthorName() + ": " + item.getContenu() + " (" + item.getCreatedAt().toLocalDate() + ")");
                }
            }
        });

        publicationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadComments(newVal.getId());
            } else {
                currentComments.clear();
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterPublications(newVal);
        });

        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        List<Publication> pubs = pubService.findAll();
        allPublications.setAll(pubs);
        searchField.clear();
        commentListView.getItems().clear();
    }

    private void filterPublications(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            allPublications.setAll(pubService.findAll());
            return;
        }
        String lowerCaseKeyword = keyword.toLowerCase();
        List<Publication> filtered = pubService.findAll().stream()
                .filter(p -> p.getTitre().toLowerCase().contains(lowerCaseKeyword) ||
                             p.getContenu().toLowerCase().contains(lowerCaseKeyword) ||
                             p.getAuthorName().toLowerCase().contains(lowerCaseKeyword))
                .collect(Collectors.toList());
        allPublications.setAll(filtered);
    }

    private void loadComments(int pubId) {
        currentComments.setAll(commentService.findByPublication(pubId));
    }

    @FXML
    private void handleDeletePublication() {
        Publication selected = publicationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une publication à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette publication ? Tous les commentaires et likes associés seront supprimés.", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (pubService.delete(selected.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Publication supprimée avec succès.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La suppression a échoué.");
            }
        }
    }

    @FXML
    private void handleDeleteComment() {
        PublicationComment selected = commentListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un commentaire à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (commentService.delete(selected.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire supprimé avec succès.");
                Publication pub = publicationListView.getSelectionModel().getSelectedItem();
                if (pub != null) loadComments(pub.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La suppression a échoué.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
