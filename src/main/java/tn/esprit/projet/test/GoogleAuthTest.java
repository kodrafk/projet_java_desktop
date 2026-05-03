package tn.esprit.projet.test;

import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.models.User;

/**
 * Simple test class to verify Google Auth database operations
 */
public class GoogleAuthTest {
    
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        
        System.out.println("=== Google Auth Database Test ===");
        
        // Test 1: Find by Google ID (should return null for new ID)
        String testGoogleId = "test_google_id_123";
        User userByGoogleId = repo.findByGoogleId(testGoogleId);
        System.out.println("1. Find by Google ID '" + testGoogleId + "': " + 
                          (userByGoogleId == null ? "NULL (expected)" : "Found: " + userByGoogleId.getEmail()));
        
        // Test 2: Find by email (test with existing email if any)
        User firstUser = repo.findAll().stream().findFirst().orElse(null);
        if (firstUser != null) {
            User userByEmail = repo.findByEmail(firstUser.getEmail());
            System.out.println("2. Find by email '" + firstUser.getEmail() + "': " + 
                              (userByEmail != null ? "Found" : "NULL"));
            
            // Test 3: Update Google ID for existing user
            String originalGoogleId = firstUser.getGoogleId();
            firstUser.setGoogleId("test_update_" + System.currentTimeMillis());
            repo.update(firstUser);
            
            User updatedUser = repo.findById(firstUser.getId());
            System.out.println("3. Update Google ID: " + 
                              (updatedUser.getGoogleId().startsWith("test_update_") ? "SUCCESS" : "FAILED"));
            
            // Restore original Google ID
            firstUser.setGoogleId(originalGoogleId);
            repo.update(firstUser);
        } else {
            System.out.println("2-3. No existing users found to test with");
        }
        
        System.out.println("=== Test Complete ===");
    }
}