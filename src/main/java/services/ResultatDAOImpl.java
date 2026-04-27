package services;

import interfaces.IService;
import models.Resultat;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ResultatDAOImpl implements IService<Resultat> {

    @Override
    public void add(Resultat resultat) {
        String req = "INSERT INTO resultat (score, score_pourcentage, est_reussi, temps_passe_min, tentative_num, date_passage, evaluation_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setFloat(1, resultat.getScore());
            ps.setFloat(2, resultat.getScorePourcentage());
            ps.setBoolean(3, resultat.isEstReussi());
            ps.setInt(4, resultat.getTempsPasseMin());
            ps.setInt(5, resultat.getTentativeNum());
            ps.setTimestamp(6, Timestamp.valueOf(resultat.getDatePassage()));
            ps.setInt(7, resultat.getEvaluationId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Resultat resultat) {
        String req = "UPDATE resultat SET score = ?, score_pourcentage = ?, est_reussi = ?, temps_passe_min = ?, tentative_num = ?, date_passage = ?, evaluation_id = ? WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setFloat(1, resultat.getScore());
            ps.setFloat(2, resultat.getScorePourcentage());
            ps.setBoolean(3, resultat.isEstReussi());
            ps.setInt(4, resultat.getTempsPasseMin());
            ps.setInt(5, resultat.getTentativeNum());
            ps.setTimestamp(6, Timestamp.valueOf(resultat.getDatePassage()));
            ps.setInt(7, resultat.getEvaluationId());
            ps.setInt(8, resultat.getId());
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
                resultats.add(resultat);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return resultats;
    }
}
