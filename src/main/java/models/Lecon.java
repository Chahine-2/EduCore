package models;

public class Lecon {
    private int id;
    private String titre;
    private String contenu;
    private String typeContenu;  // texte / video / audio / pdf / lien
    private String urlRessource;
    private int dureeMinutes;
    private int ordre;
    private boolean estObligatoire;
    private int chapitreId;

    public Lecon() {}

    public Lecon(String titre, String contenu, String typeContenu, String urlRessource,
                 int dureeMinutes, int ordre, boolean estObligatoire, int chapitreId) {
        this.titre = titre;
        this.contenu = contenu;
        this.typeContenu = typeContenu;
        this.urlRessource = urlRessource;
        this.dureeMinutes = dureeMinutes;
        this.ordre = ordre;
        this.estObligatoire = estObligatoire;
        this.chapitreId = chapitreId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getTypeContenu() { return typeContenu; }
    public void setTypeContenu(String typeContenu) { this.typeContenu = typeContenu; }

    public String getUrlRessource() { return urlRessource; }
    public void setUrlRessource(String urlRessource) { this.urlRessource = urlRessource; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public boolean isEstObligatoire() { return estObligatoire; }
    public void setEstObligatoire(boolean estObligatoire) { this.estObligatoire = estObligatoire; }

    public int getChapitreId() { return chapitreId; }
    public void setChapitreId(int chapitreId) { this.chapitreId = chapitreId; }

    @Override
    public String toString() {
        return "Lecon{id=" + id + ", titre='" + titre + "', type='" + typeContenu + "', chapitreId=" + chapitreId + "}\n";
    }
}