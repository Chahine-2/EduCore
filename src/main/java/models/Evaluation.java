package models;

import java.time.LocalDateTime;

public class Evaluation {
    private int id;
    private String titre;
    private String description;
    private EvaluationType type;
    private int dureeMinutes;
    private float noteMax;
    private float notePassage;
    private int nbTentatives;
    private boolean ordreAleatoire;
    private boolean afficherCorrec;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private EvaluationStatut statut;
    private LocalDateTime dateCreation;

    public Evaluation() {
    }

    public Evaluation(int id, String titre, String description, EvaluationType type, int dureeMinutes, float noteMax,
                      float notePassage, int nbTentatives, boolean ordreAleatoire, boolean afficherCorrec,
                      LocalDateTime dateDebut, LocalDateTime dateFin, EvaluationStatut statut,
                      LocalDateTime dateCreation) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.dureeMinutes = dureeMinutes;
        this.noteMax = noteMax;
        this.notePassage = notePassage;
        this.nbTentatives = nbTentatives;
        this.ordreAleatoire = ordreAleatoire;
        this.afficherCorrec = afficherCorrec;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    public Evaluation(String titre, String description, EvaluationType type, int dureeMinutes, float noteMax,
                      float notePassage, int nbTentatives, boolean ordreAleatoire, boolean afficherCorrec,
                      LocalDateTime dateDebut, LocalDateTime dateFin, EvaluationStatut statut) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.dureeMinutes = dureeMinutes;
        this.noteMax = noteMax;
        this.notePassage = notePassage;
        this.nbTentatives = nbTentatives;
        this.ordreAleatoire = ordreAleatoire;
        this.afficherCorrec = afficherCorrec;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
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

    public EvaluationType getType() {
        return type;
    }

    public void setType(EvaluationType type) {
        this.type = type;
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

    public int getNbTentatives() {
        return nbTentatives;
    }

    public void setNbTentatives(int nbTentatives) {
        this.nbTentatives = nbTentatives;
    }

    public boolean isOrdreAleatoire() {
        return ordreAleatoire;
    }

    public void setOrdreAleatoire(boolean ordreAleatoire) {
        this.ordreAleatoire = ordreAleatoire;
    }

    public boolean isAfficherCorrec() {
        return afficherCorrec;
    }

    public void setAfficherCorrec(boolean afficherCorrec) {
        this.afficherCorrec = afficherCorrec;
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

    public EvaluationStatut getStatut() {
        return statut;
    }

    public void setStatut(EvaluationStatut statut) {
        this.statut = statut;
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
                ", type=" + type +
                ", dureeMinutes=" + dureeMinutes +
                ", noteMax=" + noteMax +
                ", notePassage=" + notePassage +
                ", nbTentatives=" + nbTentatives +
                ", ordreAleatoire=" + ordreAleatoire +
                ", afficherCorrec=" + afficherCorrec +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut=" + statut +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
