package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Assistant IA LOCAL pour les événements — Réponses intelligentes sans API.
 * Singleton global : une seule instance partagée dans toute l'application.
 */
public class AssistantIAService {

    // ── Singleton (double-checked locking, thread-safe) ────
    private static volatile AssistantIAService instance;

    public static AssistantIAService getInstance() {
        if (instance == null) {
            synchronized (AssistantIAService.class) {
                if (instance == null) {
                    instance = new AssistantIAService();
                    System.out.println("✅ [AssistantIA] Instance globale créée — Mode LOCAL activé");
                }
            }
        }
        return instance;
    }
    // ───────────────────────────────────────────────────────

    private List<Evenement> evenements;

    // Types de questions reconnus
    private enum TypeQuestion {
        SALUTATION, LISTE_TOUS, GRATUITS, PAYANTS, PROCHAIN, STATISTIQUES,
        RECHERCHE_NOM, RECHERCHE_LIEU, RECHERCHE_COACH, RECHERCHE_DATE,
        AIDE, INCONNU
    }

    /** Constructeur privé — utiliser {@link #getInstance()} */
    private AssistantIAService() {
    }

    /** Injecte la liste des événements avant chaque appel. */
    public void setEvenements(List<Evenement> evenements) {
        this.evenements = evenements;
    }

    // ══════════════════════════════════════════════════════
    //  POINT D'ENTRÉE PRINCIPAL
    // ══════════════════════════════════════════════════════

