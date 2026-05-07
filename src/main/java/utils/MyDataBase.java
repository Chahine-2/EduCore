package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/educore?useSSL=false&serverTimezone=UTC";
    private final String  USERNAME ="root";
    private final String PASSWORD ="";
    private Connection cnx ;

    private MyDataBase() {
        connecter();
    }

    private void connecter() {
        try {
            // Chargement explicite du driver (utile si Maven est désynchronisé)
            Class.forName("com.mysql.cj.jdbc.Driver");

            this.cnx = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("✅ Base de données connectée avec succès!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL introuvable. Vérifiez votre pom.xml et rechargez Maven.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ ERREUR DE CONNEXION À LA BASE DE DONNÉES :");
            System.out.println("Vérifiez que WAMP/XAMPP est allumé et que la base 'educore' existe.");
            System.out.println("Message: " + e.getMessage());
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
}
