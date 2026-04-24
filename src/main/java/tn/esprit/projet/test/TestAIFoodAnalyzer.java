package tn.esprit.projet.test;

import tn.esprit.projet.services.AIFoodAnalyzerService;
import javafx.concurrent.Task;

/**
 * Test class for AI Food Analyzer Service
 * This is a simple test to verify the service is working
 */
public class TestAIFoodAnalyzer {
    
    public static void main(String[] args) {
        System.out.println("🤖 AI Food Analyzer Service Test");
        System.out.println("================================");
        
        AIFoodAnalyzerService service = new AIFoodAnalyzerService();
        
        // Test with a small dummy image (1x1 pixel PNG)
        byte[] dummyImage = createDummyPngImage();
        
        System.out.println("📷 Testing with dummy image...");
        
        Task<AIFoodAnalyzerService.FoodAnalysisResult> task = 
            service.analyzeImageAsync(dummyImage, "image/png");
        
        task.setOnSucceeded(e -> {
            AIFoodAnalyzerService.FoodAnalysisResult result = task.getValue();
            
            if (result.isSuccess()) {
                System.out.println("✅ Analysis successful!");
                System.out.println("   Food: " + result.getName());
                System.out.println("   Serving: " + result.getServing());
                System.out.println("   Calories: " + result.getCalories());
                System.out.println("   Protein: " + result.getProtein() + "g");
                System.out.println("   Carbs: " + result.getCarbs() + "g");
                System.out.println("   Fats: " + result.getFats() + "g");
            } else {
                System.out.println("❌ Analysis failed: " + result.getError());
            }
            
            System.out.println("\n🎉 AI Food Analyzer Service is ready!");
            System.exit(0);
        });
        
        task.setOnFailed(e -> {
            System.out.println("❌ Task failed: " + task.getException().getMessage());
            System.exit(1);
        });
        
        // Run in background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        
        // Keep main thread alive
        try {
            Thread.sleep(30000); // 30 second timeout
            System.out.println("⏰ Test timed out");
            System.exit(1);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Creates a minimal 1x1 pixel PNG image for testing
     */
    private static byte[] createDummyPngImage() {
        // Minimal 1x1 pixel PNG (transparent)
        return new byte[] {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4,
            (byte)0x89, 0x00, 0x00, 0x00, 0x0B, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte)0xDA, 0x63, 0x60, 0x00, 0x02,
            0x00, 0x00, 0x05, 0x00, 0x01, (byte)0xE2, 0x26,
            0x05, (byte)0x9B, 0x00, 0x00, 0x00, 0x00, 0x49,
            0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
        };
    }
}