package tn.esprit.projet.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {

    // IMPORTANT: Replace with your actual Gmail and App Password
    private static final String SMTP_EMAIL = "moezkooli04@gmail.com";
    private static final String SMTP_PASSWORD = "tudhqmlkuyszxoix";

    /**
     * Sends an email in the background so the UI doesn't freeze.
     */
    public static CompletableFuture<Void> sendEmailAsync(String toAddress, String subject, String messageContent) {
        return CompletableFuture.runAsync(() -> {
            sendEmail(toAddress, subject, messageContent);
        });
    }

    private static void sendEmail(String toAddress, String subject, String messageContent) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Enforce TLS 1.2 for Gmail security

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_EMAIL, "NutriLife Admin"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            
            message.setContent(messageContent, "text/html; charset=utf-8");

            System.out.println("Attempting to send email notification to: " + toAddress + "...");
            Transport.send(message);
            System.out.println("Email successfully sent to: " + toAddress);

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
