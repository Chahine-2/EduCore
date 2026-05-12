package models;

import java.time.LocalDate;

public class Reservation {
    private int id;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nbPlaces;

    public Reservation() {}

    public Reservation(String titre, String description,
                       LocalDate dateDebut, LocalDate dateFin, int nbPlaces) {
        this.titre       = titre;
        this.description = description;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.nbPlaces    = nbPlaces;
    }

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getTitre()              { return titre; }
    public void setTitre(String t)        { this.titre = t; }

    public String getDescription()        { return description; }
    public void setDescription(String d)  { this.description = d; }

    public LocalDate getDateDebut()       { return dateDebut; }
    public void setDateDebut(LocalDate d) { this.dateDebut = d; }

    public LocalDate getDateFin()         { return dateFin; }
    public void setDateFin(LocalDate d)   { this.dateFin = d; }

    public int getNbPlaces()              { return nbPlaces; }
    public void setNbPlaces(int n)        { this.nbPlaces = n; }

    @Override
    public String toString() {
        return "Reservation{id=" + id +
                ", titre='" + titre + "'" +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", places=" + nbPlaces + "}\n";
    }
}