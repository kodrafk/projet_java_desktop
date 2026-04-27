package tn.esprit.projet.utils;

import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;

/**
 * Delete Face ID for a specific user by email
 */
public class DeleteSpecificFaceId {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java DeleteSpecificFaceId <email>");
            System.exit(1);
        }

        String email = args[0];
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           DELETE FACE ID FOR SPECIFIC USER                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            UserRepository userRepo = new UserRepository();
            FaceEmbeddingRepository faceRepo = new FaceEmbeddingRepository();
            
            // Find user by email
            System.out.println("🔍 Looking for user: " + email);
            User user = userRepo.findByEmail(email);
            
            if (user == null) {
                System.err.println("❌ User not found: " + email);
                System.exit(1);
            }
            
            System.out.println("✅ User found:");
            System.out.println("   ID: " + user.getId());
            System.out.println("   Name: " + user.getFirstName() + " " + user.getLastName());
            System.out.println("   Email: " + user.getEmail());
            System.out.println();
            
            // Check if Face ID exists
            String[] embedding = faceRepo.findByUserId(user.getId());
            
            if (embedding == null) {
                System.out.println("ℹ️  No Face ID enrolled for this user");
                System.out.println("   User can enroll Face ID now");
            } else {
                System.out.println("🗑️  Deleting Face ID...");
                faceRepo.removeByUserId(user.getId());
                System.out.println("✅ Face ID deleted successfully!");
                System.out.println();
                System.out.println("User can now:");
                System.out.println("   • Enroll a new Face ID");
                System.out.println("   • Use the same face for enrollment");
                System.out.println("   • Login with Face ID after re-enrollment");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
