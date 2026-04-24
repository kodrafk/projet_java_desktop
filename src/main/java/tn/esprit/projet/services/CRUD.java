package tn.esprit.projet.services;

import java.util.List;

/**
 * Interface générique pour les opérations CRUD
 * @param <T> Type de l'entité
 */
public interface CRUD<T> {

    /**
     * Ajouter une nouvelle entité
     * @param entity L'entité à ajouter
     */
    void ajouter(T entity);

    /**
     * Modifier une entité existante
     * @param entity L'entité avec les nouvelles données
     */
    void modifier(T entity);

    /**
     * Supprimer une entité par son ID
     * @param id L'identifiant de l'entité à supprimer
     */
    void supprimer(int id);

    /**
     * Récupérer une entité par son ID
     * @param id L'identifiant de l'entité
     * @return L'entité trouvée ou null
     */
    T getById(int id);

    /**
     * Récupérer toutes les entités
     * @return Liste de toutes les entités
     */
    List<T> getAll();
}