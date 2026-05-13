package models;

import java.time.LocalDateTime;

public class ReservationMateriel {
    private int id;
    private int materielId;
    private String motif;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String statut;
    private LocalDateTime dateCreation;

    public ReservationMateriel() {}

    public ReservationMateriel(int materielId, String motif, LocalDateTime dateDebut,
                       LocalDateTime dateFin, String statut) {
        this.materielId = materielId;
        this.motif = motif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMaterielId() { return materielId; }
    public void setMaterielId(int materielId) { this.materielId = materielId; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", materielId=" + materielId + ", motif=" + motif + ", statut=" + statut + "}\n";
    }
}