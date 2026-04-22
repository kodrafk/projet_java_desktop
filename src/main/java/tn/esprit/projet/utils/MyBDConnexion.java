package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {

    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/nutriCoachpro?serverTimezone=UTC&sslMode=DISABLED";

    private static MyBDConnexion instance;
    private Connection cnx;

    private MyBDConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion DB établie");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}