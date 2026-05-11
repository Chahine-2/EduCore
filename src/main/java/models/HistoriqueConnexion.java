package models;

public class HistoriqueConnexion {
    private String date;
    private String email;
    private String statut;

    public HistoriqueConnexion(String date, String email, String statut) {
        this.date = date;
        this.email = email;
        this.statut = statut;
    }

    public String getDate() { return date; }
    public String getEmail() { return email; }
    public String getStatut() { return statut; }
}