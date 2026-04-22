package tn.esprit.projet.utils;

public class DatabaseConfig {
    public static final String HOST     = "localhost";
    public static final int    PORT     = 3306;
    public static final String DB_NAME  = "nutriCoachpro";
    public static final String USER     = "root";
    public static final String PASSWORD = "";

    public static String getUrl() {
        return "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
                + "?serverTimezone=UTC&sslMode=DISABLED";
    }
}
