package services;

import interfaces.IService;
import models.Resultat;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ResultatDAOImpl implements IService<Resultat> {

    @Override
    public void add(Resultat resultat) {
        String req = "INSERT INTO resultat (student_id, evaluation_id, score, date_passage) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, resultat.getStudentId());
            ps.setInt(2, resultat.getEvaluationId());
            if (resultat.getScore() == null) {
                ps.setNull(3, java.sql.Types.FLOAT);
            } else {
                ps.setFloat(3, resultat.getScore());
            }
            ps.setTimestamp(4, resultat.getDatePassage() == null ? null : Timestamp.valueOf(resultat.getDatePassage()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Resultat resultat) {
        String req = "UPDATE resultat SET student_id = ?, evaluation_id = ?, score = ?, date_passage = ? WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, resultat.getStudentId());
            ps.setInt(2, resultat.getEvaluationId());
            if (resultat.getScore() == null) {
                ps.setNull(3, java.sql.Types.FLOAT);
            } else {
                ps.setFloat(3, resultat.getScore());
            }
            ps.setTimestamp(4, resultat.getDatePassage() == null ? null : Timestamp.valueOf(resultat.getDatePassage()));
            ps.setInt(5, resultat.getId());
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
                resultats.add(resultat);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return resultats;
    }

    /** Insert and return generated id, or -1 on failure. */
    public int insertAndGetId(Resultat resultat) {
        String req = "INSERT INTO resultat (student_id, evaluation_id, score, date_passage) VALUES (?, ?, ?, ?)";
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
}
