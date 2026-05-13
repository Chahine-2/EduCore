package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/educore";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;
    // keep DB name for INFORMATION_SCHEMA queries
    private final String DB_NAME = "educore";

    private MyDataBase() {
        connecter();
    }

    private void connecter() {
        try {
            this.cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✓ Database connected successfully!");
            // ensure expected columns exist (helps when DB was not initialized)
            ensureColumns();
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("URL: " + URL);
            System.err.println("Username: " + USERNAME);
            System.err.println("Please ensure MySQL is running and the database 'educore' exists.");
            this.cnx = null;
        }
    }

    /**
     * Ensure optional columns used by the application exist. If they are missing,
     * attempt to add them. This makes the app more robust when the DB was created
     * without running setup.sql.
     */
    private void ensureColumns() {
        try {
            // check question.explication
            PreparedStatement checkQ = cnx.prepareStatement(
                    "SELECT COUNT(*) AS c FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'question' AND COLUMN_NAME = 'explication'");
            checkQ.setString(1, DB_NAME);
            ResultSet rsQ = checkQ.executeQuery();
            boolean addQ = false;
            if (rsQ.next()) {
                addQ = rsQ.getInt("c") == 0;
            }
            rsQ.close();
            checkQ.close();

            if (addQ) {
                try (PreparedStatement alter = cnx.prepareStatement("ALTER TABLE question ADD COLUMN explication TEXT")) {
                    alter.executeUpdate();
                    System.out.println("ℹ️ Added missing column: question.explication");
                }
            }

            // check reponse.explication
            PreparedStatement checkR = cnx.prepareStatement(
                    "SELECT COUNT(*) AS c FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'reponse' AND COLUMN_NAME = 'explication'");
            checkR.setString(1, DB_NAME);
            ResultSet rsR = checkR.executeQuery();
            boolean addR = false;
            if (rsR.next()) {
                addR = rsR.getInt("c") == 0;
            }
            rsR.close();
            checkR.close();

            if (addR) {
                try (PreparedStatement alter = cnx.prepareStatement("ALTER TABLE reponse ADD COLUMN explication TEXT")) {
                    alter.executeUpdate();
                    System.out.println("ℹ️ Added missing column: reponse.explication");
                }
            }

            // check reponse.ordre (order of answers). Add with default 0 if missing.
            PreparedStatement checkOrdre = cnx.prepareStatement(
                    "SELECT COUNT(*) AS c FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'reponse' AND COLUMN_NAME = 'ordre'");
            checkOrdre.setString(1, DB_NAME);
            ResultSet rsOrdre = checkOrdre.executeQuery();
            boolean addOrdre = false;
            if (rsOrdre.next()) {
                addOrdre = rsOrdre.getInt("c") == 0;
            }
            rsOrdre.close();
            checkOrdre.close();

            if (addOrdre) {
                try (PreparedStatement alter = cnx.prepareStatement("ALTER TABLE reponse ADD COLUMN ordre INT DEFAULT 0")) {
                    alter.executeUpdate();
                    System.out.println("ℹ️ Added missing column: reponse.ordre (default 0)");
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️ ensureColumns() failed: " + e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public synchronized Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                connecter();
            }
        } catch (SQLException e) {
            System.out.println("❌ Impossible de vérifier l'état de la connexion: " + e.getMessage());
            cnx = null;
        }
        return cnx;
    }

    // Backward-compatible alias used by older service classes.
    public Connection getConnection() {
        return getCnx();
    }
}