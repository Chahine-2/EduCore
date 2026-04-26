package models;

public class Cours {
    private int id;
    private String titre;
    private String description;
    private String objectifs;
    private String prerequis;
    private int dureeHeures;
    private String niveau;       // debutant / intermediaire / avance
    private String langue;
    private int categorieId;
    private int sousCategorieId;
    private int nbInscrits;      // géré automatiquement par la base de données
    private int nbChapitres;
    private String statut;       // brouillon / publie / archive

    public Cours() {}

    public Cours(String titre, String description, String objectifs, String prerequis,
                 int dureeHeures, String niveau, String langue, int categorieId,
                 String statut) {
        this.titre = titre;
        this.description = description;
        this.objectifs = objectifs;
        this.prerequis = prerequis;
        this.dureeHeures = dureeHeures;
        this.niveau = niveau;
        this.langue = langue;
        this.categorieId = categorieId;
        this.statut = statut;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getObjectifs() { return objectifs; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }

    public String getPrerequis() { return prerequis; }
    public void setPrerequis(String prerequis) { this.prerequis = prerequis; }

    public int getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(int dureeHeures) { this.dureeHeures = dureeHeures; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public int getSousCategorieId() { return sousCategorieId; }
    public void setSousCategorieId(int sousCategorieId) { this.sousCategorieId = sousCategorieId; }

    // nb_inscrits est géré automatiquement par la DB (lecture seule)
    public int getNbInscrits() { return nbInscrits; }
    public void setNbInscrits(int nbInscrits) { this.nbInscrits = nbInscrits; }

    public int getNbChapitres() { return nbChapitres; }
    public void setNbChapitres(int nbChapitres) { this.nbChapitres = nbChapitres; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Cours{id=" + id + ", titre='" + titre + "', niveau='" + niveau +
                "', statut='" + statut + "', categorieId=" + categorieId + "}\n";
    }
}