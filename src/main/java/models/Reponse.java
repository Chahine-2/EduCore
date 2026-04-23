package models;

public class Reponse {
    private int id;
    private String texte;
    private boolean estCorrect;
    private String explication;
    private int ordre;
    private int questionId;

    public Reponse() {
    }

    public Reponse(int id, String texte, boolean estCorrect, String explication, int ordre, int questionId) {
        this.id = id;
        this.texte = texte;
        this.estCorrect = estCorrect;
        this.explication = explication;
        this.ordre = ordre;
        this.questionId = questionId;
    }

    public Reponse(String texte, boolean estCorrect, String explication, int ordre, int questionId) {
        this.texte = texte;
        this.estCorrect = estCorrect;
        this.explication = explication;
        this.ordre = ordre;
        this.questionId = questionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public boolean isEstCorrect() {
        return estCorrect;
    }

    public void setEstCorrect(boolean estCorrect) {
        this.estCorrect = estCorrect;
    }

    public String getExplication() {
        return explication;
    }

    public void setExplication(String explication) {
        this.explication = explication;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", texte='" + texte + '\'' +
                ", estCorrect=" + estCorrect +
                ", explication='" + explication + '\'' +
                ", ordre=" + ordre +
                ", questionId=" + questionId +
                '}';
    }
}

