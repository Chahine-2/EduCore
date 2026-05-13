package services;

import interfaces.IService;
import models.Reponse;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReponseDAOImpl implements IService<Reponse> {

    @Override
    public void add(Reponse reponse) {
        String req = "INSERT INTO reponse (texte, est_correct, explication, ordre, question_id) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, reponse.getTexte());
            ps.setBoolean(2, reponse.isEstCorrect());
            ps.setString(3, reponse.getExplication());
            ps.setInt(4, reponse.getOrdre());
            ps.setInt(5, reponse.getQuestionId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Reponse reponse) {
        String req = "UPDATE reponse SET texte = ?, est_correct = ?, explication = ?, ordre = ?, question_id = ? WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, reponse.getTexte());
            ps.setBoolean(2, reponse.isEstCorrect());
            ps.setString(3, reponse.getExplication());
            ps.setInt(4, reponse.getOrdre());
            ps.setInt(5, reponse.getQuestionId());
            ps.setInt(6, reponse.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM reponse WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Reponse getById(int id) {
        String req = "SELECT * FROM reponse WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setTexte(rs.getString("texte"));
                reponse.setEstCorrect(rs.getBoolean("est_correct"));
                reponse.setExplication(rs.getString("explication"));
                reponse.setOrdre(getIntIfPresent(rs, "ordre"));
                reponse.setQuestionId(rs.getInt("question_id"));
                return reponse;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<Reponse> getAll() {
        List<Reponse> reponses = new ArrayList<>();
        String req = "SELECT * FROM reponse";

        try {
            Statement stm = MyDataBase.getInstance().getConnection().createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setTexte(rs.getString("texte"));
                reponse.setEstCorrect(rs.getBoolean("est_correct"));
                reponse.setExplication(rs.getString("explication"));
                reponse.setOrdre(getIntIfPresent(rs, "ordre"));
                reponse.setQuestionId(rs.getInt("question_id"));
                reponses.add(reponse);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return reponses;
    }

    public List<Reponse> findByQuestionId(int questionId) {
        List<Reponse> reponses = new ArrayList<>();
        String req = "SELECT * FROM reponse WHERE question_id = ? ORDER BY id ASC";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, questionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setTexte(rs.getString("texte"));
                reponse.setEstCorrect(rs.getBoolean("est_correct"));
                reponse.setExplication(rs.getString("explication"));
                reponse.setOrdre(getIntIfPresent(rs, "ordre"));
                reponse.setQuestionId(rs.getInt("question_id"));
                reponses.add(reponse);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reponses;
    }

    /**
     * Helper: returns the integer column value if the column exists in the ResultSet, otherwise 0.
     */
    private static int getIntIfPresent(ResultSet rs, String columnName) {
        try {
            java.sql.ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                if (md.getColumnName(i).equalsIgnoreCase(columnName)) {
                    int v = rs.getInt(columnName);
                    return rs.wasNull() ? 0 : v;
                }
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    public void deleteByQuestionId(int questionId) {
        String req = "DELETE FROM reponse WHERE question_id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, questionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

