package services;

import interfaces.IService;
import models.Materiel;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMateriel implements IService<Materiel> {

    public boolean codeExiste(String code) {
        String req = "SELECT COUNT(*) FROM materiel WHERE code = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // Récupérer tous les départements
    public List<String> getDepartements() {
        List<String> departements = new ArrayList<>();
        String req = "SELECT id, nom FROM departement";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                departements.add(rs.getInt("id") + " - " + rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return departements;
    }

    // Récupérer salles par département
    public List<String> getSallesParDepartement(int departementId) {
        List<String> salles = new ArrayList<>();
        String req = "SELECT id, nom FROM salle WHERE departement_id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, departementId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                salles.add(rs.getInt("id") + " - " + rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return salles;
    }

    public int getIdFromString(String str) {
        return Integer.parseInt(str.split(" - ")[0]);
    }
    @Override
    public void add(Materiel m) {
        if (codeExiste(m.getCode())) {
            System.out.println("❌ Code existe deja !");
            return;
        }
        if (m.getQuantite() <= 0) {
            System.out.println("❌ Quantite doit etre positive !");
            return;
        }
        String req = "INSERT INTO materiel (nom, code, description, quantite, etat, salle_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, m.getNom());
            ps.setString(2, m.getCode());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getQuantite());
            ps.setString(5, m.getEtat());
            ps.setInt(6, m.getSalleId());
            ps.executeUpdate();
            System.out.println("✅ Materiel ajoute !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Materiel> getAll() {
        List<Materiel> liste = new ArrayList<>();
        String req = "SELECT m.*, s.nom as salle_nom, s.latitude, s.longitude, d.nom as dept_nom " +
                "FROM materiel m " +
                "LEFT JOIN salle s ON m.salle_id = s.id " +
                "LEFT JOIN departement d ON s.departement_id = d.id";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Materiel m = new Materiel();
                m.setId(rs.getInt("id"));
                m.setNom(rs.getString("nom"));
                m.setCode(rs.getString("code"));
                m.setDescription(rs.getString("description"));
                m.setQuantite(rs.getInt("quantite"));
                m.setEtat(rs.getString("etat"));
                m.setSalleId(rs.getInt("salle_id"));
                m.setSalleNom(rs.getString("salle_nom"));
                m.setDepartementNom(rs.getString("dept_nom"));
                m.setLatitude(rs.getDouble("latitude"));
                m.setLongitude(rs.getDouble("longitude"));
                Timestamp ts = rs.getTimestamp("date_creation");
                if (ts != null) m.setDateCreation(ts.toLocalDateTime());
                liste.add(m);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    @Override
    public void update(Materiel m) {
        if (m.getQuantite() <= 0) {
            System.out.println("❌ Quantite doit etre positive !");
            return;
        }
        String req = "UPDATE materiel SET nom=?, code=?, description=?, quantite=?, etat=?, salle_id=? WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, m.getNom());
            ps.setString(2, m.getCode());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getQuantite());
            ps.setString(5, m.getEtat());
            ps.setInt(6, m.getSalleId());
            ps.setInt(7, m.getId());
            ps.executeUpdate();
            System.out.println("✅ Materiel mis a jour !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Materiel m) {
        String req = "DELETE FROM materiel WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, m.getId());
            ps.executeUpdate();
            System.out.println("✅ Materiel supprime !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void changerEtat(int materielId, String etat) {
        String req = "UPDATE materiel SET etat=? WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, etat);
            ps.setInt(2, materielId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}