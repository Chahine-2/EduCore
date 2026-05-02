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
        String req = "INSERT INTO chapitre (titre, description, ordre, duree_minutes, type_contenu, url_contenu, date_creation, cours_id) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getOrdre());
            ps.setInt(4, c.getDureeMinutes());
            ps.setString(5, c.getTypeContenu());
            ps.setString(6, c.getUrlContenu());
            ps.setDate(7, Date.valueOf(c.getDateCreation()));
            ps.setInt(8, c.getCoursId());
            ps.executeUpdate();
            System.out.println("Chapitre ajouté ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Chapitre c) {
        String req = "UPDATE chapitre SET titre=?, description=?, ordre=?, duree_minutes=?, type_contenu=?, url_contenu=?, date_creation=?, cours_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getOrdre());
            ps.setInt(4, c.getDureeMinutes());
            ps.setString(5, c.getTypeContenu());
            ps.setString(6, c.getUrlContenu());
            ps.setDate(7, Date.valueOf(c.getDateCreation()));
            ps.setInt(8, c.getCoursId());
            ps.setInt(9, c.getId());
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
        String req = "SELECT * FROM chapitre ORDER BY cours_id, ordre";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public List<Chapitre> getByCours(int coursId) {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre WHERE cours_id = ? ORDER BY ordre";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public List<Chapitre> getByType(String type) {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre WHERE type_contenu = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    private Chapitre mapResultSet(ResultSet rs) throws SQLException {
        Chapitre c = new Chapitre();
        c.setId(rs.getInt("id"));
        c.setTitre(rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setOrdre(rs.getInt("ordre"));
        c.setDureeMinutes(rs.getInt("duree_minutes"));
        c.setTypeContenu(rs.getString("type_contenu"));
        c.setUrlContenu(rs.getString("url_contenu"));
        if (rs.getDate("date_creation") != null)
            c.setDateCreation(rs.getDate("date_creation").toLocalDate());
        c.setCoursId(rs.getInt("cours_id"));
        return c;
    }
}