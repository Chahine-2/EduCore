package models;

public class Etudiant extends Utilisateur {
    private String numeroEtudiant;
    private String classe;
    private String statutAppel = "En attente";

    public Etudiant(int id, String nom, String prenom, int age, String email, int tel, String motDePasse, Role role, String numeroEtudiant, String classe, boolean statutActif) {
        super(id, nom, prenom, age, email, tel, motDePasse, role, statutActif);
        this.numeroEtudiant = numeroEtudiant;
        this.classe = classe;
    }

    // Getters
    public String getNumeroEtudiant() { return numeroEtudiant; }
    public String getClasse() { return classe; }
    public String getStatutAppel() {
        return statutAppel;
    }

    // Setters
    public void setNumeroEtudiant(String numeroEtudiant) { this.numeroEtudiant = numeroEtudiant; }
    public void setClasse(String classe) { this.classe = classe; }
    public void setStatutAppel(String statutAppel) {
        this.statutAppel = statutAppel;
    }
}