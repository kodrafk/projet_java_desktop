package tn.esprit.projet.test;

import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class Test {
    public static void main(String[] args) {
        System.out.println("--- Test de connexion et modèle Ingredient ---");

        // Test connexion BD
        try {
            MyBDConnexion myBD = MyBDConnexion.getInstance();
            Connection cnx = myBD.getCnx();

            if (cnx != null && !cnx.isClosed()) {
                System.out.println("✅ Connexion BD OK");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion : " + e.getMessage());
        }

        // Test création d'un objet Ingredient
        Ingredient ingredient = new Ingredient(
                "Tomate",
                "Tomato",
                "vegetables",
                500.0,
                "g",
                LocalDate.of(2025, 3, 15),
                "Tomates bio du marché",
                "https://example.com/tomate.jpg"
        );

        System.out.println("\n--- Test Modèle Ingredient ---");
        System.out.println(ingredient);
        System.out.println("Nom : " + ingredient.getNom());
        System.out.println("Catégorie : " + ingredient.getCategorie());
        System.out.println("Quantité : " + ingredient.getQuantite() + " " + ingredient.getUnite());
        System.out.println("Date péremption : " + ingredient.getDatePeremption());

        System.out.println("\n✅ Modèle Ingredient créé avec succès !");
    }
}