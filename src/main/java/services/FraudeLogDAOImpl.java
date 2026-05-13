package services;

import models.FraudeLog;
import models.TeacherFraudAuditRow;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FraudeLogDAOImpl implements FraudeLogDAO {

    private static final DateTimeFormatter DETECTED_AT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm", Locale.ENGLISH);

    private static void ensureFraudeLogTable(Connection cnx) {
        final String createTableSql =
                "CREATE TABLE IF NOT EXISTS fraude_log (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "resultat_id INT NOT NULL, " +
                        "etudiant_id INT NOT NULL, " +
                        "type_fraude VARCHAR(100) NOT NULL, " +
                        "description TEXT, " +
                        "date_detection DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (resultat_id) REFERENCES resultat(id)" +
                        ")";
        try (PreparedStatement createPs = cnx.prepareStatement(createTableSql)) {
            createPs.execute();
        } catch (SQLException e) {
            System.err.println("FraudeLogDAOImpl: create table warning: " + e.getMessage());
        }
    }

    @Override
    public void logFraude(FraudeLog log) {
        final String insertSql =
                "INSERT INTO fraude_log (resultat_id, etudiant_id, type_fraude, description) VALUES (?, ?, ?, ?)";

        Connection cnx = MyDataBase.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("FraudeLogDAOImpl: database connection is null.");
            return;
        }

        ensureFraudeLogTable(cnx);

        try (PreparedStatement ps = cnx.prepareStatement(insertSql)) {
            ps.setInt(1, log.getResultatId());
            ps.setInt(2, log.getEtudiantId());
            ps.setString(3, log.getTypeFraude());
            ps.setString(4, log.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("FraudeLogDAOImpl: failed to insert fraud log: " + e.getMessage());
        }
    }

    @Override
    public List<TeacherFraudAuditRow> findAllAuditRows() {
        List<TeacherFraudAuditRow> out = new ArrayList<>();
        Connection cnx = MyDataBase.getInstance().getConnection();
        if (cnx == null) {
            return out;
        }
        ensureFraudeLogTable(cnx);
        final String sql = """
                SELECT fl.id, fl.date_detection, fl.etudiant_id, fl.type_fraude, fl.description, fl.resultat_id,
                       u.nom, u.prenom, e.titre AS eval_titre
                FROM fraude_log fl
                LEFT JOIN utilisateurs u ON u.id = fl.etudiant_id
                LEFT JOIN resultat r ON r.id = fl.resultat_id
                LEFT JOIN evaluation e ON e.id = r.evaluation_id
                ORDER BY fl.date_detection DESC, fl.id DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int logId = rs.getInt("id");
                Timestamp ts = rs.getTimestamp("date_detection");
                String at = ts == null ? "—" : ts.toLocalDateTime().format(DETECTED_AT);
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                int etudiantId = rs.getInt("etudiant_id");
                String student = formatStudent(prenom, nom, etudiantId);
                String evalTitle = rs.getString("eval_titre");
                if (evalTitle == null || evalTitle.isBlank()) {
                    evalTitle = "—";
                }
                String type = rs.getString("type_fraude");
                if (type == null) {
                    type = "";
                }
                String desc = rs.getString("description");
                if (desc == null) {
                    desc = "";
                }
                out.add(new TeacherFraudAuditRow(logId, at, etudiantId, student, evalTitle, type, desc, rs.getInt("resultat_id")));
            }
        } catch (SQLException e) {
            System.err.println("FraudeLogDAOImpl: findAllAuditRows: " + e.getMessage());
        }
        return out;
    }

    private static String formatStudent(String prenom, String nom, int etudiantId) {
        String p = prenom == null ? "" : prenom.trim();
        String n = nom == null ? "" : nom.trim();
        String name = (p + " " + n).trim();
        if (!name.isEmpty()) {
            return name + "  (#" + etudiantId + ")";
        }
        return "Student #" + etudiantId;
    }
}
