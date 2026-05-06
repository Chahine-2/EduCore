package models;

public class Hackathon {
    private int id;
    private String nom;
    private String categorie;
    private int duree;
    private double prix;
    private int reservationId;

    public Hackathon() {}

    public Hackathon(String nom, String categorie, int duree,
                     double prix, int reservationId) {
        this.nom           = nom;
        this.categorie     = categorie;
        this.duree         = duree;
        this.prix          = prix;
        this.reservationId = reservationId;
    }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getNom()                   { return nom; }
    public void setNom(String n)             { this.nom = n; }

    public String getCategorie()             { return categorie; }
    public void setCategorie(String c)       { this.categorie = c; }

    public int getDuree()                    { return duree; }
    public void setDuree(int d)              { this.duree = d; }

    public double getPrix()                  { return prix; }
    public void setPrix(double p)            { this.prix = p; }

    public int getReservationId()            { return reservationId; }
    public void setReservationId(int rid)    { this.reservationId = rid; }

    @Override
    public String toString() {
        return "Acaton{id=" + id +
                ", nom='" + nom + "'" +
                ", categorie='" + categorie + "'" +
                ", duree=" + duree + "min" +
                ", prix=" + prix + "DT}\n";
    }
}
