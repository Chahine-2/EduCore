package services;

import interfaces.IService;
import models.SousCategorie;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceSousCategorie implements IService<SousCategorie> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(SousCategorie s) {
        String req = "INSERT INTO sous_categorie (nom, description, categorie_id, ordre, statut) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, s.getNom());
            ps.setString(2, s.getDescription());
            ps.setInt(3, s.getCategorieId());
            ps.setInt(4, s.getOrdre());
            ps.setString(5, s.getStatut());
            ps.executeUpdate();
            System.out.println("Sous-catégorie ajoutée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(SousCategorie s) {
        String req = "UPDATE sous_categorie SET nom=?, description=?, categorie_id=?, ordre=?, statut=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, s.getNom());
            ps.setString(2, s.getDescription());
            ps.setInt(3, s.getCategorieId());
            ps.setInt(4, s.getOrdre());
            ps.setString(5, s.getStatut());
            ps.setInt(6, s.getId());
            ps.executeUpdate();
            System.out.println("Sous-catégorie modifiée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(SousCategorie s) {
        String req = "DELETE FROM sous_categorie WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, s.getId());
            ps.executeUpdate();
            System.out.println("Sous-catégorie supprimée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<SousCategorie> getAll() {
        List<SousCategorie> liste = new ArrayList<>();
        String req = "SELECT * FROM sous_categorie";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                SousCategorie s = new SousCategorie();
                s.setId(rs.getInt("id"));
                s.setNom(rs.getString("nom"));
                s.setDescription(rs.getString("description"));
                s.setCategorieId(rs.getInt("categorie_id"));
                s.setOrdre(rs.getInt("ordre"));
                s.setStatut(rs.getString("statut"));
                liste.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}