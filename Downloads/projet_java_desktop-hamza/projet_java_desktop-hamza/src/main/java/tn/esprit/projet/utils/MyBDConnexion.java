package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {
    private static MyBDConnexion instance;
    private Connection connection;
    // Vérifiez bien le nom de la base ici : nutrilife_db d'après votre PHPMyAdmin
    private final String URL = "jdbc:mysql://localhost:3306/nutrilife_db";    private final String USER = "root";
    private final String PWD = "";

    private MyBDConnexion() {
        try {
            connection = DriverManager.getConnection(URL, USER, PWD);
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) instance = new MyBDConnexion();
        return instance;
    }

    // Indispensable pour EvenementService
    public Connection getConnection() { return connection; }

    // Indispensable pour IngredientService
    public Connection getCnx() { return connection; }
}