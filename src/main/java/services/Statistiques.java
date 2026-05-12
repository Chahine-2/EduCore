package services;

import utils.MyDataBase;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Statistiques {

    public Map<String, Integer> getReservationsParMateriel() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT m.nom, COUNT(r.id) as total " +
                "FROM materiel m LEFT JOIN reservation r ON m.id = r.materiel_id " +
                "GROUP BY m.id, m.nom ORDER BY total DESC";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                String nom = rs.getString("nom");
                if (nom.length() > 12) nom = nom.substring(0, 12) + "...";
                map.put(nom, rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getMaterielsParEtat() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT etat, COUNT(*) as total FROM materiel GROUP BY etat";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                map.put(rs.getString("etat"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getReservationsParStatut() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT statut, COUNT(*) as total FROM reservation GROUP BY statut";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                map.put(rs.getString("statut"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getQuantiteParMateriel() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT nom, quantite FROM materiel ORDER BY nom";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                String nom = rs.getString("nom");
                if (nom.length() > 12) nom = nom.substring(0, 12) + "...";
                map.put(nom, rs.getInt("quantite"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    public void reservationsParMateriel() {
        Map<String, Integer> map = getReservationsParMateriel();
        System.out.println("\n--- Reservations par materiel ---");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("📦 " + entry.getKey() + " → " + entry.getValue() + " reservation(s)");
        }
    }

    public void materielPlusDemande() {
        String req = "SELECT m.nom, COUNT(r.id) as total " +
                "FROM materiel m JOIN reservation r ON m.id = r.materiel_id " +
                "GROUP BY m.id, m.nom ORDER BY total DESC LIMIT 1";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Materiel le plus demande ---");
            if (rs.next()) {
                System.out.println("🏆 " + rs.getString("nom") + " avec " + rs.getInt("total") + " reservation(s)");
            } else {
                System.out.println("Aucune reservation trouvee.");
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
            System.out.println("\n--- Taux d'occupation ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("📊 " + rs.getString("nom") + " → " + rs.getDouble("taux") + "%");
            }
            if (!found) System.out.println("Aucune reservation confirmee.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void materielsParEtat() {
        Map<String, Integer> map = getMaterielsParEtat();
        System.out.println("\n--- Materiels par etat ---");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("🔹 " + entry.getKey() + " : " + entry.getValue());
        }
    }

    public void reservationsMoisCourant() {
        String req = "SELECT COUNT(*) as total FROM reservation " +
                "WHERE MONTH(date_debut) = MONTH(NOW()) AND YEAR(date_debut) = YEAR(NOW())";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            System.out.println("\n--- Reservations ce mois ---");
            if (rs.next()) {
                System.out.println("📅 Total : " + rs.getInt("total") + " reservation(s)");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}