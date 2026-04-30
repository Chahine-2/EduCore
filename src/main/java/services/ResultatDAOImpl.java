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
        String req = "INSERT INTO resultat (score, score_pourcentage, est_reussi, temps_passe_min, tentative_num, date_passage, evaluation_id, etudiant_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setFloat(1, resultat.getScore());
            ps.setFloat(2, resultat.getScorePourcentage());
            ps.setBoolean(3, resultat.isEstReussi());
            ps.setInt(4, resultat.getTempsPasseMin());
            ps.setInt(5, resultat.getTentativeNum());
            ps.setTimestamp(6, Timestamp.valueOf(resultat.getDatePassage()));
            ps.setInt(7, resultat.getEvaluationId());
            ps.setInt(8, resultat.getEtudiantId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Resultat resultat) {
        String req = "UPDATE resultat SET score = ?, score_pourcentage = ?, est_reussi = ?, temps_passe_min = ?, tentative_num = ?, date_passage = ?, evaluation_id = ?, etudiant_id = ? WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setFloat(1, resultat.getScore());
            ps.setFloat(2, resultat.getScorePourcentage());
            ps.setBoolean(3, resultat.isEstReussi());
            ps.setInt(4, resultat.getTempsPasseMin());
            ps.setInt(5, resultat.getTentativeNum());
            ps.setTimestamp(6, Timestamp.valueOf(resultat.getDatePassage()));
            ps.setInt(7, resultat.getEvaluationId());
            ps.setInt(8, resultat.getEtudiantId());
            ps.setInt(9, resultat.getId());
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
                    resultat.setScore(rs.getFloat("score"));
                    resultat.setScorePourcentage(rs.getFloat("score_pourcentage"));
                    resultat.setEstReussi(rs.getBoolean("est_reussi"));
                    resultat.setTempsPasseMin(rs.getInt("temps_passe_min"));
                    resultat.setTentativeNum(rs.getInt("tentative_num"));
                    resultat.setDatePassage(rs.getTimestamp("date_passage").toLocalDateTime());
                    resultat.setEvaluationId(rs.getInt("evaluation_id"));
                    resultat.setEtudiantId(rs.getInt("etudiant_id"));
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
                resultat.setScore(rs.getFloat("score"));
                resultat.setScorePourcentage(rs.getFloat("score_pourcentage"));
                resultat.setEstReussi(rs.getBoolean("est_reussi"));
                resultat.setTempsPasseMin(rs.getInt("temps_passe_min"));
                resultat.setTentativeNum(rs.getInt("tentative_num"));
                resultat.setDatePassage(rs.getTimestamp("date_passage").toLocalDateTime());
                resultat.setEvaluationId(rs.getInt("evaluation_id"));
                resultat.setEtudiantId(rs.getInt("etudiant_id"));
                resultats.add(resultat);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return resultats;
    }


    public boolean corrigerEvaluation(int resultatId) {
        Connection conn = null;
        try {
            conn = MyDataBase.getInstance().getConnection();
            if (conn == null) {
                System.out.println("✗ Database connection is not available");
                return false;
            }

            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            final String selectAnswersSql =
                    "SELECT re.id AS reponse_etudiant_id, re.reponse_id AS reponse_id, " +
                    "r.est_correct AS reponse_est_correct, q.id AS question_id, q.points AS question_points " +
                    "FROM reponse_etudiant re " +
                    "LEFT JOIN reponse r ON re.reponse_id = r.id " +
                    "JOIN question q ON re.question_id = q.id " +
                    "WHERE re.resultat_id = ?";

            final String updateReponseEtudiantSql =
                    "UPDATE reponse_etudiant SET est_correct = ?, points_obtenus = ? WHERE id = ?";

            try (PreparedStatement psSelect = conn.prepareStatement(selectAnswersSql);
                 PreparedStatement psUpdateReponse = conn.prepareStatement(updateReponseEtudiantSql)) {

                psSelect.setInt(1, resultatId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    float totalScore = 0f;
                    boolean any = false;


                    java.util.Set<Integer> awardedQuestions = new java.util.HashSet<>();
                    while (rs.next()) {
                        any = true;
                        int reponseEtudiantId = rs.getInt("reponse_etudiant_id");
                        int questionId = rs.getInt("question_id");
                        Integer reponseId = (Integer) rs.getObject("reponse_id");

                        boolean reponseEstCorrect = rs.getBoolean("reponse_est_correct");
                        if (rs.wasNull()) {
                            reponseEstCorrect = false;
                        }

                        float questionPoints = rs.getFloat("question_points");


                        float pointsObtained = 0f;
                        if (reponseEstCorrect && !awardedQuestions.contains(questionId)) {
                            pointsObtained = questionPoints;
                            awardedQuestions.add(questionId);
                            totalScore += pointsObtained;
                        }

                        System.out.println("Auto-correction -> question " + questionId +
                                ", reponse " + reponseId +
                                ", correct=" + reponseEstCorrect +
                                ", points=" + pointsObtained);

                        psUpdateReponse.setBoolean(1, reponseEstCorrect);
                        psUpdateReponse.setFloat(2, pointsObtained);
                        psUpdateReponse.setInt(3, reponseEtudiantId);
                        psUpdateReponse.addBatch();
                    }

                    if (any) {
                        psUpdateReponse.executeBatch();
                    } else {
                        System.out.println("Aucune réponse étudiant trouvée pour resultat_id=" + resultatId);
                    }

                    final String selectNotePassageSql =
                            "SELECT e.note_passage, r.evaluation_id " +
                            "FROM resultat r JOIN evaluation e ON r.evaluation_id = e.id " +
                            "WHERE r.id = ?";

                    // 4) Also compute the maximum possible score for this evaluation,
                    //    so score_pourcentage can be refreshed too.
                    final String selectMaxScoreSql =
                            "SELECT COALESCE(SUM(q.points), 0) AS max_score " +
                            "FROM question q JOIN resultat r ON q.evaluation_id = r.evaluation_id " +
                            "WHERE r.id = ?";

                    try (PreparedStatement psNote = conn.prepareStatement(selectNotePassageSql)) {
                        psNote.setInt(1, resultatId);
                        try (ResultSet rsNote = psNote.executeQuery()) {
                            if (!rsNote.next()) {
                                throw new SQLException("Cannot find evaluation.note_passage for resultat id " + resultatId);
                            }
                            float notePassage = rsNote.getFloat("note_passage");
                            float maxScore = 0f;
                            try (PreparedStatement psMax = conn.prepareStatement(selectMaxScoreSql)) {
                                psMax.setInt(1, resultatId);
                                try (ResultSet rsMax = psMax.executeQuery()) {
                                    if (rsMax.next()) {
                                        maxScore = rsMax.getFloat("max_score");
                                    }
                                }
                            }

                            boolean estReussi = totalScore >= notePassage;
                            float scorePourcentage = maxScore > 0f ? (totalScore * 100f / maxScore) : 0f;

                            System.out.println("Total score = " + totalScore +
                                    ", note_passage = " + notePassage +
                                    ", score_pourcentage = " + scorePourcentage +
                                    ", est_reussi = " + estReussi);

                            final String updateResultatSql = "UPDATE resultat SET score = ?, score_pourcentage = ?, est_reussi = ? WHERE id = ?";
                            try (PreparedStatement psUpdateResultat = conn.prepareStatement(updateResultatSql)) {
                                psUpdateResultat.setFloat(1, totalScore);
                                psUpdateResultat.setFloat(2, scorePourcentage);
                                psUpdateResultat.setBoolean(3, estReussi);
                                psUpdateResultat.setInt(4, resultatId);
                                int updated = psUpdateResultat.executeUpdate();
                                if (updated != 1) {
                                    throw new SQLException("Expected to update 1 resultat row, updated: " + updated);
                                }
                            }
                        }
                    }

                    conn.commit();
                    System.out.println("✓ Corrigé terminé pour resultat_id=" + resultatId + " (score total = " + totalScore + ")");
                    return true;
                }
            } finally {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignore) {
                }
            }
        } catch (SQLException e) {

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rb) {
                System.err.println("Rollback failed: " + rb.getMessage());
            }
            System.err.println("Erreur lors de la correction: " + e.getMessage());
            return false;
        }
    }
}
