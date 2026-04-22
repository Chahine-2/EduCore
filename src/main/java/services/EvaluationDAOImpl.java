package services;

import interfaces.IService;
import models.Evaluation;
import models.EvaluationStatut;
import models.EvaluationType;
import utils.DBConnection;

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
        String req = "INSERT INTO evaluation (titre, description, type, duree_minutes, note_max, note_passage, nb_tentatives, ordre_aleatoire, afficher_correc, date_debut, date_fin, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            LocalDateTime dateFin = resolveDateFin(evaluation);
            PreparedStatement ps = DBConnection.getInstance().getConnection()
                    .prepareStatement(req);
            ps.setString(1, evaluation.getTitre());
            ps.setString(2, evaluation.getDescription());
            ps.setString(3, evaluation.getType().getDbValue());
            ps.setInt(4, evaluation.getDureeMinutes());
            ps.setFloat(5, evaluation.getNoteMax());
            ps.setFloat(6, evaluation.getNotePassage());
            ps.setInt(7, evaluation.getNbTentatives());
            ps.setBoolean(8, evaluation.isOrdreAleatoire());
            ps.setBoolean(9, evaluation.isAfficherCorrec());
            ps.setTimestamp(10, Timestamp.valueOf(evaluation.getDateDebut()));
            ps.setTimestamp(11, Timestamp.valueOf(dateFin));
            ps.setString(12, evaluation.getStatut().getDbValue());

            ps.executeUpdate();

            ps.getGeneratedKeys();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Evaluation evaluation) {
        String req = "UPDATE evaluation SET titre = ?, description = ?, type = ?, duree_minutes = ?, note_max = ?, note_passage = ?, nb_tentatives = ?, ordre_aleatoire = ?, afficher_correc = ?, date_debut = ?, date_fin = ?, statut = ? WHERE id = ?";
        try {
            LocalDateTime dateFin = resolveDateFin(evaluation);
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(req);
            ps.setString(1, evaluation.getTitre());
            ps.setString(2, evaluation.getDescription());
            ps.setString(3, evaluation.getType().getDbValue());
            ps.setInt(4, evaluation.getDureeMinutes());
            ps.setFloat(5, evaluation.getNoteMax());
            ps.setFloat(6, evaluation.getNotePassage());
            ps.setInt(7, evaluation.getNbTentatives());
            ps.setBoolean(8, evaluation.isOrdreAleatoire());
            ps.setBoolean(9, evaluation.isAfficherCorrec());
            ps.setTimestamp(10, Timestamp.valueOf(evaluation.getDateDebut()));
            ps.setTimestamp(11, Timestamp.valueOf(dateFin));
            ps.setString(12, evaluation.getStatut().getDbValue());
            ps.setInt(13, evaluation.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM evaluation WHERE id = ?";
        try {
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(req);
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
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setTitre(rs.getString("titre"));
                evaluation.setDescription(rs.getString("description"));
                evaluation.setType(EvaluationType.fromDbValue(rs.getString("type")));
                evaluation.setDureeMinutes(rs.getInt("duree_minutes"));
                evaluation.setNoteMax(rs.getFloat("note_max"));
                evaluation.setNotePassage(rs.getFloat("note_passage"));
                evaluation.setNbTentatives(rs.getInt("nb_tentatives"));
                evaluation.setOrdreAleatoire(rs.getBoolean("ordre_aleatoire"));
                evaluation.setAfficherCorrec(rs.getBoolean("afficher_correc"));
                evaluation.setDateDebut(toLocalDateTime(rs.getTimestamp("date_debut")));
                evaluation.setDateFin(toLocalDateTime(rs.getTimestamp("date_fin")));
                evaluation.setStatut(EvaluationStatut.fromDbValue(rs.getString("statut")));
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
            Statement stm = DBConnection.getInstance().getConnection().createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setTitre(rs.getString("titre"));
                evaluation.setDescription(rs.getString("description"));
                evaluation.setType(EvaluationType.fromDbValue(rs.getString("type")));
                evaluation.setDureeMinutes(rs.getInt("duree_minutes"));
                evaluation.setNoteMax(rs.getFloat("note_max"));
                evaluation.setNotePassage(rs.getFloat("note_passage"));
                evaluation.setNbTentatives(rs.getInt("nb_tentatives"));
                evaluation.setOrdreAleatoire(rs.getBoolean("ordre_aleatoire"));
                evaluation.setAfficherCorrec(rs.getBoolean("afficher_correc"));
                evaluation.setDateDebut(toLocalDateTime(rs.getTimestamp("date_debut")));
                evaluation.setDateFin(toLocalDateTime(rs.getTimestamp("date_fin")));
                evaluation.setStatut(EvaluationStatut.fromDbValue(rs.getString("statut")));
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

    private LocalDateTime resolveDateFin(Evaluation evaluation) {
        LocalDateTime dateDebut = evaluation.getDateDebut();
        if (dateDebut == null) {
            throw new IllegalArgumentException("date_debut is required to compute date_fin");
        }
        LocalDateTime dateFin = dateDebut.plusMinutes(evaluation.getDureeMinutes());
        evaluation.setDateFin(dateFin);
        return dateFin;
    }
}
