package tn.esprit.projet.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Sends an "inactive user" signal email to the admin.
 * Mirrors the PHP SignalService from the previous project.
 *
 * SMTP credentials: sghaiersalim2004@gmail.com / app-password
 */
public class EmailSignalService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;
    private static final String USERNAME  = "sghaiersalim2004@gmail.com";
    private static final String PASSWORD  = "koabmwchljmpwoxq";   // Gmail app-password

    /**
     * Sends a nudge email FROM the admin account TO the user's own email.
     *
     * @param userName            full name of the inactive user
     * @param userEmail           email of the inactive user (this is the recipient)
     * @param lastObjectiveTitle  title of their last objective (or null)
     * @param inactiveSince       date since no active objective
     * @return true if sent successfully
     */
    public boolean sendSignal(String userName, String userEmail,
                              String lastObjectiveTitle, LocalDate inactiveSince) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust",       SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            String lastObj = (lastObjectiveTitle != null && !lastObjectiveTitle.isBlank())
                    ? "📋 Last objective: <em>" + lastObjectiveTitle + "</em>"
                    : "⚠️ No objective ever activated";

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:0 auto;">
                  <div style="background:#2E7D5A;padding:15px 20px;border-radius:10px 10px 0 0;">
                    <h2 style="color:#fff;margin:0;">🥗 NutriTrack — Time to get back on track!</h2>
                  </div>
                  <div style="background:#fff;padding:24px;border:1px solid #e5e7eb;border-top:none;">
                    <p style="font-size:15px;color:#1a1a2e;">Hi <strong>%s</strong>,</p>
                    <p style="color:#475569;">We noticed you haven't activated a nutrition objective yet.
                       Staying consistent with your goals is key to seeing results!</p>
                    %s
                    <div style="margin-top:20px;padding:14px;background:#f0fdf4;border-radius:8px;border-left:4px solid #2E7D5A;">
                      <strong style="color:#166534;">👉 Log in and activate your objective to keep your progress going.</strong>
                    </div>
                  </div>
                  <div style="background:#f3f4f6;padding:10px 20px;border-radius:0 0 10px 10px;border:1px solid #e5e7eb;border-top:none;">
                    <small style="color:#9ca3af;">NutriTrack • %s — This is an automated reminder from your admin.</small>
                  </div>
                </div>
                """.formatted(
                    userName,
                    lastObj.startsWith("📋")
                        ? "<p style='color:#475569;font-size:13px;'>%s</p>".formatted(lastObj)
                        : "<p style='color:#d97706;font-size:13px;'>%s</p>".formatted(lastObj),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(USERNAME, "NutriTrack Admin"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail)); // → sent to the user
            msg.setSubject("⚠️ NutriTrack: Activate your nutrition objective");
            msg.setContent(html, "text/html; charset=utf-8");

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Email signal failed: " + e.getMessage());
            return false;
        }
    }
}
