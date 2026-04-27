package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service météo — Open-Meteo (gratuit, sans clé API) + Nominatim (géocodage).
 *
 * Stratégie selon la date de l'événement :
 *  • Aujourd'hui ou futur ≤ 16 j  → API forecast  (api.open-meteo.com)
 *  • Passé récent (≤ 5 j)         → API forecast  (données actuelles)
 *  • Passé > 5 j et ≤ 365 j       → API archive   (archive-api.open-meteo.com)
 *  • Futur > 16 j                  → message "trop tôt"
 *  • Passé > 365 j                 → message "trop ancien"
 */
public class MeteoService {

    // ─────────────────────────────────────────────────────────────────────────
    // Résultat
    // ─────────────────────────────────────────────────────────────────────────
    public static class MeteoResult {
        public final boolean disponible;
        public final String  emoji;
        public final String  description;
        public final double  tempMin;
        public final double  tempMax;
        public final int     precipitationMm;
        public final int     humidite;
        public final String  message;       // texte court pour le badge carte
        public final String  messageEmail;  // bloc HTML pour l'email

        /** Résultat valide */
        public MeteoResult(String emoji, String description,
                           double tempMin, double tempMax,
                           int precipitationMm, int humidite) {
            this.disponible      = true;
            this.emoji           = emoji;
            this.description     = description;
            this.tempMin         = tempMin;
            this.tempMax         = tempMax;
            this.precipitationMm = precipitationMm;
            this.humidite        = humidite;
            this.message         = emoji + " " + description
                                   + "  " + Math.round(tempMin) + "°/"
                                   + Math.round(tempMax) + "°C";
            this.messageEmail    = buildEmailBlock(emoji, description,
                                                   tempMin, tempMax,
                                                   precipitationMm, humidite);
        }

        /** Résultat indisponible */
        public MeteoResult(String raison) {
            this.disponible      = false;
            this.emoji           = "🌍";
            this.description     = raison;
            this.tempMin         = 0; this.tempMax = 0;
            this.precipitationMm = 0; this.humidite = 0;
            this.message         = "🌍 Météo non disponible";
            this.messageEmail    = "";
        }

        // ── Bloc HTML email ───────────────────────────────────────────────────
        private static String buildEmailBlock(String emoji, String description,
                                              double tMin, double tMax,
                                              int pluie, int humidite) {
            String bg   = getCouleurFond(description);
            String fg   = getCouleurTexte(description);
            return
                "<div style=\"background:" + bg + ";border-radius:16px;padding:24px 28px;" +
                "margin:24px 0;border:2px solid " + fg + "33;\">" +
                "<h3 style=\"color:" + fg + ";font-size:17px;font-weight:700;" +
                "margin:0 0 16px 0;\">🌤️ Météo prévue le jour de l'événement</h3>" +
                "<div style=\"display:flex;align-items:center;gap:16px;flex-wrap:wrap;\">" +
                "<div style=\"font-size:52px;line-height:1;\">" + emoji + "</div>" +
                "<div>" +
                "<div style=\"font-size:22px;font-weight:800;color:" + fg + ";\">" +
                description + "</div>" +
                "<div style=\"font-size:15px;color:" + fg + ";opacity:0.85;margin-top:4px;\">" +
                "🌡️ " + Math.round(tMin) + "°C — " + Math.round(tMax) + "°C" +
                (humidite > 0 ? "&nbsp;&nbsp;💧 Humidité : " + humidite + "%" : "") +
                (pluie > 0   ? "&nbsp;&nbsp;🌧️ Précipitations : " + pluie + " mm" : "") +
                "</div></div></div>" +
                "<p style=\"margin:14px 0 0;font-size:13px;color:" + fg +
                ";opacity:0.7;\">Source : Open-Meteo.com — données indicatives</p>" +
                "</div>";
        }

        private static String getCouleurFond(String desc) {
            String d = desc.toLowerCase();
            if (d.contains("dégagé") || d.contains("clair") || d.contains("soleil")) return "#fef9c3";
            if (d.contains("nuage")  || d.contains("couvert"))                        return "#f1f5f9";
            if (d.contains("pluie")  || d.contains("averse") || d.contains("bruine"))return "#dbeafe";
            if (d.contains("orage")  || d.contains("tonnerre"))                       return "#ede9fe";
            if (d.contains("neige")  || d.contains("grêle"))                          return "#e0f2fe";
            if (d.contains("brouillard") || d.contains("brume"))                      return "#f8fafc";
            return "#f0fdf4";
        }

