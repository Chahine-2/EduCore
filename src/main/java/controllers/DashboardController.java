package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import models.Hackathon;
import models.Reservation;
import services.ServiceHackathon;
import services.ServicePaiement;
import services.ServiceReservation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    // ── KPI labels ─────────────────────────────────────────────────────
    @FXML private Label lbTotalReservations;
    @FXML private Label lbTotalHackathons;
    @FXML private Label lbTotalPlaces;
    @FXML private Label lbPrixMoyen;
    @FXML private Label lbTotalPaiements;   // montant total DT

    // ── Graphiques ─────────────────────────────────────────────────────
    @FXML private BarChart<String, Number>  barChart;            // hackathons/catégorie
    @FXML private PieChart                  pieChart;            // places/réservation
    @FXML private BarChart<String, Number>  barChartPaiements;   // paiements/mois (NOUVEAU)

    // ── Services ───────────────────────────────────────────────────────
    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceHackathon   sh = new ServiceHackathon();
    private final ServicePaiement    sp = new ServicePaiement();

    @FXML
    public void initialize() {
        List<Reservation> reservations = sr.getAll();
        List<Hackathon>   hackathons   = sh.getAll();
        chargerKPI(reservations, hackathons);
        chargerBarChart(hackathons);
        chargerPieChart(reservations);
        chargerBarChartPaiements();   // ← nouveau
    }

    // ── KPI ────────────────────────────────────────────────────────────
    private void chargerKPI(List<Reservation> reservations, List<Hackathon> hackathons) {

        lbTotalReservations.setText(String.valueOf(reservations.size()));
        lbTotalHackathons.setText(String.valueOf(hackathons.size()));

        int totalPlaces = reservations.stream().mapToInt(Reservation::getNbPlaces).sum();
        lbTotalPlaces.setText(String.valueOf(totalPlaces));

        if (!hackathons.isEmpty()) {
            double prixMoyen = hackathons.stream().mapToDouble(Hackathon::getPrix).average().orElse(0);
            lbPrixMoyen.setText(String.format("%.1f", prixMoyen));
        } else {
            lbPrixMoyen.setText("0");
        }

        // Total paiements CONFIRMÉS en DT
        double totalPaiements = sp.getTotalPaiements();
        lbTotalPaiements.setText(String.format("%.2f", totalPaiements));
    }

    // ── BarChart hackathons par catégorie ─────────────────────────────
    private void chargerBarChart(List<Hackathon> hackathons) {
        Map<String, Long> parCategorie = hackathons.stream().collect(
                Collectors.groupingBy(
                        h -> h.getCategorie() == null || h.getCategorie().isEmpty() ? "Autre" : h.getCategorie(),
                        Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hackathons");
        parCategorie.forEach((cat, count) -> series.getData().add(new XYChart.Data<>(cat, count)));

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.lookupAll(".default-color0.chart-bar")
                .forEach(node -> node.setStyle("-fx-bar-fill: #1a73e8;"));
        barChart.setStyle("-fx-background-color: transparent;");
    }

    // ── PieChart places par réservation ──────────────────────────────
    private void chargerPieChart(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            pieChart.setData(FXCollections.observableArrayList(new PieChart.Data("Aucune donnée", 1)));
            return;
        }
        var slices = reservations.stream()
                .map(r -> new PieChart.Data(
                        r.getTitre().length() > 14 ? r.getTitre().substring(0, 14) + "…" : r.getTitre(),
                        r.getNbPlaces()))
                .collect(Collectors.toList());
        pieChart.setData(FXCollections.observableArrayList(slices));
        pieChart.setStyle("-fx-background-color: transparent;");
    }

    // ── BarChart paiements par mois (NOUVEAU) ─────────────────────────
    private void chargerBarChartPaiements() {
        Map<String, Double> parMois = sp.getPaiementsParMois();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus (DT)");

        if (parMois.isEmpty()) {
            series.getData().add(new XYChart.Data<>("Aucun paiement", 0));
        } else {
            parMois.forEach((mois, total) -> series.getData().add(new XYChart.Data<>(mois, total)));
        }

        barChartPaiements.getData().clear();
        barChartPaiements.getData().add(series);

        // Colorer les barres en violet
        barChartPaiements.lookupAll(".default-color0.chart-bar")
                .forEach(node -> node.setStyle("-fx-bar-fill: #7c3aed;"));
        barChartPaiements.setStyle("-fx-background-color: transparent;");
    }

    // ── Navigation ─────────────────────────────────────────────────────
    @FXML void allerReservation(ActionEvent event) { naviguer("/GestionReservation.fxml"); }
    @FXML void allerHackathon(ActionEvent event)   { naviguer("/GestionHackathon.fxml"); }
    @FXML void allerPaiement(ActionEvent event)    { naviguer("/Paiement.fxml"); }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            lbTotalReservations.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println("Erreur navigation : " + e.getMessage());
        }
    }
}