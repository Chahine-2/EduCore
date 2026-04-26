package models;

public class CommentaireCours {
    private int id;
    private String contenu;
    private int note;       // 1 à 5
    private int coursId;

    public CommentaireCours() {}

    public CommentaireCours(String contenu, int note, int coursId) {
        this.contenu = contenu;
        this.note = note;
        this.coursId = coursId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    @Override
    public String toString() {
        return "CommentaireCours{id=" + id + ", note=" + note + ", coursId=" + coursId + "}\n";
    }
}