package models;

import java.time.LocalDate;

public class Cours {
    private int id;
    private String titre;
    private String description;
    private String objectifs;
    private int dureeHeures;
    private String niveau;
    private String categorie;
    private boolean estCertifiant;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean visible = true;   // ← ajouté

    public Cours() {}

    public Cours(String titre, String description, String objectifs,
                 int dureeHeures, String niveau, String categorie,
                 boolean estCertifiant,
                 LocalDate dateDebut, LocalDate dateFin) {
        this.titre         = titre;
        this.description   = description;
        this.objectifs     = objectifs;
        this.dureeHeures   = dureeHeures;
        this.niveau        = niveau;
        this.categorie     = categorie;
        this.estCertifiant = estCertifiant;
        this.dateDebut     = dateDebut;
        this.dateFin       = dateFin;
        this.visible       = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getObjectifs() { return objectifs; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }

    public int getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(int dureeHeures) { this.dureeHeures = dureeHeures; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public boolean isEstCertifiant() { return estCertifiant; }
    public void setEstCertifiant(boolean estCertifiant) { this.estCertifiant = estCertifiant; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public boolean isVisible() { return visible; }        // ← ajouté
    public void setVisible(boolean visible) { this.visible = visible; }  // ← ajouté

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", titre='" + titre + "'" +
                ", niveau='" + niveau + "'" +
                ", categorie='" + categorie + "'" +
                ", duree=" + dureeHeures + "h" +
                ", certifiant=" + estCertifiant +
                ", visible=" + visible +
                ", debut=" + dateDebut +
                ", fin=" + dateFin +
                "}\n";
    }
}