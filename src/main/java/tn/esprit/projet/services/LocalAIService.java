package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Ingredient;
import java.util.*;

/**
 * Service IA LOCAL - Fonctionne SANS API externe
 * Utilise des réponses intelligentes prédéfinies basées sur des mots-clés
 */
public class LocalAIService {
    
    private List<Evenement> evenements;
    private List<Ingredient> ingredients;
    
    public void setEvenements(List<Evenement> evenements) {
        this.evenements = evenements;
    }
    
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
    
    public String poserQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "👋 Bonjour ! Je suis votre assistant Nutri Coach Pro.\n\n" +
                   "Je peux vous aider avec :\n" +
                   "• 📅 Informations sur les événements\n" +
                   "• 🥗 Conseils nutritionnels\n" +
                   "• 💪 Programmes d'entraînement\n" +
                   "• 🍎 Ingrédients et recettes\n\n" +
                   "Posez-moi une question ! ✨";
        }
        
        String q = question.toLowerCase().trim();
        
        // Détection des événements
        if (q.contains("événement") || q.contains("event") || q.contains("cours") || q.contains("séance")) {
            return repondreEvenements(q);
        }
        
        // Détection nutrition
        if (q.contains("nutrition") || q.contains("manger") || q.contains("régime") || 
            q.contains("calories") || q.contains("protéine") || q.contains("glucide")) {
            return repondreNutrition(q);
        }
        
        // Détection sport
        if (q.contains("sport") || q.contains("exercice") || q.contains("entraînement") || 
            q.contains("musculation") || q.contains("cardio") || q.contains("yoga")) {
            return recondreSport(q);
        }
        
        // Détection ingrédients
        if (q.contains("ingrédient") || q.contains("recette") || q.contains("cuisine")) {
            return repondreIngredients(q);
        }
        
        // Détection perte de poids
        if (q.contains("maigrir") || q.contains("poids") || q.contains("perdre") || q.contains("mincir")) {
            return recondrePertePoids();
        }
        
        // Détection prise de masse
        if (q.contains("muscle") || q.contains("masse") || q.contains("prendre du poids") || q.contains("grossir")) {
            return recondrePriseMasse();
        }
        
        // Réponse générale
        return repondreGeneral(q);
    }
    
    private String repondreEvenements(String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("📅 **Événements Nutri Coach Pro**\n\n");
        
        if (evenements != null && !evenements.isEmpty()) {
            sb.append("Voici nos événements disponibles :\n\n");
            int count = 0;
            for (Evenement ev : evenements) {
                if (count >= 5) break; // Limiter à 5 événements
                sb.append("🎯 **").append(ev.getNom()).append("**\n");
                sb.append("   📍 Lieu : ").append(ev.getLieu()).append("\n");
                sb.append("   👤 Coach : ").append(ev.getCoach_name()).append("\n");
                sb.append("   📅 Date : ").append(ev.getDate_debut().toLocalDate()).append("\n");
                if (ev.getPrix() > 0) {
                    sb.append("   💰 Prix : ").append(ev.getPrix()).append(" TND\n");
                } else {
                    sb.append("   💰 GRATUIT\n");
                }
                sb.append("\n");
                count++;
            }
            sb.append("💡 **Conseil** : Inscrivez-vous directement depuis la page Événements !");
        } else {
            sb.append("Aucun événement disponible pour le moment.\n\n");
            sb.append("Revenez bientôt pour découvrir nos prochaines activités ! 🎉");
        }
        
        return sb.toString();
    }
    
    private String repondreNutrition(String q) {
        return "🥗 **Conseils Nutritionnels**\n\n" +
               "Pour une alimentation équilibrée :\n\n" +
               "✅ **Protéines** (25-30%)\n" +
               "   • Poulet, poisson, œufs, légumineuses\n" +
               "   • Essentielles pour les muscles\n\n" +
               "✅ **Glucides** (40-50%)\n" +
               "   • Riz complet, quinoa, patates douces\n" +
               "   • Source d'énergie principale\n\n" +
               "✅ **Lipides** (20-30%)\n" +
               "   • Avocat, noix, huile d'olive\n" +
               "   • Importants pour les hormones\n\n" +
               "💧 **Hydratation** : 2-3L d'eau par jour\n\n" +
               "🍎 **Fruits & Légumes** : 5 portions minimum\n\n" +
               "💡 **Astuce** : Mangez toutes les 3-4 heures pour maintenir votre métabolisme actif !";
    }
    
    private String recondreSport(String q) {
        return "💪 **Programme d'Entraînement**\n\n" +
               "**Pour débutants :**\n" +
               "• 3 séances/semaine de 45 minutes\n" +
               "• Cardio léger + renforcement musculaire\n" +
               "• Repos entre les séances\n\n" +
               "**Pour intermédiaires :**\n" +
               "• 4-5 séances/semaine\n" +
               "• Split training (haut/bas du corps)\n" +
               "• HIIT 2x/semaine\n\n" +
               "**Pour avancés :**\n" +
               "• 5-6 séances/semaine\n" +
               "• Programme spécialisé\n" +
               "• Périodisation de l'entraînement\n\n" +
               "🎯 **Conseil** : Consultez nos événements pour des cours avec des coachs professionnels !\n\n" +
               "⚠️ **Important** : Échauffez-vous toujours 10 minutes avant l'effort !";
    }
    
    private String repondreIngredients(String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("🍎 **Ingrédients & Recettes**\n\n");
        
        if (ingredients != null && !ingredients.isEmpty()) {
            sb.append("Ingrédients disponibles dans votre stock :\n\n");
            Map<String, List<String>> parCategorie = new HashMap<>();
            
            for (Ingredient ing : ingredients) {
                String cat = ing.getCategorie() != null ? ing.getCategorie() : "Autre";
                parCategorie.computeIfAbsent(cat, k -> new ArrayList<>()).add(ing.getNom());
            }
            
            for (Map.Entry<String, List<String>> entry : parCategorie.entrySet()) {
                sb.append("**").append(entry.getKey()).append("** :\n");
                for (String nom : entry.getValue()) {
                    sb.append("   • ").append(nom).append("\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("Aucun ingrédient dans votre stock.\n\n");
        }
        
        sb.append("💡 **Idées recettes saines** :\n");
        sb.append("• Bowl Buddha (quinoa, légumes, avocat)\n");
        sb.append("• Smoothie protéiné (banane, whey, lait d'amande)\n");
        sb.append("• Salade César fitness (poulet grillé, parmesan)\n");
        sb.append("• Overnight oats (avoine, fruits, miel)\n");
        
        return sb.toString();
    }
    
    private String recondrePertePoids() {
        return "🎯 **Programme Perte de Poids**\n\n" +
               "**Nutrition** (70% du succès) :\n" +
               "• Déficit calorique de 300-500 kcal/jour\n" +
               "• Protéines élevées (2g/kg de poids)\n" +
               "• Glucides modérés autour de l'entraînement\n" +
               "• Lipides sains (avocat, noix)\n\n" +
               "**Entraînement** (30% du succès) :\n" +
               "• 3-4 séances de musculation/semaine\n" +
               "• 2-3 séances de cardio (HIIT ou modéré)\n" +
               "• 10 000 pas par jour minimum\n\n" +
               "**Sommeil** :\n" +
               "• 7-9 heures par nuit\n" +
               "• Crucial pour la récupération\n\n" +
               "💧 **Hydratation** : 3L d'eau/jour\n\n" +
               "⚠️ **Attention** : Perte saine = 0.5-1kg par semaine maximum !";
    }
    
    private String recondrePriseMasse() {
        return "💪 **Programme Prise de Masse**\n\n" +
               "**Nutrition** (Surplus calorique) :\n" +
               "• +300-500 kcal au-dessus de la maintenance\n" +
               "• Protéines : 2-2.5g/kg de poids\n" +
               "• Glucides : 4-6g/kg (énergie)\n" +
               "• Lipides : 1g/kg minimum\n\n" +
               "**Entraînement** :\n" +
               "• 4-5 séances de musculation/semaine\n" +
               "• Focus sur les exercices composés\n" +
               "• Progressive overload (augmenter les charges)\n" +
               "• Repos : 48h entre les mêmes groupes musculaires\n\n" +
               "**Repas** :\n" +
               "• 5-6 repas par jour\n" +
               "• Collation post-entraînement (protéines + glucides)\n\n" +
               "💡 **Astuce** : La prise de masse propre = 0.5-1kg par mois !\n\n" +
               "🎯 Rejoignez nos événements de musculation avec des coachs experts !";
    }
    
    private String repondreGeneral(String q) {
        return "🤖 **Assistant Nutri Coach Pro**\n\n" +
               "Je peux vous aider avec :\n\n" +
               "📅 **Événements** : Découvrez nos cours et séances\n" +
               "🥗 **Nutrition** : Conseils alimentaires personnalisés\n" +
               "💪 **Sport** : Programmes d'entraînement adaptés\n" +
               "🍎 **Recettes** : Idées de repas sains\n" +
               "🎯 **Objectifs** : Perte de poids ou prise de masse\n\n" +
               "Posez-moi une question plus précise pour que je puisse mieux vous aider ! ✨\n\n" +
               "**Exemples** :\n" +
               "• \"Quels sont les prochains événements ?\"\n" +
               "• \"Comment perdre du poids ?\"\n" +
               "• \"Quel programme de musculation ?\"\n" +
               "• \"Conseils nutrition pour débutant\"";
    }
}
