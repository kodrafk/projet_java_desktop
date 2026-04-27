import os

files_to_fix = {
    "c:/Users/Lenovo/IdeaProjects/validCRUD/projetKitchenComplet/projetJAV/.gitignore": """target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### IntelliJ IDEA ###
.idea/modules.xml
.idea/jarRepositories.xml
.idea/compiler.xml
.idea/libraries/
*.iws
*.iml
*.ipr

### Eclipse ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Mac OS ###
.DS_Store

### Local config (contains API keys — never commit) ###
config.properties
uploads/
""",
    "c:/Users/Lenovo/IdeaProjects/validCRUD/projetKitchenComplet/projetJAV/src/main/java/tn/esprit/projet/models/Ingredient.java": """package tn.esprit.projet.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.time.LocalDate;
import java.util.Objects;

public class Ingredient {

    // ═══════════ ATTRIBUTS ═══════════
    private int id;
    private String nom;
    private String nomEn;
    private String categorie;
    private double quantite;
    private String unite;
    private LocalDate datePeremption;
    private String notes;
    private String image;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    // ═══════════ CONSTRUCTEURS ═══════════

    /** Constructeur vide (pour JavaFX TableView) */
    public Ingredient() {
    }

    /** Constructeur complet (SANS id - pour création) */
    public Ingredient(String nom, String nomEn, String categorie, double quantite,
                      String unite, LocalDate datePeremption, String notes, String image) {
        this.nom = nom;
        this.nomEn = nomEn;
        this.categorie = categorie;
        this.quantite = quantite;
        this.unite = unite;
        this.datePeremption = datePeremption;
        this.notes = notes;
        this.image = image;
    }

    /** Constructeur complet (AVEC id - pour modification/affichage) */
    public Ingredient(int id, String nom, String nomEn, String categorie, double quantite,
                      String unite, LocalDate datePeremption, String notes, String image) {
        this.id = id;
        this.nom = nom;
        this.nomEn = nomEn;
        this.categorie = categorie;
        this.quantite = quantite;
        this.unite = unite;
        this.datePeremption = datePeremption;
        this.notes = notes;
        this.image = image;
    }

    // ═══════════ GETTERS / SETTERS ═══════════

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomEn() {
        return nomEn;
    }

    public void setNomEn(String nomEn) {
        this.nomEn = nomEn;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // ═══════════ MÉTHODES UTILITAIRES ═══════════

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", quantite=" + quantite +
                ", unite='" + unite + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
""",
    "c:/Users/Lenovo/IdeaProjects/validCRUD/projetKitchenComplet/projetJAV/src/main/java/tn/esprit/projet/services/IngredientService.java": """package tn.esprit.projet.services;

import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientService implements CRUD<Ingredient> {

    private final Connection cnx;

    public IngredientService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
    }

    @Override
    public void ajouter(Ingredient ingredient) {
        String query = "INSERT INTO ingredient (nom, nom_en, categorie, quantite, unite, date_peremption, notes, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ingredient.getNom());
            ps.setString(2, ingredient.getNomEn());
            ps.setString(3, ingredient.getCategorie());
            ps.setDouble(4, ingredient.getQuantite());
            ps.setString(5, ingredient.getUnite());

            if (ingredient.getDatePeremption() != null) {
                ps.setDate(6, Date.valueOf(ingredient.getDatePeremption()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, ingredient.getNotes());
            ps.setString(8, ingredient.getImage());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ingredient.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Ingredient ajouté avec succès: " + ingredient.getNom() + " (ID: " + ingredient.getId() + ")");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Ingredient ingredient) {
        String query = "UPDATE ingredient SET nom = ?, nom_en = ?, categorie = ?, quantite = ?, " +
                "unite = ?, date_peremption = ?, notes = ?, image = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, ingredient.getNom());
            ps.setString(2, ingredient.getNomEn());
            ps.setString(3, ingredient.getCategorie());
            ps.setDouble(4, ingredient.getQuantite());
            ps.setString(5, ingredient.getUnite());

            if (ingredient.getDatePeremption() != null) {
                ps.setDate(6, Date.valueOf(ingredient.getDatePeremption()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, ingredient.getNotes());
            ps.setString(8, ingredient.getImage());
            ps.setInt(9, ingredient.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Ingredient modifié avec succès: ID " + ingredient.getId());
            } else {
                System.out.println("Aucun ingredient trouvé avec l'ID: " + ingredient.getId());
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM ingredient WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Ingredient supprimé avec succès: ID " + id);
            } else {
                System.out.println("Aucun ingredient trouvé avec l'ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public Ingredient getById(int id) {
        String query = "SELECT * FROM ingredient WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIngredient(rs);
                } else {
                    System.out.println("Aucun ingredient trouvé avec l'ID : " + id);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Ingredient> getAll() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredient ORDER BY id DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }

        return ingredients;
    }

    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();

        ingredient.setId(rs.getInt("id"));
        ingredient.setNom(rs.getString("nom"));
        ingredient.setNomEn(rs.getString("nom_en"));
        ingredient.setCategorie(rs.getString("categorie"));
        ingredient.setQuantite(rs.getDouble("quantite"));
        ingredient.setUnite(rs.getString("unite"));

        Date datePeremption = rs.getDate("date_peremption");
        if (datePeremption != null) {
            ingredient.setDatePeremption(datePeremption.toLocalDate());
        }

        ingredient.setNotes(rs.getString("notes"));
        ingredient.setImage(rs.getString("image"));

        return ingredient;
    }

    public List<Ingredient> rechercherParNom(String nom) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredient WHERE nom LIKE ? ORDER BY nom";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, "%" + nom + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(mapResultSetToIngredient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }

        return ingredients;
    }

    public void updateQuantite(int id, double delta) {
        String query = "UPDATE ingredient SET quantite = quantite + ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setDouble(1, delta);
            ps.setInt(2, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Quantité de l'ingrédient (ID: " + id + ") mise à jour de " + delta);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de la quantité: " + e.getMessage());
        }
    }
}
""",
    "c:/Users/Lenovo/IdeaProjects/validCRUD/projetKitchenComplet/projetJAV/src/main/java/tn/esprit/projet/utils/MyBDConnexion.java": """package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {

    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/nutricoachpro?serverTimezone=UTC&sslMode=DISABLED";

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
""",
    "c:/Users/Lenovo/IdeaProjects/validCRUD/projetKitchenComplet/projetJAV/src/main/java/tn/esprit/projet/MainApp.java": """package tn.esprit.projet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 1100, 720);
        primaryStage.setTitle("NutriLife - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
""",
    "c:/Users/Lenovo/IdeaProjects/validCRUD/projetKitchenComplet/projetJAV/pom.xml": """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>tn.esprit</groupId>
    <artifactId>projetJAV</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>17.0.12</javafx.version>
    </properties>

    <dependencies>
        <!-- MySQL Connector -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>

        <!-- JavaFX Controls -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- JavaFX FXML -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itextpdf</artifactId>
            <version>5.5.13.3</version>
        </dependency>

        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        <!-- 1. ZXing : Lire les codes-barres -->
        <dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>core</artifactId>
            <version>3.5.2</version>
        </dependency>

        <dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>javase</artifactId>
            <version>3.5.2</version>
        </dependency>
        <!-- 3. Webcam Capture : Accès webcam  -->
        <dependency>
            <groupId>com.github.sarxos</groupId>
            <artifactId>webcam-capture</artifactId>
            <version>0.3.12</version>
        </dependency>

        <!-- BCrypt for password hashing -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

        <!-- Jackson for JSON roles parsing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>

        <!-- JavaFX Swing for image loading -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-swing</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- JavaFX Web for Lottie animations -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-web</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- Jakarta Mail for sending email signals -->
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>jakarta.mail</artifactId>
            <version>2.0.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin Maven JavaFX -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>tn.esprit.projet.MainApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
"""
}

for path, content in files_to_fix.items():
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
        print(f"Fixed {path}")
