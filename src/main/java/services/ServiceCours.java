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
        String req = "INSERT INTO cours (titre, description, objectifs, duree_heures, niveau, categorie, est_certifiant, date_debut, date_fin) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getObjectifs());
            ps.setInt(4, c.getDureeHeures());
            ps.setString(5, c.getNiveau());
            ps.setString(6, c.getCategorie());
            ps.setBoolean(7, c.isEstCertifiant());
            ps.setDate(8, Date.valueOf(c.getDateDebut()));
            ps.setDate(9, Date.valueOf(c.getDateFin()));
            ps.executeUpdate();
            System.out.println("Cours ajouté ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Cours c) {
        String req = "UPDATE cours SET titre=?, description=?, objectifs=?, duree_heures=?, niveau=?, categorie=?, est_certifiant=?, date_debut=?, date_fin=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getObjectifs());
            ps.setInt(4, c.getDureeHeures());
            ps.setString(5, c.getNiveau());
            ps.setString(6, c.getCategorie());
            ps.setBoolean(7, c.isEstCertifiant());
            ps.setDate(8, Date.valueOf(c.getDateDebut()));
            ps.setDate(9, Date.valueOf(c.getDateFin()));
            ps.setInt(10, c.getId());
            ps.executeUpdate();
            System.out.println("Cours modifié ✅");
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
            System.out.println("Cours supprimé ✅");
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
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public List<Cours> getByNiveau(String niveau) {
        List<Cours> liste = new ArrayList<>();
        String req = "SELECT * FROM cours WHERE niveau = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, niveau);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public List<Cours> getByCategorie(String categorie) {
        List<Cours> liste = new ArrayList<>();
        String req = "SELECT * FROM cours WHERE categorie = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, categorie);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public List<Cours> getCertifiants() {
        List<Cours> liste = new ArrayList<>();
        String req = "SELECT * FROM cours WHERE est_certifiant = TRUE";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public void getCoursAvecChapitres() {
        String req = "SELECT co.titre AS cours, co.niveau, co.categorie, co.est_certifiant, " +
                "ch.titre AS chapitre, ch.ordre, ch.type_contenu, ch.duree_minutes " +
                "FROM cours co " +
                "JOIN chapitre ch ON co.id = ch.cours_id " +
                "ORDER BY co.id, ch.ordre";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("=== Cours avec Chapitres ===");
            while (rs.next()) {
                System.out.printf("📘 %-25s [%-15s][%-15s] → Ch.%d : %-25s (%s - %dmin)%n",
                        rs.getString("cours"),
                        rs.getString("niveau"),
                        rs.getString("categorie"),
                        rs.getInt("ordre"),
                        rs.getString("chapitre"),
                        rs.getString("type_contenu"),
                        rs.getInt("duree_minutes")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Cours mapResultSet(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setId(rs.getInt("id"));
        c.setTitre(rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setObjectifs(rs.getString("objectifs"));
        c.setDureeHeures(rs.getInt("duree_heures"));
        c.setNiveau(rs.getString("niveau"));
        c.setCategorie(rs.getString("categorie"));
        c.setEstCertifiant(rs.getBoolean("est_certifiant"));
        if (rs.getDate("date_debut") != null)
            c.setDateDebut(rs.getDate("date_debut").toLocalDate());
        if (rs.getDate("date_fin") != null)
            c.setDateFin(rs.getDate("date_fin").toLocalDate());
        return c;
    }
}