package services;

import interfaces.IService;
import models.Resultat;
import models.TeacherEvalAttemptRow;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultatDAOImpl implements IService<Resultat> {

    private static final DateTimeFormatter ATTEMPT_AT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm", Locale.ENGLISH);

    @Override
    public void add(Resultat resultat) {
        String req = "INSERT INTO resultat (student_id, evaluation_id, score, date_passage, fraude_detecte) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, resultat.getStudentId());
            ps.setInt(2, resultat.getEvaluationId());
            if (resultat.getScore() == null) {
                ps.setNull(3, java.sql.Types.FLOAT);
            } else {
                ps.setFloat(3, resultat.getScore());
            }
            ps.setTimestamp(4, resultat.getDatePassage() == null ? null : Timestamp.valueOf(resultat.getDatePassage()));
            ps.setBoolean(5, resultat.isFraudeDetecte());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Resultat resultat) {
        String req = "UPDATE resultat SET student_id = ?, evaluation_id = ?, score = ?, date_passage = ?, fraude_detecte = ? WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, resultat.getStudentId());
            ps.setInt(2, resultat.getEvaluationId());
            if (resultat.getScore() == null) {
                ps.setNull(3, java.sql.Types.FLOAT);
            } else {
                ps.setFloat(3, resultat.getScore());
            }
            ps.setTimestamp(4, resultat.getDatePassage() == null ? null : Timestamp.valueOf(resultat.getDatePassage()));
            ps.setBoolean(5, resultat.isFraudeDetecte());
            ps.setInt(6, resultat.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM resultat WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Resultat getById(int id) {
        String req = "SELECT * FROM resultat WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Resultat resultat = new Resultat();
                    resultat.setId(rs.getInt("id"));
                    resultat.setStudentId(rs.getInt("student_id"));
                    resultat.setEvaluationId(rs.getInt("evaluation_id"));
                    java.sql.Timestamp ts = rs.getTimestamp("date_passage");
                    resultat.setDatePassage(ts == null ? null : ts.toLocalDateTime());
                    float s = rs.getFloat("score");
                    if (rs.wasNull()) {
                        resultat.setScore(null);
                    } else {
                        resultat.setScore(s);
                    }
                    resultat.setFraudeDetecte(rs.getBoolean("fraude_detecte"));
                    return resultat;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<Resultat> getAll() {
        List<Resultat> resultats = new ArrayList<>();
        String req = "SELECT * FROM resultat";

        try (Statement stm = MyDataBase.getInstance().getConnection().createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                Resultat resultat = new Resultat();
                resultat.setId(rs.getInt("id"));
                resultat.setStudentId(rs.getInt("student_id"));
                resultat.setEvaluationId(rs.getInt("evaluation_id"));
                java.sql.Timestamp ts = rs.getTimestamp("date_passage");
                resultat.setDatePassage(ts == null ? null : ts.toLocalDateTime());
                float s = rs.getFloat("score");
                if (rs.wasNull()) {
                    resultat.setScore(null);
                } else {
                    resultat.setScore(s);
                }
                resultat.setFraudeDetecte(rs.getBoolean("fraude_detecte"));
                resultats.add(resultat);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return resultats;
    }

    /** Insert and return generated id, or -1 on failure. */
    public int insertAndGetId(Resultat resultat) {
        String req = "INSERT INTO resultat (student_id, evaluation_id, score, date_passage, fraude_detecte) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection()
                .prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, resultat.getStudentId());
            ps.setInt(2, resultat.getEvaluationId());
            if (resultat.getScore() == null) {
                ps.setNull(3, java.sql.Types.FLOAT);
            } else {
                ps.setFloat(3, resultat.getScore());
            }
            ps.setTimestamp(4, resultat.getDatePassage() == null ? null : Timestamp.valueOf(resultat.getDatePassage()));
            ps.setBoolean(5, resultat.isFraudeDetecte());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    /** Most recent attempt for this student on this evaluation, or null. */
    public Resultat findLatestByStudentAndEvaluation(int studentId, int evaluationId) {
        String req = "SELECT * FROM resultat WHERE student_id = ? AND evaluation_id = ? ORDER BY date_passage DESC, id DESC LIMIT 1";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, studentId);
            ps.setInt(2, evaluationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static Resultat mapRow(ResultSet rs) throws SQLException {
        Resultat resultat = new Resultat();
        resultat.setId(rs.getInt("id"));
        resultat.setStudentId(rs.getInt("student_id"));
        resultat.setEvaluationId(rs.getInt("evaluation_id"));
        java.sql.Timestamp ts = rs.getTimestamp("date_passage");
        resultat.setDatePassage(ts == null ? null : ts.toLocalDateTime());
        float s = rs.getFloat("score");
        if (rs.wasNull()) {
            resultat.setScore(null);
        } else {
            resultat.setScore(s);
        }
        resultat.setFraudeDetecte(rs.getBoolean("fraude_detecte"));
        return resultat;
    }

    public boolean corrigerEvaluation(int resultatId) {
        Connection conn = null;
        try {
            conn = MyDataBase.getInstance().getConnection();
            if (conn == null) {
                System.out.println("✗ Database connection is not available");
                return false;
            }

            // Check if fraud was detected on this resultat
            String checkFraudSql = "SELECT fraude_detecte FROM resultat WHERE id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkFraudSql)) {
                psCheck.setInt(1, resultatId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        boolean fraudeDetecte = rsCheck.getBoolean("fraude_detecte");
                        if (fraudeDetecte) {
                            // If fraud detected, set score to 0
                            final String updateSql = "UPDATE resultat SET score = 0 WHERE id = ?";
                            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                                psUpdate.setInt(1, resultatId);
                                int updated = psUpdate.executeUpdate();
                                return updated == 1;
                            }
                        }
                    }
                }
            }

            // Compute total score by checking student answers against correct answers.
            final String selectSql =
                    "SELECT q.id AS question_id, q.points AS question_points, r.est_correct AS reponse_correct " +
                    "FROM reponse_etudiant re " +
                    "LEFT JOIN reponse r ON re.reponse_id = r.id " +
                    "JOIN question q ON re.question_id = q.id " +
                    "WHERE re.resultat_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, resultatId);
                try (ResultSet rs = ps.executeQuery()) {
                    float totalScore = 0f;
                    java.util.Set<Integer> awardedQuestions = new java.util.HashSet<>();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        int questionId = rs.getInt("question_id");
                        float questionPoints = rs.getFloat("question_points");
                        boolean reponseCorrect = rs.getBoolean("reponse_correct");
                        if (rs.wasNull()) {
                            reponseCorrect = false;
                        }
                        if (reponseCorrect && !awardedQuestions.contains(questionId)) {
                            awardedQuestions.add(questionId);
                            totalScore += questionPoints;
                        }
                    }

                    if (!any) {
                        System.out.println("Aucune réponse étudiant trouvée pour resultat_id=" + resultatId);
                        return false;
                    }

                    // update only the resultat.score column (schema doesn't include per-answer fields)
                    final String updateSql = "UPDATE resultat SET score = ? WHERE id = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setFloat(1, totalScore);
                        psUpdate.setInt(2, resultatId);
                        int updated = psUpdate.executeUpdate();
                        return updated == 1;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la correction: " + e.getMessage());
            return false;
        }
    }

    /**
     * One row per student for an evaluation (latest {@code resultat} by highest id per student),
     * with display-ready score / pass / integrity columns.
     */
    public List<TeacherEvalAttemptRow> findAttemptsForTeacherEvaluation(int evaluationId) {
        List<TeacherEvalAttemptRow> rows = new ArrayList<>();
        final String sql = """
                SELECT r.id, r.student_id, r.score, r.date_passage, r.fraude_detecte,
                       u.prenom, u.nom, e.note_max, e.note_passage
                FROM resultat r
                INNER JOIN (
                    SELECT student_id, MAX(id) AS latest_id
                    FROM resultat
                    WHERE evaluation_id = ?
                    GROUP BY student_id
                ) latest ON latest.latest_id = r.id
                LEFT JOIN utilisateurs u ON u.id = r.student_id
                JOIN evaluation e ON e.id = r.evaluation_id
                ORDER BY r.date_passage DESC, r.id DESC
                """;
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, evaluationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    float noteMax = rs.getFloat("note_max");
                    float notePassage = rs.getFloat("note_passage");
                    boolean fraud = rs.getBoolean("fraude_detecte");
                    String prenom = rs.getString("prenom");
                    String nom = rs.getString("nom");
                    String student = formatStudentName(prenom, nom, rs.getInt("student_id"));

                    float scoreVal = rs.getFloat("score");
                    boolean scoreNull = rs.wasNull();
                    String scoreText = scoreNull ? "—" : String.format(Locale.US, "%.1f / %.0f", scoreVal, noteMax);
                    String outcome;
                    if (scoreNull) {
                        outcome = "—";
                    } else {
                        outcome = scoreVal >= notePassage ? "Pass" : "Below threshold";
                    }
                    String integrity = fraud ? "Fraud flagged" : "OK";

                    Timestamp ts = rs.getTimestamp("date_passage");
                    String when = ts == null ? "—" : ts.toLocalDateTime().format(ATTEMPT_AT);

                    rows.add(new TeacherEvalAttemptRow(id, student, scoreText, outcome, integrity, when));
                }
            }
        } catch (SQLException e) {
            System.err.println("findAttemptsForTeacherEvaluation: " + e.getMessage());
        }
        return rows;
    }

    private static String formatStudentName(String prenom, String nom, int fallbackId) {
        String p = prenom == null ? "" : prenom.trim();
        String n = nom == null ? "" : nom.trim();
        String name = (p + " " + n).trim();
        if (!name.isEmpty()) {
            return name;
        }
        return "Student #" + fallbackId;
    }
}
