package models;

import java.time.LocalDateTime;

/**
 * Audit record for anti-cheat detections during an evaluation attempt.
 */
public class FraudeLog {
    private int id;
    private int resultatId;
    private int etudiantId;
    private String typeFraude;
    private String description;
    private LocalDateTime dateDetection;

    public FraudeLog() {
    }

    public FraudeLog(int resultatId, int etudiantId, String typeFraude, String description) {
        this.resultatId = resultatId;
        this.etudiantId = etudiantId;
        this.typeFraude = typeFraude;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResultatId() {
        return resultatId;
    }

    public void setResultatId(int resultatId) {
        this.resultatId = resultatId;
    }

    public int getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(int etudiantId) {
        this.etudiantId = etudiantId;
    }

    public String getTypeFraude() {
        return typeFraude;
    }

    public void setTypeFraude(String typeFraude) {
        this.typeFraude = typeFraude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateDetection() {
        return dateDetection;
    }

    public void setDateDetection(LocalDateTime dateDetection) {
        this.dateDetection = dateDetection;
    }
}
