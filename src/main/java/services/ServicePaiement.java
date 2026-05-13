package services;

import interfaces.IServiceHackatons;
import models.Paiement;
import models.Reservation;
import utils.MyDataBase;
import utils.EmailSender;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicePaiement implements IServiceHackatons<Paiement> {

    private final ServiceReservation sr = new ServiceReservation();

    // ── Masquer le numéro de carte ─────────────────────────────────────
    public static String masquerCarte(String numero) {
        String chiffres = numero.replaceAll("\\s", "");
        if (chiffres.length() < 4) return numero;
        return "**** **** **** " + chiffres.substring(chiffres.length() - 4);
    }

    // ── Ajouter un paiement ────────────────────────────────────────────
    @Override
    public void add(Paiement p) {
        String req = "INSERT INTO paiement " +
                "(reservation_id, montant, nom_carte, numero_carte, date_expiration, statut, date_paiement) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1,    p.getReservationId());
            ps.setDouble(2, p.getMontant());
            ps.setString(3, p.getNomCarte());
            ps.setString(4, masquerCarte(p.getNumeroCarte()));
            ps.setString(5, p.getDateExpiration());
            ps.setString(6, p.getStatut());
            ps.setTimestamp(7, Timestamp.valueOf(
                    p.getDatePaiement() != null ? p.getDatePaiement() : LocalDateTime.now()));
            ps.executeUpdate();
            System.out.println("Paiement enregistré !");
            envoyerEmailConfirmation(p);
        } catch (SQLException e) {
            System.out.println("Erreur SQL paiement : " + e.getMessage());
        }
    }

    // ── Lister tous les paiements ──────────────────────────────────────
    @Override
    public List<Paiement> getAll() {
        List<Paiement> list = new ArrayList<>();
        String req = "SELECT * FROM paiement ORDER BY date_paiement DESC";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs  = stm.executeQuery(req);
            while (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setReservationId(rs.getInt("reservation_id"));
                p.setMontant(rs.getDouble("montant"));
                p.setNomCarte(rs.getString("nom_carte"));
                p.setNumeroCarte(rs.getString("numero_carte"));
                p.setDateExpiration(rs.getString("date_expiration"));
                p.setStatut(rs.getString("statut"));
                Timestamp ts = rs.getTimestamp("date_paiement");
                if (ts != null) p.setDatePaiement(ts.toLocalDateTime());
                list.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll paiement : " + e.getMessage());
        }
        return list;
    }

    // ── Supprimer ──────────────────────────────────────────────────────
    @Override
    public void delete(Paiement p) {
        String req = "DELETE FROM paiement WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setInt(1, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur delete paiement : " + e.getMessage());
        }
    }

    // ── Modifier (immutabilité normale, sauf remboursement) ───────────
    @Override
    public void update(Paiement p) {
        String req = "UPDATE paiement SET statut = ? WHERE id = ?";
        try {
            PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(req);
            ps.setString(1, p.getStatut());
            ps.setInt(2, p.getId());
            ps.executeUpdate();
            System.out.println("Statut paiement mis à jour → " + p.getStatut());
        } catch (SQLException e) {
            System.out.println("Erreur update paiement : " + e.getMessage());
        }
    }

    // ── Rembourser : change statut → REMBOURSÉ + email ─────────────────
    public void rembourser(Paiement p) {
        p.setStatut("REMBOURSÉ");
        update(p);
        envoyerEmailRemboursement(p);
    }

    // ── Somme totale des paiements CONFIRMÉS ───────────────────────────
    public double getTotalPaiements() {
        double total = 0;
        String req = "SELECT SUM(montant) AS total FROM paiement WHERE statut = 'CONFIRMÉ'";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs  = stm.executeQuery(req);
            if (rs.next()) total = rs.getDouble("total");
        } catch (SQLException e) {
            System.out.println("Erreur getTotalPaiements : " + e.getMessage());
        }
        return total;
    }

    // ── Paiements par mois (pour graphique dashboard) ──────────────────
    public java.util.Map<String, Double> getPaiementsParMois() {
        java.util.LinkedHashMap<String, Double> map = new java.util.LinkedHashMap<>();
        String req =
                "SELECT DATE_FORMAT(date_paiement, '%Y-%m') AS mois, " +
                        "SUM(montant) AS total " +
                        "FROM paiement " +
                        "WHERE statut = 'CONFIRMÉ' " +
                        "GROUP BY mois " +
                        "ORDER BY mois ASC " +
                        "LIMIT 12";
        try {
            Statement stm = MyDataBase.getInstance().getCnx().createStatement();
            ResultSet rs  = stm.executeQuery(req);
            while (rs.next()) {
                map.put(rs.getString("mois"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getPaiementsParMois : " + e.getMessage());
        }
        return map;
    }

    // ── Email confirmation ─────────────────────────────────────────────
    private void envoyerEmailConfirmation(Paiement p) {
        Reservation r = sr.getAll().stream()
                .filter(res -> res.getId() == p.getReservationId())
                .findFirst().orElse(null);

        String titreResa = (r != null) ? r.getTitre() : "Réservation #" + p.getReservationId();
        String sujet = "✅ Confirmation de paiement — " + titreResa;
        String corps =
                "Bonjour " + p.getNomCarte() + ",\n\n" +
                        "Votre paiement a été confirmé avec succès !\n\n" +
                        "═══════════════════════════════════\n" +
                        "   DÉTAILS DU PAIEMENT\n" +
                        "═══════════════════════════════════\n" +
                        "  Réservation  : " + titreResa + "\n" +
                        "  Montant payé : " + String.format("%.2f", p.getMontant()) + " DT\n" +
                        "  Carte        : " + masquerCarte(p.getNumeroCarte()) + "\n" +
                        "  Date         : " + (p.getDatePaiement() != null
                        ? p.getDatePaiement().toString().replace("T", " à ")
                        : LocalDateTime.now().toString().replace("T", " à ")) + "\n" +
                        "  Statut       : " + p.getStatut() + "\n" +
                        "═══════════════════════════════════\n\n" +
                        "Merci pour votre confiance.\n" +
                        "— L'équipe Site Éducatif";

        EmailSender.envoyer(sujet, corps);
    }

    // ── Email remboursement ────────────────────────────────────────────
    private void envoyerEmailRemboursement(Paiement p) {
        Reservation r = sr.getAll().stream()
                .filter(res -> res.getId() == p.getReservationId())
                .findFirst().orElse(null);

        String titreResa = (r != null) ? r.getTitre() : "Réservation #" + p.getReservationId();
        String sujet = "↩ Remboursement effectué — " + titreResa;
        String corps =
                "Bonjour " + p.getNomCarte() + ",\n\n" +
                        "Votre remboursement a été traité avec succès.\n\n" +
                        "═══════════════════════════════════\n" +
                        "   DÉTAILS DU REMBOURSEMENT\n" +
                        "═══════════════════════════════════\n" +
                        "  Réservation    : " + titreResa + "\n" +
                        "  Montant rendu  : " + String.format("%.2f", p.getMontant()) + " DT\n" +
                        "  Carte          : " + masquerCarte(p.getNumeroCarte()) + "\n" +
                        "  Date traitement: " + LocalDateTime.now().toString().replace("T", " à ") + "\n" +
                        "  Statut         : REMBOURSÉ\n" +
                        "═══════════════════════════════════\n\n" +
                        "Le montant sera crédité sous 3 à 5 jours ouvrables.\n" +
                        "— L'équipe Site Éducatif";

        EmailSender.envoyer(sujet, corps);
    }
}