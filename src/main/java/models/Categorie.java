package models;

public class Categorie {
    private int id;
    private String nom;
    private String description;
    private String icone;
    private int ordre;
    private String statut; // "active" ou "inactive"

    public Categorie() {}

    public Categorie(String nom, String description, String icone, int ordre, String statut) {
        this.nom = nom;
        this.description = description;
        this.icone = icone;
        this.ordre = ordre;
        this.statut = statut;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }



    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom='" + nom + "', statut='" + statut + "'}\n";
    }
}