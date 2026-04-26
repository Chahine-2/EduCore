package models;

public class SousCategorie {
    private int id;
    private String nom;
    private String description;
    private int categorieId;
    private int ordre;
    private String statut;

    public SousCategorie() {}

    public SousCategorie(String nom, String description, int categorieId, int ordre, String statut) {
        this.nom = nom;
        this.description = description;
        this.categorieId = categorieId;
        this.ordre = ordre;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "SousCategorie{id=" + id + ", nom='" + nom + "', categorieId=" + categorieId + "}\n";
    }
}