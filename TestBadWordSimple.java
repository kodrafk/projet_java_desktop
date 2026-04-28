public class TestBadWordSimple {
    public static void main(String[] args) {
        try {
            // Test avec réflexion puisque les sources ne sont pas disponibles
            Class<?> badWordClass = Class.forName("tn.esprit.projet.utils.BadWordAPI");
            java.lang.reflect.Method hasProfanityMethod = badWordClass.getMethod("hasProfanity", String.class);
            
            // Tests
            String[] testTexts = {
                "Hello world",
                "This is shit",
                "You are an idiot", 
                "Great service!",
                "This contains fuck word"
            };
            
            System.out.println("=== Testing BadWord API ===");
            for (String text : testTexts) {
                Boolean result = (Boolean) hasProfanityMethod.invoke(null, text);
                System.out.println("\"" + text + "\" -> " + (result ? "❌ BLOCKED" : "✅ ALLOWED"));
            }
            
        } catch (Exception e) {
            System.err.println("Error testing BadWord API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}