    public String poserQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return repondreBonjour();
        }
        System.out.println("🤖 [IA Local] Question : " + question);
        String reponse = analyserEtRepondre(question.trim());
        System.out.println("✅ [IA Local] Réponse générée");
        return reponse;
    }

    /**
     * Analyse la question et retourne la réponse appropriée
     */
    private String analyserEtRepondre(String question) {
        String q = normaliser(question);
        TypeQuestion type = detecterTypeQuestion(q);

        switch (type) {
            case SALUTATION:     return repondreBonjour();
            case AIDE:           return repondreAide();
            case LISTE_TOUS:     return repondreListe();
            case GRATUITS:       return repondreGratuits();
            case PAYANTS:        return repondrePayants();
            case PROCHAIN:       return repondreProchain();
            case STATISTIQUES:   return repondreStatistiques();
            case RECHERCHE_LIEU: return rechercherParLieu(q);
            case RECHERCHE_COACH:return rechercherParCoach(q);
            case RECHERCHE_DATE: return rechercherParDate(q);
            case RECHERCHE_NOM:  return rechercherParNom(q);
            default:             return repondreInconnu(question);
        }
    }

    /**
     * Détecte le type de question avec précision
     */
    private TypeQuestion detecterTypeQuestion(String q) {
        // Salutations
        if (contientMot(q, "bonjour", "salut", "hello", "bonsoir", "coucou", "hi", "hey", "salam")) {
            return TypeQuestion.SALUTATION;
        }

        // Aide
        if (contientMot(q, "aide", "help", "comment", "que peux tu", "quoi faire")) {
            return TypeQuestion.AIDE;
        }

        // Statistiques
        if (contientMot(q, "statistique", "stat", "combien", "nombre", "total", "resume", "bilan")) {
            return TypeQuestion.STATISTIQUES;
        }

        // Prochain événement
        if (contientMot(q, "prochain", "prochainement", "bientot", "quand", "suivant")) {
            return TypeQuestion.PROCHAIN;
        }

        // Événements gratuits
        if (contientMot(q, "gratuit", "gratuite", "gratuits", "free", "sans frais", "0 tnd")) {
            return TypeQuestion.GRATUITS;
        }

        // Événements payants
        if (contientMot(q, "payant", "payants", "payer", "cout", "tarif", "prix")) {
            return TypeQuestion.PAYANTS;
        }

        // Recherche par lieu
        if (contientMot(q, "lieu", "ou", "endroit", "salle", "stade", "parc", "gym", "centre", "tunis", "sfax", "sousse")) {
            return TypeQuestion.RECHERCHE_LIEU;
        }
        if (evenements != null) {
            for (Evenement ev : evenements) {
                if (ev.getLieu() != null && q.contains(normaliser(ev.getLieu()))) {
                    return TypeQuestion.RECHERCHE_LIEU;
                }
            }
        }

        // Recherche par coach
        if (contientMot(q, "coach", "entraineur", "formateur", "instructeur")) {
            return TypeQuestion.RECHERCHE_COACH;
        }
        if (evenements != null) {
            for (Evenement ev : evenements) {
                if (ev.getCoach_name() != null && q.contains(normaliser(ev.getCoach_name()))) {
                    return TypeQuestion.RECHERCHE_COACH;
                }
            }
        }

        // Recherche par date
        if (contientMot(q, "date", "jour", "mois", "semaine", "aujourd hui", "demain",
                        "janvier", "fevrier", "mars", "avril", "mai", "juin",
                        "juillet", "aout", "septembre", "octobre", "novembre", "decembre")) {
            return TypeQuestion.RECHERCHE_DATE;
        }

        // Liste tous
        if (contientMot(q, "tous", "toutes", "liste", "afficher", "montrer", "voir", "evenements")) {
            return TypeQuestion.LISTE_TOUS;
        }

        // Recherche par nom
        if (evenements != null) {
            for (Evenement ev : evenements) {
                String nomNorm = normaliser(ev.getNom());
                if (nomNorm.length() > 2 && (q.contains(nomNorm) || nomNorm.contains(q))) {
                    return TypeQuestion.RECHERCHE_NOM;
                }
            }
        }

        return TypeQuestion.INCONNU;
    }

    // ══════════════════════════════════════════════════════
    //  RÉPONSES SPÉCIFIQUES
    // ══════════════════════════════════════════════════════

    private String repondreBonjour() {
        int nbEvents = evenements != null ? evenements.size() : 0;
        return "Bonjour ! Je suis votre assistant IA Nutri Coach.\n\n" +
               "Actuellement, " + nbEvents + " evenement" + (nbEvents > 1 ? "s sont" : " est") +
               " disponible" + (nbEvents > 1 ? "s" : "") + ".\n\n" +
               "Je peux vous aider a :\n" +
               "• Voir tous les evenements\n" +
               "• Trouver des evenements gratuits\n" +
               "• Voir le prochain evenement\n" +
               "• Chercher par nom, lieu ou coach\n" +
               "• Voir les statistiques\n\n" +
               "Que souhaitez-vous savoir ?";
    }

    private String repondreAide() {
        return "Voici ce que je peux faire pour vous :\n\n" +
               "- Lister tous les evenements\n" +
               "- Trouver les evenements gratuits\n" +
               "- Trouver les evenements payants\n" +
               "- Voir le prochain evenement\n" +
               "- Voir les statistiques\n" +
               "- Chercher par lieu\n" +
               "- Chercher par coach\n" +
               "- Chercher par nom\n\n" +
               "Posez votre question !";
    }

    private String repondreListe() {
        if (evenements == null || evenements.isEmpty()) {
            return "Aucun evenement disponible pour le moment.";
        }
        StringBuilder sb = new StringBuilder("Tous les evenements (" + evenements.size() + ") :\n\n");
        for (int i = 0; i < evenements.size(); i++) {
            Evenement ev = evenements.get(i);
            sb.append((i + 1)).append(". ").append(ev.getNom()).append("\n");
            sb.append("   Lieu : ").append(ev.getLieu()).append("\n");
            sb.append("   Date : ").append(ev.getDate_debut().toLocalDate())
              .append(" a ").append(ev.getDate_debut().toLocalTime().toString().substring(0, 5)).append("\n");
            sb.append("   ").append(ev.getPrix() > 0 ? "Prix : " + ev.getPrix() + " TND" : "Gratuit").append("\n\n");
        }
        return sb.toString().trim();
    }

    private String repondreGratuits() {
        if (evenements == null) return "Aucun evenement disponible.";
        List<Evenement> gratuits = evenements.stream()
            .filter(ev -> ev.getPrix() <= 0)
            .collect(Collectors.toList());

        if (gratuits.isEmpty()) {
            return "Tous les evenements actuels sont payants.\n\nConsultez regulierement pour des evenements gratuits !";
        }

        StringBuilder sb = new StringBuilder("Evenements gratuits (" + gratuits.size() + ") :\n\n");
        for (Evenement ev : gratuits) {
            sb.append("- ").append(ev.getNom()).append("\n");
            sb.append("  Lieu : ").append(ev.getLieu()).append("\n");
            sb.append("  Date : ").append(ev.getDate_debut().toLocalDate())
              .append(" a ").append(ev.getDate_debut().toLocalTime().toString().substring(0, 5)).append("\n");
            sb.append("  Coach : ").append(ev.getCoach_name()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String repondrePayants() {
        if (evenements == null) return "Aucun evenement disponible.";
        List<Evenement> payants = evenements.stream()
            .filter(ev -> ev.getPrix() > 0)
            .sorted(Comparator.comparingDouble(Evenement::getPrix))
            .collect(Collectors.toList());

        if (payants.isEmpty()) {
            return "Tous les evenements actuels sont gratuits !\n\nProfitez-en !";
        }

        StringBuilder sb = new StringBuilder("Evenements payants (" + payants.size() + ") :\n\n");
        for (Evenement ev : payants) {
            sb.append("- ").append(ev.getNom()).append(" - ").append(ev.getPrix()).append(" TND\n");
            sb.append("  Lieu : ").append(ev.getLieu()).append("\n");
            sb.append("  Date : ").append(ev.getDate_debut().toLocalDate()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String repondreProchain() {
        if (evenements == null || evenements.isEmpty()) {
            return "Aucun evenement disponible pour le moment.";
        }
        Optional<Evenement> prochain = evenements.stream()
            .filter(ev -> ev.getDate_debut().isAfter(LocalDateTime.now()))
            .min(Comparator.comparing(Evenement::getDate_debut));

        if (!prochain.isPresent()) {
            Evenement dernier = evenements.stream()
                .max(Comparator.comparing(Evenement::getDate_debut))
                .orElse(evenements.get(0));
            return "Prochain evenement :\n\n" + formaterEvenement(dernier) +
                   "\n\nCet evenement est peut-etre deja passe.";
        }

        Evenement ev = prochain.get();
        long jours = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.now().toLocalDate(), ev.getDate_debut().toLocalDate());

        String delai = jours == 0 ? "Aujourd'hui !" :
                       jours == 1 ? "Demain !" :
                       "Dans " + jours + " jour" + (jours > 1 ? "s" : "");

        return "Prochain evenement :\n\n" + formaterEvenement(ev) + "\n\n" + delai;
    }

    private String repondreStatistiques() {
        if (evenements == null || evenements.isEmpty()) {
            return "Aucun evenement disponible.";
        }
        long gratuits = evenements.stream().filter(ev -> ev.getPrix() <= 0).count();
        long payants = evenements.size() - gratuits;
        long complets = evenements.stream().filter(Evenement::estComplet).count();
        long disponibles = evenements.stream().filter(ev -> !ev.estComplet()).count();
        double prixMoyen = evenements.stream().filter(ev -> ev.getPrix() > 0).mapToDouble(Evenement::getPrix).average().orElse(0);

        StringBuilder sb = new StringBuilder("Statistiques des evenements :\n\n");
        sb.append("Total : ").append(evenements.size()).append(" evenements\n");
        sb.append("Gratuits : ").append(gratuits).append("\n");
        sb.append("Payants : ").append(payants).append("\n");
        if (payants > 0) sb.append("Prix moyen : ").append(String.format("%.2f", prixMoyen)).append(" TND\n");
        sb.append("\nCapacite :\n");
        sb.append("Disponibles : ").append(disponibles).append("\n");
        sb.append("Complets : ").append(complets).append("\n\n");

        for (Evenement ev : evenements) {
            if (!ev.estIllimite()) {
                String statut = ev.estComplet() ? "COMPLET" : ev.getPlacesRestantes() + " places";
                sb.append("- ").append(ev.getNom()).append(" : ").append(statut).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String rechercherParLieu(String q) {
        if (evenements == null) return "Aucun evenement disponible.";
        List<Evenement> resultats = evenements.stream()
            .filter(ev -> normaliser(ev.getLieu()).contains(q) || q.contains(normaliser(ev.getLieu())))
            .collect(Collectors.toList());

        if (resultats.isEmpty()) {
            return "Aucun evenement trouve pour ce lieu.\n\nLieux disponibles :\n" +
                   evenements.stream().map(ev -> "- " + ev.getLieu()).distinct().collect(Collectors.joining("\n"));
        }

        StringBuilder sb = new StringBuilder("Evenements trouves (" + resultats.size() + ") :\n\n");
        for (Evenement ev : resultats) {
            sb.append(formaterEvenement(ev)).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String rechercherParCoach(String q) {
        if (evenements == null) return "Aucun evenement disponible.";
        List<Evenement> resultats = evenements.stream()
            .filter(ev -> normaliser(ev.getCoach_name()).contains(q) || q.contains(normaliser(ev.getCoach_name())))
            .collect(Collectors.toList());

        if (resultats.isEmpty()) {
            return "Aucun evenement trouve pour ce coach.\n\nCoachs disponibles :\n" +
                   evenements.stream().map(ev -> "- " + ev.getCoach_name()).distinct().collect(Collectors.joining("\n"));
        }

        StringBuilder sb = new StringBuilder("Evenements avec ce coach (" + resultats.size() + ") :\n\n");
        for (Evenement ev : resultats) {
            sb.append(formaterEvenement(ev)).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String rechercherParDate(String q) {
        if (evenements == null) return "Aucun evenement disponible.";

        List<Evenement> resultats = evenements.stream()
            .filter(ev -> {
                String dateStr = ev.getDate_debut().toLocalDate().toString();
                return q.contains(dateStr.substring(0, 4)) || q.contains(dateStr.substring(5, 7));
            })
            .collect(Collectors.toList());

        if (resultats.isEmpty()) {
            return "Aucun evenement trouve pour cette periode.\n\nDates disponibles :\n" +
                   evenements.stream().map(ev -> "- " + ev.getDate_debut().toLocalDate() + " - " + ev.getNom())
                       .collect(Collectors.joining("\n"));
        }

        StringBuilder sb = new StringBuilder("Evenements trouves (" + resultats.size() + ") :\n\n");
        for (Evenement ev : resultats) {
            sb.append(formaterEvenement(ev)).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String rechercherParNom(String q) {
        if (evenements == null) return "Aucun evenement disponible.";
        List<Evenement> resultats = evenements.stream()
            .filter(ev -> normaliser(ev.getNom()).contains(q) || q.contains(normaliser(ev.getNom())))
            .collect(Collectors.toList());

        if (resultats.isEmpty()) return repondreInconnu(q);

        if (resultats.size() == 1) {
            Evenement ev = resultats.get(0);
            return ev.getNom() + "\n\n" + formaterEvenementComplet(ev);
        }

        StringBuilder sb = new StringBuilder("Evenements trouves (" + resultats.size() + ") :\n\n");
        for (Evenement ev : resultats) {
            sb.append(formaterEvenement(ev)).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String repondreInconnu(String question) {
        return "Je ne suis pas sur de comprendre votre question.\n\n" +
               "Tapez 'aide' pour voir ce que je peux faire,\n" +
               "ou posez-moi une question sur nos evenements !";
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════

    private String formaterEvenement(Evenement ev) {
        String capaciteInfo = ev.estIllimite() ? "Illimite"
            : ev.estComplet() ? "COMPLET (" + ev.getCapacite() + "/" + ev.getCapacite() + ")"
            : ev.getPlacesRestantes() + " place" + (ev.getPlacesRestantes() > 1 ? "s" : "") + " dispo / " + ev.getCapacite();

        return "- " + ev.getNom() + "\n" +
               "  Lieu : " + ev.getLieu() + "\n" +
               "  Date : " + ev.getDate_debut().toLocalDate() +
               " a " + ev.getDate_debut().toLocalTime().toString().substring(0, 5) + "\n" +
               "  Coach : " + ev.getCoach_name() + "\n" +
               "  " + (ev.getPrix() > 0 ? "Prix : " + ev.getPrix() + " TND" : "Gratuit") + "\n" +
               "  " + capaciteInfo;
    }

    private String formaterEvenementComplet(Evenement ev) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lieu : ").append(ev.getLieu()).append("\n");
        sb.append("Date : ").append(ev.getDate_debut().toLocalDate()).append("\n");
        sb.append("Heure : ").append(ev.getDate_debut().toLocalTime().toString().substring(0, 5)).append("\n");
        sb.append("Coach : ").append(ev.getCoach_name()).append("\n");
        sb.append("Statut : ").append(ev.getStatut()).append("\n");
        sb.append(ev.getPrix() > 0 ? "Prix : " + ev.getPrix() + " TND" : "Gratuit").append("\n");
        if (!ev.estIllimite()) {
            sb.append("Places : ").append(ev.getNbParticipants()).append("/").append(ev.getCapacite());
            if (ev.estComplet()) sb.append(" COMPLET");
            else sb.append(" (").append(ev.getPlacesRestantes()).append(" restante").append(ev.getPlacesRestantes() > 1 ? "s" : "").append(")");
            sb.append("\n");
        }
        if (ev.getDescription() != null && !ev.getDescription().isEmpty()) {
            sb.append("Description : ").append(ev.getDescription());
        }
        return sb.toString();
    }

    private String normaliser(String s) {
        if (s == null) return "";
        return s.toLowerCase()
            .replace("é", "e").replace("è", "e").replace("ê", "e").replace("ë", "e")
            .replace("à", "a").replace("â", "a").replace("ä", "a")
            .replace("ù", "u").replace("û", "u").replace("ü", "u")
            .replace("î", "i").replace("ï", "i")
            .replace("ô", "o").replace("ö", "o")
            .replace("ç", "c")
            .replace("'", " ").replace("-", " ")
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private boolean contientMot(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.equals(mot) ||
                texte.startsWith(mot + " ") ||
                texte.endsWith(" " + mot) ||
                texte.contains(" " + mot + " ")) {
                return true;
            }
        }
        return false;
    }
}
