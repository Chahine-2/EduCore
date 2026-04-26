package services;

import interfaces.IService;
import models.Categorie;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements IService<Categorie> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Categorie c) {
        String req = "INSERT INTO categorie (nom, description, icone, ordre, statut) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getIcone());
            ps.setInt(4, c.getOrdre());
            ps.setString(5, c.getStatut());
            ps.executeUpdate();
            System.out.println("Catégorie ajoutée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Categorie c) {
        String req = "UPDATE categorie SET nom=?, description=?, icone=?, ordre=?, statut=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getIcone());
            ps.setInt(4, c.getOrdre());
            ps.setString(5, c.getStatut());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
            System.out.println("Catégorie modifiée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Categorie c) {
        String req = "DELETE FROM categorie WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getId());
            ps.executeUpdate();
            System.out.println("Catégorie supprimée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Categorie> getAll() {
        List<Categorie> liste = new ArrayList<>();
        String req = "SELECT * FROM categorie";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                c.setIcone(rs.getString("icone"));
                c.setOrdre(rs.getInt("ordre"));
                c.setStatut(rs.getString("statut"));
                liste.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}