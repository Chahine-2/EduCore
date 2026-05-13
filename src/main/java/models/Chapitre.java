package models;

import java.time.LocalDate;

public class Chapitre {
    private int id;
    private String titre;
    private String description;
    private int ordre;
    private int dureeMinutes;
    private String typeContenu;
    private String urlContenu;
    private LocalDate dateCreation;
    private int coursId;
    private boolean visible = true;

    public Chapitre() {}

    public Chapitre(String titre, String description, int ordre,
                    int dureeMinutes, String typeContenu,
                    String urlContenu, LocalDate dateCreation, int coursId) {
        this.titre         = titre;
        this.description   = description;
        this.ordre         = ordre;
        this.dureeMinutes  = dureeMinutes;
        this.typeContenu   = typeContenu;
        this.urlContenu    = urlContenu;
        this.dateCreation  = dateCreation;
        this.coursId       = coursId;
        this.visible       = true;
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

    public String getTypeContenu() { return typeContenu; }
    public void setTypeContenu(String typeContenu) { this.typeContenu = typeContenu; }

    public String getUrlContenu() { return urlContenu; }
    public void setUrlContenu(String urlContenu) { this.urlContenu = urlContenu; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public boolean isVisible() { return visible; }        // ← ajouté
    public void setVisible(boolean visible) { this.visible = visible; }  // ← ajouté

    @Override
    public String toString() {
        return "Chapitre{" +
                "id=" + id +
                ", titre='" + titre + "'" +
                ", ordre=" + ordre +
                ", type='" + typeContenu + "'" +
                ", duree=" + dureeMinutes + "min" +
                ", visible=" + visible +
                ", coursId=" + coursId +
                "}\n";
    }
}