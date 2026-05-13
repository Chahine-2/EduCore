package models;

public class Question {
    private int id;
    private String texte;
    private QuestionType type;
    private float points;
    private String explication;
    private int evaluationId;

    public Question() {
    }

    public Question(int id, String texte, QuestionType type, float points, int evaluationId) {
        this.id = id;
        this.texte = texte;
        this.type = type;
        this.points = points;
        this.evaluationId = evaluationId;
    }

    public Question(String texte, QuestionType type, float points, int evaluationId) {
        this.texte = texte;
        this.type = type;
        this.points = points;
        this.evaluationId = evaluationId;
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

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public float getPoints() {
        return points;
    }

    public void setPoints(float points) {
        this.points = points;
    }

    public String getExplication() {
        return explication;
    }

    public void setExplication(String explication) {
        this.explication = explication;
    }


    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", texte='" + texte + '\'' +
                ", type=" + type +
                ", points=" + points +
                ", explication='" + explication + '\'' +
                ", evaluationId=" + evaluationId +
                '}';
    }
}

