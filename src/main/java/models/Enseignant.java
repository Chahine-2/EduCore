package models;

public class Enseignant extends Utilisateur {
    private String specialite;
    private String matricule;

    public Enseignant(int id, String nom, String prenom, int age, String email, int tel, String motDePasse, Role role, String specialite, String matricule, boolean statutActif) {
        super(id, nom, prenom, age, email, tel, motDePasse, role, statutActif);
        this.specialite = specialite;
        this.matricule = matricule;
    }

    // Getters
    public String getSpecialite() { return specialite; }
    public String getMatricule() { return matricule; }

    // Setters
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
}