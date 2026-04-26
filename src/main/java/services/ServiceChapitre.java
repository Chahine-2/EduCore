package services;

import interfaces.IService;
import models.Chapitre;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceChapitre implements IService<Chapitre> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Chapitre c) {
        String req = "INSERT INTO chapitre (titre, description, ordre, duree_minutes, est_gratuit, cours_id) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getOrdre());
            ps.setInt(4, c.getDureeMinutes());
            ps.setBoolean(5, c.isEstGratuit());
            ps.setInt(6, c.getCoursId());
            ps.executeUpdate();
            System.out.println("Chapitre ajouté ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Chapitre c) {
        String req = "UPDATE chapitre SET titre=?, description=?, ordre=?, duree_minutes=?, est_gratuit=?, cours_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getOrdre());
            ps.setInt(4, c.getDureeMinutes());
            ps.setBoolean(5, c.isEstGratuit());
            ps.setInt(6, c.getCoursId());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
            System.out.println("Chapitre modifié ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Chapitre c) {
        String req = "DELETE FROM chapitre WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getId());
            ps.executeUpdate();
            System.out.println("Chapitre supprimé ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Chapitre> getAll() {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Chapitre c = new Chapitre();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setOrdre(rs.getInt("ordre"));
                c.setDureeMinutes(rs.getInt("duree_minutes"));
                c.setEstGratuit(rs.getBoolean("est_gratuit"));
                c.setCoursId(rs.getInt("cours_id"));
                liste.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}