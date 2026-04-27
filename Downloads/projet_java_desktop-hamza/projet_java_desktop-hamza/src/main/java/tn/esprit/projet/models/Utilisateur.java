package tn.esprit.projet.models;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role; // "admin" ou "user"

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom, String email, String motDePasse, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters
    public int getId()           { return id; }
    public String getNom()       { return nom; }
    public String getPrenom()    { return prenom; }
    public String getEmail()     { return email; }
    public String getMotDePasse(){ return motDePasse; }
    public String getRole()      { return role; }

    // Setters
    public void setId(int id)                  { this.id = id; }
    public void setNom(String nom)             { this.nom = nom; }
    public void setPrenom(String prenom)       { this.prenom = prenom; }
    public void setEmail(String email)         { this.email = email; }
    public void setMotDePasse(String mdp)      { this.motDePasse = mdp; }
    public void setRole(String role)           { this.role = role; }

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + role + ")";
    }
}
