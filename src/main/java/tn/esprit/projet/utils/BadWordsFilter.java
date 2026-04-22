package tn.esprit.projet.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Vérifie si un texte contient des mots inappropriés via l'API profanity.dev
 * API gratuite, open-source, sans clé requise.
 * Endpoint : POST https://vector.profanity.dev
 * Body     : {"message": "<text>"}
 * Response : {"isProfanity": true/false, "score": 0.0-1.0}
 */
public class BadWordsFilter {

    private static final String API_URL = "https://vector.profanity.dev";
    /** Seuil de score au-delà duquel on considère le texte inapproprié (0.90 = extrêmement strict) */
    private static final double THRESHOLD = 0.90;
    /** Mode debug : affiche les détails dans la console */
    private static final boolean DEBUG = true;

    public static class Result {
        public final boolean isProfanity;
        public final double score;

        public Result(boolean isProfanity, double score) {
            this.isProfanity = isProfanity;
            this.score = score;
        }
    }

    /**
     * Vérifie le texte via l'API profanity.dev
     * @return Result avec isProfanity=true si le texte est inapproprié.
     *         En cas d'erreur réseau, retourne isProfanity=false (fail-open).
     */
    public static Result check(String text) {
        if (text == null || text.isBlank()) return new Result(false, 0.0);
        
        // Ignorer les textes qui sont juste des répétitions (aaaa, bbbb, ffff, etc.)
        String cleanText = text.replaceAll("\\s+", "").toLowerCase();
        if (cleanText.matches("(.)\\1{10,}")) {
            if (DEBUG) System.out.println("[BadWordsFilter] Ignored: repetitive text");
            return new Result(false, 0.0);
        }
        
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);

            // Escape quotes in text for JSON
            String safeText = text.replace("\\", "\\\\")
                                  .replace("\"", "\\\"")
                                  .replace("\n", " ")
                                  .replace("\r", " ");
            String body = "{\"message\":\"" + safeText + "\"}";
            
            if (DEBUG) System.out.println("[BadWordsFilter] Request: " + body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("[BadWordsFilter] API returned status: " + status);
                return new Result(false, 0.0);
            }

            Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            if (DEBUG) System.out.println("[BadWordsFilter] Response: " + response);

            // Parse JSON: {"isProfanity":true,"score":0.87}
            boolean isProfanity = response.contains("\"isProfanity\":true");
            double score = 0.0;
            
            int scoreIdx = response.indexOf("\"score\":");
            if (scoreIdx != -1) {
                String scoreStr = response.substring(scoreIdx + 8);
                // Extract number (handle both 0.87 and 0.87})
                scoreStr = scoreStr.replaceAll("[^0-9.]", "");
                if (!scoreStr.isEmpty()) {
                    try { 
                        score = Double.parseDouble(scoreStr); 
                    } catch (NumberFormatException e) {
                        System.err.println("[BadWordsFilter] Failed to parse score: " + scoreStr);
                    }
                }
            }

            if (DEBUG) System.out.println("[BadWordsFilter] isProfanity=" + isProfanity + 
                                         ", score=" + score + ", threshold=" + THRESHOLD);

            // Apply threshold: only block if score is high enough
            if (isProfanity && score < THRESHOLD) {
                if (DEBUG) System.out.println("[BadWordsFilter] Score below threshold, allowing content");
                isProfanity = false;
            }

            return new Result(isProfanity, score);

        } catch (Exception e) {
            System.err.println("[BadWordsFilter] API Error: " + e.getMessage());
            if (DEBUG) e.printStackTrace();
            // Fail-open: allow content if API is unavailable
            return new Result(false, 0.0);
        }
    }

    /**
     * Vérifie plusieurs champs en une seule passe (concatène avec espace).
     */
    public static Result checkAll(String... texts) {
        StringBuilder sb = new StringBuilder();
        for (String t : texts) {
            if (t != null && !t.isBlank()) {
                sb.append(t).append(" ");
            }
        }
        return check(sb.toString().trim());
    }
}
