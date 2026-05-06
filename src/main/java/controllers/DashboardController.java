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

    // ── Graphiques ─────────────────────────────────────────────────────
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    // ── Services ───────────────────────────────────────────────────────
    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceHackathon   sh = new ServiceHackathon();

    @FXML
    public void initialize() {
        List<Reservation> reservations = sr.getAll();
        List<Hackathon>   hackathons   = sh.getAll();

        chargerKPI(reservations, hackathons);
        chargerBarChart(hackathons);
        chargerPieChart(reservations);
    }

    // ── KPI ────────────────────────────────────────────────────────────
    private void chargerKPI(List<Reservation> reservations, List<Hackathon> hackathons) {

        // Nombre total
        lbTotalReservations.setText(String.valueOf(reservations.size()));
        lbTotalHackathons.setText(String.valueOf(hackathons.size()));

        // Total des places
        int totalPlaces = reservations.stream()
                .mapToInt(Reservation::getNbPlaces)
                .sum();
        lbTotalPlaces.setText(String.valueOf(totalPlaces));

        // Prix moyen des hackathons
        if (!hackathons.isEmpty()) {
            double prixMoyen = hackathons.stream()
                    .mapToDouble(Hackathon::getPrix)
                    .average()
                    .orElse(0);
            lbPrixMoyen.setText(String.format("%.1f", prixMoyen));
        } else {
            lbPrixMoyen.setText("0");
        }
    }

    // ── BarChart : hackathons par catégorie ────────────────────────────
    private void chargerBarChart(List<Hackathon> hackathons) {

        // Grouper par catégorie et compter
        Map<String, Long> parCategorie = hackathons.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getCategorie().isEmpty() ? "Autre" : h.getCategorie(),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hackathons");

        parCategorie.forEach((categorie, count) ->
                series.getData().add(new XYChart.Data<>(categorie, count))
        );

        barChart.getData().clear();
        barChart.getData().add(series);

        // Style des barres
        barChart.lookupAll(".default-color0.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #1a73e8;")
        );
        barChart.setStyle("-fx-background-color: transparent;");
    }

    // ── PieChart : places par réservation ─────────────────────────────
    private void chargerPieChart(List<Reservation> reservations) {

        if (reservations.isEmpty()) {
            pieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Aucune donnée", 1)
            ));
            return;
        }

        var slices = reservations.stream()
                .map(r -> new PieChart.Data(
                        r.getTitre().length() > 14
                                ? r.getTitre().substring(0, 14) + "…"
                                : r.getTitre(),
                        r.getNbPlaces()
                ))
                .collect(Collectors.toList());

        pieChart.setData(FXCollections.observableArrayList(slices));
        pieChart.setStyle("-fx-background-color: transparent;");
    }

    // ── Navigation ─────────────────────────────────────────────────────
    @FXML
    void allerReservation(ActionEvent event) {
        naviguer("/GestionReservation.fxml");
    }

    @FXML
    void allerHackathon(ActionEvent event) {
        naviguer("/GestionHackathon.fxml");
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            lbTotalReservations.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println("Erreur navigation : " + e.getMessage());
        }
    }
}