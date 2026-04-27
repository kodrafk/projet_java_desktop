package tn.esprit.projet.services;

import tn.esprit.projet.models.Utilisateur;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private final Connection connection;

    public UtilisateurService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
        creerTableSiNecessaire();
        insererAdminParDefaut();
    }

    // ── Création de la table si elle n'existe pas ──────────────────────────
    private void creerTableSiNecessaire() {
        String sql = """
                CREATE TABLE IF NOT EXISTS utilisateur (
                    id          INT AUTO_INCREMENT PRIMARY KEY,
                    nom         VARCHAR(100) NOT NULL,
                    prenom      VARCHAR(100) NOT NULL,
                    email       VARCHAR(150) NOT NULL UNIQUE,
                    mot_de_passe VARCHAR(255) NOT NULL,
                    role        VARCHAR(20)  NOT NULL DEFAULT 'user'
                )
                """;
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Erreur création table utilisateur : " + e.getMessage());
        }
    }

    // ── Insérer un admin par défaut si la table est vide ──────────────────
    private void insererAdminParDefaut() {
        try {
            String check = "SELECT COUNT(*) FROM utilisateur WHERE role = 'admin'";
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(check)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insert = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = connection.prepareStatement(insert)) {
                        ps.setString(1, "Admin");
                        ps.setString(2, "Super");
                        ps.setString(3, "admin@nutricoach.tn");
                        ps.setString(4, "admin123");
                        ps.setString(5, "admin");
                        ps.executeUpdate();
                        System.out.println("✅ Compte admin créé : admin@nutricoach.tn / admin123");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur insertion admin par défaut : " + e.getMessage());
        }
    }

    // ── Authentification ──────────────────────────────────────────────────
    /**
     * Vérifie les identifiants et retourne l'utilisateur si valide, null sinon.
     */
    public Utilisateur authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, motDePasse);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur authentification : " + e.getMessage());
        }
        return null;
    }

    // ── Inscription ───────────────────────────────────────────────────────
    public boolean inscrire(Utilisateur u) {
        // Vérifier si l'email existe déjà
        if (emailExiste(u.getEmail())) return false;

        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole() != null ? u.getRole() : "user");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur inscription : " + e.getMessage());
            return false;
        }
    }

    // ── Vérifier si email existe ──────────────────────────────────────────
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // ── Liste de tous les utilisateurs ───────────────────────────────────
    public List<Utilisateur> getAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY id";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur getAll utilisateurs : " + e.getMessage());
        }
        return list;
    }

    // ── Mapper ResultSet → Utilisateur ───────────────────────────────────
    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("role")
        );
    }
}
