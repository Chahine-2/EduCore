package services;

import interfaces.IService;
import interfaces.IServiceHackatons;
import models.Hackathon;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHackathon implements IServiceHackatons<Hackathon> {

    @Override
    public void add(Hackathon a) {
        String req = "INSERT INTO acaton (nom, categorie, duree, prix, reservation_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, a.getNom());
            ps.setString(2, a.getCategorie());
            ps.setInt(3, a.getDuree());
            ps.setDouble(4, a.getPrix());
            ps.setInt(5, a.getReservationId());
            ps.executeUpdate();
            System.out.println("Acaton ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Hackathon> getAll() {
        List<Hackathon> list = new ArrayList<>();
        String req = "SELECT * FROM acaton";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs  = stm.executeQuery(req);
            while (rs.next()) {
                Hackathon a = new Hackathon();
                a.setId(rs.getInt("id"));
                a.setNom(rs.getString("nom"));
                a.setCategorie(rs.getString("categorie"));
                a.setDuree(rs.getInt("duree"));
                a.setPrix(rs.getDouble("prix"));
                a.setReservationId(rs.getInt("reservation_id"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    @Override
    public void delete(Hackathon a) {
        String req = "DELETE FROM acaton WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, a.getId());
            ps.executeUpdate();
            System.out.println("Acaton supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Hackathon a) {
        String req = "UPDATE acaton SET nom=?, categorie=?, duree=?, prix=?, reservation_id=? WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, a.getNom());
            ps.setString(2, a.getCategorie());
            ps.setInt(3, a.getDuree());
            ps.setDouble(4, a.getPrix());
            ps.setInt(5, a.getReservationId());
            ps.setInt(6, a.getId());
            ps.executeUpdate();
            System.out.println("Acaton mis à jour !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
