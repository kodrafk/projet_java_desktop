package tn.esprit.projet.test;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.services.EmailServicePro;

import java.time.LocalDateTime;

/**
 * Test du service d'envoi d'emails
 */
public class TestEmailServicePro {

    public static void main(String[] args) {
        System.out.println("=== TEST EMAIL SERVICE PRO ===\n");
        
        // Créer un événement de test avec le constructeur approprié
        Evenement evenementTest = new Evenement(
            "Séance de Yoga Matinale",                          // nom
            LocalDateTime.of(2026, 5, 15, 8, 0),               // date_debut
            LocalDateTime.of(2026, 5, 15, 9, 30),              // date_fin
            "Salle de Sport Esprit, Tunis",                     // lieu
            "Confirmé",                                         // statut
            "yoga.jpg",                                         // image
            "Une séance relaxante pour bien commencer la journée", // description
            "Coach Sarah",                                      // coach_name
            "Relaxation et bien-être",                         // objectifs
            25.0,                                               // prix
            20                                                  // capacite
        );
        
        // Créer le service
        EmailServicePro emailService = new EmailServicePro();
        
        // IMPORTANT : Remplacez par votre vraie adresse email pour le test
        String emailTest = "hamzabezzine417@gmail.com"; // ⚠️ CHANGEZ CETTE ADRESSE
        String nomTest = "Hamza Test";
        String telTest = "+216 12 345 678";
        
        System.out.println("📧 Envoi d'un email de test à : " + emailTest);
        System.out.println("👤 Participant : " + nomTest);
        System.out.println("🎯 Événement : " + evenementTest.getNom());
        System.out.println("\n⏳ Envoi en cours...\n");
        
        // Tester l'envoi
        boolean resultat = emailService.envoyerEmailConfirmation(
            nomTest, 
            emailTest, 
            telTest, 
            evenementTest
        );
        
        System.out.println("\n" + "=".repeat(50));
        if (resultat) {
            System.out.println("✅ SUCCÈS : Email envoyé avec succès !");
            System.out.println("💡 Vérifiez votre boîte de réception : " + emailTest);
            System.out.println("   (N'oubliez pas de vérifier le dossier spam)");
        } else {
            System.out.println("❌ ÉCHEC : L'email n'a pas pu être envoyé");
            System.out.println("\n🔍 Vérifications à faire :");
            System.out.println("   1. Connexion Internet active");
            System.out.println("   2. Mot de passe d'application Gmail correct");
            System.out.println("   3. Validation en 2 étapes activée sur Gmail");
            System.out.println("   4. Adresse email destinataire valide");
            System.out.println("\n📖 Guide pour créer un mot de passe d'application Gmail :");
            System.out.println("   → https://support.google.com/accounts/answer/185833");
        }
        System.out.println("=".repeat(50));
    }
}
