package services;

import interfaces.IService;
import models.Evaluation;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAOImpl implements IService<Evaluation> {

    @Override
    public void add(Evaluation evaluation) {
        String req = "INSERT INTO evaluation (titre, description, duree_minutes, note_max, note_passage, date_debut, date_fin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection()
                    .prepareStatement(req);
            ps.setString(1, evaluation.getTitre());
            ps.setString(2, evaluation.getDescription());
            ps.setInt(3, evaluation.getDureeMinutes());
            ps.setFloat(4, evaluation.getNoteMax());
            ps.setFloat(5, evaluation.getNotePassage());
            ps.setTimestamp(6, evaluation.getDateDebut() == null ? null : Timestamp.valueOf(evaluation.getDateDebut()));
            ps.setTimestamp(7, evaluation.getDateFin() == null ? null : Timestamp.valueOf(evaluation.getDateFin()));

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Evaluation evaluation) {
        String req = "UPDATE evaluation SET titre = ?, description = ?, duree_minutes = ?, note_max = ?, note_passage = ?, date_debut = ?, date_fin = ? WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, evaluation.getTitre());
            ps.setString(2, evaluation.getDescription());
            ps.setInt(3, evaluation.getDureeMinutes());
            ps.setFloat(4, evaluation.getNoteMax());
            ps.setFloat(5, evaluation.getNotePassage());
            ps.setTimestamp(6, evaluation.getDateDebut() == null ? null : Timestamp.valueOf(evaluation.getDateDebut()));
            ps.setTimestamp(7, evaluation.getDateFin() == null ? null : Timestamp.valueOf(evaluation.getDateFin()));
            ps.setInt(8, evaluation.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM evaluation WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Evaluation getById(int id) {
        String req = "SELECT * FROM evaluation WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setTitre(rs.getString("titre"));
                evaluation.setDescription(rs.getString("description"));
                evaluation.setDureeMinutes(rs.getInt("duree_minutes"));
                evaluation.setNoteMax(rs.getFloat("note_max"));
                evaluation.setNotePassage(rs.getFloat("note_passage"));
                evaluation.setDateDebut(toLocalDateTime(rs.getTimestamp("date_debut")));
                evaluation.setDateFin(toLocalDateTime(rs.getTimestamp("date_fin")));
                evaluation.setDateCreation(toLocalDateTime(rs.getTimestamp("date_creation")));
                return evaluation;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> evaluations = new ArrayList<>();
        String req = "SELECT * FROM evaluation";

        try {
            Statement stm = MyDataBase.getInstance().getConnection().createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setTitre(rs.getString("titre"));
                evaluation.setDescription(rs.getString("description"));
                evaluation.setDureeMinutes(rs.getInt("duree_minutes"));
                evaluation.setNoteMax(rs.getFloat("note_max"));
                evaluation.setNotePassage(rs.getFloat("note_passage"));
                evaluation.setDateDebut(toLocalDateTime(rs.getTimestamp("date_debut")));
                evaluation.setDateFin(toLocalDateTime(rs.getTimestamp("date_fin")));
                evaluation.setDateCreation(toLocalDateTime(rs.getTimestamp("date_creation")));

                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return evaluations;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
