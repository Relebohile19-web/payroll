package com.example.payroll_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/payroll_management?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "666666"; // Change to your actual password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add this to pom.xml:");
            System.err.println("<dependency>");
            System.err.println("    <groupId>mysql</groupId>");
            System.err.println("    <artifactId>mysql-connector-java</artifactId>");
            System.err.println("    <version>8.0.28</version>");
            System.err.println("</dependency>");
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to load MySQL JDBC driver");
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Successfully connected to database");
        return connection;
    }
}