package services;

import models.FeedbackEtudiant;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceFeedback {
    public ServiceFeedback() {
        ensureTableExists();
    }

    private Connection getConnectionOrThrow() throws SQLException {
        Connection cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new SQLException("Connexion à la base indisponible.");
        }
        return cnx;
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS feedback_etudiant (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "cours_id INT NOT NULL, " +
            "chapitre_id INT NOT NULL, " +
            "message TEXT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_feedback_cours (cours_id), " +
            "INDEX idx_feedback_chapitre (chapitre_id)" +
            ")";
        try (Statement st = getConnectionOrThrow().createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("❌ Erreur création table feedback_etudiant: " + e.getMessage());
        }
    }

    public boolean addFeedback(int coursId, int chapitreId, String message) {
        String sql = "INSERT INTO feedback_etudiant (cours_id, chapitre_id, message) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setInt(1, coursId);
            ps.setInt(2, chapitreId);
            ps.setString(3, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout feedback: " + e.getMessage());
            return false;
        }
    }

    public List<FeedbackEtudiant> getFeedbacksByCours(int coursId) {
        List<FeedbackEtudiant> items = new ArrayList<>();
        String sql = "SELECT f.id, f.cours_id, f.chapitre_id, f.message, c.titre AS cours_titre, ch.titre AS chapitre_titre " +
            "FROM feedback_etudiant f " +
            "JOIN cours c ON c.id = f.cours_id " +
            "JOIN chapitre ch ON ch.id = f.chapitre_id " +
            "WHERE f.cours_id = ? " +
            "ORDER BY f.created_at DESC";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeedbackEtudiant item = new FeedbackEtudiant();
                    item.setId(rs.getInt("id"));
                    item.setCoursId(rs.getInt("cours_id"));
                    item.setChapitreId(rs.getInt("chapitre_id"));
                    item.setCoursTitre(rs.getString("cours_titre"));
                    item.setChapitreTitre(rs.getString("chapitre_titre"));
                    item.setMessage(rs.getString("message"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lecture feedbacks: " + e.getMessage());
        }
        return items;
    }
}
