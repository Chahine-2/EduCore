package models;

import java.time.LocalDateTime;

public class Resultat {
    private int id;
    private float score;
    private float scorePourcentage;
    private boolean estReussi;
    private int tempsPasseMin;
    private int tentativeNum;
    private LocalDateTime datePassage;
    private int evaluationId;

    public Resultat() {
    }

    public Resultat(int id, float score, float scorePourcentage, boolean estReussi, int tempsPasseMin,
                    int tentativeNum, LocalDateTime datePassage, int evaluationId) {
        this.id = id;
        this.score = score;
        this.scorePourcentage = scorePourcentage;
        this.estReussi = estReussi;
        this.tempsPasseMin = tempsPasseMin;
        this.tentativeNum = tentativeNum;
        this.datePassage = datePassage;
        this.evaluationId = evaluationId;
    }

    public Resultat(float score, float scorePourcentage, boolean estReussi, int tempsPasseMin,
                    int tentativeNum, LocalDateTime datePassage, int evaluationId) {
        this.score = score;
        this.scorePourcentage = scorePourcentage;
        this.estReussi = estReussi;
        this.tempsPasseMin = tempsPasseMin;
        this.tentativeNum = tentativeNum;
        this.datePassage = datePassage;
        this.evaluationId = evaluationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getScorePourcentage() {
        return scorePourcentage;
    }

    public void setScorePourcentage(float scorePourcentage) {
        this.scorePourcentage = scorePourcentage;
    }

    public boolean isEstReussi() {
        return estReussi;
    }

    public void setEstReussi(boolean estReussi) {
        this.estReussi = estReussi;
    }

    public int getTempsPasseMin() {
        return tempsPasseMin;
    }

    public void setTempsPasseMin(int tempsPasseMin) {
        this.tempsPasseMin = tempsPasseMin;
    }

    public int getTentativeNum() {
        return tentativeNum;
    }

    public void setTentativeNum(int tentativeNum) {
        this.tentativeNum = tentativeNum;
    }

    public LocalDateTime getDatePassage() {
        return datePassage;
    }

    public void setDatePassage(LocalDateTime datePassage) {
        this.datePassage = datePassage;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    @Override
    public String toString() {
        return "Resultat{" +
                "id=" + id +
                ", score=" + score +
                ", scorePourcentage=" + scorePourcentage +
                ", estReussi=" + estReussi +
                ", tempsPasseMin=" + tempsPasseMin +
                ", tentativeNum=" + tentativeNum +
                ", datePassage=" + datePassage +
                ", evaluationId=" + evaluationId +
                '}';
    }
}

