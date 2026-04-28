package models;

public class Etudiant extends Utilisateur {
    private String numeroEtudiant;
    private String classe;

    public Etudiant(int id, String nom, String prenom, int age, String email, int tel, String motDePasse, Role role, String numeroEtudiant, String classe) {
        super(id, nom, prenom, age, email, tel, motDePasse, role);
        this.numeroEtudiant = numeroEtudiant;
        this.classe = classe;
    }

    // Getters
    public String getNumeroEtudiant() { return numeroEtudiant; }
    public String getClasse() { return classe; }

    // Setters
    public void setNumeroEtudiant(String numeroEtudiant) { this.numeroEtudiant = numeroEtudiant; }
    public void setClasse(String classe) { this.classe = classe; }
}