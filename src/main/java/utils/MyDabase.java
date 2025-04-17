package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDabase {
    private String url="jdbc:mysql://localhost:3306/syncylinky";
    private String user="root";
    private String password="";
    private Connection con;
    private static MyDabase instance;

    private MyDabase() {
        try {
            con = DriverManager.getConnection(url,user,password);
            System.out.println("Connected");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static MyDabase getInstance() {
        if (instance == null) {
            instance = new MyDabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return con;
    }
}
