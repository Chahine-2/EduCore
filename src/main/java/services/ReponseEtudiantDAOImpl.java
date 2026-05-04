package services;

import interfaces.IService;
import models.ReponseEtudiant;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ReponseEtudiantDAOImpl implements IService<ReponseEtudiant> {

    @Override
    public void add(ReponseEtudiant reponseEtudiant) {
        String req = "INSERT INTO reponse_etudiant (resultat_id, question_id, reponse_id, texte_libre) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, reponseEtudiant.getResultatId());
            ps.setInt(2, reponseEtudiant.getQuestionId());
            if (reponseEtudiant.getReponseId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, reponseEtudiant.getReponseId());
            }
            ps.setString(4, reponseEtudiant.getTexteLibre());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(ReponseEtudiant reponseEtudiant) {
        String req = "UPDATE reponse_etudiant SET resultat_id = ?, question_id = ?, reponse_id = ?, texte_libre = ? WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, reponseEtudiant.getResultatId());
            ps.setInt(2, reponseEtudiant.getQuestionId());
            if (reponseEtudiant.getReponseId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, reponseEtudiant.getReponseId());
            }
            ps.setString(4, reponseEtudiant.getTexteLibre());
            ps.setInt(5, reponseEtudiant.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM reponse_etudiant WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public ReponseEtudiant getById(int id) {
        String req = "SELECT * FROM reponse_etudiant WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ReponseEtudiant reponseEtudiant = new ReponseEtudiant();
                reponseEtudiant.setId(rs.getInt("id"));
                reponseEtudiant.setResultatId(rs.getInt("resultat_id"));
                reponseEtudiant.setQuestionId(rs.getInt("question_id"));
                int reponseId = rs.getInt("reponse_id");
                reponseEtudiant.setReponseId(rs.wasNull() ? null : reponseId);
                reponseEtudiant.setTexteLibre(rs.getString("texte_libre"));
                return reponseEtudiant;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<ReponseEtudiant> getAll() {
        List<ReponseEtudiant> reponsesEtudiant = new ArrayList<>();
        String req = "SELECT * FROM reponse_etudiant";

        try {
            Statement stm = MyDataBase.getInstance().getConnection().createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                ReponseEtudiant reponseEtudiant = new ReponseEtudiant();
                reponseEtudiant.setId(rs.getInt("id"));
                reponseEtudiant.setResultatId(rs.getInt("resultat_id"));
                reponseEtudiant.setQuestionId(rs.getInt("question_id"));
                int reponseId = rs.getInt("reponse_id");
                reponseEtudiant.setReponseId(rs.wasNull() ? null : reponseId);
                reponseEtudiant.setTexteLibre(rs.getString("texte_libre"));
                reponsesEtudiant.add(reponseEtudiant);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return reponsesEtudiant;
    }
}

