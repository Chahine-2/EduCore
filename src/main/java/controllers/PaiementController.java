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

    @FXML private ComboBox<String> cbReservation;
    @FXML private TextField        tfNomCarte;
    @FXML private TextField        tfNumeroCarte;
    @FXML private TextField        tfExpiration;
    @FXML private TextField        tfCVV;
    @FXML private TextField        tfMontant;
    @FXML private Button           btnPayer;
    @FXML private Label            lbMessage;
    @FXML private Label            lbDejaPayee;

    @FXML private TableView<Paiement>            tablePaiement;
    @FXML private TableColumn<Paiement, Integer> colId;
    @FXML private TableColumn<Paiement, Integer> colReservation;
    @FXML private TableColumn<Paiement, Double>  colMontant;
    @FXML private TableColumn<Paiement, String>  colNom;
    @FXML private TableColumn<Paiement, String>  colCarte;
    @FXML private TableColumn<Paiement, String>  colStatut;
    @FXML private TableColumn<Paiement, String>  colDate;
    @FXML private TextField                      tfRecherche;

    private final ServicePaiement    sp = new ServicePaiement();
    private final ServiceReservation sr = new ServiceReservation();
    private List<Reservation>        reservations;
    private ObservableList<Paiement> tousLesPaiements = FXCollections.observableArrayList();
    private boolean reservationDejaPayee = false;
    private Runnable onRetour;

    /** Called by the dashboard to wire the Retour button back to the reservation pane. */
    public void setOnRetour(Runnable callback) {
        this.onRetour = callback;
    }

    private static final String OK   = "-fx-border-color:#0f9d58;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String ERR  = "-fx-border-color:#d93025;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String WARN = "-fx-border-color:#f9ab00;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String NONE = "-fx-border-color:#dde1e7;-fx-border-radius:6;-fx-background-radius:6;";

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
        for (Reservation r : reservations)
            cbReservation.getItems().add(r.getId() + " — " + r.getTitre());
    }

    private void chargerTableau() {
        tousLesPaiements = FXCollections.observableArrayList(sp.getAll());
        tablePaiement.setItems(tousLesPaiements);
        if (tfRecherche != null) tfRecherche.clear();
    }

    @FXML
    public void rechercherPaiement(javafx.scene.input.KeyEvent event) {
        String motCle = tfRecherche.getText().toLowerCase().trim();
        if (motCle.isEmpty()) {
            tablePaiement.setItems(tousLesPaiements);
        } else {
            ObservableList<Paiement> filtres = FXCollections.observableArrayList();
            for (Paiement p : tousLesPaiements) {
                boolean matchNom    = p.getNomCarte()    != null && p.getNomCarte().toLowerCase().contains(motCle);
                boolean matchCarte  = p.getNumeroCarte() != null && p.getNumeroCarte().toLowerCase().contains(motCle);
                boolean matchStatut = p.getStatut()      != null && p.getStatut().toLowerCase().contains(motCle);
                if (matchNom || matchCarte || matchStatut) filtres.add(p);
            }
            tablePaiement.setItems(filtres);
        }
    }

    @FXML
    public void selectionnerPaiement(MouseEvent event) {
        Paiement sel = tablePaiement.getSelectionModel().getSelectedItem();
        if (sel != null) info("ℹ Paiement sélectionné — cliquez « Rembourser » pour l'annuler.");
    }

    @FXML
    public void rembourserPaiement(ActionEvent event) {
        Paiement sel = tablePaiement.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un paiement à rembourser !"); return; }
        if ("REMBOURSÉ".equals(sel.getStatut())) { avertissement("⚠ Ce paiement est déjà REMBOURSÉ."); return; }
        if ("ANNULÉ".equals(sel.getStatut()))    { avertissement("⚠ Ce paiement est déjà ANNULÉ."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de remboursement");
        confirm.setHeaderText("Rembourser ce paiement ?");
        confirm.setContentText("Porteur : " + sel.getNomCarte()
                + "\nMontant : " + String.format("%.2f", sel.getMontant()) + " DT"
                + "\n\nUn email de remboursement sera envoyé automatiquement.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sp.rembourser(sel);
                succes("✔ Remboursement effectué ! Email envoyé à " + sel.getNomCarte() + ".");
                chargerTableau();
            }
        });
    }

    private void attacherValidations() {

        // ── Nom porteur : min 3, max 80, lettres uniquement (+ espaces) ─
        tfNomCarte.textProperty().addListener((obs, o, n) -> {
            String v = n.trim();
            if (v.isEmpty()) {
                tfNomCarte.setStyle(NONE);
            } else if (v.length() < 3) {
                tfNomCarte.setStyle(ERR); infoChamp("Nom porteur : minimum 3 caractères.");
            } else if (v.length() > 80) {
                tfNomCarte.setText(n.substring(0, 80));
                tfNomCarte.setStyle(WARN); infoChamp("Nom porteur : maximum 80 caractères.");
            } else if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
                tfNomCarte.setStyle(ERR); infoChamp("Nom porteur : lettres et espaces uniquement.");
            } else {
                tfNomCarte.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Numéro carte : 16 chiffres, formaté, validé par Luhn ───────
        tfNumeroCarte.textProperty().addListener((obs, o, n) -> {
            String chiffres = n.replaceAll("[^\\d]", "");
            if (chiffres.length() > 16) chiffres = chiffres.substring(0, 16);
            String formate = chiffres.replaceAll("(\\d{4})(?=\\d)", "$1 ");
            if (!n.equals(formate)) {
                tfNumeroCarte.setText(formate);
                tfNumeroCarte.positionCaret(formate.length()); return;
            }
            if (chiffres.isEmpty()) {
                tfNumeroCarte.setStyle(NONE);
            } else if (chiffres.length() < 16) {
                tfNumeroCarte.setStyle(ERR); infoChamp("N° carte : " + chiffres.length() + "/16 chiffres saisis.");
            } else if (!luhn(chiffres)) {
                tfNumeroCarte.setStyle(ERR); infoChamp("N° carte : numéro invalide (échec validation Luhn).");
            } else {
                tfNumeroCarte.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Expiration : MM/YYYY, pas expirée ───────────────────────────
        tfExpiration.textProperty().addListener((obs, o, n) -> {
            String chiffres = n.replaceAll("[^\\d]", "");
            if (chiffres.length() > 6) chiffres = chiffres.substring(0, 6);
            String formate = chiffres.length() > 2
                    ? chiffres.substring(0, 2) + "/" + chiffres.substring(2) : chiffres;
            if (!n.equals(formate)) {
                tfExpiration.setText(formate);
                tfExpiration.positionCaret(formate.length()); return;
            }
            if (formate.isEmpty()) {
                tfExpiration.setStyle(NONE);
            } else if (!formate.matches("\\d{2}/\\d{4}")) {
                tfExpiration.setStyle(ERR); infoChamp("Expiration : format MM/YYYY requis.");
            } else {
                int mois = Integer.parseInt(formate.substring(0, 2));
                int an   = Integer.parseInt(formate.substring(3));
                if (mois < 1 || mois > 12) {
                    tfExpiration.setStyle(ERR); infoChamp("Expiration : mois invalide (01–12).");
                } else if (!expirationValide(formate)) {
                    tfExpiration.setStyle(ERR); infoChamp("Expiration : carte expirée !");
                } else {
                    tfExpiration.setStyle(OK); lbMessage.setText("");
                }
            }
            validerFormulaire();
        });

        // ── CVV : exactement 3 chiffres ─────────────────────────────────
        tfCVV.textProperty().addListener((obs, o, n) -> {
            String chiffres = n.replaceAll("[^\\d]", "");
            if (chiffres.length() > 3) chiffres = chiffres.substring(0, 3);
            if (!n.equals(chiffres)) { tfCVV.setText(chiffres); return; }
            if (chiffres.isEmpty()) {
                tfCVV.setStyle(NONE);
            } else if (chiffres.length() < 3) {
                tfCVV.setStyle(ERR); infoChamp("CVV : " + chiffres.length() + "/3 chiffres saisis.");
            } else {
                tfCVV.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Montant : décimal > 0, max 99 999.99 DT ─────────────────────
        tfMontant.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[\\d.]*")) { tfMontant.setText(n.replaceAll("[^\\d.]", "")); return; }
            if (n.chars().filter(c -> c == '.').count() > 1) { tfMontant.setText(o); return; }
            if (n.isEmpty()) {
                tfMontant.setStyle(NONE);
            } else {
                try {
                    double v = Double.parseDouble(n);
                    if (v <= 0) {
                        tfMontant.setStyle(ERR); infoChamp("Montant : doit être supérieur à 0 DT.");
                    } else if (v > 99999.99) {
                        tfMontant.setStyle(ERR); infoChamp("Montant : maximum 99 999,99 DT.");
                    } else if (v < 1) {
                        tfMontant.setStyle(WARN); infoChamp("Montant : inférieur à 1 DT — êtes-vous sûr ?");
                    } else {
                        tfMontant.setStyle(OK); lbMessage.setText("");
                    }
                } catch (NumberFormatException e) { tfMontant.setStyle(ERR); }
            }
            validerFormulaire();
        });

        // ── Réservation : vérifier doublon à la sélection ───────────────
        cbReservation.valueProperty().addListener((obs, o, n) -> {
            verifierDoublon(n); validerFormulaire();
        });
    }

    private void verifierDoublon(String selectionCombo) {
        lbDejaPayee.setText(""); reservationDejaPayee = false;
        if (selectionCombo == null) return;
        try {
            int resaId = Integer.parseInt(selectionCombo.split(" — ")[0].trim());
            boolean existe = tousLesPaiements.stream()
                    .anyMatch(p -> p.getReservationId() == resaId && "CONFIRMÉ".equalsIgnoreCase(p.getStatut()));
            if (existe) {
                lbDejaPayee.setText("⛔ Cette réservation a déjà un paiement CONFIRMÉ !");
                reservationDejaPayee = true;
            }
        } catch (NumberFormatException ignored) {}
    }

    private void validerFormulaire() {
        String nom = tfNomCarte.getText().trim();
        boolean nomOk = nom.length() >= 3 && nom.length() <= 80 && nom.matches("[a-zA-ZÀ-ÿ\\s\\-']+");
        String chiffres = tfNumeroCarte.getText().replaceAll("\\s", "");
        boolean carteOk = chiffres.length() == 16 && luhn(chiffres);
        boolean expiOk  = expirationValide(tfExpiration.getText().trim());
        boolean cvvOk   = tfCVV.getText().trim().length() == 3;
        boolean montantOk = false;
        try { double v = Double.parseDouble(tfMontant.getText().trim()); montantOk = v > 0 && v <= 99999.99; }
        catch (NumberFormatException ignored) {}
        boolean resaOk = cbReservation.getValue() != null;
        btnPayer.setDisable(!(nomOk && carteOk && expiOk && cvvOk && montantOk && resaOk) || reservationDejaPayee);
    }

    // ── Luhn ──────────────────────────────────────────────────────────
    private boolean luhn(String numero) {
        if (numero == null || numero.length() != 16) return false;
        int somme = 0; boolean dbl = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int d = numero.charAt(i) - '0';
            if (d < 0 || d > 9) return false;
            if (dbl) { d *= 2; if (d > 9) d -= 9; }
            somme += d; dbl = !dbl;
        }
        return somme % 10 == 0;
    }

    private boolean expirationValide(String exp) {
        if (exp == null || !exp.matches("\\d{2}/\\d{4}")) return false;
        int mois = Integer.parseInt(exp.substring(0, 2));
        int an   = Integer.parseInt(exp.substring(3));
        if (mois < 1 || mois > 12) return false;
        LocalDateTime now = LocalDateTime.now();
        return an > now.getYear() || (an == now.getYear() && mois >= now.getMonthValue());
    }

    @FXML
    public void payer(ActionEvent event) {
        try {
            String sel = cbReservation.getValue();
            int reservationId = Integer.parseInt(sel.split(" — ")[0].trim());
            // Double vérification sécurité
            boolean dejaConfirme = tousLesPaiements.stream()
                    .anyMatch(p -> p.getReservationId() == reservationId && "CONFIRMÉ".equalsIgnoreCase(p.getStatut()));
            if (dejaConfirme) { erreur("⛔ Paiement refusé : cette réservation est déjà payée !"); return; }

            // Vérifier montant cohérent avec la réservation
            double montant = Double.parseDouble(tfMontant.getText().trim());
            if (montant <= 0) { erreur("⚠ Montant invalide !"); return; }

            Paiement p = new Paiement();
            p.setReservationId(reservationId);
            p.setNomCarte(tfNomCarte.getText().trim());
            p.setNumeroCarte(tfNumeroCarte.getText().replaceAll("\\s", ""));
            p.setDateExpiration(tfExpiration.getText().trim());
            p.setMontant(montant);
            p.setStatut("CONFIRMÉ");
            p.setDatePaiement(LocalDateTime.now());
            sp.add(p);
            succes("✔ Paiement de " + String.format("%.2f", montant) + " DT confirmé ! Email envoyé.");
            viderFormulaire(); chargerTableau();
        } catch (Exception e) { erreur("⚠ Erreur : " + e.getMessage()); }
    }

    @FXML public void allerReservation(ActionEvent e) {
        if (onRetour != null) {
            onRetour.run();
        } else {
            // fallback: standalone mode
            naviguer("/GestionReservation.fxml");
        }
    }

    // allerDashboard kept for standalone/legacy use only — not shown in embedded mode
    @FXML public void allerDashboard(ActionEvent e) { naviguer("/Dashboard.fxml"); }

    private void naviguer(String fxml) {
        try { lbMessage.getScene().setRoot(FXMLLoader.load(getClass().getResource(fxml))); }
        catch (IOException e) { System.out.println("Erreur navigation : " + e.getMessage()); }
    }

    private void erreur(String m)        { lbMessage.setStyle("-fx-text-fill:#d93025;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void succes(String m)        { lbMessage.setStyle("-fx-text-fill:#0f9d58;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void info(String m)          { lbMessage.setStyle("-fx-text-fill:#1a73e8;-fx-font-style:italic;"); lbMessage.setText(m); }
    private void avertissement(String m) { lbMessage.setStyle("-fx-text-fill:#f9ab00;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void infoChamp(String m)     { lbMessage.setStyle("-fx-text-fill:#5f6368;-fx-font-style:italic;"); lbMessage.setText(m); }

    private void viderFormulaire() {
        cbReservation.setValue(null); lbDejaPayee.setText(""); reservationDejaPayee = false;
        tfNomCarte.clear();    tfNomCarte.setStyle(NONE);
        tfNumeroCarte.clear(); tfNumeroCarte.setStyle(NONE);
        tfExpiration.clear();  tfExpiration.setStyle(NONE);
        tfCVV.clear();         tfCVV.setStyle(NONE);
        tfMontant.clear();     tfMontant.setStyle(NONE);
        btnPayer.setDisable(true); lbMessage.setText("");
    }
}