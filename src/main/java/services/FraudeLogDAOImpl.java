package services;

import models.FraudeLog;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FraudeLogDAOImpl implements FraudeLogDAO {

    @Override
    public void logFraude(FraudeLog log) {
        final String createTableSql =
                "CREATE TABLE IF NOT EXISTS fraude_log (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "resultat_id INT NOT NULL, " +
                        "etudiant_id INT NOT NULL, " +
                        "type_fraude VARCHAR(100) NOT NULL, " +
                        "description TEXT, " +
                        "date_detection DATETIME DEFAULT NOW(), " +
                        "FOREIGN KEY (resultat_id) REFERENCES resultat(id)" +
                        ")";

        final String insertSql =
                "INSERT INTO fraude_log (resultat_id, etudiant_id, type_fraude, description) VALUES (?, ?, ?, ?)";

        Connection cnx = MyDataBase.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("FraudeLogDAOImpl: database connection is null.");
            return;
        }

        try (PreparedStatement createPs = cnx.prepareStatement(createTableSql)) {
            createPs.execute();
        } catch (SQLException e) {
            // Non-blocking; still try to insert in case table already exists.
            System.err.println("FraudeLogDAOImpl: create table warning: " + e.getMessage());
        }

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
}
