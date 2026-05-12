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
    private int salleId;
    private String salleNom;
    private String departementNom;
    private double latitude;
    private double longitude;

    public Materiel() {}

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
    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }
    public String getSalleNom() { return salleNom; }
    public void setSalleNom(String salleNom) { this.salleNom = salleNom; }
    public String getDepartementNom() { return departementNom; }
    public void setDepartementNom(String departementNom) { this.departementNom = departementNom; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Materiel{id=" + id + ", nom=" + nom + ", code=" + code + ", quantite=" + quantite + ", etat=" + etat + "}\n";
    }
}