package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import models.Materiel;

public class MapController {

    @FXML private WebView mapView;
    @FXML private Label lblTitre;
    @FXML private Label lblSalle;

    public void afficherMateriel(Materiel m) {
        lblTitre.setText("Localisation : " + m.getNom());
        lblSalle.setText("Salle : " + m.getSalleNom() + " | Departement : " + m.getDepartementNom());

        // Si coordonnées par défaut → utiliser ESPRIT Ariana
        double lat = (m.getLatitude() == 36.8065) ? 36.8978 : m.getLatitude();
        double lon = (m.getLongitude() == 10.1815) ? 10.1873 : m.getLongitude();

        WebEngine engine = mapView.getEngine();

        String html = "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta charset='utf-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "html,body,#map{width:100%;height:100%;margin:0;padding:0;}" +
                "</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map').setView([" + lat + "," + lon + "], 18);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{" +
                "attribution:'OpenStreetMap'}).addTo(map);" +
                "var icon = L.divIcon({" +
                "className:''," +
                "html:'<div style=\"background:#1a73e8;width:30px;height:30px;border-radius:50% 50% 50% 0;transform:rotate(-45deg);border:3px solid white;box-shadow:0 2px 8px rgba(0,0,0,0.4);\"></div>'," +
                "iconSize:[30,30]," +
                "iconAnchor:[15,30]" +
                "});" +
                "var marker = L.marker([" + lat + "," + lon + "], {icon:icon}).addTo(map);" +
                "marker.bindPopup(" +
                "'<div style=\"font-family:Arial;padding:5px;\">" +
                "<h3 style=\"color:#1a73e8;margin:0 0 8px 0;\">" + m.getNom() + "</h3>" +
                "<hr style=\"border-color:#e0e0e0;margin:5px 0;\">" +
                "<b>Code:</b> " + m.getCode() + "<br>" +
                "<b>Etat:</b> " + m.getEtat() + "<br>" +
                "<b>Salle:</b> " + m.getSalleNom() + "<br>" +
                "<b>Departement:</b> " + m.getDepartementNom() + "<br>" +
                "<b>Quantite:</b> " + m.getQuantite() + "" +
                "</div>'" +
                ").openPopup();" +
                "L.circle([" + lat + "," + lon + "], {" +
                "color:'#1a73e8'," +
                "fillColor:'#1a73e8'," +
                "fillOpacity:0.1," +
                "radius:30" +
                "}).addTo(map);" +
                "</script>" +
                "</body></html>";

        engine.loadContent(html);
    }
}