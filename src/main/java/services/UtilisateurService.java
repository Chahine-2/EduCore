package services;

import interfaces.IUtilisateurService;
import models.*;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements IUtilisateurService {

    private Connection connection;

    public UtilisateurService() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Utilisateur authentifier(String email, String motDePasse) {
        String query = "SELECT u.*, r.nom_role FROM utilisateurs u " +
                "JOIN roles r ON u.role_id = r.id " +
                "WHERE u.email = ? AND u.mot_de_passe = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, motDePasse);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                int age = resultSet.getInt("age");
                int tel = resultSet.getInt("tel");
                int roleId = resultSet.getInt("role_id");
                String nomRole = resultSet.getString("nom_role");

                Role role = new Role(roleId, nomRole);

                switch (nomRole) {
                    case "Etudiant":
                        return new Etudiant(id, nom, prenom, age, email, tel, motDePasse, role, "N/A", "N/A");
                    case "Enseignant":
                        return new Enseignant(id, nom, prenom, age, email, tel, motDePasse, role, "N/A", "N/A");
                    case "Administrateur":
                        return new Administrateur(id, nom, prenom, age, email, tel, motDePasse, role, "N/A");
                    default:
                        System.out.println("Rôle non reconnu.");
                        return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public boolean ajouterEtudiant(Etudiant etudiant) {
        String queryUtilisateur = "INSERT INTO utilisateurs (nom, prenom, age, email, tel, mot_de_passe, role_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String queryEtudiant = "INSERT INTO etudiants (utilisateur_id, numero_etudiant, classe) VALUES (?, ?, ?)";

        try {
            // Disable auto-commit to run a transaction (both inserts must succeed, or neither do)
            connection.setAutoCommit(false);

            // 1. Insert into base table and get the new ID
            try (PreparedStatement psUser = connection.prepareStatement(queryUtilisateur, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, etudiant.getNom());
                psUser.setString(2, etudiant.getPrenom());
                psUser.setInt(3, etudiant.getAge());
                psUser.setString(4, etudiant.getEmail());
                psUser.setInt(5, etudiant.getTel());
                psUser.setString(6, etudiant.getMotDePasse());
                psUser.setInt(7, 3); // Assuming '3' is the ID for the 'Etudiant' role in your DB

                psUser.executeUpdate();
                ResultSet rs = psUser.getGeneratedKeys();

                if (rs.next()) {
                    int newUserId = rs.getInt(1);

                    // 2. Insert into the specific Etudiant table
                    try (PreparedStatement psEtud = connection.prepareStatement(queryEtudiant)) {
                        psEtud.setInt(1, newUserId);
                        psEtud.setString(2, etudiant.getNumeroEtudiant());
                        psEtud.setString(3, etudiant.getClasse());
                        psEtud.executeUpdate();
                    }
                }
            }
            connection.commit(); // Save the transaction
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public List<Utilisateur> listerUtilisateurs() {
        List<Utilisateur> liste = new ArrayList<>();
        String query = "SELECT u.id, u.nom, u.prenom, u.email, r.nom_role FROM utilisateurs u JOIN roles r ON u.role_id = r.id";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // For the CLI list, we are just pulling the basic info to display
                Role role = new Role(0, rs.getString("nom_role"));
                // Using an anonymous subclass just to hold data for the list view
                Utilisateur u = new Utilisateur(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), 0, rs.getString("email"), 0, "", role) {};
                liste.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    @Override
    public boolean modifierEmailUtilisateur(int id, String nouvelEmail) {
        String query = "UPDATE utilisateurs SET email = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, nouvelEmail);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimerUtilisateur(int id) {
        // Because we set ON DELETE CASCADE in the database schema, deleting from 'utilisateurs'
        // will automatically delete the linked record in 'etudiants', 'enseignants', etc.
        String query = "DELETE FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
