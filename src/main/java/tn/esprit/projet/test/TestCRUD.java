package tn.esprit.projet.test;

import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.services.IngredientService;

import java.time.LocalDate;
import java.util.List;

public class TestCRUD {
    public static void main(String[] args) {
        IngredientService service = new IngredientService();


        // ═══ TEST 1 : AJOUTER ═══
        System.out.println("--- TEST 1 : AJOUTER ---");
        Ingredient carotte = new Ingredient(
                "Carotte bio",
                "Carrot",
                "vegetables",
                300.0,
                "g",
                LocalDate.of(2025, 3, 20),
                "Carottes du marché local",
                null
        );
        service.ajouter(carotte);

        // ═══ TEST 2 : AFFICHER TOUS ═══
        System.out.println("\n--- TEST 2 : AFFICHER TOUS ---");
        List<Ingredient> tous = service.getAll();
        tous.forEach(System.out::println);

        // ═══ TEST 3 : RÉCUPÉRER PAR ID ═══
        System.out.println("\n--- TEST 3 : GET BY ID ---");
        if (!tous.isEmpty()) {
            int premierID = tous.get(0).getId();
            Ingredient trouve = service.getById(premierID);
            System.out.println("Trouvé : " + trouve);
        }

        // ═══ TEST 4 : MODIFIER ═══
        System.out.println("\n--- TEST 4 : MODIFIER ---");
        if (!tous.isEmpty()) {
            Ingredient aModifier = tous.get(0);
            aModifier.setQuantite(500.0);
            aModifier.setNotes("Quantité mise à jour");
            service.modifier(aModifier);
        }

        // ═══ TEST 5 : RECHERCHER ═══
        System.out.println("\n--- TEST 5 : RECHERCHER ---");
        List<Ingredient> resultats = service.rechercherParNom("car");
        resultats.forEach(System.out::println);

    }
}