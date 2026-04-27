package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service d'envoi d'emails de confirmation pour les inscriptions aux événements
 */
public class EmailService {

    // Configuration SMTP Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "hamzabezzine417@gmail.com";
    private static final String EMAIL_PASSWORD = "ulkx swnl quhd sesn"; // Mot de passe d'application Gmail

    /**
     * Envoie un email de confirmation d'inscription à un événement
     * 
     * @param nomParticipant Nom du participant
     * @param emailParticipant Email du participant
     * @param telephone Téléphone du participant (optionnel)
     * @param evenement Événement auquel le participant s'inscrit
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant, 
                                           String telephone, Evenement evenement) {
        try {
            // Configuration des propriétés SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Création de la session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "Nutri Coach Pro"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailParticipant));
            message.setSubject("✅ Confirmation d'inscription - " + evenement.getNom());

            // Contenu HTML de l'email
            String htmlContent = genererContenuEmail(nomParticipant, telephone, evenement);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Envoi de l'email
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à : " + emailParticipant);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère le contenu HTML de l'email de confirmation
     */
    private String genererContenuEmail(String nomParticipant, String telephone, Evenement evenement) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateDebut = evenement.getDate_debut().toLocalDate().format(dateFormatter);
        String heureDebut = evenement.getDate_debut().toLocalTime().format(timeFormatter);
        String dateFin = evenement.getDate_fin().toLocalDate().format(dateFormatter);
        String heureFin = evenement.getDate_fin().toLocalTime().format(timeFormatter);

        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <meta charset=\"UTF-8\">" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "    <style>" +
            "        body {" +
            "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +
            "            background-color: #f8fafc;" +
            "            margin: 0;" +
            "            padding: 20px;" +
            "        }" +
            "        .container {" +
            "            max-width: 600px;" +
            "            margin: 0 auto;" +
            "            background-color: white;" +
            "            border-radius: 16px;" +
            "            overflow: hidden;" +
            "            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);" +
            "        }" +
            "        .header {" +
            "            background: linear-gradient(135deg, #1F4D3A 0%, #2E7D5A 100%);" +
            "            color: white;" +
            "            padding: 40px 30px;" +
            "            text-align: center;" +
            "        }" +
            "        .header h1 {" +
            "            margin: 0;" +
            "            font-size: 28px;" +
            "            font-weight: bold;" +
            "        }" +
            "        .header p {" +
            "            margin: 10px 0 0 0;" +
            "            font-size: 16px;" +
            "            opacity: 0.9;" +
            "        }" +
            "        .content {" +
            "            padding: 40px 30px;" +
            "        }" +
            "        .greeting {" +
            "            font-size: 18px;" +
            "            color: #1e293b;" +
            "            margin-bottom: 20px;" +
            "        }" +
            "        .event-card {" +
            "            background-color: #f0fdf4;" +
            "            border: 2px solid #bbf7d0;" +
            "            border-radius: 12px;" +
            "            padding: 24px;" +
            "            margin: 24px 0;" +
            "        }" +
            "        .event-title {" +
            "            font-size: 22px;" +
            "            font-weight: bold;" +
            "            color: #1F4D3A;" +
            "            margin: 0 0 16px 0;" +
            "        }" +
            "        .event-info {" +
            "            display: flex;" +
            "            align-items: center;" +
            "            margin: 12px 0;" +
            "            color: #166534;" +
            "            font-size: 15px;" +
            "        }" +
            "        .event-info span {" +
            "            margin-right: 8px;" +
            "            font-size: 18px;" +
            "        }" +
            "        .description {" +
            "            background-color: #f8fafc;" +
            "            border-left: 4px solid #D6A46D;" +
            "            padding: 16px;" +
            "            margin: 20px 0;" +
            "            border-radius: 4px;" +
            "            color: #475569;" +
            "            font-style: italic;" +
            "        }" +
            "        .map-button {" +
            "            display: inline-block;" +
            "            background-color: #3b82f6;" +
            "            color: white;" +
            "            text-decoration: none;" +
            "            padding: 14px 28px;" +
            "            border-radius: 8px;" +
            "            font-weight: bold;" +
            "            margin: 20px 0;" +
            "            transition: background-color 0.3s;" +
            "        }" +
            "        .map-button:hover {" +
            "            background-color: #2563eb;" +
            "        }" +
            "        .footer {" +
            "            background-color: #f8fafc;" +
            "            padding: 30px;" +
            "            text-align: center;" +
            "            color: #64748b;" +
            "            font-size: 14px;" +
            "        }" +
            "        .footer strong {" +
            "            color: #1F4D3A;" +
            "        }" +
            "        .divider {" +
            "            height: 1px;" +
            "            background-color: #e2e8f0;" +
            "            margin: 24px 0;" +
            "        }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>🎉 Inscription Confirmée !</h1>" +
            "            <p>Nutri Coach Pro</p>" +
            "        </div>" +
            "        " +
            "        <div class=\"content\">" +
            "            <p class=\"greeting\">Bonjour <strong>" + nomParticipant + "</strong>,</p>" +
            "            " +
            "            <p>Nous sommes ravis de confirmer votre inscription à l'événement suivant :</p>" +
            "            " +
            "            <div class=\"event-card\">" +
            "                <h2 class=\"event-title\">🎯 " + evenement.getNom() + "</h2>" +
            "                " +
            "                <div class=\"event-info\">" +
            "                    <span>👤</span>" +
            "                    <strong>Coach :</strong>&nbsp;" + evenement.getCoach_name() +
            "                </div>" +
            "                " +
            "                <div class=\"event-info\">" +
            "                    <span>📅</span>" +
            "                    <strong>Date de début :</strong>&nbsp;" + dateDebut + " à " + heureDebut +
            "                </div>" +
            "                " +
            "                <div class=\"event-info\">" +
            "                    <span>🏁</span>" +
            "                    <strong>Date de fin :</strong>&nbsp;" + dateFin + " à " + heureFin +
            "                </div>" +
            "                " +
            "                <div class=\"event-info\">" +
            "                    <span>📍</span>" +
            "                    <strong>Lieu :</strong>&nbsp;" + evenement.getLieu() +
            "                </div>" +
            "                " +
            "                <div class=\"event-info\">" +
            "                    <span>✅</span>" +
            "                    <strong>Statut :</strong>&nbsp;" + evenement.getStatut() +
            "                </div>" +
            "            </div>" +
            "            " +
            (evenement.getDescription() != null && !evenement.getDescription().isEmpty() ?
            "            <div class=\"description\">" +
            "                <strong>📝 Description :</strong><br>" +
            "                " + evenement.getDescription() +
            "            </div>" : "") +
            "            " +
            "            <div class=\"divider\"></div>" +
            "            " +
            "            <p><strong>📋 Vos informations :</strong></p>" +
            "            <ul style=\"color: #475569;\">" +
            "                <li><strong>Nom :</strong> " + nomParticipant + "</li>" +
            (telephone != null && !telephone.isEmpty() ?
            "                <li><strong>Téléphone :</strong> " + telephone + "</li>" : "") +
            "            </ul>" +
            "            " +
            "            <div class=\"divider\"></div>" +
            "            " +
            "            <p style=\"text-align: center;\">" +
            "                <a href=\"https://www.google.com/maps/search/?api=1&query=" + 
            java.net.URLEncoder.encode(evenement.getLieu(), java.nio.charset.StandardCharsets.UTF_8) + 
            "\" class=\"map-button\">🗺️ Voir le lieu sur Google Maps</a>" +
            "            </p>" +
            "            " +
            "            <p style=\"color: #64748b; font-size: 14px; margin-top: 30px;\">" +
            "                <strong>💡 Conseils :</strong><br>" +
            "                • Arrivez 10 minutes avant le début<br>" +
            "                • Apportez une tenue confortable<br>" +
            "                • N'oubliez pas votre bouteille d'eau<br>" +
            "            </p>" +
            "        </div>" +
            "        " +
            "        <div class=\"footer\">" +
            "            <p><strong>Nutri Coach Pro</strong></p>" +
            "            <p>Votre partenaire pour une vie saine et active</p>" +
            "            <p style=\"margin-top: 20px; font-size: 12px;\">" +
            "                Cet email a été envoyé automatiquement, merci de ne pas y répondre." +
            "            </p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    /**
     * Teste la configuration email en envoyant un email de test
     */
    public boolean testerConfiguration(String emailTest) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "Nutri Coach Pro"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTest));
            message.setSubject("✅ Test de configuration - Nutri Coach Pro");
            message.setText("Ceci est un email de test. La configuration fonctionne correctement !");

            Transport.send(message);
            System.out.println("✅ Email de test envoyé avec succès !");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
