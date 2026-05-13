package services;

import interfaces.IService;
import models.Question;
import models.QuestionType;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements IService<Question> {

    @Override
    public void add(Question question) {
        String req = "INSERT INTO question (texte, type, points, explication, evaluation_id) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection()
                    .prepareStatement(req);
            ps.setString(1, question.getTexte());
            ps.setString(2, question.getType().getDbValue());
            ps.setFloat(3, question.getPoints());
            ps.setString(4, question.getExplication());
            ps.setInt(5, question.getEvaluationId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Question question) {
        String req = "UPDATE question SET texte = ?, type = ?, points = ?, explication = ?, evaluation_id = ? WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, question.getTexte());
            ps.setString(2, question.getType().getDbValue());
            ps.setFloat(3, question.getPoints());
            ps.setString(4, question.getExplication());
            ps.setInt(5, question.getEvaluationId());
            ps.setInt(6, question.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM question WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Question getById(int id) {
        String req = "SELECT * FROM question WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setTexte(rs.getString("texte"));
                question.setType(QuestionType.fromDbValue(rs.getString("type")));
                question.setPoints(rs.getFloat("points"));
                question.setExplication(rs.getString("explication"));
                question.setEvaluationId(rs.getInt("evaluation_id"));
                return question;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<Question> getAll() {
        List<Question> questions = new ArrayList<>();
        String req = "SELECT * FROM question";

        try {
            Statement stm = MyDataBase.getInstance().getConnection().createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setTexte(rs.getString("texte"));
                question.setType(QuestionType.fromDbValue(rs.getString("type")));
                question.setPoints(rs.getFloat("points"));
                question.setExplication(rs.getString("explication"));
                question.setEvaluationId(rs.getInt("evaluation_id"));
                questions.add(question);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return questions;
    }

    /** Questions belonging to one evaluation, stable order by id. */
    public List<Question> findByEvaluationId(int evaluationId) {
        List<Question> questions = new ArrayList<>();
        String req = "SELECT * FROM question WHERE evaluation_id = ? ORDER BY id ASC";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, evaluationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setTexte(rs.getString("texte"));
                question.setType(QuestionType.fromDbValue(rs.getString("type")));
                question.setPoints(rs.getFloat("points"));
                question.setExplication(rs.getString("explication"));
                question.setEvaluationId(rs.getInt("evaluation_id"));
                questions.add(question);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return questions;
    }

    /** Insert and return generated primary key, or -1 on failure. */
    public int insertAndGetId(Question question) {
        String req = "INSERT INTO question (texte, type, points, explication, evaluation_id) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection()
                    .prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, question.getTexte());
            ps.setString(2, question.getType().getDbValue());
            ps.setFloat(3, question.getPoints());
            ps.setString(4, question.getExplication());
            ps.setInt(5, question.getEvaluationId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }
}

