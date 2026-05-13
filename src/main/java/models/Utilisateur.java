package models;

public abstract class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private int age;
    private String email;
    private int tel;
    private String motDePasse;
    private Role role; // The relationship to the Role class
    private boolean statutActif = true;

    public Utilisateur(int id, String nom, String prenom, int age, String email, int tel, String motDePasse, Role role, boolean statutActif) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.email = email;
        this.tel = tel;
        this.motDePasse = motDePasse;
        this.role = role;
        this.statutActif = statutActif;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public int getTel() { return tel; }
    public String getMotDePasse() { return motDePasse; }
    public Role getRole() { return role; }
    public boolean isStatutActif() { return statutActif; }
    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setAge(int age) { this.age = age; }
    public void setEmail(String email) { this.email = email; }
    public void setTel(int tel) { this.tel = tel; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setRole(Role role) { this.role = role; }
    public void setStatutActif(boolean statutActif) { this.statutActif = statutActif; }
}