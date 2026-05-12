package services;

import interfaces.IService;
import models.Reservation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IService<Reservation> {

    private ServiceMateriel sm = new ServiceMateriel();

    // Vérifier conflit de dates
    public boolean conflitDates(int materielId, java.time.LocalDateTime debut, java.time.LocalDateTime fin) {
        String req = "SELECT COUNT(*) FROM reservation WHERE materiel_id=? AND statut != 'annulee' " +
                "AND date_debut < ? AND date_fin > ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, materielId);
            ps.setTimestamp(2, Timestamp.valueOf(fin));
            ps.setTimestamp(3, Timestamp.valueOf(debut));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // Vérifier disponibilité matériel
    public boolean materielDisponible(int materielId) {
        String req = "SELECT etat FROM materiel WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, materielId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("etat").equals("disponible");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public void add(Reservation r) {
        // Validation dates
        if (!r.getDateFin().isAfter(r.getDateDebut())) {
            System.out.println("❌ Erreur : la date de fin doit être après la date de début !");
            return;
        }
        // Vérifier disponibilité
        if (!materielDisponible(r.getMaterielId())) {
            System.out.println("❌ Erreur : ce matériel n'est pas disponible !");
            return;
        }
        // Vérifier conflit dates
        if (conflitDates(r.getMaterielId(), r.getDateDebut(), r.getDateFin())) {
            System.out.println("❌ Erreur : ce matériel est déjà réservé sur cette période !");
            return;
        }
        String req = "INSERT INTO reservation (materiel_id, motif, date_debut, date_fin, statut) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, r.getMaterielId());
            ps.setString(2, r.getMotif());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDateDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(r.getDateFin()));
            ps.setString(5, r.getStatut());
            ps.executeUpdate();
            System.out.println("✅ Réservation ajoutée !");

            // Changement automatique état si confirmée
            if (r.getStatut().equals("confirmee")) {
                sm.changerEtat(r.getMaterielId(), "indisponible");
                System.out.println("🔄 Matériel passé à 'indisponible' automatiquement.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Reservation> getAll() {
        List<Reservation> liste = new ArrayList<>();
        String req = "SELECT * FROM reservation";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setMaterielId(rs.getInt("materiel_id"));
                r.setMotif(rs.getString("motif"));
                Timestamp td = rs.getTimestamp("date_debut");
                if (td != null) r.setDateDebut(td.toLocalDateTime());
                Timestamp tf = rs.getTimestamp("date_fin");
                if (tf != null) r.setDateFin(tf.toLocalDateTime());
                r.setStatut(rs.getString("statut"));
                Timestamp tc = rs.getTimestamp("date_creation");
                if (tc != null) r.setDateCreation(tc.toLocalDateTime());
                liste.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    @Override
    public void update(Reservation r) {
        if (!r.getDateFin().isAfter(r.getDateDebut())) {
            System.out.println("❌ Erreur : la date de fin doit être après la date de début !");
            return;
        }
        String req = "UPDATE reservation SET materiel_id=?, motif=?, date_debut=?, date_fin=?, statut=? WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, r.getMaterielId());
            ps.setString(2, r.getMotif());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDateDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(r.getDateFin()));
            ps.setString(5, r.getStatut());
            ps.setInt(6, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réservation mise à jour !");

            // Changement automatique état
            if (r.getStatut().equals("confirmee")) {
                sm.changerEtat(r.getMaterielId(), "indisponible");
                System.out.println("🔄 Matériel passé à 'indisponible' automatiquement.");
            } else if (r.getStatut().equals("annulee")) {
                sm.changerEtat(r.getMaterielId(), "disponible");
                System.out.println("🔄 Matériel passé à 'disponible' automatiquement.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Reservation r) {
        // Récupérer materiel_id avant suppression pour remettre disponible
        String reqGet = "SELECT materiel_id FROM reservation WHERE id=?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(reqGet);
            ps.setInt(1, r.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int materielId = rs.getInt("materiel_id");
                String req = "DELETE FROM reservation WHERE id=?";
                PreparedStatement ps2 = MyDataBase.getInstance().getCnx().prepareStatement(req);
                ps2.setInt(1, r.getId());
                ps2.executeUpdate();
                sm.changerEtat(materielId, "disponible");
                System.out.println("✅ Réservation supprimée et matériel remis à 'disponible' !");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}