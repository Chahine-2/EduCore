package services;

import interfaces.IService;
import interfaces.IServiceHackatons;
import models.Reservation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IServiceHackatons<Reservation> {

    @Override
    public void add(Reservation r) {
        String req = "INSERT INTO reservation (titre, description, date_debut, date_fin, nb_places) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setDate(3, Date.valueOf(r.getDateDebut()));
            ps.setDate(4, Date.valueOf(r.getDateFin()));
            ps.setInt(5, r.getNbPlaces());
            ps.executeUpdate();
            System.out.println("Réservation ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Reservation> getAll() {
        List<Reservation> list = new ArrayList<>();
        String req = "SELECT * FROM reservation";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs  = stm.executeQuery(req);
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setDateDebut(rs.getDate("date_debut").toLocalDate());
                r.setDateFin(rs.getDate("date_fin").toLocalDate());
                r.setNbPlaces(rs.getInt("nb_places"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    @Override
    public void delete(Reservation r) {
        String req = "DELETE FROM reservation WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, r.getId());
            ps.executeUpdate();
            System.out.println("Réservation supprimée !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Reservation r) {
        String req = "UPDATE reservation SET titre=?, description=?, " +
                "date_debut=?, date_fin=?, nb_places=? WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setDate(3, Date.valueOf(r.getDateDebut()));
            ps.setDate(4, Date.valueOf(r.getDateFin()));
            ps.setInt(5, r.getNbPlaces());
            ps.setInt(6, r.getId());
            ps.executeUpdate();
            System.out.println("Réservation mise à jour !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}