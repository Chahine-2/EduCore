package models;

import java.time.LocalDateTime;

public class Materiel {
    private int id;
    private String nom;
    private String code;
    private String description;
    private int quantite;
    private String etat;
    private LocalDateTime dateCreation;

    public Materiel() {}

    public Materiel(String nom, String code, String description, int quantite, String etat) {
        this.nom = nom;
        this.code = code;
        this.description = description;
        this.quantite = quantite;
        this.etat = etat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Materiel{id=" + id + ", nom=" + nom + ", code=" + code + ", quantite=" + quantite + ", etat=" + etat + "}\n";
    }
}