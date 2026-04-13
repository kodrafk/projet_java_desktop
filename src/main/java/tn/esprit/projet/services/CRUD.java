package tn.esprit.projet.services;

import java.util.List;

/**
 * Interface générique pour les opérations CRUD
 * @param <T> Type de l'entité
 */
public interface CRUD<T> {


    void ajouter(T entity);


    void modifier(T entity);


    void supprimer(int id);

    /**
     * Récupérer une entité par son ID
     */
    T getById(int id);

    /**
     * Récupérer toutes les entités
     */
    List<T> getAll();
}