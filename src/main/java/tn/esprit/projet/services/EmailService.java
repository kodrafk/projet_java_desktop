package tn.esprit.projet.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Sends emails via Gmail SMTP (TLS, port 587).
 *
 * Configure your Gmail credentials below.
 * IMPORTANT: Use an App Password, not your real Gmail password.
 * Enable it at: https://myaccount.google.com/apppasswords
 */
public class EmailService {

    // ── Configure these ────────────────────────────────────────────────────────
    private static final String SMTP_HOST     = "smtp.gmail.com";
    private static final int    SMTP_PORT     = 587;
    private static final String SENDER_EMAIL  = "belhassenemna61@gmail.com"; // your Gmail
    private static final String SENDER_PASS   = "rtin knei ovsj ksrn";     // Gmail App Password
    private static final String SENDER_NAME   = "NutriLife";
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Send the 6-digit reset code to the user's email.
     * Returns true if sent successfully, false otherwise.
     */
    public boolean sendResetCode(String toEmail, String code) {
        String subject = "NutriLife — Your Password Reset Code";
        String body = buildEmailBody(code);
        return send(toEmail, subject, body);
    }

    private boolean send(String to, String subject, String htmlBody) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            SMTP_HOST);
            props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
            props.put("mail.smtp.ssl.trust",       SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASS);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(msg);
            System.out.println("[Email] Sent to: " + to);
            return true;

        } catch (Exception e) {
            System.err.println("[Email] Failed to send: " + e.getMessage());
            return false;
        }
    }

    private String buildEmailBody(String code) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;'>"
             + "<div style='max-width:480px;margin:auto;background:white;border-radius:16px;padding:32px;box-shadow:0 2px 12px rgba(0,0,0,0.08);'>"
             + "<h2 style='color:#2E7D32;margin-bottom:8px;'>🥗 NutriLife</h2>"
             + "<h3 style='color:#1a2e1a;'>Password Reset Code</h3>"
             + "<p style='color:#6B7280;font-size:14px;'>Use the code below to reset your password. It expires in <strong>15 minutes</strong>.</p>"
             + "<div style='background:#F0FDF4;border:2px solid #BBF7D0;border-radius:12px;padding:24px;text-align:center;margin:24px 0;'>"
             + "<span style='font-size:36px;font-weight:bold;letter-spacing:12px;color:#2E7D32;'>" + code + "</span>"
             + "</div>"
             + "<p style='color:#94A3B8;font-size:12px;'>If you did not request this, ignore this email. Your password will not change.</p>"
             + "</div></body></html>";
    }
}
