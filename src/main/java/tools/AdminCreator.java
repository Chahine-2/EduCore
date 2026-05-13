package tools;

import org.mindrot.jbcrypt.BCrypt;
import utils.DatabaseConnection;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Small command-line utility to create an admin user in the `utilisateurs` table.
 * Run from IDE (right-click -> Run) or via Maven/exec plugin.
 *
 * It asks for: first name, last name, email and password. Password input uses Console if available.
 */
public class AdminCreator {

    public static void main(String[] args) {
        Console console = System.console();
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            System.out.print("Prénom: ");
            String prenom = scanner.nextLine().trim();

            System.out.print("Nom: ");
            String nom = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            String password;
            if (console != null) {
                char[] pw = console.readPassword("Mot de passe: ");
                password = new String(pw);
            } else {
                System.out.print("Mot de passe (console non disponible, tapez en clair): ");
                password = scanner.nextLine();
            }

            if (email.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
                System.err.println("Tous les champs sont requis. Annulation.");
                return;
            }

            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Impossible d'obtenir la connexion à la base de données. Vérifiez DatabaseConnection.");
                return;
            }

            // Check if email already exists
            try {
                try (PreparedStatement check = conn.prepareStatement("SELECT id FROM utilisateurs WHERE email = ?")) {
                    check.setString(1, email);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            System.err.println("Un utilisateur existe déjà avec cet email : " + email);
                            return;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la vérification d'email : " + e.getMessage());
                e.printStackTrace();
                return;
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

            String insert = "INSERT INTO utilisateurs (nom, prenom, age, email, tel, mot_de_passe, role_id, statut_actif) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nom);
                ps.setString(2, prenom);
                ps.setInt(3, 30); // default age
                ps.setString(4, email);
                ps.setInt(5, 0); // default tel
                ps.setString(6, hashed);
                ps.setInt(7, 1); // role_id = 1 (admin)
                ps.setBoolean(8, true);

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    System.err.println("Insertion échouée, aucun enregistrement ajouté.");
                    return;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        System.out.println("Admin créé avec succès. ID = " + newId + ", email = " + email);
                    } else {
                        System.out.println("Admin créé (ID inconnu). Email = " + email);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'insertion : " + e.getMessage());
                e.printStackTrace();
            }

        }
    }
}


