package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/education";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;

    private MyDataBase() {
        connect();
    }

    private void connect() {
        try {
            this.cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to database education ......");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
                System.out.println("Reconnexion a la base de donnees...");
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return cnx;
    }
}