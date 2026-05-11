package models;

public class FeedbackEtudiant {
    private int id;
    private int coursId;
    private int chapitreId;
    private String coursTitre;
    private String chapitreTitre;
    private String message;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public int getChapitreId() {
        return chapitreId;
    }

    public void setChapitreId(int chapitreId) {
        this.chapitreId = chapitreId;
    }

    public String getCoursTitre() {
        return coursTitre;
    }

    public void setCoursTitre(String coursTitre) {
        this.coursTitre = coursTitre;
    }

    public String getChapitreTitre() {
        return chapitreTitre;
    }

    public void setChapitreTitre(String chapitreTitre) {
        this.chapitreTitre = chapitreTitre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
