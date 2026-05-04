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
        String req = "INSERT INTO reponse (texte, est_correct, question_id) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, reponse.getTexte());
            ps.setBoolean(2, reponse.isEstCorrect());
            ps.setInt(3, reponse.getQuestionId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Reponse reponse) {
        String req = "UPDATE reponse SET texte = ?, est_correct = ?, question_id = ? WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, reponse.getTexte());
            ps.setBoolean(2, reponse.isEstCorrect());
            ps.setInt(3, reponse.getQuestionId());
            ps.setInt(4, reponse.getId());

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
                reponse.setQuestionId(rs.getInt("question_id"));
                reponses.add(reponse);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return reponses;
    }
}

