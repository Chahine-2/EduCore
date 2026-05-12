package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import services.Statistiques;
import utils.MyDataBase;
import java.sql.*;
import java.util.Map;

public class StatistiquesController {

    @FXML private PieChart pieEtat;
    @FXML private PieChart pieStatut;
    @FXML private BarChart<String, Number> barReservations;
    @FXML private BarChart<String, Number> barQuantite;
    @FXML private Label lblTotalMateriels;
    @FXML private Label lblTotalReservations;
    @FXML private Label lblDisponibles;
    @FXML private Label lblMaintenance;
    @FXML private Label lblReservationsMois;
    @FXML private Label lblConfirmees;

    private Statistiques stats = new Statistiques();

    @FXML
    void initialize() {
        Platform.runLater(() -> {
            chargerCartes();
            chargerPieEtat();
            chargerPieStatut();
            chargerBarReservations();
            chargerBarQuantite();
        });
    }

    private void chargerCartes() {
        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            PreparedStatement ps;
            ResultSet rs;

            ps = cnx.prepareStatement("SELECT COUNT(*) FROM materiel");
            rs = ps.executeQuery();
            if (rs.next()) lblTotalMateriels.setText(String.valueOf(rs.getInt(1)));

            ps = cnx.prepareStatement("SELECT COUNT(*) FROM materiel WHERE etat='disponible'");
            rs = ps.executeQuery();
            if (rs.next()) lblDisponibles.setText(String.valueOf(rs.getInt(1)));

            ps = cnx.prepareStatement("SELECT COUNT(*) FROM materiel WHERE etat='maintenance'");
            rs = ps.executeQuery();
            if (rs.next()) lblMaintenance.setText(String.valueOf(rs.getInt(1)));

            ps = cnx.prepareStatement("SELECT COUNT(*) FROM reservation");
            rs = ps.executeQuery();
            if (rs.next()) lblTotalReservations.setText(String.valueOf(rs.getInt(1)));

            ps = cnx.prepareStatement("SELECT COUNT(*) FROM reservation WHERE MONTH(date_debut)=MONTH(NOW()) AND YEAR(date_debut)=YEAR(NOW())");
            rs = ps.executeQuery();
            if (rs.next()) lblReservationsMois.setText(String.valueOf(rs.getInt(1)));

            ps = cnx.prepareStatement("SELECT COUNT(*) FROM reservation WHERE statut='confirmee'");
            rs = ps.executeQuery();
            if (rs.next()) lblConfirmees.setText(String.valueOf(rs.getInt(1)));

        } catch (SQLException e) {
            System.out.println("Erreur cartes : " + e.getMessage());
        }
    }

    private void chargerPieEtat() {
        pieEtat.getData().clear();
        Map<String, Integer> map = stats.getMaterielsParEtat();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            pieEtat.getData().add(new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            ));
        }
    }

    private void chargerPieStatut() {
        pieStatut.getData().clear();
        Map<String, Integer> map = stats.getReservationsParStatut();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            pieStatut.getData().add(new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            ));
        }
    }

    private void chargerBarReservations() {
        barReservations.getData().clear();
        Map<String, Integer> map = stats.getReservationsParMateriel();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservations");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey(), entry.getValue()
            ));
        }
        barReservations.getData().add(series);
    }

    private void chargerBarQuantite() {
        barQuantite.getData().clear();
        Map<String, Integer> map = stats.getQuantiteParMateriel();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantite");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey(), entry.getValue()
            ));
        }
        barQuantite.getData().add(series);
    }
}{}