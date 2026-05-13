package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Hackathon;
import services.ServiceHackathon;
import services.ServiceReservation;

import java.io.IOException;

public class GestionHackathonController {

    @FXML private TextField tfNom;
    @FXML private TextField tfCategorie;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private TextField tfReservationId;
    @FXML private TextField tfRecherche;
    @FXML private Button    btnAjouter;
    @FXML private Label     lbMessage;

    @FXML private TableView<Hackathon>            tableAcaton;
    @FXML private TableColumn<Hackathon, String>  colNom;
    @FXML private TableColumn<Hackathon, String>  colCategorie;
    @FXML private TableColumn<Hackathon, Integer> colDuree;
    @FXML private TableColumn<Hackathon, Double>  colPrix;
    @FXML private TableColumn<Hackathon, Integer> colReservationId;

    private final ServiceHackathon   sa = new ServiceHackathon();
    private final ServiceReservation sr = new ServiceReservation();
    private ObservableList<Hackathon> tousLesHackathons = FXCollections.observableArrayList();

    private static final String OK   = "-fx-border-color:#0f9d58;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String ERR  = "-fx-border-color:#d93025;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String WARN = "-fx-border-color:#f9ab00;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String NONE = "-fx-border-color:#dde1e7;-fx-border-radius:6;-fx-background-radius:6;";

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        chargerTableau();
        attacherValidations();
    }

    private void attacherValidations() {

        // ── Nom : min 3, max 100, doit contenir une lettre ─────────────
        tfNom.textProperty().addListener((obs, o, n) -> {
            String v = n.trim();
            if (v.isEmpty()) {
                tfNom.setStyle(NONE);
            } else if (v.length() < 3) {
                tfNom.setStyle(ERR); infoChamp("Nom : minimum 3 caractères.");
            } else if (v.length() > 100) {
                tfNom.setText(n.substring(0, 100));
                tfNom.setStyle(WARN); infoChamp("Nom : maximum 100 caractères atteint.");
            } else if (!v.matches(".*[a-zA-ZÀ-ÿ].*")) {
                tfNom.setStyle(ERR); infoChamp("Nom : doit contenir au moins une lettre.");
            } else {
                tfNom.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Catégorie : optionnelle, si remplie min 3, max 50, lettres ──
        tfCategorie.textProperty().addListener((obs, o, n) -> {
            String v = n.trim();
            if (v.isEmpty()) {
                tfCategorie.setStyle(NONE);
            } else if (v.length() < 3) {
                tfCategorie.setStyle(ERR); infoChamp("Catégorie : minimum 3 caractères.");
            } else if (v.length() > 50) {
                tfCategorie.setText(n.substring(0, 50));
                tfCategorie.setStyle(WARN); infoChamp("Catégorie : maximum 50 caractères atteint.");
            } else if (!v.matches(".*[a-zA-ZÀ-ÿ].*")) {
                tfCategorie.setStyle(ERR); infoChamp("Catégorie : doit contenir au moins une lettre.");
            } else {
                tfCategorie.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Durée : entier, entre 1 et 480 minutes (8h max) ────────────
        tfDuree.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) { tfDuree.setText(n.replaceAll("[^\\d]", "")); return; }
            if (n.isEmpty()) {
                tfDuree.setStyle(NONE);
            } else {
                try {
                    int v = Integer.parseInt(n);
                    if (v <= 0) {
                        tfDuree.setStyle(ERR); infoChamp("Durée : doit être supérieure à 0 minutes.");
                    } else if (v > 480) {
                        tfDuree.setStyle(ERR); infoChamp("Durée : maximum 480 minutes (8 heures).");
                    } else if (v < 30) {
                        tfDuree.setStyle(WARN); infoChamp("Durée : moins de 30 min — êtes-vous sûr ?");
                    } else {
                        tfDuree.setStyle(OK); lbMessage.setText("");
                    }
                } catch (NumberFormatException e) { tfDuree.setStyle(ERR); }
            }
            validerFormulaire();
        });

        // ── Prix : décimal >= 0, max 9999.99 DT ────────────────────────
        tfPrix.textProperty().addListener((obs, o, n) -> {
            // Accepter uniquement chiffres et point décimal
            if (!n.matches("[\\d.]*")) { tfPrix.setText(n.replaceAll("[^\\d.]", "")); return; }
            // Bloquer plus d'un point
            if (n.chars().filter(c -> c == '.').count() > 1) { tfPrix.setText(o); return; }
            if (n.isEmpty()) {
                tfPrix.setStyle(NONE);
            } else {
                try {
                    double v = Double.parseDouble(n);
                    if (v < 0) {
                        tfPrix.setStyle(ERR); infoChamp("Prix : ne peut pas être négatif.");
                    } else if (v > 9999.99) {
                        tfPrix.setStyle(ERR); infoChamp("Prix : maximum 9 999,99 DT.");
                    } else if (v == 0) {
                        tfPrix.setStyle(WARN); infoChamp("Prix : hackathon gratuit (0 DT) — OK.");
                    } else {
                        tfPrix.setStyle(OK); lbMessage.setText("");
                    }
                } catch (NumberFormatException e) { tfPrix.setStyle(ERR); }
            }
            validerFormulaire();
        });

        // ── ID Réservation : entier > 0, doit exister en BD ────────────
        tfReservationId.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) { tfReservationId.setText(n.replaceAll("[^\\d]", "")); return; }
            if (n.isEmpty()) {
                tfReservationId.setStyle(NONE);
            } else {
                try {
                    int v = Integer.parseInt(n);
                    if (v <= 0) {
                        tfReservationId.setStyle(ERR); infoChamp("ID Réservation : doit être supérieur à 0.");
                    } else {
                        // Vérifier existence en BD
                        boolean existe = sr.getAll().stream().anyMatch(r -> r.getId() == v);
                        if (existe) {
                            tfReservationId.setStyle(OK); lbMessage.setText("");
                        } else {
                            tfReservationId.setStyle(ERR);
                            infoChamp("ID Réservation " + v + " : n'existe pas dans la base de données !");
                        }
                    }
                } catch (NumberFormatException e) { tfReservationId.setStyle(ERR); }
            }
            validerFormulaire();
        });
    }

    private void validerFormulaire() {
        String nom = tfNom.getText().trim();
        boolean nomOk = nom.length() >= 3 && nom.length() <= 100 && nom.matches(".*[a-zA-ZÀ-ÿ].*");
        String cat = tfCategorie.getText().trim();
        boolean catOk = cat.isEmpty() || (cat.length() >= 3 && cat.length() <= 50 && cat.matches(".*[a-zA-ZÀ-ÿ].*"));
        boolean dureeOk = false;
        try { int v = Integer.parseInt(tfDuree.getText().trim()); dureeOk = v > 0 && v <= 480; }
        catch (NumberFormatException ignored) {}
        boolean prixOk = false;
        try { double v = Double.parseDouble(tfPrix.getText().trim()); prixOk = v >= 0 && v <= 9999.99; }
        catch (NumberFormatException ignored) {}
        boolean idOk = false;
        try {
            int v = Integer.parseInt(tfReservationId.getText().trim());
            idOk = v > 0 && sr.getAll().stream().anyMatch(r -> r.getId() == v);
        } catch (NumberFormatException ignored) {}
        btnAjouter.setDisable(!(nomOk && catOk && dureeOk && prixOk && idOk));
    }

    @FXML
    public void rechercherHackathon(javafx.scene.input.KeyEvent event) {
        String motCle = tfRecherche.getText().toLowerCase().trim();
        if (motCle.isEmpty()) {
            tableAcaton.setItems(tousLesHackathons);
        } else {
            ObservableList<Hackathon> filtres = FXCollections.observableArrayList();
            for (Hackathon h : tousLesHackathons) {
                if (h.getNom().toLowerCase().contains(motCle)
                        || (h.getCategorie() != null && h.getCategorie().toLowerCase().contains(motCle))) {
                    filtres.add(h);
                }
            }
            tableAcaton.setItems(filtres);
        }
    }

    @FXML
    public void ajouterAcaton(ActionEvent event) {
        if (tableAcaton.getSelectionModel().getSelectedItem() != null) {
            erreur("⚠ Désélectionnez la ligne avant d'ajouter !"); return;
        }
        String nom = tfNom.getText().trim();
        // Nom unique
        boolean nomExiste = tousLesHackathons.stream().anyMatch(h -> h.getNom().equalsIgnoreCase(nom));
        if (nomExiste) { erreur("⚠ Un hackathon avec ce nom existe déjà !"); tfNom.setStyle(ERR); return; }
        try {
            int duree = Integer.parseInt(tfDuree.getText().trim());
            double prix = Double.parseDouble(tfPrix.getText().trim());
            int resId = Integer.parseInt(tfReservationId.getText().trim());
            if (duree <= 0 || duree > 480) { erreur("⚠ Durée : entre 1 et 480 minutes !"); return; }
            if (prix < 0 || prix > 9999.99) { erreur("⚠ Prix : entre 0 et 9 999,99 DT !"); return; }
            if (resId <= 0) { erreur("⚠ ID Réservation invalide !"); return; }
            boolean existe = sr.getAll().stream().anyMatch(r -> r.getId() == resId);
            if (!existe) { erreur("⚠ ID Réservation " + resId + " n'existe pas en base de données !"); return; }

            Hackathon a = new Hackathon();
            a.setNom(nom); a.setCategorie(tfCategorie.getText().trim());
            a.setDuree(duree); a.setPrix(prix); a.setReservationId(resId);
            sa.add(a);
            succes("✔ Hackathon \"" + nom + "\" ajouté avec succès !");
            viderFormulaire(); chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Durée et ID = entiers, Prix = décimal !"); }
    }

    @FXML
    public void modifierAcaton(ActionEvent event) {
        Hackathon sel = tableAcaton.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un hackathon à modifier !"); return; }
        String nom = tfNom.getText().trim();
        if (nom.length() < 3) { erreur("⚠ Nom : minimum 3 caractères !"); return; }
        if (!nom.matches(".*[a-zA-ZÀ-ÿ].*")) { erreur("⚠ Nom : doit contenir au moins une lettre !"); return; }
        String cat = tfCategorie.getText().trim();
        if (!cat.isEmpty() && cat.length() < 3) { erreur("⚠ Catégorie : minimum 3 caractères !"); return; }
        try {
            int duree = Integer.parseInt(tfDuree.getText().trim());
            double prix = Double.parseDouble(tfPrix.getText().trim());
            int resId = Integer.parseInt(tfReservationId.getText().trim());
            if (duree <= 0 || duree > 480) { erreur("⚠ Durée : entre 1 et 480 minutes !"); return; }
            if (prix < 0 || prix > 9999.99) { erreur("⚠ Prix : entre 0 et 9 999,99 DT !"); return; }
            boolean resExiste = sr.getAll().stream().anyMatch(r -> r.getId() == resId);
            if (!resExiste) { erreur("⚠ ID Réservation " + resId + " n'existe pas en BD !"); return; }
            // Nom unique sauf lui-même
            boolean nomExiste = tousLesHackathons.stream()
                    .anyMatch(h -> h.getNom().equalsIgnoreCase(nom) && h.getId() != sel.getId());
            if (nomExiste) { erreur("⚠ Ce nom est déjà utilisé par un autre hackathon !"); return; }
            boolean rienChange = nom.equals(sel.getNom())
                    && cat.equals(sel.getCategorie() == null ? "" : sel.getCategorie())
                    && duree == sel.getDuree() && prix == sel.getPrix() && resId == sel.getReservationId();
            if (rienChange) { avertissement("⚠ Aucune modification détectée !"); return; }
            sel.setNom(nom); sel.setCategorie(cat);
            sel.setDuree(duree); sel.setPrix(prix); sel.setReservationId(resId);
            sa.update(sel);
            succes("✔ Hackathon modifié avec succès !"); viderFormulaire(); chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Durée et ID = entiers, Prix = décimal !"); }
    }

    @FXML
    public void supprimerAcaton(ActionEvent event) {
        Hackathon sel = tableAcaton.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un hackathon à supprimer !"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer \"" + sel.getNom() + "\" ?");
        alert.setContentText("Cette action est irréversible !");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sa.delete(sel); succes("✔ Hackathon supprimé !"); viderFormulaire(); chargerTableau();
            }
        });
    }

    @FXML
    public void selectionnerLigne(javafx.scene.input.MouseEvent event) {
        Hackathon s = tableAcaton.getSelectionModel().getSelectedItem();
        if (s != null) {
            tfNom.setText(s.getNom());
            tfCategorie.setText(s.getCategorie() == null ? "" : s.getCategorie());
            tfDuree.setText(String.valueOf(s.getDuree()));
            tfPrix.setText(String.valueOf(s.getPrix()));
            tfReservationId.setText(s.getReservationId() == 0 ? "" : String.valueOf(s.getReservationId()));
            info("ℹ \"" + s.getNom() + "\" sélectionné — modifiez et cliquez Modifier.");
        }
    }

    private void chargerTableau() {
        tousLesHackathons = FXCollections.observableArrayList(sa.getAll());
        tableAcaton.setItems(tousLesHackathons);
        if (tfRecherche != null) tfRecherche.clear();
    }

    @FXML public void allerReservation(ActionEvent e) {
        try { tfNom.getScene().setRoot(FXMLLoader.load(getClass().getResource("/GestionReservation.fxml"))); }
        catch (IOException ex) { System.out.println(ex.getMessage()); }
    }
    @FXML public void allerDashboard(ActionEvent e) {
        try { tfNom.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Dashboard.fxml"))); }
        catch (IOException ex) { System.out.println(ex.getMessage()); }
    }

    private void erreur(String m)        { lbMessage.setStyle("-fx-text-fill:#d93025;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void succes(String m)        { lbMessage.setStyle("-fx-text-fill:#0f9d58;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void info(String m)          { lbMessage.setStyle("-fx-text-fill:#1a73e8;-fx-font-style:italic;"); lbMessage.setText(m); }
    private void avertissement(String m) { lbMessage.setStyle("-fx-text-fill:#f9ab00;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void infoChamp(String m)     { lbMessage.setStyle("-fx-text-fill:#5f6368;-fx-font-style:italic;"); lbMessage.setText(m); }

    private void viderFormulaire() {
        tfNom.clear();           tfNom.setStyle(NONE);
        tfCategorie.clear();     tfCategorie.setStyle(NONE);
        tfDuree.clear();         tfDuree.setStyle(NONE);
        tfPrix.clear();          tfPrix.setStyle(NONE);
        tfReservationId.clear(); tfReservationId.setStyle(NONE);
        tableAcaton.getSelectionModel().clearSelection();
        btnAjouter.setDisable(false); lbMessage.setText("");
    }
}