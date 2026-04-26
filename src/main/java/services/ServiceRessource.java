package services;

import interfaces.IService;
import models.Ressource;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRessource implements IService<Ressource> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Ressource r) {
        String req = "INSERT INTO ressource (nom, type, url, taille_ko, lecon_id, cours_id) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, r.getNom());
            ps.setString(2, r.getType());
            ps.setString(3, r.getUrl());
            ps.setInt(4, r.getTailleKo());
            ps.setInt(5, r.getLeconId());
            ps.setInt(6, r.getCoursId());
            ps.executeUpdate();
            System.out.println("Ressource ajoutée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Ressource r) {
        String req = "UPDATE ressource SET nom=?, type=?, url=?, taille_ko=?, lecon_id=?, cours_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, r.getNom());
            ps.setString(2, r.getType());
            ps.setString(3, r.getUrl());
            ps.setInt(4, r.getTailleKo());
            ps.setInt(5, r.getLeconId());
            ps.setInt(6, r.getCoursId());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
            System.out.println("Ressource modifiée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Ressource r) {
        String req = "DELETE FROM ressource WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, r.getId());
            ps.executeUpdate();
            System.out.println("Ressource supprimée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Ressource> getAll() {
        List<Ressource> liste = new ArrayList<>();
        String req = "SELECT * FROM ressource";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Ressource r = new Ressource();
                r.setId(rs.getInt("id"));
                r.setNom(rs.getString("nom"));
                r.setType(rs.getString("type"));
                r.setUrl(rs.getString("url"));
                r.setTailleKo(rs.getInt("taille_ko"));
                r.setLeconId(rs.getInt("lecon_id"));
                r.setCoursId(rs.getInt("cours_id"));
                liste.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}