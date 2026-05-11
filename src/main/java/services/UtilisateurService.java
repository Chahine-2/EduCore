package services;

import interfaces.IUtilisateurService;
import models.*;
import org.mindrot.jbcrypt.BCrypt;
import utils.DatabaseConnection;
import utils.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements IUtilisateurService {

    private Connection connection;
    /** Current attendance session for {@link #enregistrerPresence} (set by {@link #preparerNouvelleSessionAppel}). */
    private Integer sessionAppelCourante;

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
        String motDePasseSecurise = BCrypt.hashpw(etudiant.getMotDePasse(), BCrypt.gensalt());

        String queryUtilisateur = "INSERT INTO utilisateurs (nom, prenom, age, email, tel, mot_de_passe, role_id, statut_actif) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String queryEtudiant = "INSERT INTO etudiants (utilisateur_id, numero_etudiant, classe) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psUser = connection.prepareStatement(queryUtilisateur, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, etudiant.getNom());
                psUser.setString(2, etudiant.getPrenom());
                psUser.setInt(3, etudiant.getAge());
                psUser.setString(4, etudiant.getEmail());
                psUser.setInt(5, etudiant.getTel());
                psUser.setString(6, motDePasseSecurise);
                psUser.setInt(7, 3);
                psUser.setBoolean(8, true);

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
            connection.commit();
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
    public List<HistoriqueConnexion> recupererHistoriqueConnexions() {
        List<HistoriqueConnexion> liste = new ArrayList<>();
        String query = "SELECT email_tente, date_tentative, statut_reussite FROM historique_connexions ORDER BY date_tentative DESC LIMIT 30";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String date = rs.getString("date_tentative");
                String email = rs.getString("email_tente");
                String statut = rs.getBoolean("statut_reussite") ? "✅ SUCCÈS" : "❌ ÉCHEC";

                liste.add(new HistoriqueConnexion(date, email, statut));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    @Override
    public boolean ajouterEnseignant(Enseignant enseignant) {
        String motDePasseSecurise = BCrypt.hashpw(enseignant.getMotDePasse(), BCrypt.gensalt());

        String queryUtilisateur = "INSERT INTO utilisateurs (nom, prenom, age, email, tel, mot_de_passe, role_id, statut_actif) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String queryEnseignant = "INSERT INTO enseignants (utilisateur_id, specialite, matricule) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psUser = connection.prepareStatement(queryUtilisateur, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, enseignant.getNom());
                psUser.setString(2, enseignant.getPrenom());
                psUser.setInt(3, enseignant.getAge());
                psUser.setString(4, enseignant.getEmail());
                psUser.setInt(5, enseignant.getTel());
                psUser.setString(6, motDePasseSecurise);
                psUser.setInt(7, 2);
                psUser.setBoolean(8, true);

                psUser.executeUpdate();

                ResultSet rs = psUser.getGeneratedKeys();
                if (rs.next()) {
                    int newUserId = rs.getInt(1);

                    try (PreparedStatement psEns = connection.prepareStatement(queryEnseignant)) {
                        psEns.setInt(1, newUserId);
                        psEns.setString(2, enseignant.getSpecialite());
                        psEns.setString(3, enseignant.getMatricule());
                        psEns.executeUpdate();
                    }
                }
            }
            connection.commit();
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
    public List<String> listerClassesExistantes() {
        List<String> classes = new ArrayList<>();
        String query = "SELECT DISTINCT classe FROM etudiants WHERE classe IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                classes.add(rs.getString("classe"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    @Override
    public boolean marquerPresence(int enseignantId, String classe, List<Integer> etudiantsPresents, List<Integer> etudiantsAbsents) {
        String querySession = "INSERT INTO sessions (classe, enseignant_id) VALUES (?, ?)";
        String queryPresence = "INSERT INTO presences (session_id, etudiant_id, statut) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psSession = connection.prepareStatement(querySession, Statement.RETURN_GENERATED_KEYS)) {
                psSession.setString(1, classe);
                psSession.setInt(2, enseignantId);
                psSession.executeUpdate();

                ResultSet rs = psSession.getGeneratedKeys();
                if (rs.next()) {
                    int sessionId = rs.getInt(1);

                    try (PreparedStatement psPres = connection.prepareStatement(queryPresence)) {
                        for (int id : etudiantsPresents) {
                            psPres.setInt(1, sessionId);
                            psPres.setInt(2, id);
                            psPres.setString(3, "Présent");
                            psPres.addBatch();
                        }
                        for (int id : etudiantsAbsents) {
                            psPres.setInt(1, sessionId);
                            psPres.setInt(2, id);
                            psPres.setString(3, "Absent");
                            psPres.addBatch();
                        }
                        psPres.executeBatch();
                    }
                }
            }
            connection.commit();
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
    public boolean ajouterClasse(String nomClasse) {
        String query = "INSERT INTO classes (nom_classe) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, nomClasse);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur : Cette classe existe peut-être déjà.");
            return false;
        }
    }

    @Override
    public List<String> listerToutesLesClasses() {
        List<String> classes = new ArrayList<>();
        String query = "SELECT nom_classe FROM classes ORDER BY nom_classe ASC";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                classes.add(rs.getString("nom_classe"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    @Override
    public Utilisateur getUtilisateurComplet(int id, String roleNom) {
        String query = "SELECT * FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                int age = rs.getInt("age");
                String email = rs.getString("email");
                int tel = rs.getInt("tel");
                boolean actif = rs.getBoolean("statut_actif");
                Role role = new Role(rs.getInt("role_id"), roleNom);

                if (roleNom.equals("Etudiant")) {
                    String q2 = "SELECT * FROM etudiants WHERE utilisateur_id = ?";
                    try (PreparedStatement ps2 = connection.prepareStatement(q2)) {
                        ps2.setInt(1, id);
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) {
                            return new Etudiant(id, nom, prenom, age, email, tel, "", role, rs2.getString("numero_etudiant"), rs2.getString("classe"), actif);
                        }
                    }
                } else if (roleNom.equals("Enseignant")) {
                    String q2 = "SELECT * FROM enseignants WHERE utilisateur_id = ?";
                    try (PreparedStatement ps2 = connection.prepareStatement(q2)) {
                        ps2.setInt(1, id);
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) {
                            return new Enseignant(id, nom, prenom, age, email, tel, "", role, rs2.getString("specialite"), rs2.getString("matricule"), actif);
                        }
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean modifierUtilisateur(Utilisateur user) {
        String queryUser = "UPDATE utilisateurs SET nom=?, prenom=?, age=?, email=?, tel=? WHERE id=?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(queryUser)) {
                ps.setString(1, user.getNom());
                ps.setString(2, user.getPrenom());
                ps.setInt(3, user.getAge());
                ps.setString(4, user.getEmail());
                ps.setInt(5, user.getTel());
                ps.setInt(6, user.getId());
                ps.executeUpdate();
            }

            if (user instanceof Etudiant) {
                Etudiant etud = (Etudiant) user;
                String queryEtud = "UPDATE etudiants SET numero_etudiant=?, classe=? WHERE utilisateur_id=?";
                try (PreparedStatement ps = connection.prepareStatement(queryEtud)) {
                    ps.setString(1, etud.getNumeroEtudiant());
                    ps.setString(2, etud.getClasse());
                    ps.setInt(3, user.getId());
                    ps.executeUpdate();
                }
            } else if (user instanceof Enseignant) {
                Enseignant prof = (Enseignant) user;
                String queryProf = "UPDATE enseignants SET specialite=?, matricule=? WHERE utilisateur_id=?";
                try (PreparedStatement ps = connection.prepareStatement(queryProf)) {
                    ps.setString(1, prof.getSpecialite());
                    ps.setString(2, prof.getMatricule());
                    ps.setInt(3, user.getId());
                    ps.executeUpdate();
                }
            }
            connection.commit();
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
    public List<Etudiant> listerEtudiantsParClasse(String classe) {
        List<Etudiant> liste = new ArrayList<>();
        String query = "SELECT u.*, e.numero_etudiant, e.classe FROM utilisateurs u JOIN etudiants e ON u.id = e.utilisateur_id WHERE e.classe = ? AND u.statut_actif = 1";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, classe);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Role roleEtud = new Role(3, "Etudiant");
                Etudiant etud = new Etudiant(
                        rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                        rs.getInt("age"), rs.getString("email"), rs.getInt("tel"),
                        rs.getString("mot_de_passe"), roleEtud,
                        rs.getString("numero_etudiant"), rs.getString("classe"), true
                );
                liste.add(etud);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    @Override
    public void preparerNouvelleSessionAppel(String classe) {
        sessionAppelCourante = null;
        if (classe == null || classe.isBlank()) {
            return;
        }
        Utilisateur current = UserSession.getCurrentUser();
        if (current == null) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO sessions (classe, enseignant_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, classe.trim());
            ps.setInt(2, current.getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    sessionAppelCourante = keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️ preparerNouvelleSessionAppel: " + e.getMessage());
            e.printStackTrace();
            sessionAppelCourante = null;
        }
    }

    @Override
    public boolean enregistrerPresence(int etudiantId, String statut) {
        if (sessionAppelCourante != null) {
            try {
                try (PreparedStatement del = connection.prepareStatement(
                        "DELETE FROM presences WHERE session_id = ? AND etudiant_id = ?")) {
                    del.setInt(1, sessionAppelCourante);
                    del.setInt(2, etudiantId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = connection.prepareStatement(
                        "INSERT INTO presences (session_id, etudiant_id, statut) VALUES (?, ?, ?)")) {
                    ins.setInt(1, sessionAppelCourante);
                    ins.setInt(2, etudiantId);
                    ins.setString(3, statut);
                    return ins.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                System.out.println("❌ enregistrerPresence (session): " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        // Legacy schema (no sessions row prepared): date_appel column
        String query = "INSERT INTO presences (etudiant_id, date_appel, statut) VALUES (?, CURDATE(), ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, statut);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ enregistrerPresence (legacy): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}