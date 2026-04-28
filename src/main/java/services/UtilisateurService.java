package services;

import interfaces.IUtilisateurService;
import models.*;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}