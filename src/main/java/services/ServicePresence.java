package services;

import models.Presence;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ServicePresence {

    private Connection getConnectionOrThrow() throws SQLException {
        Connection cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new SQLException("Connexion à la base indisponible. Vérifiez que MySQL/WAMP/XAMPP est démarré.");
        }
        return cnx;
    }

    // Add presence record
    public void add(Presence presence) throws SQLException {
        String req = "INSERT INTO presence (etudiant_id, cours_id, date_presence, est_present, notes) VALUES (?,?,?,?,?)";
        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, presence.getEtudiantId());
            ps.setInt(2, presence.getCoursId());
            ps.setDate(3, java.sql.Date.valueOf(presence.getDatePresence()));
            ps.setBoolean(4, presence.isEstPresent());
            ps.setString(5, presence.getNotes());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Présence enregistrée");
            }
        }
    }

    // Update presence record
    public void update(Presence presence) throws SQLException {
        String req = "UPDATE presence SET est_present = ?, notes = ? WHERE id = ?";
        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setBoolean(1, presence.isEstPresent());
            ps.setString(2, presence.getNotes());
            ps.setInt(3, presence.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Présence mise à jour");
            }
        }
    }

    // Get all presence records for a specific course
    public List<Presence> getPresenceByCoursId(int coursId) throws SQLException {
        List<Presence> presences = new ArrayList<>();
        String req = "SELECT * FROM presence WHERE cours_id = ? ORDER BY date_presence DESC";
        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Presence presence = mapResultSetToPresence(rs);
                    presences.add(presence);
                }
            }
        }
        return presences;
    }

    // Get presence records for a specific student
    public List<Presence> getPresenceByEtudiantId(int etudiantId) throws SQLException {
        List<Presence> presences = new ArrayList<>();
        String req = "SELECT * FROM presence WHERE etudiant_id = ? ORDER BY date_presence DESC";
        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, etudiantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Presence presence = mapResultSetToPresence(rs);
                    presences.add(presence);
                }
            }
        }
        return presences;
    }

    // Get presence records for a specific course and date
    public List<Presence> getPresenceByCoursIdAndDate(int coursId, LocalDate date) throws SQLException {
        List<Presence> presences = new ArrayList<>();
        String req = "SELECT * FROM presence WHERE cours_id = ? AND date_presence = ? ORDER BY etudiant_id";
        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, coursId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Presence presence = mapResultSetToPresence(rs);
                    presences.add(presence);
                }
            }
        }
        return presences;
    }

    // Get presence statistics for a course
    public Map<String, Object> getPresenceStatsForCours(int coursId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String req = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN est_present = TRUE THEN 1 ELSE 0 END) as presentes, " +
                "SUM(CASE WHEN est_present = FALSE THEN 1 ELSE 0 END) as absentes " +
                "FROM presence WHERE cours_id = ?";

        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("total", rs.getInt("total"));
                    stats.put("presentes", rs.getInt("presentes"));
                    stats.put("absentes", rs.getInt("absentes"));
                    int total = rs.getInt("total");
                    int presentes = rs.getInt("presentes");
                    stats.put("tauxPresence", total > 0 ? (presentes * 100.0 / total) : 0.0);
                }
            }
        }
        return stats;
    }

    // Get presence rate for specific student and course
    public double getTauxPresenceEtudiant(int etudiantId, int coursId) throws SQLException {
        String req = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN est_present = TRUE THEN 1 ELSE 0 END) as presentes " +
                "FROM presence WHERE etudiant_id = ? AND cours_id = ?";

        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, etudiantId);
            ps.setInt(2, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int presentes = rs.getInt("presentes");
                    return total > 0 ? (presentes * 100.0 / total) : 0.0;
                }
            }
        }
        return 0;
    }

    // Delete presence record
    public void delete(int presenceId) throws SQLException {
        String req = "DELETE FROM presence WHERE id = ?";
        Connection cnx = getConnectionOrThrow();
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, presenceId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Présence supprimée");
            }
        }
    }

    // Helper method to map ResultSet to Presence object
    private Presence mapResultSetToPresence(ResultSet rs) throws SQLException {
        Presence presence = new Presence();
        presence.setId(rs.getInt("id"));
        presence.setEtudiantId(rs.getInt("etudiant_id"));
        presence.setCoursId(rs.getInt("cours_id"));
        presence.setDatePresence(rs.getDate("date_presence").toLocalDate());
        presence.setEstPresent(rs.getBoolean("est_present"));
        presence.setNotes(rs.getString("notes"));
        Timestamp timestamp = rs.getTimestamp("date_enregistrement");
        if (timestamp != null) {
            presence.setDateEnregistrement(timestamp.toLocalDateTime());
        }
        return presence;
    }
}





