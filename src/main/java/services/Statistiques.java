package services;

import utils.MyDataBase;
import java.sql.*;

public class Statistiques {

    public void reservationsParMateriel() {
        String req = "SELECT m.nom, COUNT(r.id) as total " +
                "FROM materiel m LEFT JOIN reservation r ON m.id = r.materiel_id " +
                "GROUP BY m.id, m.nom ORDER BY total DESC";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Réservations par matériel ---");
            while (rs.next()) {
                System.out.println("📦 " + rs.getString("nom") + " → " + rs.getInt("total") + " réservation(s)");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void materielPlusDemande() {
        String req = "SELECT m.nom, COUNT(r.id) as total " +
                "FROM materiel m JOIN reservation r ON m.id = r.materiel_id " +
                "GROUP BY m.id, m.nom ORDER BY total DESC LIMIT 1";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Matériel le plus demandé ---");
            if (rs.next()) {
                System.out.println("🏆 " + rs.getString("nom") + " avec " + rs.getInt("total") + " réservation(s)");
            } else {
                System.out.println("Aucune réservation trouvée.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void tauxOccupation() {
        String req = "SELECT m.nom, " +
                "ROUND(SUM(TIMESTAMPDIFF(HOUR, r.date_debut, r.date_fin)) / 8.0 * 100, 2) as taux " +
                "FROM materiel m JOIN reservation r ON m.id = r.materiel_id " +
                "WHERE r.statut = 'confirmee' " +
                "GROUP BY m.id, m.nom ORDER BY taux DESC";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Taux d'occupation (8h/jour) ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("📊 " + rs.getString("nom") + " → " + rs.getDouble("taux") + "%");
            }
            if (!found) System.out.println("Aucune réservation confirmée trouvée.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void materielsParEtat() {
        String req = "SELECT etat, COUNT(*) as total FROM materiel GROUP BY etat";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Matériels par état ---");
            while (rs.next()) {
                System.out.println("🔹 " + rs.getString("etat") + " : " + rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void reservationsMoisCourant() {
        String req = "SELECT COUNT(*) as total FROM reservation " +
                "WHERE MONTH(date_debut) = MONTH(NOW()) AND YEAR(date_debut) = YEAR(NOW())";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Réservations ce mois-ci ---");
            if (rs.next()) {
                System.out.println("📅 Total : " + rs.getInt("total") + " réservation(s)");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}