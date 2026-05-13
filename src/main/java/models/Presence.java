package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Presence {
    private int id;
    private int etudiantId;
    private int coursId;
    private LocalDate datePresence;
    private boolean estPresent;
    private String notes;
    private LocalDateTime dateEnregistrement;

    public Presence() {}

    public Presence(int etudiantId, int coursId, LocalDate datePresence, boolean estPresent) {
        this.etudiantId = etudiantId;
        this.coursId = coursId;
        this.datePresence = datePresence;
        this.estPresent = estPresent;
        this.dateEnregistrement = LocalDateTime.now();
    }

    public Presence(int etudiantId, int coursId, LocalDate datePresence, boolean estPresent, String notes) {
        this(etudiantId, coursId, datePresence, estPresent);
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public LocalDate getDatePresence() { return datePresence; }
    public void setDatePresence(LocalDate datePresence) { this.datePresence = datePresence; }

    public boolean isEstPresent() { return estPresent; }
    public void setEstPresent(boolean estPresent) { this.estPresent = estPresent; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getDateEnregistrement() { return dateEnregistrement; }
    public void setDateEnregistrement(LocalDateTime dateEnregistrement) { this.dateEnregistrement = dateEnregistrement; }

    @Override
    public String toString() {
        return "Presence{" +
                "id=" + id +
                ", etudiantId=" + etudiantId +
                ", coursId=" + coursId +
                ", datePresence=" + datePresence +
                ", estPresent=" + estPresent +
                ", notes='" + notes + '\'' +
                ", dateEnregistrement=" + dateEnregistrement +
                '}';
    }
}

