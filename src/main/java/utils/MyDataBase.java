package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;

    private final String URL ="jdbc:mysql://127.0.0.1:3306/educore";

    private final String  USERNAME ="root";
    private final String PASSWORD ="";
    private Connection cnx ;

    private MyDataBase() {
        connecter();
    }

    private void connecter() {
        try {

            this.cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✓ Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("URL: " + URL);
            System.err.println("Username: " + USERNAME);
            System.err.println("Please ensure MySQL is running and the database 'educore' exists.");

            this.cnx = null;
        }

    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
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
        return cnx;
    }
}