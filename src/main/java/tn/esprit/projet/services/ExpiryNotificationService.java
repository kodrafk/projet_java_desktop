package tn.esprit.projet.services;

import tn.esprit.projet.utils.MyBDConnexion;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ExpiryNotificationService {

    private static final int DAYS_BEFORE_EXPIRY = 3;

    public static void checkAndNotify() {
        System.out.println("[ExpiryNotification] Verification des dates d'expiration...");

        // Force AWT non-headless AVANT tout appel SystemTray
        System.setProperty("java.awt.headless", "false");

        List<String> expiringIngredients = getExpiringIngredients();

        if (expiringIngredients.isEmpty()) {
            System.out.println("[ExpiryNotification] Aucun ingredient proche de l'expiration.");
            return;
        }

        sendWindowsNotification(expiringIngredients);
    }

    private static List<String> getExpiringIngredients() {
        List<String> expiring = new ArrayList<>();
        try {
            Connection cnx = MyBDConnexion.getInstance().getCnx();
            String sql = "SELECT nom, date_peremption FROM ingredient " +
                    "WHERE date_peremption IS NOT NULL " +
                    "AND date_peremption <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                    "AND date_peremption >= CURDATE()";
            PreparedStatement pst = cnx.prepareStatement(sql);
            pst.setInt(1, DAYS_BEFORE_EXPIRY);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String nom = rs.getString("nom");
                LocalDate dateExp = rs.getDate("date_peremption").toLocalDate();
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dateExp);
                if (daysLeft == 0)      expiring.add(nom + " (expires TODAY)");
                else if (daysLeft == 1) expiring.add(nom + " (expires tomorrow)");
                else                    expiring.add(nom + " (expires in " + daysLeft + " days)");
            }
        } catch (Exception e) {
            System.err.println("[ExpiryNotification] DB Error: " + e.getMessage());
        }
        return expiring;
    }

    private static void sendWindowsNotification(List<String> ingredients) {
        // Forcer AWT non-headless
        System.setProperty("java.awt.headless", "false");

        if (!SystemTray.isSupported()) {
            System.err.println("[ExpiryNotification] SystemTray non supportÃ©.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // IcÃ´ne : essayer logo.png, sinon carrÃ© vert gÃ©nÃ©rÃ©
            Image image;
            java.net.URL logoUrl = ExpiryNotificationService.class.getResource("/images/logo.png");
            if (logoUrl != null) {
                image = Toolkit.getDefaultToolkit().createImage(logoUrl);
            } else {
                BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = fallback.createGraphics();
                g.setColor(new Color(46, 125, 90));
                g.fillRect(0, 0, 16, 16);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("N", 4, 12);
                g.dispose();
                image = fallback;
            }

            TrayIcon trayIcon = new TrayIcon(image, "Nutri Coach Pro");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

            String title   = "Nutri Coach Pro - Expiry Alert";
            String message = buildMessage(ingredients);

            trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
            System.out.println("[ExpiryNotification] Notification envoyee : " + ingredients.size() + " ingredient(s)");

            // Retirer l'icÃ´ne aprÃ¨s 10 secondes
            Thread t = new Thread(() -> {
                try { Thread.sleep(10000); } catch (InterruptedException ignored) {}
                tray.remove(trayIcon);
            });
            t.setDaemon(true);
            t.start();

        } catch (Exception e) {
            System.err.println("[ExpiryNotification] Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String buildMessage(List<String> ingredients) {
        if (ingredients.size() == 1) {
            return ingredients.get(0) + "\nOpen Nutri Coach Pro to take action.";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(ingredients.size(), 3);
        for (int i = 0; i < limit; i++) {
            sb.append("â€¢ ").append(ingredients.get(i)).append("\n");
        }
        if (ingredients.size() > 3) {
            sb.append("... and ").append(ingredients.size() - 3).append(" more.");
        }
        return sb.toString();
    }
}

