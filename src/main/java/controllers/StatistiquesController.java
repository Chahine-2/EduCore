package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import services.Statistiques;
import utils.MyDataBase;
import java.sql.*;

public class StatistiquesController {

    @FXML private TextArea taResultat;

    private Statistiques stats = new Statistiques();

    @FXML
    public void afficherReservationsParMateriel(ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Reservations par materiel ===\n\n");
        String req = "SELECT m.nom, COUNT(r.id) as total " +
                "FROM materiel m LEFT JOIN reservation r ON m.id = r.materiel_id " +
                "GROUP BY m.id, m.nom ORDER BY total DESC";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                sb.append("📦 ").append(rs.getString("nom"))
                        .append(" → ").append(rs.getInt("total")).append(" reservation(s)\n");
            }
        } catch (SQLException ex) {
            sb.append("Erreur : ").append(ex.getMessage());
        }
        taResultat.setText(sb.toString());
    }

    @FXML
    public void afficherMaterielPlusDemande(ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Materiel le plus demande ===\n\n");
        String req = "SELECT m.nom, COUNT(r.id) as total " +
                "FROM materiel m JOIN reservation r ON m.id = r.materiel_id " +
                "GROUP BY m.id, m.nom ORDER BY total DESC LIMIT 1";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                sb.append("🏆 ").append(rs.getString("nom"))
                        .append(" avec ").append(rs.getInt("total")).append(" reservation(s)\n");
            } else {
                sb.append("Aucune reservation trouvee.");
            }
        } catch (SQLException ex) {
            sb.append("Erreur : ").append(ex.getMessage());
        }
        taResultat.setText(sb.toString());
    }

    @FXML
    public void afficherMaterielsParEtat(ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Materiels par etat ===\n\n");
        String req = "SELECT etat, COUNT(*) as total FROM materiel GROUP BY etat";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                sb.append("🔹 ").append(rs.getString("etat"))
                        .append(" : ").append(rs.getInt("total")).append("\n");
            }
        } catch (SQLException ex) {
            sb.append("Erreur : ").append(ex.getMessage());
        }
        taResultat.setText(sb.toString());
    }

    @FXML
    public void afficherReservationsMois(ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Reservations ce mois-ci ===\n\n");
        String req = "SELECT COUNT(*) as total FROM reservation " +
                "WHERE MONTH(date_debut) = MONTH(NOW()) AND YEAR(date_debut) = YEAR(NOW())";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                sb.append("📅 Total : ").append(rs.getInt("total")).append(" reservation(s)\n");
            }
        } catch (SQLException ex) {
            sb.append("Erreur : ").append(ex.getMessage());
        }
        taResultat.setText(sb.toString());
    }
}