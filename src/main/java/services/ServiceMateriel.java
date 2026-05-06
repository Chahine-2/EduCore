package services;

import interfaces.IService;
import models.Materiel;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMateriel implements IService<Materiel> {

    // Vérifier si le code existe déjà (validation doublon)
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

    @Override
    public void add(Materiel m) {
        // Validation code unique
        if (codeExiste(m.getCode())) {
            System.out.println("❌ Erreur : un matériel avec ce code existe déjà !");
            return;
        }
        // Validation quantité
        if (m.getQuantite() <= 0) {
            System.out.println("❌ Erreur : la quantité doit être positive !");
            return;
        }
        String req = "INSERT INTO materiel (nom, code, description, quantite, etat) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, m.getNom());
            ps.setString(2, m.getCode());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getQuantite());
            ps.setString(5, m.getEtat());
            ps.executeUpdate();
            System.out.println("✅ Matériel ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Materiel> getAll() {
        List<Materiel> liste = new ArrayList<>();
        String req = "SELECT * FROM materiel";
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
            System.out.println("❌ Erreur : la quantité doit être positive !");
            return;
        }
        String req = "UPDATE materiel SET nom=?, code=?, description=?, quantite=?, etat=? WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, m.getNom());
            ps.setString(2, m.getCode());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getQuantite());
            ps.setString(5, m.getEtat());
            ps.setInt(6, m.getId());
            ps.executeUpdate();
            System.out.println("✅ Matériel mis à jour !");
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
            System.out.println("✅ Matériel supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Changer état matériel automatiquement
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