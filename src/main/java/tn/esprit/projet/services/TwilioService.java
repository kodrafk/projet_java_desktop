package tn.esprit.projet.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.InputStream;
import java.util.Properties;

/**
 * TwilioService — sends SMS messages via Twilio.
 * Configure credentials in src/main/resources/twilio.properties
 */
public class TwilioService {

    private static TwilioService instance;

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private boolean initialized = false;

    private TwilioService() {
        String sid = "", token = "", from = "";
        try (InputStream in = getClass().getResourceAsStream("/twilio.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                sid   = props.getProperty("twilio.account_sid", "");
                token = props.getProperty("twilio.auth_token", "");
                from  = props.getProperty("twilio.from_number", "");

                if (!sid.startsWith("YOUR") && !sid.isBlank()) {
                    Twilio.init(sid, token);
                    initialized = true;
                    System.out.println("[Twilio] Initialized successfully.");
                } else {
                    System.out.println("[Twilio] Not configured — update twilio.properties.");
                }
            } else {
                System.err.println("[Twilio] twilio.properties not found.");
            }
        } catch (Exception e) {
            System.err.println("[Twilio] Init error: " + e.getMessage());
        }
        this.accountSid = sid;
        this.authToken  = token;
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
            System.err.println("[Twilio] Cannot send SMS — not initialized. Check twilio.properties.");
            return false;
        }
        if (toPhone == null || toPhone.isBlank()) {
            System.err.println("[Twilio] Cannot send SMS — recipient phone number is empty.");
            return false;
        }
        try {
            Message msg = Message.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(fromNumber),
                body
            ).create();
            System.out.println("[Twilio] ✅ SMS sent. SID: " + msg.getSid() + " | Status: " + msg.getStatus());
            return true;
        } catch (Exception e) {
            System.err.println("[Twilio] ❌ Failed to send SMS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send an SMS and return the Twilio message SID (or null on failure).
     * @param toPhone  recipient phone in E.164 format
     * @param body     message text
     * @return Twilio SID string, or null if failed
     */
    public String sendSmsWithSid(String toPhone, String body) {
        if (!initialized) {
            System.err.println("[Twilio] Cannot send SMS — not initialized. Check twilio.properties.");
            return null;
        }
        if (toPhone == null || toPhone.isBlank()) {
            System.err.println("[Twilio] Cannot send SMS — recipient phone number is empty.");
            return null;
        }
        try {
            Message msg = Message.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(fromNumber),
                body
            ).create();
            System.out.println("[Twilio] ✅ SMS sent. SID: " + msg.getSid() + " | Status: " + msg.getStatus());
            return msg.getSid();
        } catch (Exception e) {
            System.err.println("[Twilio] ❌ Failed to send SMS: " + e.getMessage());
            return null;
        }
    }

    public boolean isConfigured() { return initialized; }
}
