package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.services.MeteoService;

import javax.mail.*;
import javax.mail.AuthenticationFailedException;
import javax.mail.internet.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service d'envoi d'emails de confirmation PROFESSIONNEL
 * Design moderne et harmonisé avec l'application
 */
public class EmailServicePro {

    // Configuration SMTP Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "hamzabezzine417@gmail.com";
    // Mot de passe d'application Gmail (16 caractères, sans espaces)
    private static final String EMAIL_PASSWORD = "dauz dluy rjox rjdz";
    
    // Mode simulation (pour tests sans envoyer de vrais emails)
    private static boolean MODE_SIMULATION = false;
    
    /**
     * Active/désactive le mode simulation
     */
    public static void setModeSimulation(boolean activer) {
        MODE_SIMULATION = activer;
        System.out.println(activer ? "🎭 Mode simulation activé" : "📧 Mode envoi réel activé");
    }

    /**
     * Envoie un email de confirmation avec design professionnel
     */
    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant,
                                           String telephone, Evenement evenement) {
        return envoyerEmailConfirmation(nomParticipant, emailParticipant, telephone, evenement, null);
    }

    /**
     * Envoie un email de confirmation avec météo intégrée
     */
    public boolean envoyerEmailConfirmation(String nomParticipant, String emailParticipant,
                                           String telephone, Evenement evenement,
                                           MeteoService.MeteoResult meteo) {
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  📧 ENVOI EMAIL DE CONFIRMATION                               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        
        // Validation des paramètres
        if (emailParticipant == null || emailParticipant.trim().isEmpty()) {
            System.err.println("❌ Adresse email invalide ou vide");
            return false;
        }
        
        if (nomParticipant == null || nomParticipant.trim().isEmpty()) {
            System.err.println("❌ Nom du participant invalide ou vide");
            return false;
        }
        
        if (evenement == null) {
            System.err.println("❌ Événement invalide");
            return false;
        }
        
        System.out.println("� Destinataire : " + emailParticipant);
        System.out.println("👤 Participant : " + nomParticipant);
        System.out.println("🎯 Événement : " + evenement.getNom());
        
        // Mode simulation
        if (MODE_SIMULATION) {
            System.out.println("\n🎭 MODE SIMULATION ACTIVÉ");
            System.out.println("   L'email ne sera PAS envoyé réellement");
            System.out.println("   Contenu généré avec succès :");
            System.out.println("   ✅ Destinataire : " + emailParticipant);
            System.out.println("   ✅ Sujet : Confirmation d'inscription - " + evenement.getNom());
            System.out.println("   ✅ Contenu HTML : " + genererEmailProfessionnel(nomParticipant, telephone, evenement, meteo).length() + " caractères");
            System.out.println("\n💡 Pour envoyer de vrais emails, désactivez le mode simulation");
            System.out.println("   EmailServicePro.setModeSimulation(false);");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
            return true; // Succès simulé
        }
        
        System.out.println("📤 Mode envoi réel activé");
        
        try {
            // Configuration SMTP optimisée
            System.out.println("\n🔧 Configuration SMTP...");
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.connectiontimeout", "15000");
            props.put("mail.smtp.timeout", "15000");
            props.put("mail.smtp.writetimeout", "15000");
            props.put("mail.debug", "false");
            
            System.out.println("   ✅ Host : " + SMTP_HOST);
            System.out.println("   ✅ Port : " + SMTP_PORT);
            System.out.println("   ✅ TLS : activé");
            
            // Créer la session avec authentification
            System.out.println("\n🔐 Authentification...");
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("   → Email : " + EMAIL_FROM);
                    System.out.println("   → Mot de passe : " + EMAIL_PASSWORD.substring(0, 4) + "..." + EMAIL_PASSWORD.substring(EMAIL_PASSWORD.length()-4));
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            session.setDebug(false);
            System.out.println("   ✅ Session créée");

            // Créer le message
            System.out.println("\n📝 Création du message...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "Nutri Coach Pro"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailParticipant.trim()));
            message.setSubject("✅ Confirmation d'inscription - " + evenement.getNom());

            // Générer et attacher le contenu HTML
            String htmlContent = genererEmailProfessionnel(nomParticipant, telephone, evenement, meteo);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            message.saveChanges();
            
            System.out.println("   ✅ Message créé (" + htmlContent.length() + " caractères)");

            // Envoyer le message
            System.out.println("\n📤 Envoi en cours...");
            System.out.println("   Connexion au serveur SMTP...");
            Transport.send(message);
            
            System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ EMAIL ENVOYÉ AVEC SUCCÈS !                                ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");
            System.out.println("📧 Destinataire : " + emailParticipant);
            System.out.println("🎯 Événement : " + evenement.getNom());
            System.out.println("⏰ Envoyé à : " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
            
            return true;

        } catch (AuthenticationFailedException e) {
            System.err.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.err.println("║  ❌ ERREUR AUTHENTIFICATION Gmail                             ║");
            System.err.println("╚═══════════════════════════════════════════════════════════════╝");
            System.err.println("📧 Email : " + EMAIL_FROM);
            System.err.println("🔑 Mot de passe : " + EMAIL_PASSWORD.substring(0, 4) + "..." + EMAIL_PASSWORD.substring(EMAIL_PASSWORD.length()-4));
            System.err.println("❌ Message : " + e.getMessage());
            System.err.println("\n💡 SOLUTIONS :");
            System.err.println("   1. Vérifiez que le mot de passe d'application est correct");
            System.err.println("      → 16 caractères sans espaces (format: xxxx xxxx xxxx xxxx)");
            System.err.println("   2. Vérifiez que la validation en 2 étapes est activée");
            System.err.println("      → https://myaccount.google.com/security");
            System.err.println("   3. Générez un nouveau mot de passe d'application");
            System.err.println("      → https://myaccount.google.com/apppasswords");
            System.err.println("   4. Activez le mode simulation pour tester sans envoyer");
            System.err.println("      → EmailServicePro.setModeSimulation(true);");
            System.err.println("╚═══════════════════════════════════════════════════════════════╝\n");
            e.printStackTrace();
            return false;
            
        } catch (MessagingException e) {
            System.err.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.err.println("║  ❌ ERREUR SMTP                                                ║");
            System.err.println("╚═══════════════════════════════════════════════════════════════╝");
            System.err.println("📧 Destinataire : " + emailParticipant);
            System.err.println("❌ Message : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("❌ Cause : " + e.getCause().getMessage());
            }
            System.err.println("\n💡 SOLUTIONS :");
            System.err.println("   1. Vérifiez votre connexion Internet");
            System.err.println("   2. Vérifiez l'adresse email du destinataire");
            System.err.println("   3. Vérifiez que le port 587 n'est pas bloqué");
            System.err.println("   4. Essayez de désactiver temporairement le pare-feu");
            System.err.println("   5. Activez le mode simulation pour tester sans envoyer");
            System.err.println("      → EmailServicePro.setModeSimulation(true);");
            System.err.println("╚═══════════════════════════════════════════════════════════════╝\n");
            e.printStackTrace();
            return false;
            
        } catch (Exception e) {
            System.err.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.err.println("║  ❌ ERREUR INATTENDUE                                          ║");
            System.err.println("╚═══════════════════════════════════════════════════════════════╝");
            System.err.println("❌ Type : " + e.getClass().getName());
            System.err.println("❌ Message : " + e.getMessage());
            System.err.println("\n💡 SOLUTION :");
            System.err.println("   Activez le mode simulation pour tester sans envoyer");
            System.err.println("   → EmailServicePro.setModeSimulation(true);");
            System.err.println("╚═══════════════════════════════════════════════════════════════╝\n");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère un email HTML ultra-professionnel
     */
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
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"fr\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Confirmation - Nutri Coach Pro</title>");
        html.append("<style>");
        html.append("@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }");
        html.append("body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px; }");
        html.append(".email-wrapper { max-width: 680px; margin: 0 auto; background: #ffffff; border-radius: 24px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }");
        html.append(".header { background: linear-gradient(135deg, #1F4D3A 0%, #2E7D5A 50%, #3D9B6F 100%); padding: 50px 40px; text-align: center; position: relative; }");
        html.append(".logo { width: 80px; height: 80px; background: rgba(255,255,255,0.2); border-radius: 20px; display: inline-flex; align-items: center; justify-content: center; font-size: 40px; margin-bottom: 20px; backdrop-filter: blur(10px); border: 2px solid rgba(255,255,255,0.3); }");
        html.append(".header h1 { color: #ffffff; font-size: 32px; font-weight: 800; margin-bottom: 12px; text-shadow: 0 2px 10px rgba(0,0,0,0.2); }");
        html.append(".header p { color: rgba(255,255,255,0.95); font-size: 16px; font-weight: 500; }");
        html.append(".success-badge { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 16px 32px; margin: -30px 40px 0; border-radius: 16px; box-shadow: 0 10px 30px rgba(16,185,129,0.3); position: relative; z-index: 2; }");
        html.append(".success-badge h2 { font-size: 24px; font-weight: 700; margin: 0; }");
        html.append(".content { padding: 50px 40px; }");
        html.append(".greeting { font-size: 20px; color: #1e293b; margin-bottom: 24px; font-weight: 600; }");
        html.append(".greeting strong { color: #1F4D3A; font-weight: 700; }");
        html.append(".intro-text { color: #64748b; font-size: 16px; margin-bottom: 32px; line-height: 1.7; }");
        html.append(".event-card { background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%); border: 2px solid #86efac; border-radius: 20px; padding: 32px; margin: 32px 0; position: relative; }");
        html.append(".event-card::before { content: ''; position: absolute; top: 0; left: 0; width: 6px; height: 100%; background: linear-gradient(180deg, #1F4D3A 0%, #2E7D5A 100%); }");
        html.append(".event-title { font-size: 26px; font-weight: 800; color: #1F4D3A; margin-bottom: 24px; display: flex; align-items: center; gap: 12px; }");
        html.append(".event-icon { width: 48px; height: 48px; background: linear-gradient(135deg, #1F4D3A 0%, #2E7D5A 100%); border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; font-size: 24px; }");
        html.append(".detail-row { display: flex; align-items: center; gap: 12px; padding: 12px; background: rgba(255,255,255,0.6); border-radius: 12px; margin-bottom: 12px; }");
        html.append(".detail-icon { width: 40px; height: 40px; background: linear-gradient(135deg, #1F4D3A 0%, #2E7D5A 100%); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; }");
        html.append(".detail-label { font-size: 12px; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }");
        html.append(".detail-value { font-size: 15px; color: #1e293b; font-weight: 600; }");
        html.append(".description-box { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); border-left: 4px solid #D6A46D; padding: 20px 24px; border-radius: 12px; margin: 24px 0; }");
        html.append(".description-box strong { color: #92400e; font-size: 14px; font-weight: 700; display: block; margin-bottom: 8px; }");
        html.append(".description-box p { color: #78350f; font-size: 14px; line-height: 1.6; margin: 0; }");
        html.append(".participant-info { background: #f8fafc; border-radius: 16px; padding: 24px; margin: 24px 0; }");
        html.append(".participant-info h3 { font-size: 16px; color: #1e293b; font-weight: 700; margin-bottom: 16px; }");
        html.append(".info-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid #e2e8f0; }");
        html.append(".info-item:last-child { border-bottom: none; }");
        html.append(".info-item strong { color: #475569; font-weight: 600; min-width: 100px; }");
        html.append(".info-item span { color: #1e293b; font-weight: 500; }");
        html.append(".map-button-container { text-align: center; margin: 32px 0; }");
        html.append(".map-button { display: inline-block; background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: white; text-decoration: none; padding: 18px 40px; border-radius: 14px; font-weight: 700; font-size: 16px; box-shadow: 0 10px 30px rgba(59,130,246,0.4); }");
        html.append(".tips-box { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); border-radius: 16px; padding: 24px; margin: 32px 0; }");
        html.append(".tips-box h3 { color: #1e40af; font-size: 16px; font-weight: 700; margin-bottom: 16px; }");
        html.append(".tips-list { list-style: none; padding: 0; margin: 0; }");
        html.append(".tips-list li { color: #1e40af; font-size: 14px; padding: 8px 0; padding-left: 28px; position: relative; }");
        html.append(".tips-list li::before { content: '✓'; position: absolute; left: 0; color: #10b981; font-weight: 700; font-size: 16px; }");
        html.append(".footer { background: linear-gradient(135deg, #1e293b 0%, #334155 100%); padding: 40px; text-align: center; }");
        html.append(".footer-logo { font-size: 24px; margin-bottom: 16px; }");
        html.append(".footer h3 { color: #ffffff; font-size: 20px; font-weight: 700; margin-bottom: 8px; }");
        html.append(".footer p { color: #94a3b8; font-size: 14px; margin: 8px 0; }");
        html.append(".footer-note { color: #64748b; font-size: 12px; margin-top: 24px; padding-top: 24px; border-top: 1px solid #334155; }");
        html.append("@media only screen and (max-width: 600px) { body { padding: 20px 10px; } .header { padding: 40px 24px; } .header h1 { font-size: 24px; } .content { padding: 32px 24px; } .event-card { padding: 24px; } .event-title { font-size: 20px; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"email-wrapper\">");
        
        // Header
        html.append("<div class=\"header\">");
        html.append("<div class=\"logo\">🏋️</div>");
        html.append("<h1>Inscription Confirmée !</h1>");
        html.append("<p>Nutri Coach Pro</p>");
        html.append("</div>");
        
        // Badge de succès
        html.append("<div class=\"success-badge\">");
        html.append("<h2>✅ Votre place est réservée</h2>");
        html.append("</div>");
        
        // Contenu
        html.append("<div class=\"content\">");
        html.append("<p class=\"greeting\">Bonjour <strong>").append(nomParticipant).append("</strong>,</p>");
        html.append("<p class=\"intro-text\">Nous sommes ravis de confirmer votre inscription ! Préparez-vous à vivre une expérience exceptionnelle avec notre équipe de professionnels.</p>");
        
        // Carte événement
        html.append("<div class=\"event-card\">");
        html.append("<div class=\"event-title\"><div class=\"event-icon\">🎯</div><span>").append(evenement.getNom()).append("</span></div>");
        
        html.append("<div class=\"detail-row\"><div class=\"detail-icon\">👤</div><div><div class=\"detail-label\">Coach</div><div class=\"detail-value\">").append(evenement.getCoach_name()).append("</div></div></div>");
        html.append("<div class=\"detail-row\"><div class=\"detail-icon\">📅</div><div><div class=\"detail-label\">Date de début</div><div class=\"detail-value\">").append(dateDebut).append(" à ").append(heureDebut).append("</div></div></div>");
        html.append("<div class=\"detail-row\"><div class=\"detail-icon\">🏁</div><div><div class=\"detail-label\">Date de fin</div><div class=\"detail-value\">").append(dateFin).append(" à ").append(heureFin).append("</div></div></div>");
        html.append("<div class=\"detail-row\"><div class=\"detail-icon\">📍</div><div><div class=\"detail-label\">Lieu</div><div class=\"detail-value\">").append(MeteoService.getLieuPropre(evenement.getLieu())).append("</div></div></div>");
        html.append("<div class=\"detail-row\"><div class=\"detail-icon\">✅</div><div><div class=\"detail-label\">Statut</div><div class=\"detail-value\">").append(evenement.getStatut()).append("</div></div></div>");
        html.append("</div>");
        
        // Description
        if (evenement.getDescription() != null && !evenement.getDescription().isEmpty()) {
            html.append("<div class=\"description-box\"><strong>📝 À propos de cet événement</strong><p>").append(evenement.getDescription()).append("</p></div>");
        }
        
        // ── Bloc météo ──
        if (meteo != null && meteo.disponible && !meteo.messageEmail.isBlank()) {
            html.append(meteo.messageEmail);
        }
        
        // Infos participant
        html.append("<div class=\"participant-info\"><h3>📋 Vos informations</h3>");
        html.append("<div class=\"info-item\"><strong>Nom :</strong><span>").append(nomParticipant).append("</span></div>");
        if (telephone != null && !telephone.isEmpty()) {
            html.append("<div class=\"info-item\"><strong>Téléphone :</strong><span>").append(telephone).append("</span></div>");
        }
        html.append("</div>");
        
        // Bouton Google Maps
        String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" +
                         java.net.URLEncoder.encode(MeteoService.getLieuPropre(evenement.getLieu()), java.nio.charset.StandardCharsets.UTF_8);
        html.append("<div class=\"map-button-container\"><a href=\"").append(mapsUrl).append("\" class=\"map-button\">🗺️ Voir le lieu sur Google Maps</a></div>");
        
        // Conseils
        html.append("<div class=\"tips-box\"><h3>💡 Conseils pour votre venue</h3><ul class=\"tips-list\">");
        html.append("<li>Arrivez 10 minutes avant le début pour vous préparer</li>");
        html.append("<li>Apportez une tenue confortable adaptée à l'activité</li>");
        html.append("<li>N'oubliez pas votre bouteille d'eau pour rester hydraté</li>");
        html.append("<li>Prévoyez une serviette si nécessaire</li>");
        html.append("</ul></div>");
        
        html.append("</div>");
        
        // Footer
        html.append("<div class=\"footer\">");
        html.append("<div class=\"footer-logo\">🏋️</div>");
        html.append("<h3>Nutri Coach Pro</h3>");
        html.append("<p>Votre partenaire pour une vie saine et active</p>");
        html.append("<p>Excellence • Passion • Résultats</p>");
        html.append("<p class=\"footer-note\">Cet email a été envoyé automatiquement. Merci de ne pas y répondre.<br>© 2026 Nutri Coach Pro. Tous droits réservés.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * Teste la configuration email
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
            message.setSubject("✅ Test - Design Professionnel");
            message.setText("Test du nouveau design professionnel !");

            Transport.send(message);
            System.out.println("✅ Email de test envoyé !");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