        private static String getCouleurTexte(String desc) {
            String d = desc.toLowerCase();
            if (d.contains("dégagé") || d.contains("clair") || d.contains("soleil")) return "#854d0e";
            if (d.contains("nuage")  || d.contains("couvert"))                        return "#334155";
            if (d.contains("pluie")  || d.contains("averse") || d.contains("bruine"))return "#1e40af";
            if (d.contains("orage")  || d.contains("tonnerre"))                       return "#4c1d95";
            if (d.contains("neige")  || d.contains("grêle"))                          return "#0c4a6e";
            if (d.contains("brouillard") || d.contains("brume"))                      return "#475569";
            return "#166534";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Détection plein air — système intelligent
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Normalise une chaîne pour la comparaison :
     * - minuscules
     * - suppression des accents
     * - remplacement de tout séparateur (espace, tiret, underscore, point) par un espace
     * - suppression des espaces multiples
     */
    private static String normaliserLieu(String s) {
        if (s == null) return "";
        String r = s.toLowerCase();
        // Accents français + arabes translittérés
        r = r.replace("é","e").replace("è","e").replace("ê","e").replace("ë","e")
             .replace("à","a").replace("â","a").replace("ä","a")
             .replace("ô","o").replace("ö","o").replace("ò","o").replace("ó","o")
             .replace("û","u").replace("ù","u").replace("ü","u")
             .replace("î","i").replace("ï","i")
             .replace("ç","c").replace("ñ","n");
        // Séparateurs → espace
        r = r.replaceAll("[\\-_./,;:()\\[\\]'\"]+", " ");
        // Espaces multiples → un seul
        r = r.replaceAll("\\s+", " ").trim();
        return r;
    }

    /**
     * Retourne true si le lieu est en plein air.
     *
     * Stratégie à 4 niveaux :
     *  0. Mots-clés INTÉRIEUR (gym, salle, etc.) → retourne false immédiatement
     *  1. Préfixe [OUTDOOR] explicite (posé par l'admin)
     *  2. Mots-clés EXACTS dans les tokens du lieu normalisé
     *  3. Sous-chaînes (pour les noms composés comme "sidibousaid")
     *  4. Expressions multi-mots (bord de mer, plein air, etc.)
     */
    public static boolean isOutdoor(String lieu) {
        if (lieu == null || lieu.isBlank()) return false;

        // Niveau 1 — préfixe explicite
        if (lieu.startsWith("[OUTDOOR]")) {
            System.out.println("✅ [OUTDOOR] préfixe détecté : " + lieu);
            return true;
        }

        String norm = normaliserLieu(lieu);
        System.out.println("🔍 Analyse lieu normalisé : '" + norm + "' (original: '" + lieu + "')");

        // ── Niveau 0 : mots-clés INTÉRIEUR — priorité absolue ──────────────
        // Si le lieu contient un de ces mots → intérieur, pas de météo
        String[] indoorKeywords = {
            "gym","salle","hall","centre","center","club","studio","dojo",
            "piscine","complexe","arena","palais","maison","hotel","restaurant",
            "cafe","cafeteria","bureau","office","batiment","immeuble","tour",
            "ecole","universite","faculte","lycee","college","bibliotheque",
            "musee","galerie","theatre","cinema","auditorium","conference",
            "indoor","interieur","couvert","couverte","fermee","ferme"
        };
        String normNoSpaceCheck = norm.replace(" ", "");
        for (String kw : indoorKeywords) {
            // Vérifier token exact
            for (String token : norm.split("\\s+")) {
                if (token.equals(kw)) {
                    System.out.println("❌ Intérieur détecté (token exact): " + kw);
                    return false;
                }
            }
            // Vérifier sous-chaîne sans espace
            if (normNoSpaceCheck.contains(kw)) {
                System.out.println("❌ Intérieur détecté (sous-chaîne): " + kw);
                return false;
            }
        }

        // ── Niveau 2 : mots-clés exacts (token par token) ──────────────────
        String[] tokens = norm.split("\\s+");

        String[] exactKeywords = {
            // Nature / espaces verts
            "parc","jardin","foret","bois","montagne","colline","vallee","plaine",
            "campagne","nature","reserve","lac","riviere","fleuve","oued","dune",
            "desert","sahara","champs","prairie","verger",
            // Sport extérieur
            "stade","terrain","piste","circuit","velodrome","hippodrome",
            "court","campo","esplanade","arene","amphitheatre","tribune",
            // Plage / mer / eau
            "plage","mer","ocean","corniche","marina","port","jetee","quai",
            "berge","rive","littoral","cote","baie","golfe",
            // Lieux ouverts génériques
            "exterieur","outdoor","dehors","parvis",
            "promenade","chemin","sentier","allee",
            // Stades / complexes sportifs tunisiens
            "rades","zouiten","barca","olympique","municipal","national",
            "menzah","aouina","gammarth","carthage","byrsa",
            // Villes côtières tunisiennes (souvent plein air)
            "hammamet","monastir","djerba","tabarka","bizerte",
            "nabeul","kelibia","mahdia","zarzis",
            // Mots arabes translittérés
            "muntazah","hadiqua","shati","cornich","midan","maydan"
        };

        for (String token : tokens) {
            for (String kw : exactKeywords) {
                if (token.equals(kw)) {
                    System.out.println("✅ Plein air détecté (token exact): " + kw);
                    return true;
                }
            }
        }

        // ── Niveau 3 : sous-chaînes (noms composés sans espace) ────────────
        String normNoSpace = norm.replace(" ", "");

        String[] substringKeywords = {
            "parc","jardin","stade","terrain","plage","corniche","esplanade",
            "outdoor","exterieur","sidibou","sidibousaid","hammamet",
            "monastir","djerba","tabarka","bizerte","nabeul","kelibia",
            "rades","menzah","gammarth","carthage","zouiten","olympique",
            "municipal","national","hippodrome","velodrome","amphitheatre",
            "complexesportif","espacesportif","parcnational","parcurbain",
            "jardinnational","jardinpublic","bord","rivage","littoral"
        };

        for (String kw : substringKeywords) {
            if (normNoSpace.contains(kw)) {
                System.out.println("✅ Plein air détecté (sous-chaîne): " + kw);
                return true;
            }
        }

        // ── Niveau 4 : expressions multi-mots ──────────────────────────────
        String[] multiWordKeywords = {
            "bord de mer","bord du lac","bord de riviere","bord de fleuve",
            "open air","plein air","en plein","au grand air",
            "espace sportif","espace vert","espace public",
            "sidi bou","sidi bousaid","el menzah","el aouina",
            "ben arous","cite sportive","parc national","parc urbain",
            "jardin public","jardin national","jardin botanique",
            "stade de","terrain de","piste de","circuit de"
        };

        for (String kw : multiWordKeywords) {
            if (norm.contains(kw)) {
                System.out.println("✅ Plein air détecté (multi-mots): " + kw);
                return true;
            }
        }

        System.out.println("❌ Aucun mot-clé plein air trouvé");
        return false;
    }

    /**
     * Retourne le lieu nettoyé (sans le préfixe [OUTDOOR])
     */
    public static String getLieuPropre(String lieu) {
        if (lieu == null) return "";
        return lieu.startsWith("[OUTDOOR]") ? lieu.substring(9).trim() : lieu.trim();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Point d'entrée public
    // ─────────────────────────────────────────────────────────────────────────
    public MeteoResult getMeteo(Evenement ev) {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("🌤️  DÉBUT ANALYSE MÉTÉO");
        System.out.println("═══════════════════════════════════════════════════════");
        
        if (ev == null || ev.getLieu() == null || ev.getLieu().isBlank()) {
            System.out.println("❌ Lieu non renseigné");
            return new MeteoResult("Lieu non renseigné");
        }
        if (ev.getDate_debut() == null) {
            System.out.println("❌ Date non renseignée");
            return new MeteoResult("Date non renseignée");
        }

        System.out.println("📋 Événement: " + ev.getNom());
        System.out.println("📍 Lieu brut: " + ev.getLieu());
        System.out.println("📅 Date: " + ev.getDate_debut());

        // Afficher météo uniquement pour les événements en plein air
        if (!isOutdoor(ev.getLieu())) {
            System.out.println("🏠 RÉSULTAT: Intérieur détecté → pas de météo");
            System.out.println("═══════════════════════════════════════════════════════\n");
            return new MeteoResult("Événement en intérieur");
        }

        // Nettoyer le lieu (enlever préfixe [OUTDOOR] si présent)
        String lieuPropre = getLieuPropre(ev.getLieu());
        System.out.println("🌿 RÉSULTAT: Plein air confirmé!");
        System.out.println("📍 Lieu propre: " + lieuPropre);

        try {
            // 1. Géocodage du lieu
            System.out.println("\n🔍 Étape 1: Géocodage...");
            double[] coords = geocoder(lieuPropre);
            if (coords == null) {
                System.err.println("❌ Géocodage échoué pour : " + lieuPropre);
                System.out.println("🔄 Utilisation de données météo simulées...");
                return getMeteoSimulee(ev);
            }

            double    lat        = coords[0];
            double    lon        = coords[1];
            LocalDate dateEv     = ev.getDate_debut().toLocalDate();
            LocalDate today      = LocalDate.now();
            long      ecartJours = ChronoUnit.DAYS.between(today, dateEv);

            System.out.printf("✅ Coordonnées trouvées: %.4f, %.4f%n", lat, lon);
            System.out.println("\n📅 Étape 2: Analyse temporelle...");
            System.out.println("   Aujourd'hui: " + today);
            System.out.println("   Date événement: " + dateEv);
            System.out.println("   Écart: " + ecartJours + " jours");

            // 2. Sélection de l'API selon la date
            MeteoResult result;
            if (ecartJours > 16) {
                System.out.println("⏰ Futur lointain (>" + ecartJours + "j) → données simulées");
                result = getMeteoSimulee(ev);
            } else if (ecartJours >= 0) {
                System.out.println("🔮 Futur proche (" + ecartJours + "j) → API forecast");
                result = fetchForecast(lat, lon, dateEv);
            } else if (ecartJours >= -7) {
                System.out.println("📊 Passé récent (" + ecartJours + "j) → API forecast avec past_days");
                result = fetchForecastPastDays(lat, lon, dateEv, (int) Math.abs(ecartJours));
            } else if (ecartJours >= -365) {
                System.out.println("📚 Passé ancien (" + ecartJours + "j) → API archive");
                result = fetchArchive(lat, lon, dateEv);
            } else {
                System.out.println("⏳ Trop ancien (" + ecartJours + "j) → données simulées");
                result = getMeteoSimulee(ev);
            }
            
            System.out.println("\n🎯 RÉSULTAT FINAL: " + (result.disponible ? "✅ Météo disponible" : "❌ Météo indisponible"));
            if (result.disponible) {
                System.out.println("   " + result.emoji + " " + result.description);
                System.out.println("   🌡️ " + Math.round(result.tempMin) + "°C - " + Math.round(result.tempMax) + "°C");
            } else {
                System.out.println("   Raison: " + result.description);
            }
            System.out.println("═══════════════════════════════════════════════════════\n");
            return result;

        } catch (Exception e) {
            System.err.println("❌ MeteoService erreur : " + e.getMessage());
            e.printStackTrace();
            System.out.println("🔄 Utilisation de données météo simulées...");
            System.out.println("═══════════════════════════════════════════════════════\n");
            return getMeteoSimulee(ev);
        }
    }
    
    /**
     * Génère des données météo simulées réalistes pour la Tunisie
     * Utilisé comme fallback quand l'API ne fonctionne pas
     */
    private MeteoResult getMeteoSimulee(Evenement ev) {
        System.out.println("🎲 Génération de données météo simulées...");
        
        // Utiliser la date et le lieu pour générer des données cohérentes
        LocalDate date = ev.getDate_debut().toLocalDate();
        int mois = date.getMonthValue();
        int jour = date.getDayOfMonth();
        String lieu = ev.getLieu().toLowerCase();
        
        // Seed basé sur la date pour avoir des résultats cohérents
        int seed = (mois * 100 + jour) % 10;
        
        // Températures selon la saison en Tunisie
        double tempMin, tempMax;
        int wcode;
        String emoji, description;
        int pluie = 0;
        int humidite;
        
        if (mois >= 6 && mois <= 9) {
            // Été (juin-septembre) : chaud et sec
            tempMin = 22 + (seed % 5);
            tempMax = 30 + (seed % 8);
            humidite = 40 + (seed % 20);
            
            if (seed < 2) {
                wcode = 2; emoji = "⛅"; description = "Partiellement nuageux";
            } else {
                wcode = 0; emoji = "☀️"; description = "Ciel dégagé";
            }
        } else if (mois >= 11 || mois <= 2) {
            // Hiver (novembre-février) : doux et parfois pluvieux
            tempMin = 8 + (seed % 6);
            tempMax = 16 + (seed % 6);
            humidite = 60 + (seed % 25);
            
            if (seed < 3) {
                wcode = 61; emoji = "🌧️"; description = "Pluie modérée";
                pluie = 5 + (seed % 10);
            } else if (seed < 6) {
                wcode = 3; emoji = "☁️"; description = "Couvert";
            } else {
                wcode = 1; emoji = "🌤️"; description = "Principalement dégagé";
            }
        } else {
            // Printemps/Automne : agréable
            tempMin = 14 + (seed % 6);
            tempMax = 22 + (seed % 6);
            humidite = 50 + (seed % 25);
            
            if (seed < 2) {
                wcode = 51; emoji = "🌦️"; description = "Bruine légère";
                pluie = 2 + (seed % 5);
            } else if (seed < 5) {
                wcode = 2; emoji = "⛅"; description = "Partiellement nuageux";
            } else {
                wcode = 1; emoji = "🌤️"; description = "Principalement dégagé";
            }
        }
        
        // Ajustement pour les lieux côtiers (plus frais et humides)
        if (lieu.contains("plage") || lieu.contains("corniche") || lieu.contains("marina") || 
            lieu.contains("hammamet") || lieu.contains("monastir") || lieu.contains("djerba")) {
            tempMin -= 2;
            tempMax -= 2;
            humidite += 10;
        }
        
        System.out.println("✅ Météo simulée générée:");
        System.out.println("   " + emoji + " " + description);
        System.out.println("   🌡️ " + Math.round(tempMin) + "°C - " + Math.round(tempMax) + "°C");
        System.out.println("   💧 Humidité: " + humidite + "%");
        if (pluie > 0) {
            System.out.println("   🌧️ Précipitations: " + pluie + " mm");
        }
        System.out.println("   ℹ️ Données simulées (API non disponible)");
        
        return new MeteoResult(emoji, description + " (simulé)", tempMin, tempMax, pluie, humidite);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Géocodage Nominatim
    // ─────────────────────────────────────────────────────────────────────────
    private double[] geocoder(String lieu) {
        try {
            String encoded = URLEncoder.encode(lieu, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&q="
                         + encoded + "&limit=1&accept-language=fr";
            String json = httpGet(url, "NutriCoachMeteo/2.0");
            if (json == null || !json.startsWith("[{")) return null;
            double lat = parseJsonDouble(json, "lat");
            double lon = parseJsonDouble(json, "lon");
            if (lat == 0.0 && lon == 0.0) return null;
            return new double[]{lat, lon};
        } catch (Exception e) {
            System.err.println("⚠️ Géocodage : " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API Forecast — futur (aujourd'hui + 16 jours)
    // ─────────────────────────────────────────────────────────────────────────
    private MeteoResult fetchForecast(double lat, double lon, LocalDate date) throws Exception {
        String d = date.toString();
        String url = String.format(java.util.Locale.US,
            "https://api.open-meteo.com/v1/forecast" +
            "?latitude=%.4f&longitude=%.4f" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
            "precipitation_sum,precipitation_probability_max" +
            "&start_date=%s&end_date=%s&timezone=auto&forecast_days=16",
            lat, lon, d, d);

        System.out.println("🌐 Appel API Forecast...");
        System.out.println("   URL: " + url);
        
        String json = httpGet(url, "NutriCoachMeteo/2.0");
        if (json == null || json.isBlank()) {
            System.err.println("❌ Forecast: réponse vide");
            return new MeteoResult("Données indisponibles");
        }

        System.out.println("📦 Réponse reçue (" + json.length() + " caractères)");
        System.out.println("   Extrait: " + json.substring(0, Math.min(300, json.length())));

        if (json.contains("\"error\":true")) {
            System.err.println("❌ Forecast erreur API");
            return new MeteoResult("Données indisponibles");
        }

        if (!json.contains("\"weather_code\":[")) {
            System.err.println("❌ Forecast: pas de données daily");
            return new MeteoResult("Données indisponibles");
        }

        int    wcode = (int) parseFirstArrayDouble(json, "weather_code");
        double tMax  = parseFirstArrayDouble(json, "temperature_2m_max");
        double tMin  = parseFirstArrayDouble(json, "temperature_2m_min");
        double pluie = parseFirstArrayDouble(json, "precipitation_sum");
        int    probP = (int) parseFirstArrayDouble(json, "precipitation_probability_max");

        System.out.printf("✅ Données extraites: wcode=%d, tMin=%.1f°C, tMax=%.1f°C%n", wcode, tMin, tMax);
        
        String[] ed = wmoCodeToEmojiDesc(wcode);
        return new MeteoResult(ed[0], ed[1], tMin, tMax, (int) Math.round(pluie), probP);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API Forecast avec past_days — passé récent (≤ 7 jours)
    // Open-Meteo conserve les données observées des 7 derniers jours
    // dans l'API forecast via le paramètre past_days
    // ─────────────────────────────────────────────────────────────────────────
    private MeteoResult fetchForecastPastDays(double lat, double lon,
                                              LocalDate date, int pastDays) throws Exception {
        // past_days doit être entre 1 et 92 (mais pour les données récentes, max 7 jours)
        int pd = Math.max(1, Math.min(pastDays + 1, 92));
        String url = String.format(java.util.Locale.US,
            "https://api.open-meteo.com/v1/forecast" +
            "?latitude=%.4f&longitude=%.4f" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
            "precipitation_sum,precipitation_probability_max" +
            "&past_days=%d&forecast_days=1&timezone=auto",
            lat, lon, pd);

        System.out.println("🌐 Forecast past_days URL: " + url);
        String json = httpGet(url, "NutriCoachMeteo/2.0");
        if (json == null || json.isBlank()) {
            System.err.println("⚠️ Forecast past_days: réponse vide, essai archive");
            return fetchArchive(lat, lon, date);
        }

        System.out.println("📦 Past_days réponse (premiers 500 chars): " + json.substring(0, Math.min(500, json.length())));

        if (json.contains("\"error\":true")) {
            System.err.println("⚠️ Forecast past_days: erreur API détectée");
            System.err.println("   Réponse complète: " + json);
            return fetchArchive(lat, lon, date);
        }
        
        if (!json.contains("\"weather_code\":[")) {
            System.err.println("⚠️ Forecast past_days: pas de données daily, essai archive");
            return fetchArchive(lat, lon, date);
        }

        // L'API retourne un tableau de (past_days + forecast_days) jours
        // On doit trouver l'index correspondant à la date voulue
        // en parsant le tableau "time"
        int idx = findDateIndex(json, date);
        System.out.println("📅 Index de la date " + date + " dans le tableau: " + idx);

        int    wcode = (int) parseArrayDoubleAtIndex(json, "weather_code", idx);
        double tMax  = parseArrayDoubleAtIndex(json, "temperature_2m_max", idx);
        double tMin  = parseArrayDoubleAtIndex(json, "temperature_2m_min", idx);
        double pluie = parseArrayDoubleAtIndex(json, "precipitation_sum", idx);
        int    probP = (int) parseArrayDoubleAtIndex(json, "precipitation_probability_max", idx);

        // Vérifier si on a des données valides
        if (wcode == 0 && tMax == 0.0 && tMin == 0.0) {
            System.err.println("⚠️ Forecast past_days: données nulles, essai archive");
            return fetchArchive(lat, lon, date);
        }

        System.out.printf("✅ Past_days: wcode=%d tMin=%.1f tMax=%.1f%n", wcode, tMin, tMax);
        String[] ed = wmoCodeToEmojiDesc(wcode);
        return new MeteoResult(ed[0], ed[1], tMin, tMax, (int) Math.round(pluie), probP);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API Archive ERA5 (données historiques > 7 jours)
    // ─────────────────────────────────────────────────────────────────────────
    private MeteoResult fetchArchive(double lat, double lon, LocalDate date) {
        try {
            String d = date.toString();
            String url = String.format(java.util.Locale.US,
                "https://archive-api.open-meteo.com/v1/archive" +
                "?latitude=%.4f&longitude=%.4f" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
                "precipitation_sum,relative_humidity_2m_max" +
                "&start_date=%s&end_date=%s&timezone=auto",
                lat, lon, d, d);

            System.out.println("🌐 Archive URL: " + url);
            String json = httpGet(url, "NutriCoachMeteo/2.0");
            if (json == null || json.isBlank()) {
                System.err.println("⚠️ Archive: réponse vide");
                return new MeteoResult("Données indisponibles");
            }

            System.out.println("📦 Archive réponse (premiers 300 chars): " + json.substring(0, Math.min(300, json.length())));

            if (json.contains("\"error\":true")) {
                System.err.println("⚠️ Archive: erreur API détectée");
                System.err.println("   Réponse complète: " + json);
                return new MeteoResult("Données indisponibles");
            }
            
            if (!json.contains("\"weather_code\":[")) {
                System.err.println("⚠️ Archive: pas de données daily");
                return new MeteoResult("Données indisponibles");
            }

            int    wcode = (int) parseFirstArrayDouble(json, "weather_code");
            double tMax  = parseFirstArrayDouble(json, "temperature_2m_max");
            double tMin  = parseFirstArrayDouble(json, "temperature_2m_min");
            double pluie = parseFirstArrayDouble(json, "precipitation_sum");
            double hum   = parseFirstArrayDouble(json, "relative_humidity_2m_max");

            if (wcode == 0 && tMax == 0.0 && tMin == 0.0) {
                System.err.println("⚠️ Archive: données nulles ou invalides");
                return new MeteoResult("Données indisponibles");
            }

            System.out.printf("✅ Archive: wcode=%d tMin=%.1f tMax=%.1f%n", wcode, tMin, tMax);
            String[] ed = wmoCodeToEmojiDesc(wcode);
            return new MeteoResult(ed[0], ed[1], tMin, tMax, (int) Math.round(pluie), (int) Math.round(hum));
        } catch (Exception e) {
            System.err.println("❌ Erreur fetchArchive: " + e.getMessage());
            return new MeteoResult("Données indisponibles");
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
    private String[] wmoCodeToEmojiDesc(int code) {
        if (code == 0)                return new String[]{"☀️",  "Ciel dégagé"};
        if (code == 1)                return new String[]{"🌤️", "Principalement dégagé"};
        if (code == 2)                return new String[]{"⛅",  "Partiellement nuageux"};
        if (code == 3)                return new String[]{"☁️",  "Couvert"};
        if (code >= 45 && code <= 48) return new String[]{"🌫️", "Brouillard"};
        if (code >= 51 && code <= 55) return new String[]{"🌦️", "Bruine légère"};
        if (code >= 56 && code <= 57) return new String[]{"🌧️", "Bruine verglaçante"};
        if (code >= 61 && code <= 63) return new String[]{"🌧️", "Pluie modérée"};
        if (code == 65)               return new String[]{"🌧️", "Pluie forte"};
        if (code >= 66 && code <= 67) return new String[]{"🌨️", "Pluie verglaçante"};
        if (code >= 71 && code <= 73) return new String[]{"❄️",  "Neige légère"};
        if (code == 75)               return new String[]{"❄️",  "Neige forte"};
        if (code == 77)               return new String[]{"🌨️", "Grains de neige"};
        if (code >= 80 && code <= 82) return new String[]{"🌦️", "Averses de pluie"};
        if (code >= 85 && code <= 86) return new String[]{"🌨️", "Averses de neige"};
        if (code >= 95 && code <= 99) return new String[]{"⛈️",  "Orage"};
        return new String[]{"🌡️", "Conditions variables"};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP GET
    // ─────────────────────────────────────────────────────────────────────────
    private String httpGet(String urlStr, String userAgent) throws Exception {
        HttpURLConnection conn = null;
        try {
            System.out.println("🔗 Connexion à : " + urlStr);
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000); // 10 secondes
            conn.setReadTimeout(10000);    // 10 secondes
            
            int status = conn.getResponseCode();
            System.out.println("📡 Code HTTP: " + status);
            
            // Lire même en cas d'erreur HTTP pour récupérer le message d'erreur JSON
            java.io.InputStream is = (status == 200)
                ? conn.getInputStream() : conn.getErrorStream();
            
            if (is == null) {
                System.err.println("❌ InputStream null (status=" + status + ")");
                return null;
            }
            
            BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            
            String response = sb.toString();
            System.out.println("✅ Réponse reçue: " + response.length() + " caractères");
            
            return response;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("❌ Timeout: " + e.getMessage());
            throw new Exception("Timeout de connexion à l'API météo");
        } catch (java.net.UnknownHostException e) {
            System.err.println("❌ Hôte inconnu: " + e.getMessage());
            throw new Exception("Impossible de résoudre l'hôte de l'API météo");
        } catch (java.io.IOException e) {
            System.err.println("❌ Erreur I/O: " + e.getMessage());
            throw new Exception("Erreur de connexion à l'API météo: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parseurs JSON minimalistes (sans dépendance externe)
    // ─────────────────────────────────────────────────────────────────────────

    /** Extrait un double d'un champ JSON : "key":"val" ou "key":val */
    private double parseJsonDouble(String json, String key) {
        try {
            // Essai avec guillemets autour de la valeur
            String s1 = "\"" + key + "\":\"";
            int i = json.indexOf(s1);
            int start;
            if (i >= 0) {
                start = i + s1.length();
            } else {
                String s2 = "\"" + key + "\":";
                i = json.indexOf(s2);
                if (i < 0) return 0.0;
                start = i + s2.length();
                if (start < json.length() && json.charAt(start) == '"') start++;
            }
            int end = start;
            while (end < json.length() &&
                   (Character.isDigit(json.charAt(end)) ||
                    json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
            if (start >= end) return 0.0;
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) { return 0.0; }
    }

    /** Extrait le 1er élément d'un tableau JSON : "key":[val, ...] */
    private double parseFirstArrayDouble(String json, String key) {
        return parseArrayDoubleAtIndex(json, key, 0);
    }

    /**
     * Trouve l'index d'une date (format "yyyy-MM-dd") dans le tableau "time"
     * du JSON Open-Meteo. Retourne 0 si non trouvé.
     */
    private int findDateIndex(String json, LocalDate date) {
        try {
            String dateStr = date.toString(); // "2026-04-22"
            String search = "\"time\":[";
            int i = json.indexOf(search);
            if (i < 0) {
                System.err.println("⚠️ findDateIndex: 'time' array not found in JSON");
                return 0;
            }
            int start = i + search.length();
            int end = json.indexOf("]", start);
            if (end < 0) {
                System.err.println("⚠️ findDateIndex: closing bracket not found");
                return 0;
            }
            String timeArray = json.substring(start, end);
            System.out.println("📅 Time array: " + timeArray);
            // timeArray = "\"2026-04-20\",\"2026-04-21\",\"2026-04-22\",..."
            String[] parts = timeArray.split(",");
            for (int idx = 0; idx < parts.length; idx++) {
                String cleanPart = parts[idx].replace("\"", "").trim();
                System.out.println("  [" + idx + "] = " + cleanPart + " (looking for " + dateStr + ")");
                if (cleanPart.equals(dateStr)) {
                    System.out.println("✅ Found date at index " + idx);
                    return idx;
                }
            }
            System.err.println("⚠️ Date " + dateStr + " not found in time array");
            return 0;
        } catch (Exception e) {
            System.err.println("⚠️ findDateIndex error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Extrait l'élément à l'index donné d'un tableau JSON : "key":[v0, v1, v2, ...]
     */
    private double parseArrayDoubleAtIndex(String json, String key, int index) {
        try {
            String search = "\"" + key + "\":[";
            int i = json.indexOf(search);
            if (i < 0) {
                System.err.println("⚠️ parseArrayDoubleAtIndex: key '" + key + "' not found");
                return 0.0;
            }
            int start = i + search.length();
            int end = json.indexOf("]", start);
            if (end < 0) {
                System.err.println("⚠️ parseArrayDoubleAtIndex: closing bracket not found for key '" + key + "'");
                return 0.0;
            }
            String arrayStr = json.substring(start, end);
            // arrayStr = "1.5,2.3,null,4.0,..."
            String[] parts = arrayStr.split(",");
            if (index >= parts.length) {
                System.err.println("⚠️ parseArrayDoubleAtIndex: index " + index + " >= array length " + parts.length + " for key '" + key + "'");
                return 0.0;
            }
            String val = parts[index].trim();
            if (val.equals("null") || val.isEmpty()) {
                System.out.println("⚠️ parseArrayDoubleAtIndex: null value at index " + index + " for key '" + key + "'");
                return 0.0;
            }
            double result = Double.parseDouble(val);
            System.out.println("✅ parseArrayDoubleAtIndex: " + key + "[" + index + "] = " + result);
            return result;
        } catch (Exception e) {
            System.err.println("⚠️ parseArrayDoubleAtIndex error for key '" + key + "': " + e.getMessage());
            return 0.0;
        }
    }
}
