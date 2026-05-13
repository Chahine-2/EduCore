package models;

public class ReponseEtudiant {
    private int id;
    private int resultatId;
    private int questionId;
    private Integer reponseId;
    private String texteLibre;


    public ReponseEtudiant() {
    }

    public ReponseEtudiant(int id, int resultatId, int questionId, Integer reponseId, String texteLibre) {
        this.id = id;
        this.resultatId = resultatId;
        this.questionId = questionId;
        this.reponseId = reponseId;
        this.texteLibre = texteLibre;
    }

    public ReponseEtudiant(int resultatId, int questionId, Integer reponseId, String texteLibre) {
        this.resultatId = resultatId;
        this.questionId = questionId;
        this.reponseId = reponseId;
        this.texteLibre = texteLibre;
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


    @Override
    public String toString() {
        return "ReponseEtudiant{" +
                "id=" + id +
                ", resultatId=" + resultatId +
                ", questionId=" + questionId +
                ", reponseId=" + reponseId +
                ", texteLibre='" + texteLibre + '\'' +
                ", texteLibre='" + texteLibre + '\'' +
                '}';
    }
}

