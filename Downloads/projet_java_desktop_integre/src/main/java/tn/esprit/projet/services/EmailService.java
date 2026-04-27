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
    private static final String EMAIL_PASSWORD = "dauz dluy rjox rjdz"; // App password Gmail

    /**
     * Envoie un email de confirmation d'inscription à un événement
     */
    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant, 
                                           String telephone, Evenement evenement) {
        try {
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

            String htmlContent = genererContenuEmail(nomParticipant, telephone, evenement);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à : " + emailParticipant);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String genererContenuEmail(String nomParticipant, String telephone, Evenement evenement) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateDebut = evenement.getDate_debut().toLocalDate().format(dateFormatter);
        String heureDebut = evenement.getDate_debut().toLocalTime().format(timeFormatter);

        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; padding: 20px;'>" +
               "<h2 style='color: #2E7D5A;'>🎉 Inscription Confirmée !</h2>" +
               "<p>Bonjour <strong>" + nomParticipant + "</strong>,</p>" +
               "<p>Vous êtes inscrit à l'événement : <strong>" + evenement.getNom() + "</strong></p>" +
               "<p>📍 Lieu : " + evenement.getLieu() + "</p>" +
               "<p>📅 Date : " + dateDebut + " à " + heureDebut + "</p>" +
               "<hr>" +
               "<p style='text-align: center;'>" +
               "<a href='https://www.google.com/maps/search/?api=1&query=" + 
               java.net.URLEncoder.encode(evenement.getLieu(), java.nio.charset.StandardCharsets.UTF_8) + 
               "' style='background-color: #3b82f6; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Voir sur Google Maps</a>" +
               "</p>" +
               "</div>";
    }
}
