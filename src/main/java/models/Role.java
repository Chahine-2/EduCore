package models;

public class Role {
    private int id;
    private String nomRole;

    public Role(int id, String nomRole) {
        this.id = id;
        this.nomRole = nomRole;
    }

    // Getters
    public int getId() { return id; }
    public String getNomRole() { return nomRole; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
}