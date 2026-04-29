package services;

import interfaces.IService;
import models.ProgrammeCours;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProgrammeCours implements IService<ProgrammeCours> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(ProgrammeCours p) {
        String req = "INSERT INTO programme_cours (cours_id, jour, heure_debut, heure_fin, frequence) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, p.getCoursId());
            ps.setString(2, p.getJour());
            ps.setString(3, p.getHeureDebut());
            ps.setString(4, p.getHeureFin());
            ps.setString(5, p.getFrequence());
            ps.executeUpdate();
            System.out.println("Programme ajouté ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(ProgrammeCours p) {
        String req = "UPDATE programme_cours SET cours_id=?, jour=?, heure_debut=?, heure_fin=?, frequence=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, p.getCoursId());
            ps.setString(2, p.getJour());
            ps.setString(3, p.getHeureDebut());
            ps.setString(4, p.getHeureFin());
            ps.setString(5, p.getFrequence());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
            System.out.println("Programme modifié ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(ProgrammeCours p) {
        String req = "DELETE FROM programme_cours WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, p.getId());
            ps.executeUpdate();
            System.out.println("Programme supprimé ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<ProgrammeCours> getAll() {
        List<ProgrammeCours> liste = new ArrayList<>();
        String req = "SELECT * FROM programme_cours";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                ProgrammeCours p = new ProgrammeCours();
                p.setId(rs.getInt("id"));
                p.setCoursId(rs.getInt("cours_id"));
                p.setJour(rs.getString("jour"));
                p.setHeureDebut(rs.getString("heure_debut"));
                p.setHeureFin(rs.getString("heure_fin"));
                p.setFrequence(rs.getString("frequence"));
                liste.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}