package services;

import interfaces.IService;
import models.Lecon;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceLecon implements IService<Lecon> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Lecon l) {
        String req = "INSERT INTO lecon (titre, contenu, type_contenu, url_ressource, duree_minutes, ordre, est_obligatoire, chapitre_id) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, l.getTitre());
            ps.setString(2, l.getContenu());
            ps.setString(3, l.getTypeContenu());
            ps.setString(4, l.getUrlRessource());
            ps.setInt(5, l.getDureeMinutes());
            ps.setInt(6, l.getOrdre());
            ps.setBoolean(7, l.isEstObligatoire());
            ps.setInt(8, l.getChapitreId());
            ps.executeUpdate();
            System.out.println("Leçon ajoutée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Lecon l) {
        String req = "UPDATE lecon SET titre=?, contenu=?, type_contenu=?, url_ressource=?, duree_minutes=?, ordre=?, est_obligatoire=?, chapitre_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, l.getTitre());
            ps.setString(2, l.getContenu());
            ps.setString(3, l.getTypeContenu());
            ps.setString(4, l.getUrlRessource());
            ps.setInt(5, l.getDureeMinutes());
            ps.setInt(6, l.getOrdre());
            ps.setBoolean(7, l.isEstObligatoire());
            ps.setInt(8, l.getChapitreId());
            ps.setInt(9, l.getId());
            ps.executeUpdate();
            System.out.println("Leçon modifiée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Lecon l) {
        String req = "DELETE FROM lecon WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, l.getId());
            ps.executeUpdate();
            System.out.println("Leçon supprimée ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Lecon> getAll() {
        List<Lecon> liste = new ArrayList<>();
        String req = "SELECT * FROM lecon";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Lecon l = new Lecon();
                l.setId(rs.getInt("id"));
                l.setTitre(rs.getString("titre"));
                l.setContenu(rs.getString("contenu"));
                l.setTypeContenu(rs.getString("type_contenu"));
                l.setUrlRessource(rs.getString("url_ressource"));
                l.setDureeMinutes(rs.getInt("duree_minutes"));
                l.setOrdre(rs.getInt("ordre"));
                l.setEstObligatoire(rs.getBoolean("est_obligatoire"));
                l.setChapitreId(rs.getInt("chapitre_id"));
                liste.add(l);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}