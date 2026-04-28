package models;

public class Administrateur extends Utilisateur {
    private String niveauAcces;

    public Administrateur(int id, String nom, String prenom, int age, String email, int tel, String motDePasse, Role role, String niveauAcces) {
        super(id, nom, prenom, age, email, tel, motDePasse, role);
        this.niveauAcces = niveauAcces;
    }

    // Getters
    public String getNiveauAcces() { return niveauAcces; }

    // Setters
    public void setNiveauAcces(String niveauAcces) { this.niveauAcces = niveauAcces; }
}