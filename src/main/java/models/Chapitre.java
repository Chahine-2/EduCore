package models;

public class Chapitre {
    private int id;
    private String titre;
    private String description;
    private int ordre;
    private int dureeMinutes;
    private boolean estGratuit;
    private int coursId;

    public Chapitre() {}

    public Chapitre(String titre, String description, int ordre, int dureeMinutes, boolean estGratuit, int coursId) {
        this.titre = titre;
        this.description = description;
        this.ordre = ordre;
        this.dureeMinutes = dureeMinutes;
        this.estGratuit = estGratuit;
        this.coursId = coursId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public boolean isEstGratuit() { return estGratuit; }
    public void setEstGratuit(boolean estGratuit) { this.estGratuit = estGratuit; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    @Override
    public String toString() {
        return "Chapitre{id=" + id + ", titre='" + titre + "', ordre=" + ordre + ", coursId=" + coursId + "}\n";
    }
}