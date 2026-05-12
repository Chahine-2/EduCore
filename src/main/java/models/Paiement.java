package models;

import java.time.LocalDateTime;

public class Paiement {

    private int           id;
    private int           reservationId;
    private double        montant;
    private String        nomCarte;
    private String        numeroCarte;      // masqué côté affichage
    private String        dateExpiration;   // MM/YYYY
    private String        statut;
    private LocalDateTime datePaiement;

    public Paiement() {}

    public Paiement(int reservationId, double montant,
                    String nomCarte, String numeroCarte,
                    String dateExpiration) {
        this.reservationId  = reservationId;
        this.montant        = montant;
        this.nomCarte       = nomCarte;
        this.numeroCarte    = numeroCarte;
        this.dateExpiration = dateExpiration;
        this.statut         = "CONFIRMÉ";
    }

    // ── Getters / Setters ──────────────────────────────────────────────
    public int           getId()                        { return id; }
    public void          setId(int id)                  { this.id = id; }

    public int           getReservationId()             { return reservationId; }
    public void          setReservationId(int rid)      { this.reservationId = rid; }

    public double        getMontant()                   { return montant; }
    public void          setMontant(double m)           { this.montant = m; }

    public String        getNomCarte()                  { return nomCarte; }
    public void          setNomCarte(String n)          { this.nomCarte = n; }

    public String        getNumeroCarte()               { return numeroCarte; }
    public void          setNumeroCarte(String n)       { this.numeroCarte = n; }

    public String        getDateExpiration()            { return dateExpiration; }
    public void          setDateExpiration(String d)    { this.dateExpiration = d; }

    public String        getStatut()                    { return statut; }
    public void          setStatut(String s)            { this.statut = s; }

    public LocalDateTime getDatePaiement()              { return datePaiement; }
    public void          setDatePaiement(LocalDateTime d){ this.datePaiement = d; }

    @Override
    public String toString() {
        return "Paiement{id=" + id + ", reservationId=" + reservationId +
                ", montant=" + montant + " DT, statut=" + statut + "}";
    }
}