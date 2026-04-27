package tn.esprit.projet.services;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/**
 * Service de secours utilisant OpenAI si Gemini ne fonctionne pas
 */
public class OpenAIFallbackService {
    
    private static final String OPENAI_API_KEY = "sk-proj-votre-cle-openai-ici";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    private final HttpClient httpClient;
    
    public OpenAIFallbackService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    }
    
    public String poserQuestion(String question, String contexte) {
        try {
            String prompt = contexte + "\n\nQuestion: " + question;
            
            String body = "{"
                + "\"model\":\"gpt-3.5-turbo\","
                + "\"messages\":[{\"role\":\"user\",\"content\":" + escapeJson(prompt) + "}],"
                + "\"temperature\":0.7,"
                + "\"max_tokens\":1000"
                + "}";
            
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();
            
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            
            if (resp.statusCode() == 200) {
                return extraireTexte(resp.body());
            }
            
            throw new Exception("OpenAI API status: " + resp.statusCode());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur OpenAI: " + e.getMessage());
            return null;
        }
    }
    
    private String extraireTexte(String json) {
        try {
            String key = "\"content\":";
            int startIdx = json.indexOf(key);
            if (startIdx == -1) return null;
            
            int quoteStart = json.indexOf("\"", startIdx + key.length());
            if (quoteStart == -1) return null;
            
            StringBuilder result = new StringBuilder();
            boolean escaped = false;
            for (int i = quoteStart + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) { result.append(c); escaped = false; }
                else if (c == '\\') { escaped = true; }
                else if (c == '"') { break; }
                else { result.append(c); }
            }
            
            return result.toString()
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\").trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String escapeJson(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"")
                          .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
}
