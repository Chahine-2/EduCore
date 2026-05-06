package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuPrincipalController {

    private void ouvrirFenetre(String fxmlFile, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/" + fxmlFile)
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirMateriel(ActionEvent e) {
        ouvrirFenetre("GestionMateriel.fxml", "Gestion des Materiels");
    }

    @FXML
    public void ouvrirReservation(ActionEvent e) {
        ouvrirFenetre("GestionReservation.fxml", "Gestion des Reservations");
    }

    @FXML
    public void ouvrirStatistiques(ActionEvent e) {
        ouvrirFenetre("Statistiques.fxml", "Statistiques");
    }

    @FXML
    public void quitter(ActionEvent e) {
        Platform.exit();
    }
}