package models;

public class ProgrammeCours {
    private int id;
    private int coursId;
    private String jour;        // Lundi / Mardi ...
    private String heureDebut;
    private String heureFin;
    private String frequence;   // hebdomadaire / bimensuel / mensuel

    public ProgrammeCours() {}

    public ProgrammeCours(int coursId, String jour, String heureDebut, String heureFin, String frequence) {
        this.coursId = coursId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.frequence = frequence;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }

    @Override
    public String toString() {
        return "ProgrammeCours{id=" + id + ", jour='" + jour + "', " + heureDebut + "-" + heureFin + "}\n";
    }
}