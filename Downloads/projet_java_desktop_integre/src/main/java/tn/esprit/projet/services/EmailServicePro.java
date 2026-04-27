package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.services.MeteoService;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service d'envoi d'emails de confirmation PROFESSIONNEL
 * Adapté pour Jakarta Mail
 */
public class EmailServicePro {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "hamzabezzine417@gmail.com";
    private static final String EMAIL_PASSWORD = "dauz dluy rjox rjdz";
    
    private static boolean MODE_SIMULATION = false;
    
    public static void setModeSimulation(boolean activer) {
        MODE_SIMULATION = activer;
    }

    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant,
                                           String telephone, Evenement evenement) {
        return envoyerEmailConfirmation(nomParticipant, emailParticipant, telephone, evenement, null);
    }

    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant,
                                           String telephone, Evenement evenement,
                                           MeteoService.MeteoResult meteo) {
        
        if (MODE_SIMULATION) {
            return true;
        }
        
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailParticipant.trim()));
            message.setSubject("✅ Confirmation d'inscription - " + evenement.getNom());

            String htmlContent = genererEmailProfessionnel(nomParticipant, telephone, evenement, meteo);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String genererEmailProfessionnel(String nomParticipant, String telephone,
                                             Evenement evenement,
                                             MeteoService.MeteoResult meteo) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateDebut = evenement.getDate_debut().toLocalDate().format(dateFormatter);
        String heureDebut = evenement.getDate_debut().toLocalTime().format(timeFormatter);
        String dateFin = evenement.getDate_fin().toLocalDate().format(dateFormatter);
        String heureFin = evenement.getDate_fin().toLocalTime().format(timeFormatter);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<h1 style='color: #1F4D3A;'>Inscription Confirmée !</h1>");
        html.append("<p>Bonjour <strong>").append(nomParticipant).append("</strong>,</p>");
        html.append("<p>Votre inscription à l'événement <strong>").append(evenement.getNom()).append("</strong> est confirmée.</p>");
        html.append("<ul>");
        html.append("<li><strong>Lieu:</strong> ").append(evenement.getLieu()).append("</li>");
        html.append("<li><strong>Début:</strong> ").append(dateDebut).append(" à ").append(heureDebut).append("</li>");
        html.append("<li><strong>Fin:</strong> ").append(dateFin).append(" à ").append(heureFin).append("</li>");
        html.append("<li><strong>Coach:</strong> ").append(evenement.getCoach_name()).append("</li>");
        html.append("</ul>");
        
        if (meteo != null && meteo.disponible) {
            html.append("<div style='background: #f0fdf4; padding: 10px; border-radius: 5px; margin-top: 10px;'>");
            html.append(meteo.messageEmail);
            html.append("</div>");
        }
        
        html.append("<p>À bientôt !</p>");
        html.append("</body></html>");
        return html.toString();
    }
}
