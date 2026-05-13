package models;

import java.time.LocalDateTime;

/**
 * Simplified model to match `resultat` table:
 * id, student_id, evaluation_id, score, date_passage, fraude_detecte
 */
public class Resultat {
    private int id;
    private int studentId;
    private int evaluationId;
    private Float score;
    private LocalDateTime datePassage;
    private boolean fraudeDetecte;

    public Resultat() {}

    public Resultat(int id, int studentId, int evaluationId, Float score, LocalDateTime datePassage) {
        this.id = id;
        this.studentId = studentId;
        this.evaluationId = evaluationId;
        this.score = score;
        this.datePassage = datePassage;
        this.fraudeDetecte = false;
    }

    public Resultat(int studentId, int evaluationId, Float score, LocalDateTime datePassage) {
        this.studentId = studentId;
        this.evaluationId = evaluationId;
        this.score = score;
        this.datePassage = datePassage;
        this.fraudeDetecte = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public LocalDateTime getDatePassage() {
        return datePassage;
    }

    public void setDatePassage(LocalDateTime datePassage) {
        this.datePassage = datePassage;
    }

    public boolean isFraudeDetecte() {
        return fraudeDetecte;
    }

    public void setFraudeDetecte(boolean fraudeDetecte) {
        this.fraudeDetecte = fraudeDetecte;
    }

    @Override
    public String toString() {
        return "Resultat{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", evaluationId=" + evaluationId +
                ", score=" + score +
                ", datePassage=" + datePassage +
                ", fraudeDetecte=" + fraudeDetecte +
                '}';
    }
}

