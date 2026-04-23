package models;

public class ReponseEtudiant {
    private int id;
    private int resultatId;
    private int questionId;
    private Integer reponseId;
    private String texteLibre;
    private boolean estCorrect;
    private float pointsObtenus;

    public ReponseEtudiant() {
    }

    public ReponseEtudiant(int id, int resultatId, int questionId, Integer reponseId, String texteLibre,
                           boolean estCorrect, float pointsObtenus) {
        this.id = id;
        this.resultatId = resultatId;
        this.questionId = questionId;
        this.reponseId = reponseId;
        this.texteLibre = texteLibre;
        this.estCorrect = estCorrect;
        this.pointsObtenus = pointsObtenus;
    }

    public ReponseEtudiant(int resultatId, int questionId, Integer reponseId, String texteLibre,
                           boolean estCorrect, float pointsObtenus) {
        this.resultatId = resultatId;
        this.questionId = questionId;
        this.reponseId = reponseId;
        this.texteLibre = texteLibre;
        this.estCorrect = estCorrect;
        this.pointsObtenus = pointsObtenus;
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

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getReponseId() {
        return reponseId;
    }

    public void setReponseId(Integer reponseId) {
        this.reponseId = reponseId;
    }

    public String getTexteLibre() {
        return texteLibre;
    }

    public void setTexteLibre(String texteLibre) {
        this.texteLibre = texteLibre;
    }

    public boolean isEstCorrect() {
        return estCorrect;
    }

    public void setEstCorrect(boolean estCorrect) {
        this.estCorrect = estCorrect;
    }

    public float getPointsObtenus() {
        return pointsObtenus;
    }

    public void setPointsObtenus(float pointsObtenus) {
        this.pointsObtenus = pointsObtenus;
    }

    @Override
    public String toString() {
        return "ReponseEtudiant{" +
                "id=" + id +
                ", resultatId=" + resultatId +
                ", questionId=" + questionId +
                ", reponseId=" + reponseId +
                ", texteLibre='" + texteLibre + '\'' +
                ", estCorrect=" + estCorrect +
                ", pointsObtenus=" + pointsObtenus +
                '}';
    }
}

