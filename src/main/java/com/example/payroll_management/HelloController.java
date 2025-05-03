package com.example.payroll_management;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HelloController {

    @FXML private AnchorPane anchorpane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button signupButton, loginButton;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome back to Tasha's System!!");
        roleComboBox.getItems().addAll("Admin", "Employee");
        signupButton.setOnAction(event -> handleSignup());
        loginButton.setOnAction(event -> handleLogin());
    }

    private void handleSignup() {
        try {
            HelloApplication.setRoot("signup");
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load signup page: " + e.getMessage());
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id, role FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, use hashed passwords
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("user_id");
                String role = rs.getString("role");
                if (role.equals("Admin")) {
                    HelloApplication.setRoot("admin_dashboard");
                } else if (role.equals("Employee")) {
                    try (PreparedStatement empStmt = conn.prepareStatement("SELECT employee_id FROM employees WHERE user_id = ?")) {
                        empStmt.setString(1, userId);
                        ResultSet empRs = empStmt.executeQuery();
                        if (empRs.next()) {
                            String employeeId = empRs.getString("employee_id");
                            HelloApplication.setRootWithEmployeeId("employee_dashboard", employeeId);
                        } else {
                            showErrorAlert("Error", "Employee not found.");
                        }
                    }
                } else {
                    showErrorAlert("Error", "Invalid role.");
                }
            } else {
                showErrorAlert("Error", "Invalid username or password.");
            }
        } catch (SQLException | IOException e) {
            showErrorAlert("Database Error", "Login failed: " + e.getMessage());
        }
    }

    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSignupMouseEntered(MouseEvent event) {
        signupButton.setStyle(
                "-fx-background-color: #e91e63; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #d81b60, 10, 0.5, 0, 0);"
        );
    }

    @FXML
    private void handleSignupMouseExited(MouseEvent event) {
        signupButton.setStyle(
                "-fx-background-color: #f06292; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);"
        );
    }

    @FXML
    private void handleLoginMouseEntered(MouseEvent event) {
        loginButton.setStyle(
                "-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #8e24aa, 10, 0.5, 0, 0);"
        );
    }

    @FXML
    private void handleLoginMouseExited(MouseEvent event) {
        loginButton.setStyle(
                "-fx-background-color: #ab47bc; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #8e24aa, 5, 0.3, 0, 0);"
        );
    }
}