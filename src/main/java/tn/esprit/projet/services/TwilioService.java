package tn.esprit.projet.services;

import java.io.InputStream;
import java.util.Properties;

/**
 * TwilioService — stub implementation.
 * To enable real SMS, add the Twilio SDK dependency to pom.xml and configure twilio.properties.
 */
public class TwilioService {

    private static TwilioService instance;

    private final String fromNumber;
    private boolean initialized = false;

    private TwilioService() {
        String from = "";
        try (InputStream in = getClass().getResourceAsStream("/twilio.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String sid   = props.getProperty("twilio.account_sid", "");
                String token = props.getProperty("twilio.auth_token", "");
                from  = props.getProperty("twilio.from_number", "");

                if (!sid.startsWith("YOUR") && !sid.isBlank()) {
                    // Twilio.init(sid, token); // Uncomment when Twilio SDK is added
                    initialized = false; // Set to true when SDK is available
                    System.out.println("[Twilio] Credentials found but SDK not available. Add twilio-java to pom.xml.");
                } else {
                    System.out.println("[Twilio] Not configured — update twilio.properties.");
                }
            } else {
                System.out.println("[Twilio] twilio.properties not found — SMS disabled.");
            }
        } catch (Exception e) {
            System.err.println("[Twilio] Init error: " + e.getMessage());
        }
        this.fromNumber = from;
    }

    public static TwilioService getInstance() {
        if (instance == null) instance = new TwilioService();
        return instance;
    }

    /**
     * Send an SMS to the given phone number.
     * @param toPhone  recipient phone in E.164 format, e.g. +21612345678
     * @param body     message text
     * @return true if sent successfully
     */
    public boolean sendSms(String toPhone, String body) {
        if (!initialized) {
            System.err.println("[Twilio] SMS not sent (SDK not configured): " + toPhone + " — " + body);
            return false;
        }
        // Real implementation requires Twilio SDK in pom.xml
        return false;
    }

    /**
     * Send an SMS and return the Twilio message SID (or null on failure).
     */
    public String sendSmsWithSid(String toPhone, String body) {
        sendSms(toPhone, body);
        return null;
    }

    public boolean isConfigured() { return initialized; }
}
