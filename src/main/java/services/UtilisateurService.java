package services;

import interfaces.IUtilisateurService;
import models.*;
import org.mindrot.jbcrypt.BCrypt;
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
        String query = "SELECT u.*, r.nom_role FROM utilisateurs u JOIN roles r ON u.role_id = r.id WHERE u.email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                boolean isActif = resultSet.getBoolean("statut_actif");

                if (!isActif) {
                    System.out.println("⚠️ ALERTE : Ce compte a été suspendu.");
                    enregistrerTentativeConnexion(email, false); // ÉCHEC : Compte suspendu
                    return null;
                }

                String motDePasseHache = resultSet.getString("mot_de_passe");

                if (BCrypt.checkpw(motDePasse, motDePasseHache)) {
                    Role role = new Role(resultSet.getInt("role_id"), resultSet.getString("nom_role"));

                    enregistrerTentativeConnexion(email, true); // SUCCÈS !

                    return new Etudiant(resultSet.getInt("id"), resultSet.getString("nom"), resultSet.getString("prenom"),
                            resultSet.getInt("age"), email, resultSet.getInt("tel"),
                            motDePasseHache, role, "N/A", "N/A",true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        enregistrerTentativeConnexion(email, false); // ÉCHEC : Mauvais mot de passe ou email introuvable
        return null;
    }

    @Override
    public boolean ajouterEtudiant(Etudiant etudiant) {
        // Hachage du mot de passe
        String motDePasseSecurise = BCrypt.hashpw(etudiant.getMotDePasse(), BCrypt.gensalt());

        String queryUtilisateur = "INSERT INTO utilisateurs (nom, prenom, age, email, tel, mot_de_passe, role_id, statut_actif) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String queryEtudiant = "INSERT INTO etudiants (utilisateur_id, numero_etudiant, classe) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false); // Début de la transaction

            try (PreparedStatement psUser = connection.prepareStatement(queryUtilisateur, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, etudiant.getNom());
                psUser.setString(2, etudiant.getPrenom());
                psUser.setInt(3, etudiant.getAge());
                psUser.setString(4, etudiant.getEmail());
                psUser.setInt(5, etudiant.getTel());
                psUser.setString(6, motDePasseSecurise);
                psUser.setInt(7, 3); // Role Étudiant
                psUser.setBoolean(8, true); // Actif par défaut

                psUser.executeUpdate();

                ResultSet rs = psUser.getGeneratedKeys();
                if (rs.next()) {
                    int newUserId = rs.getInt(1);

                    try (PreparedStatement psEtud = connection.prepareStatement(queryEtudiant)) {
                        psEtud.setInt(1, newUserId);
                        psEtud.setString(2, etudiant.getNumeroEtudiant());
                        psEtud.setString(3, etudiant.getClasse());
                        psEtud.executeUpdate();
                    }
                }
            }
            connection.commit(); // Validation
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
        String query = "SELECT u.id, u.nom, u.prenom, u.email, u.statut_actif, r.nom_role FROM utilisateurs u JOIN roles r ON u.role_id = r.id";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Role role = new Role(0, rs.getString("nom_role"));
                boolean isActif = rs.getBoolean("statut_actif");

                Utilisateur u = new Utilisateur(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), 0, rs.getString("email"), 0, "", role, isActif) {};
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
        String query = "DELETE FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changerStatutCompte(int id, boolean rendreActif) {
        String query = "UPDATE utilisateurs SET statut_actif = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setBoolean(1, rendreActif);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // --- NOUVELLE FONCTIONNALITÉ : JOURNAL D'AUDIT ---
    private void enregistrerTentativeConnexion(String email, boolean succes) {
        String query = "INSERT INTO historique_connexions (email_tente, statut_reussite) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setBoolean(2, succes);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit : " + e.getMessage());
        }
    }
    @Override
    public void consulterHistoriqueConnexions() {
        // On récupère les 10 dernières tentatives
        String query = "SELECT email_tente, date_tentative, statut_reussite FROM historique_connexions ORDER BY date_tentative DESC LIMIT 10";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- 10 DERNIÈRES TENTATIVES DE CONNEXION ---");
            while (rs.next()) {
                String date = rs.getString("date_tentative");
                String email = rs.getString("email_tente");
                String statut = rs.getBoolean("statut_reussite") ? "✅ SUCCÈS" : "❌ ÉCHEC ";

                System.out.println("[" + date + "] " + statut + " | Email : " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}