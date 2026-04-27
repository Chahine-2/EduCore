package models;

public class Bareme {
    private int id;
    private int evaluationId;
    private BaremeMention mention;
    private float noteMin;
    private float noteMax;

    public Bareme() {
    }

    public Bareme(int id, int evaluationId, BaremeMention mention, float noteMin, float noteMax) {
        this.id = id;
        this.evaluationId = evaluationId;
        this.mention = mention;
        this.noteMin = noteMin;
        this.noteMax = noteMax;
    }

    public Bareme(int evaluationId, BaremeMention mention, float noteMin, float noteMax) {
        this.evaluationId = evaluationId;
        this.mention = mention;
        this.noteMin = noteMin;
        this.noteMax = noteMax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    public BaremeMention getMention() {
        return mention;
    }

    public void setMention(BaremeMention mention) {
        this.mention = mention;
    }

    public float getNoteMin() {
        return noteMin;
    }

    public void setNoteMin(float noteMin) {
        this.noteMin = noteMin;
    }

    public float getNoteMax() {
        return noteMax;
    }

    public void setNoteMax(float noteMax) {
        this.noteMax = noteMax;
    }

    @Override
    public String toString() {
        return "Bareme{" +
                "id=" + id +
                ", evaluationId=" + evaluationId +
                ", mention=" + mention +
                ", noteMin=" + noteMin +
                ", noteMax=" + noteMax +
                '}';
    }
}
