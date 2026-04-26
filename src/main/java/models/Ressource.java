package models;

public class Ressource {
    private int id;
    private String nom;
    private String type;       // pdf / video / image / lien / autre
    private String url;
    private int tailleKo;
    private int leconId;
    private int coursId;

    public Ressource() {}

    public Ressource(String nom, String type, String url, int tailleKo, int leconId, int coursId) {
        this.nom = nom;
        this.type = type;
        this.url = url;
        this.tailleKo = tailleKo;
        this.leconId = leconId;
        this.coursId = coursId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getTailleKo() { return tailleKo; }
    public void setTailleKo(int tailleKo) { this.tailleKo = tailleKo; }

    public int getLeconId() { return leconId; }
    public void setLeconId(int leconId) { this.leconId = leconId; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    @Override
    public String toString() {
        return "Ressource{id=" + id + ", nom='" + nom + "', type='" + type + "'}\n";
    }
}