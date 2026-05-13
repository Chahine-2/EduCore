package models;

import java.time.LocalDateTime;

/**
 * Model adapted to your current DB schema for `evaluation`:
 * columns: id, titre, description, note_max, note_passage, duree_minutes, date_debut, date_fin, date_creation
 */
public class Evaluation {
    private int id;
    private String titre;
    private String description;

    private int dureeMinutes;
    private float noteMax;
    private float notePassage;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateCreation;

    public Evaluation() {
    }

    public Evaluation(int id, String titre, String description, int dureeMinutes, float noteMax,
                      float notePassage, LocalDateTime dateDebut, LocalDateTime dateFin, LocalDateTime dateCreation) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dureeMinutes = dureeMinutes;
        this.noteMax = noteMax;
        this.notePassage = notePassage;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dateCreation = dateCreation;
    }

    public Evaluation(String titre, String description, int dureeMinutes, float noteMax,
                      float notePassage, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.titre = titre;
        this.description = description;
        this.dureeMinutes = dureeMinutes;
        this.noteMax = noteMax;
        this.notePassage = notePassage;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public float getNoteMax() {
        return noteMax;
    }

    public void setNoteMax(float noteMax) {
        this.noteMax = noteMax;
    }

    public float getNotePassage() {
        return notePassage;
    }

    public void setNotePassage(float notePassage) {
        this.notePassage = notePassage;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dureeMinutes=" + dureeMinutes +
                ", noteMax=" + noteMax +
                ", notePassage=" + notePassage +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
