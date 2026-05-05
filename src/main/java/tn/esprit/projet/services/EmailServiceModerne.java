package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.services.WeatherService;
import tn.esprit.projet.services.WeatherService.WeatherInfo;
import tn.esprit.projet.services.SmartWeatherService;
import tn.esprit.projet.services.SponsorService;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Locale;
import java.util.List;

/**
 * Service d'envoi d'emails modernes et professionnels pour les inscriptions aux événements
 * Design inspiré des meilleures pratiques d'email marketing
 */
public class EmailServiceModerne {

    // Configuration SMTP Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "hamzabezzine417@gmail.com";
    private static final String EMAIL_PASSWORD = "dauz dluy rjox rjdz"; // App password Gmail
    
    private final WeatherService weatherService = new WeatherService();
    private final SmartWeatherService smartWeatherService = new SmartWeatherService();
    private final SponsorService sponsorService = new SponsorService();

    /**
     * Envoie un email de confirmation moderne avec météo et design professionnel
     */
    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant, 
                                           String telephone, Evenement evenement) {
        try {
            // Validation des paramètres
            if (nomParticipant == null || nomParticipant.trim().isEmpty()) {
                System.err.println("❌ Nom du participant vide");
                return false;
            }
            if (emailParticipant == null || !emailParticipant.contains("@")) {
                System.err.println("❌ Email invalide : " + emailParticipant);
                return false;
            }
            if (evenement == null) {
                System.err.println("❌ Événement null");
                return false;
            }
            
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "Nutri Coach Pro"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailParticipant));
            message.setSubject("✅ Confirmation d'inscription - " + evenement.getNom());

            String htmlContent = genererEmailModerne(nomParticipant, emailParticipant, telephone, evenement);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            System.out.println("📧 Tentative d'envoi d'email à : " + emailParticipant);
            Transport.send(message);
            System.out.println("✅ Email moderne envoyé avec succès à : " + emailParticipant);
            return true;

        } catch (javax.mail.AuthenticationFailedException e) {
            System.err.println("❌ Erreur d'authentification SMTP : " + e.getMessage());
            System.err.println("⚠️ Vérifiez EMAIL_FROM et EMAIL_PASSWORD dans EmailServiceModerne.java");
            e.printStackTrace();
            return false;
        } catch (javax.mail.MessagingException e) {
            System.err.println("❌ Erreur de messagerie : " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère le contenu HTML moderne de l'email
     */
    private String genererEmailModerne(String nomParticipant, String emailParticipant, String telephone, Evenement evenement) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateDebut = evenement.getDate_debut().toLocalDate().format(dateFormatter);
        String heureDebut = evenement.getDate_debut().toLocalTime().format(timeFormatter);
        
        // Récupérer la météo si événement extérieur
        boolean isOutdoor = false;
        WeatherInfo weather = null;
        try {
            isOutdoor = smartWeatherService.isOutdoorEvent(evenement);
            if (isOutdoor) {
                weather = weatherService.getWeatherForDate(
                    evenement.getLieu() != null ? evenement.getLieu() : weatherService.getDefaultCity(),
                    evenement.getDate_debut().toLocalDate()
                );
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la récupération de la météo : " + e.getMessage());
            // Continuer sans météo
        }

        StringBuilder html = new StringBuilder();
        
        // Début du HTML avec styles modernes
        html.append("<!DOCTYPE html>");
        html.append("<html lang='fr'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #0f172a; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: #1e293b; }");
        html.append(".header { background: linear-gradient(135deg, #2E7D5A 0%, #1F4D3A 100%); padding: 40px 30px; text-align: center; }");
        html.append(".header-icon { font-size: 60px; margin-bottom: 15px; }");
        html.append(".header-title { color: white; font-size: 28px; font-weight: bold; margin: 0; }");
        html.append(".header-subtitle { color: #D7E6DF; font-size: 16px; margin: 10px 0 0 0; }");
        html.append(".badge { background: linear-gradient(to right, #10B981, #059669); color: white; padding: 12px 30px; border-radius: 25px; display: inline-block; font-weight: bold; margin: 20px 0; }");
        html.append(".content { padding: 30px; }");
        html.append(".greeting { color: white; font-size: 18px; margin-bottom: 20px; }");
        html.append(".message { color: #cbd5e1; font-size: 15px; line-height: 1.6; margin-bottom: 25px; }");
        html.append(".weather-box { background: linear-gradient(135deg, #854d0e 0%, #713f12 100%); border-radius: 15px; padding: 20px; margin: 25px 0; border: 2px solid #a16207; }");
        html.append(".weather-title { color: #fef3c7; font-size: 14px; font-weight: bold; margin-bottom: 15px; display: flex; align-items: center; }");
        html.append(".weather-content { display: flex; align-items: center; }");
        html.append(".weather-icon { font-size: 50px; margin-right: 15px; }");
        html.append(".weather-desc { color: #fef3c7; font-size: 18px; font-weight: bold; }");
        html.append(".weather-temp { color: #fde68a; font-size: 16px; margin-top: 5px; }");
        html.append(".weather-humidity { color: #fde68a; font-size: 13px; margin-top: 5px; }");
        html.append(".weather-source { color: #d97706; font-size: 11px; margin-top: 10px; }");
        html.append(".info-box { background-color: #334155; border-radius: 12px; padding: 20px; margin: 20px 0; }");
        html.append(".info-title { color: #cbd5e1; font-size: 14px; font-weight: bold; margin-bottom: 15px; }");
        html.append(".info-row { color: white; font-size: 15px; margin: 10px 0; display: flex; justify-content: space-between; }");
        html.append(".info-label { color: #94a3b8; }");
        html.append(".info-value { font-weight: bold; }");
        html.append(".button { background: linear-gradient(to right, #3b82f6, #2563eb); color: white; padding: 15px 30px; text-decoration: none; border-radius: 10px; display: inline-block; font-weight: bold; margin: 10px 0; }");
        html.append(".tips-box { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); border-radius: 12px; padding: 20px; margin: 25px 0; }");
        html.append(".tips-title { color: #1e40af; font-size: 14px; font-weight: bold; margin-bottom: 12px; }");
        html.append(".tip { color: #1e3a8a; font-size: 13px; margin: 8px 0; padding-left: 20px; position: relative; }");
        html.append(".sponsors-box { background-color: #334155; border-radius: 12px; padding: 20px; margin: 25px 0; text-align: center; }");
        html.append(".sponsors-title { color: #cbd5e1; font-size: 14px; font-weight: bold; margin-bottom: 15px; }");
        html.append(".sponsor-logo { width: 80px; height: 50px; object-fit: contain; margin: 10px; border-radius: 8px; background-color: white; padding: 5px; }");
        html.append(".footer { background-color: #1e3a5f; padding: 30px; text-align: center; }");
        html.append(".footer-logo { font-size: 40px; margin-bottom: 10px; }");
        html.append(".footer-title { color: white; font-size: 20px; font-weight: bold; margin: 10px 0; }");
        html.append(".footer-subtitle { color: #94a3b8; font-size: 13px; margin: 5px 0; }");
        html.append(".footer-note { color: #64748b; font-size: 11px; margin-top: 20px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        
        // Header avec icône et titre
        html.append("<div class='header'>");
        html.append("<div class='header-icon'>🏋️</div>");
        html.append("<h1 class='header-title'>Inscription Confirmée !</h1>");
        html.append("<p class='header-subtitle'>Nutri Coach Pro</p>");
        html.append("</div>");
        
        // Badge de confirmation
        html.append("<div style='text-align: center; padding: 20px 0;'>");
        html.append("<div class='badge'>✓ Votre place est réservée</div>");
        html.append("</div>");
        
        // Contenu principal
        html.append("<div class='content'>");
        
        // Salutation
        html.append("<p class='greeting'>Bonjour <strong>").append(nomParticipant.toUpperCase()).append("</strong>,</p>");
        
        // Message de confirmation
        html.append("<p class='message'>");
        html.append("Nous sommes ravis de confirmer votre inscription ! Préparez-vous à vivre une expérience exceptionnelle avec notre équipe de professionnels.");
        html.append("</p>");
        
        // Détails de l'événement
        html.append("<div class='info-box'>");
        html.append("<div class='info-title'>🎯 Détails de l'événement</div>");
        html.append("<div class='info-row'><span class='info-label'>📌 Événement :</span><span class='info-value'>").append(evenement.getNom() != null ? evenement.getNom() : "Non spécifié").append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>📅 Date :</span><span class='info-value'>").append(dateDebut).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>⏰ Heure :</span><span class='info-value'>").append(heureDebut).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>📍 Lieu :</span><span class='info-value'>").append(evenement.getLieu() != null ? evenement.getLieu() : "Non spécifié").append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>👤 Coach :</span><span class='info-value'>").append(evenement.getCoach_name() != null ? evenement.getCoach_name() : "Non spécifié").append("</span></div>");
        
        // Prix
        if (evenement.getPrix() > 0) {
            html.append("<div class='info-row'><span class='info-label'>💰 Prix :</span><span class='info-value'>").append(evenement.getPrix()).append(" TND</span></div>");
        } else {
            html.append("<div class='info-row'><span class='info-label'>💰 Prix :</span><span class='info-value' style='color: #10b981;'>GRATUIT</span></div>");
        }
        html.append("</div>");
        
        // Météo (si événement extérieur)
        if (isOutdoor && weather != null) {
            html.append("<div class='weather-box'>");
            html.append("<div class='weather-title'>☀️ Météo prévue le jour de l'événement</div>");
            html.append("<div class='weather-content'>");
            html.append("<div class='weather-icon'>").append(weather.getWeatherEmoji()).append("</div>");
            html.append("<div>");
            html.append("<div class='weather-desc'>").append(weather.getDescription()).append("</div>");
            html.append("<div class='weather-temp'>🌡️ Température : ").append(weather.getFormattedTemp()).append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("<div class='weather-source'>⚠️ Prévisions météo pour mieux vous préparer</div>");
            html.append("</div>");
        }
        
        // Informations du participant
        html.append("<div class='info-box'>");
        html.append("<div class='info-title'>👤 Vos informations</div>");
        html.append("<div class='info-row'><span class='info-label'>Nom :</span><span class='info-value'>").append(nomParticipant.toUpperCase()).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>Email :</span><span class='info-value'>").append(emailParticipant).append("</span></div>");
        if (telephone != null && !telephone.trim().isEmpty()) {
            html.append("<div class='info-row'><span class='info-label'>Téléphone :</span><span class='info-value'>").append(telephone).append("</span></div>");
        }
        html.append("</div>");
        
        // Bouton Google Maps
        String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + 
                        java.net.URLEncoder.encode(evenement.getLieu(), java.nio.charset.StandardCharsets.UTF_8);
        html.append("<div style='text-align: center; margin: 25px 0;'>");
        html.append("<a href='").append(mapsUrl).append("' class='button'>🗺️ Voir le lieu sur Google Maps</a>");
        html.append("</div>");
        
        // Sponsors / Partenaires
        try {
            List<Sponsor> sponsors = sponsorService.getByEvenementId(evenement.getId());
            if (sponsors != null && !sponsors.isEmpty()) {
                html.append("<div class='sponsors-box'>");
                html.append("<div class='sponsors-title'>🤝 Nos Partenaires</div>");
                html.append("<div style='display: flex; flex-wrap: wrap; justify-content: center; align-items: center;'>");
                for (Sponsor sponsor : sponsors) {
                    if (sponsor.getLogo() != null && !sponsor.getLogo().isEmpty()) {
                        html.append("<img src='").append(sponsor.getLogo()).append("' ");
                        html.append("alt='").append(sponsor.getNom_partenaire()).append("' ");
                        html.append("class='sponsor-logo' />");
                    }
                }
                html.append("</div>");
                html.append("</div>");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la récupération des sponsors : " + e.getMessage());
            // Continuer sans sponsors
        }
        
        // Conseils pour la venue
        html.append("<div class='tips-box'>");
        html.append("<div class='tips-title'>💡 Conseils pour votre venue</div>");
        html.append("<div class='tip'>Arrivez 10 minutes avant le début pour vous préparer</div>");
        html.append("<div class='tip'>Apportez une tenue confortable adaptée à l'activité</div>");
        html.append("<div class='tip'>N'oubliez pas votre bouteille d'eau pour rester hydraté</div>");
        html.append("<div class='tip'>Prévoyez une serviette si nécessaire</div>");
        html.append("</div>");
        
        html.append("</div>"); // Fin content
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='footer-logo'>🏆</div>");
        html.append("<div class='footer-title'>Nutri Coach Pro</div>");
        html.append("<div class='footer-subtitle'>Votre partenaire pour une vie saine et active</div>");
        html.append("<div class='footer-subtitle'>Excellence • Passion • Résultats</div>");
        html.append("<div class='footer-note'>");
        html.append("Cet email a été envoyé automatiquement. Merci de ne pas y répondre.<br>");
        html.append("© 2026 Nutri Coach Pro. Tous droits réservés.");
        html.append("</div>");
        html.append("</div>");
        
        html.append("</div>"); // Fin container
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}
