package services;

import interfaces.IService;
import models.Cours;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCours implements IService<Cours> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Cours c) {
        String req = "INSERT INTO cours (titre, description, objectifs, prerequis, duree_heures, niveau, langue, categorie_id, statut) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getObjectifs());
            ps.setString(4, c.getPrerequis());
            ps.setInt(5, c.getDureeHeures());
            ps.setString(6, c.getNiveau());
            ps.setString(7, c.getLangue());
            ps.setInt(8, c.getCategorieId());
            ps.setString(9, c.getStatut());
            ps.executeUpdate();
            System.out.println("Cours ajouté ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Cours c) {
        String req = "UPDATE cours SET titre=?, description=?, objectifs=?, prerequis=?, duree_heures=?, niveau=?, langue=?, categorie_id=?, statut=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getObjectifs());
            ps.setString(4, c.getPrerequis());
            ps.setInt(5, c.getDureeHeures());
            ps.setString(6, c.getNiveau());
            ps.setString(7, c.getLangue());
            ps.setInt(8, c.getCategorieId());
            ps.setString(9, c.getStatut());
            ps.setInt(10, c.getId());
            ps.executeUpdate();
            System.out.println("Cours modifié ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Cours c) {
        String req = "DELETE FROM cours WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getId());
            ps.executeUpdate();
            System.out.println("Cours supprimé ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> liste = new ArrayList<>();
        String req = "SELECT * FROM cours";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setObjectifs(rs.getString("objectifs"));
                c.setPrerequis(rs.getString("prerequis"));
                c.setDureeHeures(rs.getInt("duree_heures"));
                c.setNiveau(rs.getString("niveau"));
                c.setLangue(rs.getString("langue"));
                c.setCategorieId(rs.getInt("categorie_id"));
                c.setStatut(rs.getString("statut"));
                c.setNbInscrits(rs.getInt("nb_inscrits")); // lecture seule depuis la DB
                liste.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}