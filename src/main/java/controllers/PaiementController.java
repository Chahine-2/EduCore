package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import models.Paiement;
import models.Reservation;
import services.ServicePaiement;
import services.ServiceReservation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class PaiementController {

    // ── Formulaire ─────────────────────────────────────────────────────
    @FXML private ComboBox<String>  cbReservation;
    @FXML private TextField         tfNomCarte;
    @FXML private TextField         tfNumeroCarte;
    @FXML private TextField         tfExpiration;
    @FXML private TextField         tfCVV;
    @FXML private TextField         tfMontant;
    @FXML private Button            btnPayer;
    @FXML private Label             lbMessage;
    @FXML private Label             lbDejaPayee;   // ← avertissement doublon

    // ── Tableau + recherche ────────────────────────────────────────────
    @FXML private TableView<Paiement>            tablePaiement;
    @FXML private TableColumn<Paiement, Integer> colId;
    @FXML private TableColumn<Paiement, Integer> colReservation;
    @FXML private TableColumn<Paiement, Double>  colMontant;
    @FXML private TableColumn<Paiement, String>  colNom;
    @FXML private TableColumn<Paiement, String>  colCarte;
    @FXML private TableColumn<Paiement, String>  colStatut;
    @FXML private TableColumn<Paiement, String>  colDate;
    @FXML private TextField                      tfRecherche;

    // ── Services ───────────────────────────────────────────────────────
    private final ServicePaiement    sp = new ServicePaiement();
    private final ServiceReservation sr = new ServiceReservation();

    private List<Reservation>        reservations;
    private ObservableList<Paiement> tousLesPaiements = FXCollections.observableArrayList();

    private static final String OK   = "-fx-border-color: #0f9d58; -fx-border-radius:6; -fx-background-radius:6; -fx-border-width:2;";
    private static final String ERR  = "-fx-border-color: #d93025; -fx-border-radius:6; -fx-background-radius:6; -fx-border-width:2;";
    private static final String NONE = "-fx-border-color: #dde1e7; -fx-border-radius:6; -fx-background-radius:6;";

    // ── Init ───────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReservation.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCarte"));
        colCarte.setCellValueFactory(new PropertyValueFactory<>("numeroCarte"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));

        chargerReservations();
        chargerTableau();
        attacherValidations();
    }

    private void chargerReservations() {
        reservations = sr.getAll();
        cbReservation.getItems().clear();
        for (Reservation r : reservations) {
            cbReservation.getItems().add(r.getId() + " — " + r.getTitre());
        }
    }

    private void chargerTableau() {
        tousLesPaiements = FXCollections.observableArrayList(sp.getAll());
        tablePaiement.setItems(tousLesPaiements);
        if (tfRecherche != null) tfRecherche.clear();
    }

    // ── Recherche en temps réel ────────────────────────────────────────
    @FXML
    public void rechercherPaiement(javafx.scene.input.KeyEvent event) {
        String motCle = tfRecherche.getText().toLowerCase().trim();
        if (motCle.isEmpty()) {
            tablePaiement.setItems(tousLesPaiements);
        } else {
            ObservableList<Paiement> filtres = FXCollections.observableArrayList();
            for (Paiement p : tousLesPaiements) {
                boolean matchNom    = p.getNomCarte()   != null && p.getNomCarte().toLowerCase().contains(motCle);
                boolean matchCarte  = p.getNumeroCarte() != null && p.getNumeroCarte().toLowerCase().contains(motCle);
                boolean matchStatut = p.getStatut()      != null && p.getStatut().toLowerCase().contains(motCle);
                if (matchNom || matchCarte || matchStatut) filtres.add(p);
            }
            tablePaiement.setItems(filtres);
        }
    }

    // ── Sélection ligne tableau ────────────────────────────────────────
    @FXML
    public void selectionnerPaiement(MouseEvent event) {
        Paiement sel = tablePaiement.getSelectionModel().getSelectedItem();
        if (sel != null) {
            info("ℹ  Paiement sélectionné — cliquez « Rembourser » pour l'annuler.");
        }
    }

    // ── Remboursement ──────────────────────────────────────────────────
    @FXML
    public void rembourserPaiement(ActionEvent event) {
        Paiement sel = tablePaiement.getSelectionModel().getSelectedItem();
        if (sel == null) {
            erreur("⚠ Sélectionnez un paiement à rembourser !");
            return;
        }
        if ("REMBOURSÉ".equals(sel.getStatut())) {
            avertissement("⚠ Ce paiement est déjà marqué REMBOURSÉ.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de remboursement");
        confirm.setHeaderText("Rembourser ce paiement ?");
        confirm.setContentText(
                "Porteur : " + sel.getNomCarte() + "\n" +
                        "Montant : " + String.format("%.2f", sel.getMontant()) + " DT\n\n" +
                        "Un email de remboursement sera envoyé automatiquement."
        );

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sp.rembourser(sel);
                succes("✔ Remboursement effectué ! Email envoyé à " + sel.getNomCarte() + ".");
                chargerTableau();
            }
        });
    }

    // ── Validations temps réel ─────────────────────────────────────────
    private void attacherValidations() {

        tfNomCarte.textProperty().addListener((obs, o, n) -> {
            tfNomCarte.setStyle(n.trim().length() >= 3 ? OK : (n.trim().isEmpty() ? NONE : ERR));
            validerFormulaire();
        });

        tfNumeroCarte.textProperty().addListener((obs, o, n) -> {
            String chiffres = n.replaceAll("[^\\d]", "");
            if (chiffres.length() > 16) chiffres = chiffres.substring(0, 16);
            String formate = chiffres.replaceAll("(\\d{4})(?=\\d)", "$1 ");
            if (!n.equals(formate)) {
                tfNumeroCarte.setText(formate);
                tfNumeroCarte.positionCaret(formate.length());
                return;
            }
            boolean ok = chiffres.length() == 16 && luhn(chiffres);
            tfNumeroCarte.setStyle(chiffres.isEmpty() ? NONE : (ok ? OK : ERR));
            validerFormulaire();
        });

        tfExpiration.textProperty().addListener((obs, o, n) -> {
            String chiffres = n.replaceAll("[^\\d]", "");
            if (chiffres.length() > 6) chiffres = chiffres.substring(0, 6);
            String formate = chiffres.length() > 2
                    ? chiffres.substring(0, 2) + "/" + chiffres.substring(2)
                    : chiffres;
            if (!n.equals(formate)) {
                tfExpiration.setText(formate);
                tfExpiration.positionCaret(formate.length());
                return;
            }
            tfExpiration.setStyle(formate.isEmpty() ? NONE : (expirationValide(formate) ? OK : ERR));
            validerFormulaire();
        });

        tfCVV.textProperty().addListener((obs, o, n) -> {
            String chiffres = n.replaceAll("[^\\d]", "");
            if (chiffres.length() > 3) chiffres = chiffres.substring(0, 3);
            if (!n.equals(chiffres)) { tfCVV.setText(chiffres); return; }
            tfCVV.setStyle(chiffres.isEmpty() ? NONE : (chiffres.length() == 3 ? OK : ERR));
            validerFormulaire();
        });

        tfMontant.textProperty().addListener((obs, o, n) -> {
            try {
                double v = Double.parseDouble(n.trim());
                tfMontant.setStyle(v > 0 ? OK : ERR);
            } catch (NumberFormatException e) {
                tfMontant.setStyle(n.trim().isEmpty() ? NONE : ERR);
            }
            validerFormulaire();
        });

        // ── Vérification doublon à la sélection de réservation ─────────
        cbReservation.valueProperty().addListener((obs, o, n) -> {
            verifierDoublon(n);
            validerFormulaire();
        });
    }

    // ── Vérifier si la réservation est déjà payée (statut CONFIRMÉ) ───
    private boolean reservationDejaPayee = false;

    private void verifierDoublon(String selectionCombo) {
        lbDejaPayee.setText("");
        reservationDejaPayee = false;

        if (selectionCombo == null) return;
        try {
            int resaId = Integer.parseInt(selectionCombo.split(" — ")[0].trim());
            boolean existe = tousLesPaiements.stream()
                    .anyMatch(p -> p.getReservationId() == resaId
                            && "CONFIRMÉ".equalsIgnoreCase(p.getStatut()));
            if (existe) {
                lbDejaPayee.setText("⛔ Cette réservation a déjà un paiement CONFIRMÉ !");
                reservationDejaPayee = true;
            }
        } catch (NumberFormatException ignored) {}
    }

    private void validerFormulaire() {
        boolean nomOk   = tfNomCarte.getText().trim().length() >= 3;
        boolean carteOk = luhn(tfNumeroCarte.getText().replaceAll("\\s", ""))
                && tfNumeroCarte.getText().replaceAll("\\s", "").length() == 16;
        boolean expiOk  = expirationValide(tfExpiration.getText().trim());
        boolean cvvOk   = tfCVV.getText().trim().length() == 3;
        boolean montantOk;
        try { montantOk = Double.parseDouble(tfMontant.getText().trim()) > 0; }
        catch (NumberFormatException e) { montantOk = false; }
        boolean resaOk  = cbReservation.getValue() != null;

        // Bouton désactivé si doublon OU champs invalides
        btnPayer.setDisable(!(nomOk && carteOk && expiOk && cvvOk && montantOk && resaOk)
                || reservationDejaPayee);
    }

    // ── Luhn ───────────────────────────────────────────────────────────
    private boolean luhn(String numero) {
        if (numero == null || numero.length() != 16) return false;
        int somme = 0;
        boolean dbl = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int d = numero.charAt(i) - '0';
            if (d < 0 || d > 9) return false;
            if (dbl) { d *= 2; if (d > 9) d -= 9; }
            somme += d;
            dbl = !dbl;
        }
        return somme % 10 == 0;
    }

    // ── Expiration MM/YYYY ─────────────────────────────────────────────
    private boolean expirationValide(String exp) {
        if (exp == null || !exp.matches("\\d{2}/\\d{4}")) return false;
        int mois = Integer.parseInt(exp.substring(0, 2));
        int an   = Integer.parseInt(exp.substring(3));
        if (mois < 1 || mois > 12) return false;
        LocalDateTime now = LocalDateTime.now();
        return an > now.getYear() || (an == now.getYear() && mois >= now.getMonthValue());
    }

    // ── Payer ──────────────────────────────────────────────────────────
    @FXML
    public void payer(ActionEvent event) {
        try {
            String sel = cbReservation.getValue();
            int reservationId = Integer.parseInt(sel.split(" — ")[0].trim());

            // Double vérification sécurité côté action
            boolean dejaConfirme = tousLesPaiements.stream()
                    .anyMatch(p -> p.getReservationId() == reservationId
                            && "CONFIRMÉ".equalsIgnoreCase(p.getStatut()));
            if (dejaConfirme) {
                erreur("⛔ Paiement refusé : cette réservation est déjà payée !");
                return;
            }

            Paiement p = new Paiement();
            p.setReservationId(reservationId);
            p.setNomCarte(tfNomCarte.getText().trim());
            p.setNumeroCarte(tfNumeroCarte.getText().replaceAll("\\s", ""));
            p.setDateExpiration(tfExpiration.getText().trim());
            p.setMontant(Double.parseDouble(tfMontant.getText().trim()));
            p.setStatut("CONFIRMÉ");
            p.setDatePaiement(LocalDateTime.now());

            sp.add(p);
            succes("✔ Paiement confirmé ! Email de confirmation envoyé.");
            viderFormulaire();
            chargerTableau();

        } catch (Exception e) {
            erreur("⚠ Erreur : " + e.getMessage());
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────
    @FXML public void allerReservation(ActionEvent event) { naviguer("/GestionReservation.fxml"); }
    @FXML public void allerDashboard(ActionEvent event)   { naviguer("/Dashboard.fxml"); }

    private void naviguer(String fxml) {
        try { lbMessage.getScene().setRoot(FXMLLoader.load(getClass().getResource(fxml))); }
        catch (IOException e) { System.out.println("Erreur navigation : " + e.getMessage()); }
    }

    // ── Helpers ────────────────────────────────────────────────────────
    private void erreur(String m)        { lbMessage.setStyle("-fx-text-fill:#d93025;-fx-font-style:italic;"); lbMessage.setText(m); }
    private void succes(String m)        { lbMessage.setStyle("-fx-text-fill:#0f9d58;-fx-font-style:italic;"); lbMessage.setText(m); }
    private void info(String m)          { lbMessage.setStyle("-fx-text-fill:#1a73e8;-fx-font-style:italic;"); lbMessage.setText(m); }
    private void avertissement(String m) { lbMessage.setStyle("-fx-text-fill:#f9ab00;-fx-font-style:italic;"); lbMessage.setText(m); }

    private void viderFormulaire() {
        cbReservation.setValue(null);
        lbDejaPayee.setText("");
        reservationDejaPayee = false;
        tfNomCarte.clear();    tfNomCarte.setStyle(NONE);
        tfNumeroCarte.clear(); tfNumeroCarte.setStyle(NONE);
        tfExpiration.clear();  tfExpiration.setStyle(NONE);
        tfCVV.clear();         tfCVV.setStyle(NONE);
        tfMontant.clear();     tfMontant.setStyle(NONE);
        btnPayer.setDisable(true);
    }
}