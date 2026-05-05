package tn.esprit.projet.utils;

import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.FaceEmbeddingService;

import java.util.List;

/**
 * Diagnostic and repair tool for Face ID system
 */
public class FaceIdDiagnostic {

    private final FaceEmbeddingRepository embeddingRepo = new FaceEmbeddingRepository();
    private final UserRepository userRepo = new UserRepository();
    private final FaceEmbeddingService embeddingService = new FaceEmbeddingService();

    public static void main(String[] args) {
        FaceIdDiagnostic diagnostic = new FaceIdDiagnostic();
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           FACE ID DIAGNOSTIC & REPAIR TOOL                     ║");
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println();
        
        diagnostic.runDiagnostic();
        
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    DIAGNOSTIC COMPLETE                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    public void runDiagnostic() {
        System.out.println("📊 STEP 1: Checking database tables...");
        embeddingRepo.ensureTableExists();
        System.out.println("   ✅ Tables verified");
        System.out.println();

        System.out.println("📊 STEP 2: Listing all Face ID enrollments...");
        List<FaceEmbeddingRepository.UserEmbedding> allEmbeddings = embeddingRepo.findAllActiveEmbeddings();
        
        if (allEmbeddings.isEmpty()) {
            System.out.println("   ⚠️  No Face ID enrollments found in database");
            System.out.println("   → This is normal if no users have enrolled Face ID yet");
        } else {
            System.out.println("   ✅ Found " + allEmbeddings.size() + " Face ID enrollment(s)");
            System.out.println();
            
            for (FaceEmbeddingRepository.UserEmbedding ue : allEmbeddings) {
                User user = userRepo.findById(ue.userId);
                if (user != null) {
                    System.out.println("   👤 User ID: " + ue.userId);
                    System.out.println("      Name: " + user.getFirstName() + " " + user.getLastName());
                    System.out.println("      Email: " + user.getEmail());
                    System.out.println("      Embedding: " + (ue.encryptedB64 != null ? "✅ Present" : "❌ Missing"));
                    
                    // Check if embedding can be decrypted
                    try {
                        double[] embedding = embeddingService.decrypt(ue.encryptedB64, ue.ivB64, ue.tagB64);
                        System.out.println("      Decryption: ✅ Valid (" + embedding.length + " dimensions)");
                    } catch (Exception e) {
                        System.out.println("      Decryption: ❌ FAILED - " + e.getMessage());
                        System.out.println("      → This enrollment is corrupted and should be deleted");
                    }
                    System.out.println();
                } else {
                    System.out.println("   ⚠️  Orphaned Face ID for user ID: " + ue.userId + " (user not found)");
                    System.out.println("      → This enrollment should be deleted");
                    System.out.println();
                }
            }
        }

        System.out.println("📊 STEP 3: Checking for inconsistencies...");
        checkInconsistencies();
        
        System.out.println();
        System.out.println("📊 STEP 4: Recommendations...");
        provideRecommendations(allEmbeddings);
    }

    private void checkInconsistencies() {
        List<FaceEmbeddingRepository.UserEmbedding> allEmbeddings = embeddingRepo.findAllActiveEmbeddings();
        
        boolean foundIssues = false;
        
        // Check for duplicate embeddings (same face enrolled multiple times)
        for (int i = 0; i < allEmbeddings.size(); i++) {
            FaceEmbeddingRepository.UserEmbedding ue1 = allEmbeddings.get(i);
            
            try {
                double[] emb1 = embeddingService.decrypt(ue1.encryptedB64, ue1.ivB64, ue1.tagB64);
                
                for (int j = i + 1; j < allEmbeddings.size(); j++) {
                    FaceEmbeddingRepository.UserEmbedding ue2 = allEmbeddings.get(j);
                    
                    try {
                        double[] emb2 = embeddingService.decrypt(ue2.encryptedB64, ue2.ivB64, ue2.tagB64);
                        
                        double similarity = cosineSimilarity(emb1, emb2);
                        
                        if (similarity > 0.6) {
                            foundIssues = true;
                            User user1 = userRepo.findById(ue1.userId);
                            User user2 = userRepo.findById(ue2.userId);
                            
                            System.out.println("   ⚠️  DUPLICATE FACE DETECTED!");
                            System.out.println("      User 1: " + (user1 != null ? user1.getEmail() : "ID " + ue1.userId));
                            System.out.println("      User 2: " + (user2 != null ? user2.getEmail() : "ID " + ue2.userId));
                            System.out.println("      Similarity: " + String.format("%.2f%%", similarity * 100));
                            System.out.println("      → These appear to be the same person!");
                            System.out.println();
                        }
                    } catch (Exception e) {
                        // Skip corrupted embeddings
                    }
                }
            } catch (Exception e) {
                // Skip corrupted embeddings
            }
        }
        
        if (!foundIssues) {
            System.out.println("   ✅ No inconsistencies found");
        }
    }

    private void provideRecommendations(List<FaceEmbeddingRepository.UserEmbedding> allEmbeddings) {
        System.out.println();
        System.out.println("   💡 RECOMMENDATIONS:");
        System.out.println();
        
        if (allEmbeddings.isEmpty()) {
            System.out.println("   • No action needed - system is clean");
        } else {
            System.out.println("   • To delete a specific Face ID enrollment:");
            System.out.println("     → Use Admin Dashboard → User Management → Edit User → Delete Face ID");
            System.out.println();
            System.out.println("   • To delete ALL Face ID enrollments (reset system):");
            System.out.println("     → Run: DELETE FROM face_embeddings;");
            System.out.println();
            System.out.println("   • To delete a specific user's Face ID:");
            System.out.println("     → Run: DELETE FROM face_embeddings WHERE user_id = [USER_ID];");
        }
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (normA * normB);
    }
}
