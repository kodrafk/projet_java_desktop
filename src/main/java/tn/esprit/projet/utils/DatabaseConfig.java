package tn.esprit.projet.utils;

public class DatabaseConfig {
    public static final String HOST     = "localhost";
    public static final int    PORT     = 3306;
    public static final String DB_NAME  = "nutrilife_db";
    public static final String USER     = "root";
    public static final String PASSWORD = "";

    public static String getUrl() {
        return "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
                + "?useSSL=false"
                + "&serverTimezone=UTC"
                + "&autoReconnect=true"
                + "&failOverReadOnly=false"
                + "&maxReconnects=10"
                + "&connectTimeout=5000"
                + "&socketTimeout=30000"
                + "&useUnicode=true"
                + "&characterEncoding=UTF-8";
    }
}
